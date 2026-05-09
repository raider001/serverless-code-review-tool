# Code Review Workflow Design

This document describes the lifecycle of a code review in the Serverless Review Tool,
from both the **author's** perspective and the **reviewer's** perspective.
It also defines the precise conditions under which each state transition occurs.

---

## Status Reference

### Overall Review Status (`ReviewStatus`)

| Status | Display | Meaning |
|---|---|---|
| `OPEN` | Open (blue) | Review is active and accepting feedback |
| `IN_PROGRESS` | In Progress (amber) | The author is actively addressing feedback |
| `CHANGES_REQUESTED` | Changes Requested (red) | One or more reviewers have requested changes |
| `COMPLETED` | Completed (green) | The author has closed the review as done |
| `CANCELLED` | Cancelled (grey) | The author has abandoned the review |

### Per-Reviewer Status (`ReviewerStatus`)

| Status | Display | Meaning |
|---|---|---|
| `REVIEWING` | Reviewing (blue) | Reviewer is assigned and actively reviewing |
| `APPROVED` | Approved (green) | Reviewer has approved the changes |
| `CHANGES_REQUESTED` | Changes Requested (red) | Reviewer requires changes before approval |

---

## Roles

| Role | Who |
|---|---|
| **Author** | The person who created the review. Identified by the `author` field on the review. |
| **Reviewer** | Anyone listed in the review's reviewer list. |
| **Author + Reviewer** | The author when they are also in the reviewer list (allowed and common). |
| **Bystander** | Any registered user who is neither the author nor a reviewer. |

---

## Workflow: Author's Perspective

### 1. Creating a Review

The author opens the **Create Review** dialog and provides:

- **Title** — required, cannot be empty.
- **Author** — required, must be a known user.
- **Branch** — the feature branch to review, required.
- **Base Branch** — the branch to diff against, required.
- **Repositories** — one or more repositories where the branch exists.
- **Reviewers** — at least one reviewer from the known users list.
- **Summary** — optional description of the changes.

On submission the tool:
1. Fetches commits from every selected repository in the range `baseBranch..branch`.
2. Fails with an error if no commits are found (branches may be identical or missing).
3. Writes all review metadata to git notes and pushes to `origin`.
4. Sets the initial overall status to **`OPEN`**.
5. Sets each reviewer's initial status to **`REVIEWING`**.

The review is now visible to all participants.

---

### 2. Editing a Review

Available to: **Author + Reviewer only** (Edit button enabled only when current user is a reviewer).

The author opens the **Edit Review** dialog. Changes are auto-saved — there is no
explicit Save button. The dialog persists changes immediately on field exit or
reviewer modification.

**Editable fields:**
- Title
- Author
- Summary
- Reviewer list (add or remove reviewers)
- Repositories (add or remove)

**Branch and base branch are read-only** once the review is created.

**Removing a reviewer** writes a `left` status entry for that reviewer in git notes,
ensuring they are excluded from all future reads of the review.

---

### 3. Closing a Review

Available to: **Author + Reviewer only** (button labelled "Close Review").

Conditions required:
- The current user matches the review's `author` field.
- The current user is also in the reviewer list.
- The review status is not already `COMPLETED` or `CANCELLED`.

On clicking **Close Review**:
1. Overall review status is set to **`COMPLETED`**.
2. Metadata is saved to git notes and pushed.
3. The header status badge updates to green "Completed".

A completed review is a terminal state — there is currently no reopen action.

---

### 4. Marking In Progress

Available to: **Author only** (button labelled "Mark In Progress").

The author uses this after receiving feedback to signal they are actively working on
changes. This keeps the review listings clear — participants can filter by status
to see only what needs their immediate attention.

Conditions required:
- The current user matches the review's `author` field.
- The review status is not already `IN_PROGRESS`, `COMPLETED`, or `CANCELLED`.

On clicking **Mark In Progress**:
1. Overall review status is set to **`IN_PROGRESS`**.
2. Metadata is saved to git notes and pushed.
3. The header status badge updates to amber "In Progress".

The author returns the review to `OPEN` (or to `CHANGES_REQUESTED` as appropriate)
by pushing new commits and editing the review, or reviewers re-evaluate and change
their own statuses.

---

### 5. Cancelling a Review

Available to: **Author only** (button labelled "Cancel Review").

The author uses this to abandon a review entirely — for example because the
branch was abandoned, superseded, or merged by other means.

Conditions required:
- The current user matches the review's `author` field.
- The review status is not already `COMPLETED` or `CANCELLED`.

On clicking **Cancel Review**:
1. Overall review status is set to **`CANCELLED`**.
2. Metadata is saved to git notes and pushed.
3. The header status badge updates to grey "Cancelled".

