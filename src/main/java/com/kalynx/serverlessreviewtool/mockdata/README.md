# Mock Git Repository Test Harness

## Overview

This package provides a comprehensive test harness for creating mock Git repositories with realistic incremental commit histories. The repositories are stored locally on the filesystem and contain actual Git history that can be used for testing the Serverless Review Tool.

## Package Structure

```
com.kalynx.serverlessreviewtool.mockdata/
├── GitRepositoryInitializer.java          # Entry point - creates all mock repositories
├── repositories/
│   ├── BaseRepository.java                # Base class with Git command execution utilities
│   ├── JavaBackendRepository.java         # Creates Java Spring Boot repository
│   ├── PythonApiRepository.java           # Creates Python Flask API repository
│   ├── ReactFrontendRepository.java       # Creates React/TypeScript repository
│   ├── javabackend/
│   │   ├── UserServiceFileMock.java       # 5-20 commits building UserService.java
│   │   ├── AuthControllerFileMock.java    # 5-20 commits building AuthController.java
│   │   ├── DatabaseConfigFileMock.java    # 5-20 commits building DatabaseConfig.java
│   │   └── UserRepositoryFileMock.java    # 5-20 commits building UserRepository.java
│   ├── pythonapi/
│   │   ├── AppFileMock.java               # 5-20 commits building app.py
│   │   ├── ModelsFileMock.java            # 5-20 commits building models.py
│   │   ├── AuthFileMock.java              # 5-20 commits building auth.py
│   │   └── DatabaseFileMock.java          # 5-20 commits building database.py
│   └── reactfrontend/
│       ├── AppFileMock.java               # 5-20 commits building App.tsx
│       ├── LoginFormFileMock.java         # 5-20 commits building LoginForm.tsx
│       ├── UserListFileMock.java          # 5-20 commits building UserList.tsx
│       └── ApiFileMock.java               # 5-20 commits building api.ts
```

## Usage

### Running from Main Method

```bash
# From project root
java -cp target/classes com.kalynx.serverlessreviewtool.mockdata.GitRepositoryInitializer
```

Or run the `main` method in GitRepositoryInitializer from your IDE.

### Generated Repository Location

All mock repositories are created at:
```
{user.home}/.serverless-review-tool/mock-repos/
```

For example, on Windows:
```
C:\Users\{username}\.serverless-review-tool\mock-repos\
```

### Generated Repositories

1. **java-backend-service/** - Spring Boot microservice
   - UserService.java with CRUD operations and validation
   - AuthController.java with REST endpoints
   - DatabaseConfig.java with HikariCP configuration
   - UserRepository.java with JPA queries

2. **python-api-service/** - Flask REST API
   - app.py with Flask application setup
   - models.py with SQLAlchemy models
   - auth.py with JWT authentication
   - database.py with database utilities

3. **react-frontend-app/** - React + TypeScript application
   - App.tsx with main application logic
   - LoginForm.tsx with authentication UI
   - UserList.tsx with user management
   - api.ts with API client

## Commit History

Each file is built incrementally with 5-20 commits (randomly chosen), demonstrating realistic development patterns:

- **Initial commits**: Basic structure and skeleton code
- **Early commits**: Core functionality implementation
- **Middle commits**: Feature additions and enhancements
- **Later commits**: Refinements, error handling, logging, validation
- **Final commits**: Polish, optimization, and advanced features

### Example Commit Sequence for UserService.java

```
1. feat: Create initial UserService class
2. feat: Add UserRepository dependency
3. feat: Implement createUser method
4. refactor: Add Spring @Service annotation
5. feat: Add getUserById method
6. feat: Add getAllUsers method and proper Optional handling
7. fix: Add validation for empty username in createUser
8. feat: Implement updateUser method
9. feat: Add logging support and deleteUser method
10. refactor: Add comprehensive logging to all methods
... (up to 20 total commits)
```

## Design Philosophy

### Incremental Development
Each file mock simulates realistic development by:
- Starting with basic structure
- Adding dependencies incrementally
- Building features step-by-step
- Refactoring and improving code quality over time
- Adding error handling, logging, and validation gradually

### Realistic Code Evolution
The generated code demonstrates:
- Import statements added as needed
- Annotations added when framework integration occurs
- Error handling introduced after basic functionality
- Logging added later in development
- Validation and edge cases handled incrementally

### Random Commit Count
Each file generates 5-20 commits randomly to create variety and simulate different development velocities.

## Integration with Git Interface

These mock repositories work with the `Git` interface defined in:
```java
com.kalynx.serverlessreviewtool.git.Git
```

The repositories contain actual Git history and can be used to test:
- Reading commit history
- Fetching repository data
- Working with diffs between commits
- Review note operations (to be added later)

## Future Enhancements

The following features are planned but not yet implemented:

1. **Review Notes Data** - Add mock review notes following the datastructure.md format
2. **Branch Management** - Create feature branches with merge commits
3. **Multiple Contributors** - Simulate different committers
4. **Conflict Scenarios** - Add merge conflicts for testing
5. **Remote Repository Simulation** - Set up bare repositories as "remotes"

## Cleanup

The initializer automatically cleans up any existing mock repositories before creating new ones. To manually clean up:

```bash
# On Windows
Remove-Item -Recurse -Force $env:USERPROFILE\.serverless-review-tool\mock-repos

# On Linux/Mac
rm -rf ~/.serverless-review-tool/mock-repos
```

## Notes

- Each run creates fresh repositories from scratch
- Total number of commits per repository: 20-80 (4 files × 5-20 commits each + initial structure commits)
- No review notes are created yet (coming in next phase)
- Repositories are fully functional Git repositories that can be opened with any Git client

