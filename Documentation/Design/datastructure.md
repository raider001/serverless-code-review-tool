# Data Structure Design

## Overview

This document defines the data structures used in the Serverless Code Review Tool. All data is persisted as JSON within Git Notes, ensuring the system remains truly serverless with Git as the single source of truth.

---

## Core Principles

- **Git Notes as Storage**: All review data stored in Git notes under dedicated refs namespaces
- **JSON Serialization**: All structures serialized as JSON for portability and inspectability
- **Immutable Commits**: Reviews reference specific commit SHAs but can span multiple commits
- **Distributed Sync**: Data synchronized via Git push/fetch operations
- **Trust-based Model**: No cryptographic signatures; relies on Git remote access control
- **Multi-Commit Reviews**: A review can encompass multiple commits (commit range or set)

---

## Key Design Decisions

### Reviews Span Multiple Commits

**Requirement**: A review is not attached to a single commit but can encompass many commits.

**Implications**:
- Need unique review identifier (separate from commit SHA)
- Must identify review boundaries (start commit, end commit, or commit list)
- Storage strategy cannot simply use commit SHA as the key
- Review represents a logical unit of changes across multiple commits

**Questions to Answer**:
1. How do we uniquely identify a review? (UUID, sequential ID, derived from commits?)
2. How do we define review boundaries?
   - Start commit + end commit (range)?
   - Explicit list of commit SHAs?
   - Base commit + head commit (like PR model)?
3. Where do we store the review note?
   - On the tip commit?
   - On a synthetic identifier?
   - In a separate review registry?
4. How do we discover all reviews?
   - Scan all notes?
   - Maintain an index?
   - Use specific ref structure?

---

## Git Notes Organization

### Refs Namespaces

**Append-Only Structure** - Designed for **ZERO conflicts** (100% conflict-free)

```
refs/notes/reviews/{review-id}/
├── metadata/                             # Decomposed metadata fields
│   ├── author/                           # Author updates (append-only history)
│   │   └── {timestamp-microseconds}      # Each update is a new timestamped note
│   ├── title/                            # Title updates (append-only history)
│   │   ├── {timestamp-microseconds}      # Original title
│   │   └── {timestamp-microseconds}      # Updated title (if edited)
│   ├── description/                      # Description updates (append-only history)
│   │   ├── {timestamp-microseconds}      # Original description
│   │   └── {timestamp-microseconds}      # Updated description (if edited)
│   ├── status/                           # Status updates (append-only history)
│   │   ├── {timestamp-microseconds}      # Initial status (e.g., "pending")
│   │   └── {timestamp-microseconds}      # Updated status (e.g., "in_progress", "approved")
│   ├── commits/                          # Commit list updates (append-only history)
│   │   ├── {timestamp-microseconds}      # Initial commit list
│   │   └── {timestamp-microseconds}      # Updated commit list (if modified)
|   └── reviewStrategy/                   # Review strategy (e.g., "branch, commit")
|       └── {timestamp-microseconds}      # Initial strategy
├── reviewers/                            # Individual reviewer notes (conflict-free)
│   ├── {reviewer-email}/                 # Each reviewer's history
│   │   ├── {timestamp-microseconds}      # Initial review status
│   │   └── {timestamp-microseconds}      # Updated status (if changed)
│   └── {reviewer-email}/                 # Another reviewer
│       └── {timestamp-microseconds}      # Their review status
└── comments/                             # All comments
    ├── {user}_{timestamp-microseconds}   # Individual comment (unique timestamp)
    ├── {user}_{timestamp-microseconds}   # Another comment
    └── {user}_{timestamp-microseconds}   # Another comment
```

## Data Structures

**Universal JSON Format**: All notes in the system use this consistent structure:
```json
{
  "editor": "user@example.com",  
  "data": "<any-data-type>" 
}
```


## Metadata Notes

---

### Title
**Storage**: `refs/notes/reviews/{uuid}/metadata/title/{timestamp-microseconds}`

**Structure**:
```json
{
  "editor": "john@example.com",
  "data": "Add OAuth2 login flow"
}
```

---

### Description

**Structure**:
```json
{
  "editor": "john@example.com",
  "data": "Implements OAuth2 with PKCE flow for enhanced security..."
}
```

---

### Status

