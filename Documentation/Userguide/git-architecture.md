# Git Architecture
How the Serverless Review Tool uses Git Notes for distributed code reviews.
## Core Concept
All review data stored in **Git Notes** under `refs/notes/reviews/`.
### Why Git Notes?
- No central server required
- Version controlled
- Syncs like code via Git
- Fully distributed
## Data Structure
```
refs/notes/reviews/{review-id}/
├── metadata/
│   ├── title
│   ├── description
│   ├── status
│   └── branches
├── reviewers
└── comments/{comment-id}/
    ├── metadata
    ├── text
    └── status
```
## Append-Only Design
Never modify data, only append new entries in NDJSON format:
```jsonl
{"id":"01890...","timestamp":"2026-05","editor":"alice","data":"value1"}
{"id":"01891...","timestamp":"2026-05","editor":"bob","data":"value2"}
```
Benefits:
- Complete history preserved
- Rare conflicts
- Multiple users can append simultaneously
## UUID v7 IDs
Time-ordered globally unique IDs:
- 48 bits timestamp
- 74 bits randomness
- Sortable by time
- No coordinator needed
## Synchronization
Push reviews:
```bash
git push origin 'refs/notes/reviews/*'
```
Fetch reviews:
```bash
git config notes.mergeStrategy union
git fetch origin 'refs/notes/reviews/*'
```
## Trust Model
- Relies on Git remote access
- Git config for identity
- Full audit trail
- Suitable for trusted teams
## Performance
Scales to:
- ~1,000 files per review
- ~10,000 comments per review
- ~10,000 active reviews per repo
---
[← Comment System](comments-system.md) | [Back to Index](index.md)
