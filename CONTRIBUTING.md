# Contributing

Contributions to Verifyica are both welcomed and appreciated.

The project uses a [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow) branching strategy, with release branches for versioned documentation.

Release branch code is considered "locked" - no code changes accepted, but documentation changes are allowed.

- The `main` branch contains the latest unreleased code
- Release branches `release-<VERSION>` contain code and documentation for a specific release
- Google checkstyle format is required
- [Google Java Style](https://google.github.io/styleguide/javaguide.html) is required
- [Spotless](https://github.com/diffplug/spotless) for checking/applying Google Java Style
- PMD is used for static analysis
- Expand all Java imports
- Tags are used for releases

For changes, you should...

- Fork the repository
- Create a branch for your work off of `main`
- Make changes on your branch
- Build and test your changes
- Open a pull request targeting `main`, tagging `@antublue` for review
- A [Developer Certificate of Origin](DCO.md) (DCO) is required for all contributions

---

Copyright (C) 2024 The Verifyica project authors