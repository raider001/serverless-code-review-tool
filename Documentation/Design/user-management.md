# User Management Architecture

## Fundamental Limitations of Git-Based User Management

### What Git Config Can't Do

1. **Cannot Verify Identity**
   - `git config user.name` can be set to anything: `git config user.name "anyone"`
   - No cryptographic proof that "alice" is actually Alice
   - Anyone with repo access can claim to be anyone else

2. **Cannot Prevent Spoofing**
   - Local config is not signed or validated
   - Someone can change their identity between commits
   - No audit trail of who actually made local changes

3. **Cannot Enforce Consistent Identity**
   - Different devices may have different configs
   - Developer might accidentally push as "John" instead of "john"
   - No central authority to reconcile spelling/format differences

4. **Cannot Provide Audit Trail of Local Actions**
   - Git tracks commits, but not local tool actions
   - Can't prove "alice" actually clicked "Approve" - only that commit author says "alice"
   - LocalToolEvents (comments, approvals) are tracked in Git Notes, but based on trusting git config

### Why We Accept These Limitations

**Key Insight:** Git's authentication already happened. If someone pushed this code, they had valid credentials (SSH key or HTTPS token) authorized by GitHub/GitLab/etc.

**Trust Chain:**
```
1. Remote validates identity (SSH/HTTPS credentials)
   ✅ GitHub/GitLab confirms: push came from authorized account
   
2. Local tool trusts git config
   ⚠️  Tool assumes: if you authenticated remotely, we trust your local git config
   
3. Result: Git identity sufficient for code review workflows
   ✓ Prevents anonymous reviews
   ✓ Prevents random people claiming they reviewed
   ✓ Maintains reasonable audit trail
```

**Acceptable For:** Teams where developers already trust each other and have secure credentials

**Not Acceptable For:** 
- Open source projects with untrusted contributors
- Highly regulated environments needing cryptographic proof
- Organizations with strong identity verification requirements

---

## What User Management WILL Do

### Join/Leave Reviews
✅ Users can join reviews they're interested in
✅ Users can leave reviews to stop tracking them
✅ Reviewer list is maintained and distributed
✅ Audit trail of who joined/left (with timestamps)

### Permission Gating
✅ Non-reviewers: View code only
✅ Reviewers: Can comment, approve, request changes, edit review
✅ Permissions checked per-action (server-side)
✅ UI reflects permissions (buttons enabled/disabled)

### Distributed Reviewer Tracking
✅ Reviewer list stored in Git Notes (distributed with repo)
✅ No central database required
✅ Survives offline workflows
✅ Resolves merge conflicts automatically

### Basic Audit Trail
✅ Records which user joined review (timestamp)
✅ Records which user left review (status: "LEFT")
✅ Records creator of comments
✅ Records who approved/requested changes
✅ Can reconstruct review history

---

## What User Management WON'T Do

### No Cryptographic Verification
❌ Will NOT verify git config matches actual person
❌ Will NOT validate commit signatures (optional future)
❌ Will NOT check SSH keys
❌ Will NOT integrate with LDAP/Active Directory

### No Spoofing Prevention
❌ Will NOT stop Charlie from setting `git config user.name "alice"`
❌ Will NOT detect if someone else used Alice's computer
❌ Will NOT validate multi-factor authentication
❌ Will NOT require login/password authentication

### No Central Authority
❌ Will NOT have a central user database
❌ Will NOT sync with GitHub/GitLab user lists
❌ Will NOT assign permissions from a role database
❌ Will NOT enforce password policies

### No Complex Authorization
❌ Will NOT have role-based access control (RBAC)
❌ Will NOT have fine-grained permissions
❌ Will NOT deny certain users from reviewing
❌ Will NOT have approval chains or hierarchies

### No User Administration
❌ Will NOT have admin panel to manage users
❌ Will NOT have user provisioning/deprovisioning
❌ Will NOT track user activity across all reviews
❌ Will NOT have compliance reporting

---

## Core Principles

### Principle 1: Trust Your Credentials
If someone authenticated to Git remote (SSH/HTTPS), their subsequent identity claims are trusted locally.

### Principle 2: Distributed Verification
Authorization decisions are recorded in Git, verified by Git operations, not a central server.

### Principle 3: Minimal State
No separate user database, no config file, no registration required. Just use git config.

### Principle 4: Audit Over Prevention
We can't prevent spoofing, but we CAN record who claimed to do what (audit trail).