A cancelled review is a terminal state — there is currently no reopen action.

---

## Workflow: Reviewer's Perspective

### 1. Joining a Review

Available to: **Bystanders** (any user not yet in the reviewer list).

The button shows **"Join Review"**. On click:
1. The user is added to the reviewer list with status **`REVIEWING`**.
2. The change is written to git notes and pushed.
3. The reviewer list in the header updates immediately.

---

### 2. Reviewing Changes

Once a user is a reviewer they can:
- Browse changed files in the code viewer.
- Leave inline comments on any line of any file.
- Mark comments as needing resolution.
- Resolve and re-open comments.

---

### 3. Approving or Requesting Changes

Available to: **Reviewers only** (buttons in the bottom action bar).

| Button | Effect on reviewer's status | Effect on overall review status |
|---|---|---|
| **Approve** | Sets personal status → `APPROVED` | Auto-synced: if no reviewer now has `CHANGES_REQUESTED`, overall → `IN_PROGRESS` |
| **Request Changes** | Sets personal status → `CHANGES_REQUESTED` | Overall → `CHANGES_REQUESTED` immediately |

**Auto-sync rule:** After every reviewer decision, the tool evaluates all current reviewer statuses:
- If **at least one** reviewer has `CHANGES_REQUESTED` → overall status = `CHANGES_REQUESTED`
- If **no** reviewer has `CHANGES_REQUESTED` → overall status = `IN_PROGRESS`

This rule does **not** apply when the overall status is in a terminal state (`COMPLETED` or `CANCELLED`).

The reviewer's status badge in the header updates for all participants.

---

### 4. Leaving a Review

Available to: **Reviewers who are not also the author** (button labelled "Leave Review").

On click:
1. A `left` status entry is written for this reviewer in git notes.
2. The reviewer is removed from all future loads of the reviewer list.
3. The reviewer list in the header updates.

> **Note:** If the current user is both **author and reviewer**, the "Leave Review"
> button is replaced by "Close Review". The author+reviewer cannot leave without closing.

---

## State Transition Diagram

```
                        ┌──────────────────────────────────────────────────────┐
                        │                   Review Created                     │
                        │        Overall: OPEN                                 │
                        │        Each reviewer: REVIEWING                      │
                        └───────────────────────┬──────────────────────────────┘
                                                │
          ┌──────────────────┬──────────────────┼────────────────────────────────┐
          │                  │                  │                                │
          ▼                  ▼                  ▼                                ▼
  Reviewer clicks   Author clicks      Reviewer clicks              Author clicks
  "Request Changes" "Mark In Progress" "Approve"                   "Close Review"
          │                  │                  │                                │
          ▼                  ▼                  ▼                                ▼
  Overall:           Overall:           Overall: unchanged           Overall:
  CHANGES_REQUESTED  IN_PROGRESS        (OPEN or                     COMPLETED
  Reviewer:          (Author working)   CHANGES_REQUESTED)           (terminal)
  CHANGES_REQUESTED                     Reviewer: APPROVED

          │                  │
          │                  └─── Author pushes changes ───► OPEN (re-opened for review)
          │
          └─── Author clicks "Mark In Progress" ───► IN_PROGRESS

  From any non-terminal state:
  Author clicks "Cancel Review" ──────────────────────────────────► CANCELLED (terminal)
```

---

## Button Visibility Summary

| Situation | Visible / Enabled Buttons |
|---|---|
| No review loaded | All buttons disabled |
| Current user is a **bystander** | Join Review |
| Current user is a **reviewer** (not author) | Edit *(disabled)*, Leave Review, Approve, Request Changes |
| Current user is the **author** (not reviewer) | Join Review, Mark In Progress, Cancel Review |
| Current user is both **author and reviewer** | Edit, Close Review, Mark In Progress, Cancel Review, Approve, Request Changes |

**Author button states:**

| Button | Enabled when |
|---|---|
| Mark In Progress | Status is `OPEN` or `CHANGES_REQUESTED` |
| Cancel Review | Status is not already `COMPLETED` or `CANCELLED` |
| Close Review | Status is not already `COMPLETED` or `CANCELLED` |

---

## Notes on Persistence

All state (review status, reviewer statuses, reviewer list, title, author, summary)
is stored as append-only streams in **git notes**, pushed to `origin` after every
change. This means:

- There is no separate server or database.
- The source of truth is always the remote git repository.
- After any write, the tool reloads metadata from git to show the confirmed state.
- Concurrent writes are handled via fetch-before-push with automatic retries on
  non-fast-forward or stale-ref rejections.
- Removed reviewers are not deleted — a `left` entry is appended so the history
  is preserved while they are excluded from the active list.






