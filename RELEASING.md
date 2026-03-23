# Releasing Verifyica

This repository publishes artifacts to Maven Central using `./release.sh` and the Maven `release` profile.

## Prerequisites

- Run the script from the repository root where `mvnw` is available.
- Use a clean git working tree.
- Start from the `main` branch.
- Have push access to the remote tracked by `main` (or `origin` if no branch remote is configured).
- Have `gpg` available locally because the Maven `release` profile signs artifacts during `verify`.
- Have Maven settings configured for publishing to Maven Central.

## Release

Run the release script with the target version:

```bash
./release.sh 1.0.7
```

The script performs the full release flow:

1. Validates the version format.
2. Verifies the repository is on a clean `main` branch.
3. Verifies the release branch and tag do not already exist locally or on the remote.
4. Runs a baseline release build with `./mvnw -Prelease clean verify`.
5. Creates the release branch `release-<version>`.
6. Sets the project version to `<version>`.
7. Runs `./mvnw -Prelease clean verify`.
8. Runs `./mvnw -Prelease clean deploy`.
9. Commits the release changes as `Release <version>`.
10. Creates the annotated tag `v<version>`.
11. Pushes the release branch.
12. Switches back to `main`.
13. Sets the project version to `<version>-post`.
14. Runs `./mvnw -Prelease clean verify`.
15. Commits the post-release changes as `Prepare for development`.
16. Pushes `main`.
17. Pushes the release tag.

## Conventions

- Release tag: `v<version>`
- Release branch: `release-<version>`
- Post-release development version: `<version>-post`
- Accepted version format: `MAJOR.MINOR.PATCH` or `MAJOR.MINOR.PATCH-label`
- The release version must not already end in `-post`

---

Copyright (C) Verifyica project authors and contributors. All rights reserved.
