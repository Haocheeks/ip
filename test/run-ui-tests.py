#!/usr/bin/env python3
"""Runs the UI test cases described in test/ui-test-plan.md against Hermes.

Each test case is executed in its own throwaway working directory, so the real
data/Hermes.txt in the project is never read or written. The first failing case
stops the session immediately, as required by the test plan.
"""

import difflib
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
SOURCE_DIR = PROJECT_ROOT / "src" / "main" / "java"
PLAN_FILE = PROJECT_ROOT / "test" / "ui-test-plan.md"
MAIN_CLASS = "Hermes"

# Output above this line is the startup banner, which carries no behaviour worth
# asserting in every case, so comparison starts after it.
GREETING = "How may I assist you today?"

RUN_TIMEOUT_SECONDS = 30


class TestCase:
    def __init__(self, case_id, title):
        self.case_id = case_id
        self.title = title
        self.aim = ""
        self.setup = []          # initial lines of data/Hermes.txt
        self.runs = []           # list of (input_lines, expected_lines)
        self._pending_input = None

    def add_input(self, lines):
        self._pending_input = lines

    def add_expected(self, lines):
        if self._pending_input is None:
            raise ValueError(f"{self.case_id}: an expected output has no input before it")
        self.runs.append((self._pending_input, lines))
        self._pending_input = None

    def validate(self):
        if self._pending_input is not None:
            raise ValueError(f"{self.case_id}: an input block has no expected output after it")
        if not self.runs:
            raise ValueError(f"{self.case_id}: no input/expected pairs")
        if not self.aim:
            raise ValueError(f"{self.case_id}: no aim recorded")


def parse_plan(path):
    """Reads the markdown plan into TestCase objects.

    Recognised markers, each optionally followed by a fenced code block:
      ## TC-01 - title      starts a case
      **Aim:** ...          one line
      **Setup:**            fenced block of data-file lines, or omitted for none
      **Input:**            fenced block of commands typed into one program run
      **Expected output:**  fenced block the run's output must match
    A case may repeat Input/Expected to describe several runs that share one
    data directory, which is how persistence across restarts is tested.
    """
    lines = path.read_text().splitlines()
    cases = []
    current = None
    index = 0

    def read_fence(start):
        """Returns (block_lines, next_index) for a fence at or just after start."""
        i = start
        while i < len(lines) and not lines[i].strip():
            i += 1
        if i >= len(lines) or not lines[i].startswith("```"):
            return None, start
        i += 1
        block = []
        while i < len(lines) and not lines[i].startswith("```"):
            block.append(lines[i])
            i += 1
        return block, i + 1

    while index < len(lines):
        line = lines[index]

        heading = re.match(r"^##\s+(TC-\S+)\s*[-–—:]?\s*(.*)$", line)
        if heading:
            if current:
                current.validate()
                cases.append(current)
            current = TestCase(heading.group(1), heading.group(2).strip())
            index += 1
            continue

        if current:
            aim = re.match(r"^\*\*Aim:\*\*\s*(.*)$", line)
            if aim:
                # An aim may wrap over several lines; it ends at the first blank line.
                parts = [aim.group(1).strip()]
                index += 1
                while index < len(lines) and lines[index].strip():
                    parts.append(lines[index].strip())
                    index += 1
                current.aim = " ".join(part for part in parts if part)
                continue

            if re.match(r"^\*\*Setup:\*\*", line):
                block, index = read_fence(index + 1)
                current.setup = block or []
                continue

            if re.match(r"^\*\*Input", line):
                block, index = read_fence(index + 1)
                if block is None:
                    raise ValueError(f"{current.case_id}: Input marker has no code block")
                current.add_input(block)
                continue

            if re.match(r"^\*\*Expected output", line):
                block, index = read_fence(index + 1)
                if block is None:
                    raise ValueError(f"{current.case_id}: Expected marker has no code block")
                current.add_expected(block)
                continue

        index += 1

    if current:
        current.validate()
        cases.append(current)
    return cases