### Principle 5: Suitable for Trusted Teams
Design assumes developers are:
- All part of same organization/team
- Already trust each other socially
- All have secure credentials already
- Want simplicity over security theater

---

## Git Config Identity Model

### How Identification Works

**Source (in order of preference):**
1. `git config user.name` (repository or global config)
2. `git config --global user.name` (global config)
3. Java system property `user.name` (OS-level fallback)

**Used For:**
- Review creation/authorship
- Reviewer tracking (who joined a review)
- Comment authorship  
- Approval/rejection tracking
- All audit trails

### The Assumption

> Developers legitimately cloned the repo using credentials GitHub/GitLab authenticated. We trust local git config represents their actual identity.

### Why This Works in Practice

- **Social enforcement**: Developers know their actions are tracked and auditable
- **Git push enforcement**: Only credentialed accounts can write reviews to remote
- **Small team assumption**: In a 50-person team, spoofing would be quickly noticed
- **Code review context**: Authors care about who reviews their code
- **Simplicity**: No complex identity infrastructure needed

---

## Reviewer List Management

### How It's Stored

**Location:** Git Notes (distributed with repository)
**Format:** NDJSON (newline-delimited JSON)
**Pattern:** Append-only log

**Example Entry:**
```json
{"name": "alice", "status": "REVIEWING", "timestamp": "2026-05-09T10:30:00Z"}
{"name": "alice", "status": "LEFT", "timestamp": "2026-05-09T11:00:00Z"}
{"name": "alice", "status": "REVIEWING", "timestamp": "2026-05-09T11:15:00Z"}
```

### Resolution Rules

1. **For each user:** Keep only the latest entry (by timestamp)
2. **Filter:** Exclude any entries with status="LEFT"
3. **Result:** Current reviewer list

**Example:**
- Alice joined (REVIEWING)
- Alice left (LEFT)  
- Alice rejoined (REVIEWING)
- **Current state:** Alice is reviewer (latest entry is REVIEWING)

### Why Append-Only?

✅ No overwrite conflicts (both sides add, never erase)
✅ Complete audit trail (can see full history)
✅ Git merges automatically (union merge strategy)
✅ Lightweight operations (just appending)
✅ Supports rejoining reviews

---

## The Spoofing Prevention Dilemma

### The Tension

**Option A: Add Local Verification**
- Check git config against SSH keys
- Validate against Git commit history
- Require SSH signing
- Make it harder to spoof in the UI

**Option B: Accept Git-Level Spoofing**
- Don't add extra verification
- Trust git config as-is
- Acknowledge the limitation
- Keep system simple

### The Catch

Both options have the same fundamental problem:

```
If someone wants to create a fake review, they can:

Option A (with local verification):
  ❌ Can't spoof in the UI (blocked by verification)
  ✓ But can git push reviews directly, bypassing verification
  → Security theater: verification only protects against lazy spoofing

Option B (no local verification):
  ✓ Can spoof in the UI (easy)
  ✓ Can git push reviews directly (easy)
  → Honest about the limitation
```

### The Real Issue

Local verification in the tool only works if:
1. **You trust the tool** (they're using it correctly)
2. **People don't bypass the tool** (they're not manually git pushing)

But in a Git-based, distributed system:
- People CAN manually git push (it's Git!)
- You CAN'T prevent them (it's distributed!)
- Therefore, local verification has limited value

### Current Decision: No Local Spoofing Prevention

**Why:** This is a Git-based tool. If someone determined wants to spoof, they can work around any local checks by pushing directly. Local verification creates a false sense of security without actually preventing spoofing.

**Better approach:** When cryptographic verification is needed:
- Use cryptographic signatures (GPG/SSH signing on Git Notes)
- Verify at the Git level, not just in the UI
- Can't be bypassed because it's stored in the verified commit

**For now:** Accept the limitation. Suitable teams don't need this protection.

---

## Summary: What We're Actually Building

| Aspect | What It IS | What It ISN'T |
|--------|-----------|--------------|
| **Identity** | Git config trust chain | Cryptographic verification |
| **Authorization** | Permission gating per-review | Central access control |
| **Storage** | Git Notes (distributed) | User database |
| **Audit Trail** | Who acted when/where | Proof of identity |
| **Suitability** | Trusted teams | Open source/hostile environments |
| **Spoofing Prevention** | Accepted limitation | Local verification (bypassed anyway) |

**Bottom Line:** User management for Serverless Review Tool is simple, distributed, and sufficient for teams that already trust each other. We accept spoofing as a limitation because local prevention doesn't actually prevent determined spoofing in a Git-based system.



