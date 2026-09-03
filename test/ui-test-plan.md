# Hermes UI test plan

Text-based UI tests for Hermes. Each case feeds a list of commands into the
program and compares what comes back against a recorded expectation.

Run them with the `test-ui` skill, or directly:

```bash
python3 test/run-ui-tests.py
```

## How a case is written

| Marker | Meaning |
| --- | --- |
| `## TC-nn - title` | Starts a test case. |
| `**Aim:**` | One line saying what the case is meant to establish. |
| `**Setup:**` | Optional fenced block giving the starting contents of `data/Hermes.txt`. Omit it to start with no data file. |
| `**Input:**` | Fenced block of commands typed into one run of the program. |
| `**Expected output:**` | Fenced block the run's output must match exactly. |

A case may repeat `**Input:**` and `**Expected output:**` to describe several
runs sharing one data directory. That is how behaviour across a restart is
tested: run 1 changes something, run 2 starts the program again and checks the
change survived.

## How output is compared

Every case runs in its own throwaway directory, so the project's real
`data/Hermes.txt` is never read or written.

Comparison starts after the greeting line, and ignores the `___` rules printed
around each reply and any blank lines. An expected block therefore records what
Hermes said, not how the reply was framed. Everything else must match exactly,
including indentation and punctuation.

Dates in these cases are fixed literals rather than anything relative to today,
so the expectations stay valid over time.

---

## TC-01 - Adding each task type

**Aim:** Each of the three task types can be added, is acknowledged with the
right type tag, and appears in `list` in the order it was added.

**Input:**

```text
todo read book
deadline submit report /by 27 Aug 2026 1500
event team sync /from 26 Aug 2026 1000 /to 26 Aug 2026 1100
list
bye
```

**Expected output:**

```text
Got it. I've added this task:
  [T][ ] read book
Now you have 1 task in the list.
Got it. I've added this task:
  [D][ ] submit report (by: 27 Aug 2026 1500)
Now you have 2 tasks in the list.
Got it. I've added this task:
  [E][ ] team sync (from: 26 Aug 2026 1000 to: 26 Aug 2026 1100)
Now you have 3 tasks in the list.
1. [T][ ] read book
2. [D][ ] submit report (by: 27 Aug 2026 1500)
3. [E][ ] team sync (from: 26 Aug 2026 1000 to: 26 Aug 2026 1100)
Goodbye, thank you for contacting me!
```

---

## TC-02 - Accepting several date formats

**Aim:** Different input formats are all accepted and normalised to one display
format, and a date given without a time is stored as midnight.

**Input:**

```text
deadline iso /by 2026-08-27 1500
deadline slashes /by 27/8/2026
deadline words /by 27 Aug 2026 1500
list
bye
```

**Expected output:**

```text
Got it. I've added this task:
  [D][ ] iso (by: 27 Aug 2026 1500)
Now you have 1 task in the list.
Got it. I've added this task:
  [D][ ] slashes (by: 27 Aug 2026 0000)
Now you have 2 tasks in the list.
Got it. I've added this task:
  [D][ ] words (by: 27 Aug 2026 1500)
Now you have 3 tasks in the list.
1. [D][ ] iso (by: 27 Aug 2026 1500)
2. [D][ ] slashes (by: 27 Aug 2026 0000)
3. [D][ ] words (by: 27 Aug 2026 1500)
Goodbye, thank you for contacting me!
```

---

## TC-03 - Marking and unmarking

**Aim:** `mark` and `unmark` change the completion flag shown in `list` and can
be applied in either direction.

**Input:**

```text
todo read book
mark 1
list
unmark 1
list
bye
```

**Expected output:**

```text
Got it. I've added this task:
  [T][ ] read book
Now you have 1 task in the list.
Roger, I will mark this task as completed:
  [T][X] read book
1. [T][X] read book
Alright, I will mark this task as incomplete:
  [T][ ] read book
1. [T][ ] read book
Goodbye, thank you for contacting me!
```

---

## TC-04 - Deleting a task

**Aim:** `delete` removes the task at the given position, reports the remaining
count, and the later tasks close the gap.

**Input:**

```text
todo alpha
todo beta
delete 1
list
bye
```

**Expected output:**

```text
Got it. I've added this task:
  [T][ ] alpha
Now you have 1 task in the list.
Got it. I've added this task:
  [T][ ] beta
Now you have 2 tasks in the list.
Roger, I've removed this task:
  [T][ ] alpha
Now you have 1 task in the list.
1. [T][ ] beta
Goodbye, thank you for contacting me!
```

