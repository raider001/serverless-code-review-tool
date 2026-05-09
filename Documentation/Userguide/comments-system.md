# Comment System
The three-state comment system distinguishes between observations and actionable issues.
## The Three States
### 1. Observation (Blue 🔵)
General feedback that doesn't block approval.
**Use for**:
- Code style suggestions
- Questions about implementation
- Future improvement ideas
- Positive feedback
### 2. Unresolved (Orange 🟠)
Issues requiring resolution before approval.
**Use for**:
- Bugs or errors
- Security vulnerabilities
- Missing features
- Standard violations
### 3. Resolved (Green 🟢)
Issues that have been addressed.
## The One-Way Gate
```
Observation → [ONE-WAY] → Unresolved ↔ Resolved
```
- Observations stay observations (cannot become unresolved)
- Unresolved issues can toggle to/from resolved
- Clear intentionality required to flag issues
## Creating Comments
**Observation**: Just type and save
**Unresolved**: Check "Mark as Needs Resolution"
## Resolution Workflow
1. Reviewer creates unresolved comment
2. Author fixes the code
3. Author marks as resolved
4. Reviewer verifies fix
5. If inadequate, mark as unresolved again
## Best Practices
- Use observations for suggestions
- Reserve unresolved for actual issues
- Fix code before marking resolved
- Add explanation when resolving
---
[← Managing Reviews](managing-reviews.md) | [Next: Git Architecture →](git-architecture.md)
