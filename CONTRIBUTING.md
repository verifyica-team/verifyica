# Contributing

Contributions to Verifyica are both welcomed and appreciated.

The project uses an Enhanced GitHub Flow branching strategy.

- `release` branch for the latest released code (default)
- `development` for active development

Release branch code is considered "**locked**" the source of truth for the latest release. Only documentation changes are allowed.

- The `development` branch contains the latest unreleased code
- Google checkstyle format is required
- [Google Java Style](https://google.github.io/styleguide/javaguide.html) is required
- [Spotless](https://github.com/diffplug/spotless) for checking/applying Google Java Style
- PMD is used for static analysis
- Expand all Java imports
- Tags are used for releases

For changes, you should...

- Fork the repository
- Create a branch for your work off of `development`
- Make changes on your branch
- Build and test your changes
- Open a pull request targeting `development`, tagging `@antublue` for review
- A [Developer Certificate of Origin](DCO.md) (DCO) is required for all contributions

---

Copyright (C) 2024 The Verifyica project authors