---

## TC-05 - Tasks survive a restart

**Aim:** Tasks added in one session are written to storage and read back
unchanged by the next session, with their dates intact.

**Input:**

```text
todo persist me
deadline also me /by 27 Aug 2026 1500
bye
```

**Expected output:**

```text
Got it. I've added this task:
  [T][ ] persist me
Now you have 1 task in the list.
Got it. I've added this task:
  [D][ ] also me (by: 27 Aug 2026 1500)
Now you have 2 tasks in the list.
Goodbye, thank you for contacting me!
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
1. [T][ ] persist me
2. [D][ ] also me (by: 27 Aug 2026 1500)
Goodbye, thank you for contacting me!
```

---

## TC-06 - due lists only what is outstanding and in range

**Aim:** `due` reports the incomplete, dated tasks falling on or before the
cutoff, soonest first; it skips completed tasks, undated todos and anything
later than the cutoff, and it leaves the numbering used by `list` untouched.

**Setup:**

```text
D | 0 | essay | 2026-08-29T09:00
T | 0 | borrow book
D | 1 | done early | 2026-08-26T09:00
E | 0 | meeting | 2026-08-26T10:00 | 2026-08-26T11:00
D | 0 | far future | 2026-12-25T09:00
```

**Input:**

```text
list
due /by 30 Aug 2026 1200
bye
```

**Expected output:**

```text
1. [D][ ] essay (by: 29 Aug 2026 0900)
2. [T][ ] borrow book
3. [D][X] done early (by: 26 Aug 2026 0900)
4. [E][ ] meeting (from: 26 Aug 2026 1000 to: 26 Aug 2026 1100)
5. [D][ ] far future (by: 25 Dec 2026 0900)
[E][ ] meeting (from: 26 Aug 2026 1000 to: 26 Aug 2026 1100)
[D][ ] essay (by: 29 Aug 2026 0900)
Goodbye, thank you for contacting me!
```

---

## TC-07 - due rejects bad input instead of crashing

**Aim:** A missing `/by`, an empty date and an unparseable date each produce a
message and leave the session running; a cutoff nothing matches says so rather
than replying with nothing.

**Setup:**

```text
T | 0 | borrow book
```

**Input:**

```text
due
due /by
due /by notadate
due /by 01 Jan 2020 0000
bye
```

**Expected output:**

```text
A due needs a /by date, for example: due /by 28 Aug 2026 1600
A due needs a /by date, for example: due /by 28 Aug 2026 1600
The date-time format notadate is not recognised, try formatting it as dd MM yyyy HHmm instead.
Nothing is due by 01 Jan 2020 0000
Goodbye, thank you for contacting me!
```

---

## TC-08 - sort orders by deadline

**Aim:** `sort` reorders the list by deadline with the soonest first, places
undated tasks after the dated ones and completed tasks last, and the order it
prints is the order `list` shows afterwards.

**Setup:**

```text
D | 0 | essay | 2026-08-29T09:00
T | 0 | borrow book
D | 1 | report | 2026-08-27T15:00
E | 0 | meeting | 2026-08-26T10:00 | 2026-08-26T11:00
D | 0 | old | 2026-08-25T08:00
```

**Input:**

```text
list
sort
list
bye
```

**Expected output:**

```text
1. [D][ ] essay (by: 29 Aug 2026 0900)
2. [T][ ] borrow book
3. [D][X] report (by: 27 Aug 2026 1500)
4. [E][ ] meeting (from: 26 Aug 2026 1000 to: 26 Aug 2026 1100)
5. [D][ ] old (by: 25 Aug 2026 0800)
I have sorted your tasks by deadline:
1. [D][ ] old (by: 25 Aug 2026 0800)
2. [E][ ] meeting (from: 26 Aug 2026 1000 to: 26 Aug 2026 1100)
3. [D][ ] essay (by: 29 Aug 2026 0900)
4. [T][ ] borrow book
5. [D][X] report (by: 27 Aug 2026 1500)
1. [D][ ] old (by: 25 Aug 2026 0800)
2. [E][ ] meeting (from: 26 Aug 2026 1000 to: 26 Aug 2026 1100)
3. [D][ ] essay (by: 29 Aug 2026 0900)
4. [T][ ] borrow book
5. [D][X] report (by: 27 Aug 2026 1500)
Goodbye, thank you for contacting me!
```