**Structure**:
```json
{
  "editor": "john@example.com",
  "data": "pending"
}
```

**Possible Values**:
- `pending` - Review opened, awaiting reviewers
- `in_progress` - Review being actively reviewed
- `approved` - All required reviewers have approved
- `rejected` - Review has been rejected
- `changes_requested` - Changes requested before approval

---

### Commits
**Storage**: `refs/notes/reviews/{uuid}/metadata/commits/{timestamp-microseconds}`

**Structure**:
```json
{
  "editor": "john@example.com",
  "data": [
    "abc123def456789...",
    "def456ghi789012...",
    "ghi789jkl012345..."
  ]
}
```

### ReviewStrategy
**Storage**: `refs/notes/reviews/{uuid}/metadata/reviewStrategy/{timestamp-microseconds}`

Two formats will be supported:
- `branch,
- commit`

**Branch Structure**:
```json
{
  "editor": "john@example.com",
  "data": {
    "branchToReview": "feature/new-login",
    "branchToReviewAgainst": "master-staging"
  }
}
```

**Commit Structure**:
```json
{
  "editor": "john@example.com",
  "data": {
    "commits": [
      "abc123def456789...",
      "def456ghi789012...",
      "ghi789jkl012345..."
    ]
  }
}
```

---

---

## Reviewers Notes

**Storage**: `refs/notes/reviews/{uuid}/reviewers/{reviewer-email}/{timestamp-microseconds}`

**Structure**:
```json
{
  "editor": "jane@example.com",
  "data": {
    "status": "approved",
    "summary_comment": "Looks good overall, minor suggestions in comments"
  }
}
```

**Possible Status Values**:
- `pending` - Assigned but not yet reviewed
- `approved` - Reviewer approves the changes
- `rejected` - Reviewer rejects the changes
- `changes_requested` - Reviewer requests modifications

**Example** (with status changes):
```
refs/notes/reviews/550e8400-.../reviewers/alice@example.com/
├── 2026-04-23T15:00:00.123456Z   # {"status": "pending", ...}
└── 2026-04-23T16:00:00.234567Z   # {"status": "approved", ...} (updated)

refs/notes/reviews/550e8400-.../reviewers/bob@example.com/
├── 2026-04-23T15:30:00.345678Z   # {"status": "pending", ...}
└── 2026-04-23T17:15:00.456789Z   # {"status": "changes_requested", ...} (updated)
```
---



### 2. Comment

**Purpose**: Comments and discussions on specific parts of the code

**Storage**: `refs/notes/reviews/{review-id}/comments/{user}_{timestamp-microseconds}`

**Structure**:
```json
{
  "editor": "john@example.com",
  "data": {
    "text": "Consider adding null check here",
    "context": {
      "commit": "abc123def456...",
      "file": "src/login.py",
      "line": 42,
      "line_end": 45,
      "code_snippet": "if user.id:"
    },
    "reply_to": "janesmith_2026-04-23T13:00:00.456789Z",
    "resolved": false,
    "type": "suggestion"
  }
}
```

---

## Summary

**Storage Paths**:
- Author: `refs/notes/reviews/{uuid}/metadata/author/{timestamp}`
- Title: `refs/notes/reviews/{uuid}/metadata/title/{timestamp}`
- Description: `refs/notes/reviews/{uuid}/metadata/description/{timestamp}`
- Status: `refs/notes/reviews/{uuid}/metadata/status/{timestamp}`
- Commits: `refs/notes/reviews/{uuid}/metadata/commits/{timestamp}`
- Reviewers: `refs/notes/reviews/{uuid}/reviewers/{email}/{timestamp}`
- Comments: `refs/notes/reviews/{uuid}/comments/{user}_{timestamp}`

**Design Philosophy**:
- **Structure IS the data**: The hierarchical path provides context
- **JSON for content only**: Minimal `{"editor": "...", "data": ...}` payload
- **Never update, always append**: Immutable history eliminates conflicts
- **Timestamps for ordering**: Microsecond precision ensures uniqueness
- **Editor for accountability**: Every change tracked to source

## References

- [Mission Directive](../MISSION_DIRECTIVE.md)
- [Git Notes Documentation](https://git-scm.com/docs/git-notes)

---




*Document Status: **STABLE** - Core design finalized*
