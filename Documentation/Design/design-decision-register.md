# Design Decision Register

## Review Database
Captures all design decisions and why for the project.
### Decision Use 
- git notes as database

### Why
- **Purpose**: Centralized repository for tracking and documenting design decisions made during the development process.
- **Format**: Markdown files organized by decision category (e.g., architecture, data structure, user interface).
- **Version Control**: Each decision is versioned, allowing for tracking changes and historical context.
- **Access Control**: Controlled access to ensure only authorized personnel can modify or add new decisions.
- **Documentation**: Includes rationale, alternatives considered, and impact analysis for each decision.
- **Infrastructure Agnostic**: Only requirement is git. Will work with any service (Bitbucket, GitHub, GitLab) that supports git and git notes. No reliance on external databases or services.
- **Security**: By using git notes, we can leverage existing git security and access control mechanisms to protect the integrity of the design decisions.

### Acknowledgements
- Won't have immediate support for automated updates on clients. (We will require webhooks or similar to notify users of changes)

## Data Update Method
- **File based**: Updates and changes will be stored in individual files in the repository to prevent merge conflicts.

### Why
- **Auditing**: Each change is recorded and auditable for transparency and traceability.
- **Conflict Avoidance**: By using individual files for each decision, we can avoid merge conflicts that arise from multiple contributors editing the same file.

### Acknowledgements
- Can become file heavy in the system.
  - Mitigation: We can implement a cleanup strategy to archive old decisions or use a structured directory system to manage files effectively.

## Review Discovery