---

## TC-09 - A sorted order survives a restart

**Aim:** `sort` writes the new order to storage, so the next session starts in
sorted order rather than reverting to the original one.

**Setup:**

```text
D | 0 | later | 2026-08-29T09:00
D | 0 | sooner | 2026-08-25T08:00
```

**Input:**

```text
sort
bye
```

**Expected output:**

```text
I have sorted your tasks by deadline:
1. [D][ ] sooner (by: 25 Aug 2026 0800)
2. [D][ ] later (by: 29 Aug 2026 0900)
Goodbye, thank you for contacting me!
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
1. [D][ ] sooner (by: 25 Aug 2026 0800)
2. [D][ ] later (by: 29 Aug 2026 0900)
Goodbye, thank you for contacting me!
```

---

## TC-10 - Unreadable storage lines are skipped, not fatal

**Aim:** An unknown type letter, a missing field and an invalid completion flag
are each skipped with a warning naming how many were lost, and the readable
tasks around them still load.

**Setup:**

```text
T | 0 | good one
X | 0 | unknown type
D | 0 | missing date field
T | 5 | bad flag
E | 0 | good event | 2026-08-26T10:00 | 2026-08-26T11:00
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
Sorry, I could not read 3 lines in my records and have skipped them.
Anything I cannot read is lost the next time I save, so please check
data/Hermes.txt first if you need it.
1. [T][ ] good one
2. [E][ ] good event (from: 26 Aug 2026 1000 to: 26 Aug 2026 1100)
Goodbye, thank you for contacting me!
```

---

## TC-11 - Rejecting input the program cannot act on

**Aim:** An unrecognised command is reported by name, and a description holding
the storage separator is refused before it can corrupt the data file.

**Input:**

```text
dance
todo a | b
bye
```

**Expected output:**

```text
Sorry, I am not familiar with the 'dance' command.
Sorry, a task cannot contain '|', as I use it to separate fields when saving your tasks.
Goodbye, thank you for contacting me!
```

---

## TC-12 - Deleting several tasks at once

**Aim:** `delete` accepts more than one task number, removes every task named,
and reports them together.

**Input:**

```text
todo alpha
todo beta
todo gamma
delete 1 3
list
bye
```

**Expected output:**

```text
Got it. I've added this task:
  [T][ ] alpha
Now you have 1 task in the list.
Got it. I've added this task:
  [T][ ] beta
Now you have 2 tasks in the list.
Got it. I've added this task:
  [T][ ] gamma
Now you have 3 tasks in the list.
Roger, I've removed these tasks:
  [T][ ] alpha
  [T][ ] gamma
Now you have 1 task in the list.
1. [T][ ] beta
Goodbye, thank you for contacting me!
```

---

## TC-13 - Task numbers in any order, repeats ignored

**Aim:** The task numbers given to `delete` need not be in order, and naming
the same task twice removes it once rather than failing. The second delete
repeats a task number in a different spelling, which is the case that once
ended the session with an IndexOutOfBoundsException.

**Input:**

```text
todo alpha
todo beta
todo gamma
delete 3 1
list
delete 1 01
list
bye
```

**Expected output:**

```text
Got it. I've added this task:
  [T][ ] alpha
Now you have 1 task in the list.
Got it. I've added this task:
  [T][ ] beta
Now you have 2 tasks in the list.
Got it. I've added this task:
  [T][ ] gamma
Now you have 3 tasks in the list.
Roger, I've removed these tasks:
  [T][ ] alpha
  [T][ ] gamma
Now you have 1 task in the list.
1. [T][ ] beta
Roger, I've removed this task:
  [T][ ] beta
Now you have 0 tasks in the list.
Goodbye, thank you for contacting me!
```

---

## TC-14 - One bad number cancels the whole delete

**Aim:** When any of the numbers given to `delete` names no task, nothing is
removed. A delete that went ahead with the numbers it understood would leave
the list half changed and disagreeing with the data file.

**Input:**

```text
todo alpha
todo beta
delete 1 99
list
bye
```

**Expected output:**

```text
Got it. I've added this task:
  [T][ ] alpha
Now you have 1 task in the list.
Got it. I've added this task:
  [T][ ] beta
Now you have 2 tasks in the list.
Sorry, I have no task numbered 99.
1. [T][ ] alpha
2. [T][ ] beta
Goodbye, thank you for contacting me!
```
