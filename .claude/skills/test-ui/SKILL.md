---
name: test-ui
description: Run the text-UI regression tests for Hermes from test/ui-test-plan.md, feeding each case's commands into the program and checking the output against the recorded expectation. Use when asked to test the UI, run the UI tests, check the chatbot's output, verify a command still behaves correctly, or add a UI test case.
---

# Hermes text-UI tests

Runs each case in `test/ui-test-plan.md` against the real program and compares
what it prints with the expected output. Stops at the first failure.

## Running the tests

Hermes needs Java 25. Switch to it in the same command, since shell state does
not persist between calls:

```bash
source ~/.sdkman/bin/sdkman-init.sh && sdk use java 25.0.3.fx-zulu && python3 test/run-ui-tests.py
```

The runner compiles `src/main/java` fresh each time, so there is no build step
to do first.

## Reporting the result

Show the console session the runner prints — the commands fed in and the
replies that came back for every case. That transcript is the point of the
exercise, so do not summarise it away.

**On success**, report the transcript and the count of passing cases.

**On failure**, the runner has already stopped and printed the aim, the input,
the expected output, the actual output and a diff. Relay all of it, then say
which case failed and which cases were therefore not run. Do not run the
remaining cases separately to "see how bad it is" — the plan calls for the
session to end at the first failure.

Then diagnose: work out whether the program regressed or the expectation is out
of date. Say which you think it is and why. Do not edit `test/ui-test-plan.md`
to make a failure disappear without saying plainly that is what you are doing
and why the new expectation is the correct one.

Exit code is 0 when every case passes and 1 otherwise.

## Adding a test case

Append a case to `test/ui-test-plan.md` following the format documented at the
top of that file: an `## TC-nn - title` heading, an `**Aim:**` line, an
optional `**Setup:**` block of starting `data/Hermes.txt` lines, then paired
`**Input:**` and `**Expected output:**` blocks.

Repeat the Input/Expected pair within one case to test behaviour across a
restart — the runs share a data directory, so run 2 sees what run 1 saved.

Write the expected output by hand from what the program is supposed to do, then
run the suite. If it disagrees, decide which side is wrong before changing
either. Pasting the program's actual output into the expectation tests nothing
except that the program is consistent with itself.

Use fixed dates such as `27 Aug 2026 1500` rather than anything relative to
today, so the case does not start failing once that date passes.

## How comparison works

- Every case runs in its own temporary directory, so the project's real
  `data/Hermes.txt` is never touched. Keep it that way: `LogBook` opens that
  path relative to the working directory, so running the program from the
  project root during a test would overwrite the user's actual task list.
- Comparison begins after the greeting line, skipping the startup banner.
- The `___` rules around each reply and all blank lines are ignored.
- Everything else must match exactly, including leading spaces and punctuation.

## Files

- `test/ui-test-plan.md` — the cases, and the authority on expected behaviour.
- `test/run-ui-tests.py` — the runner. Changing how output is compared means
  changing `normalise()` here and the note in the plan describing it.
