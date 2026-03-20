# Releasing

This document describes the release process for Verifyica.

## Prerequisites

- Clean git working directory (no uncommitted changes)
- `~/.m2/verifyica.settings.xml` configured for Maven Central deployment
- Git remote with push access
- On `main` or `master` branch

## Release Script

The `release.py` script handles the complete release workflow:

1. Validates prerequisites
2. Updates version across all Maven modules
3. Builds and verifies the project
4. Deploys artifacts to Maven Central
5. Creates git tag and release branch
6. Bumps to post-release version

## Usage

### Dry-Run (Default)

Preview what the release would do without making changes:

```bash
./release.py 1.0.7
```

### Execute Release

Perform the actual release:

```bash
./release.py 1.0.7 --execute
```

### Verbose Output

Enable detailed logging:

```bash
./release.py 1.0.7 --execute --verbose
```

### Resume from Step

Resume an interrupted release from a specific step:

```bash
./release.py 1.0.7 --execute --resume deploy
```

Available steps: `check_prerequisites`, `update_version`, `build_verify`, `deploy`, `git_release`, `post_release`

### Specify Git Remote

```bash
./release.py 1.0.7 --execute --remote upstream
```

### Disable Colored Output

```bash
./release.py 1.0.7 --execute --no-color
```

## Release Workflow

| Step | Description |
|------|-------------|
| `check_prerequisites` | Validates settings file, git, and mvnw exist |
| `update_version` | Sets version across all Maven modules |
| `build_verify` | Runs `./mvnw -B clean verify` |
| `deploy` | Deploys to Maven Central using settings file |
| `git_release` | Commits, tags, creates and pushes release branch |
| `post_release` | Bumps version to `-post` for next development cycle |

## Version Format

Versions must follow semantic versioning: `MAJOR.MINOR.PATCH`

Examples:
- `1.0.7`
- `1.1.0`
- `2.0.0-beta`

Versions ending with `-post` are reserved for post-release development snapshots.

## Rollback

If a release fails, the script attempts to rollback to the original state. However, you may need to manually:

1. `git checkout main` (or your original branch)
2. Reset version if needed: `./mvnw versions:set -DnewVersion=X.Y.Z-post -DprocessAllModules`
3. Clean backup files: `rm -Rf $(find . -name "*versionsBackup")`

## Help

```bash
./release.py --help
```

---

Copyright (C) Verifyica project authors and contributors. All rights reserved.