def normalise(raw):
    """Reduces program output to the lines worth comparing.

    Drops the startup banner, the divider rules printed around every reply, and
    blank lines, so an expected block records what Hermes said rather than how
    the reply was framed.
    """
    text = raw.splitlines()
    for position, line in enumerate(text):
        if GREETING in line:
            text = text[position + 1:]
            break
    kept = []
    for line in text:
        line = line.rstrip()
        if not line:
            continue
        if set(line) == {"_"}:
            continue
        kept.append(line)
    return kept


def compile_sources(classes_dir):
    sources = sorted(str(p) for p in SOURCE_DIR.rglob("*.java"))
    if not sources:
        raise SystemExit(f"No .java sources found in {SOURCE_DIR}")
    result = subprocess.run(
        ["javac", "-d", str(classes_dir), *sources],
        capture_output=True, text=True,
    )
    if result.returncode != 0:
        print("COMPILATION FAILED\n")
        print(result.stdout)
        print(result.stderr)
        raise SystemExit(1)
    if result.stderr.strip():
        print("Compiler warnings:")
        print(result.stderr.rstrip())
        print()


def run_once(classes_dir, work_dir, input_lines):
    stdin_text = "\n".join(input_lines) + "\n"
    try:
        result = subprocess.run(
            ["java", "-cp", str(classes_dir), MAIN_CLASS],
            input=stdin_text, capture_output=True, text=True,
            cwd=str(work_dir), timeout=RUN_TIMEOUT_SECONDS,
        )
    except subprocess.TimeoutExpired:
        return f"<<< the program did not exit within {RUN_TIMEOUT_SECONDS}s >>>"
    return result.stdout + result.stderr


def report_failure(case, run_number, input_lines, expected, actual, raw):
    print()
    print("=" * 70)
    print(f"FAILED: {case.case_id} - {case.title}   (run {run_number})")
    print("=" * 70)
    print(f"Aim: {case.aim}")
    print()
    print("Input:")
    for line in input_lines:
        print(f"  > {line}")
    print()
    print("Expected output:")
    for line in expected:
        print(f"  {line}")
    print()
    print("Actual output:")
    for line in actual:
        print(f"  {line}")
    print()
    print("Difference (- expected, + actual):")
    for line in difflib.unified_diff(expected, actual, "expected", "actual", lineterm="", n=2):
        print(f"  {line}")
    print()
    print("Raw console output for this run:")
    for line in raw.splitlines():
        print(f"  | {line}")
    print()
    print(f"Session terminated at {case.case_id}. Later test cases were not run.")


def main():
    if not PLAN_FILE.exists():
        raise SystemExit(f"Test plan not found: {PLAN_FILE}")

    cases = parse_plan(PLAN_FILE)
    if not cases:
        raise SystemExit(f"No test cases found in {PLAN_FILE}")

    workspace = Path(tempfile.mkdtemp(prefix="hermes-ui-test-"))
    classes_dir = workspace / "classes"
    classes_dir.mkdir()

    try:
        print(f"Compiling {SOURCE_DIR.relative_to(PROJECT_ROOT)} ...")
        compile_sources(classes_dir)
        print(f"Running {len(cases)} test case(s) from {PLAN_FILE.relative_to(PROJECT_ROOT)}")
        print(f"Sandbox: {workspace}")
        print()

        for case in cases:
            print("-" * 70)
            print(f"{case.case_id} - {case.title}")
            print(f"Aim: {case.aim}")
            print("-" * 70)

            work_dir = workspace / case.case_id
            (work_dir / "data").mkdir(parents=True)
            if case.setup:
                (work_dir / "data" / "Hermes.txt").write_text("\n".join(case.setup) + "\n")

            for run_number, (input_lines, expected) in enumerate(case.runs, start=1):
                if len(case.runs) > 1:
                    print(f"[run {run_number} of {len(case.runs)}]")
                raw = run_once(classes_dir, work_dir, input_lines)
                actual = normalise(raw)

                # The console record of this run: what was typed, what came back.
                for line in input_lines:
                    print(f"  > {line}")
                for line in actual:
                    print(f"  {line}")
                print()

                if actual != expected:
                    report_failure(case, run_number, input_lines, expected, actual, raw)
                    return 1

            print(f"PASSED: {case.case_id}")
            print()

        print("=" * 70)
        print(f"All {len(cases)} test case(s) passed.")
        print("=" * 70)
        return 0
    finally:
        shutil.rmtree(workspace, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
