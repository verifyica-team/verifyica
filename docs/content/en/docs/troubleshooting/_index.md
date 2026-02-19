---
title: "Troubleshooting"
linkTitle: "Troubleshooting"
weight: 8
description: >
  Solutions to common problems
---

This section helps you diagnose and fix common issues with Verifyica.

## Quick Troubleshooting

| Problem | Common Cause | Solution |
|---------|--------------|----------|
| No tests discovered | Missing @ArgumentSupplier or @Test | Add required annotations |
| Tests run sequentially | Default parallelism = 1 | Configure parallelism |
| Test failures with parallelism | Shared mutable state | Use instance variables or synchronization |
| Container startup failures | Port conflicts | Use dynamic ports or sequential execution |
| Tests timeout | Blocking operations | Check for deadlocks, increase timeout |

## Troubleshooting Guides

- [**Common Issues**](common-issues/) - Frequent problems and solutions
- [**FAQ**](faq/) - Frequently asked questions

## Getting Help

If you can't find a solution:

1. **Search GitHub Issues:** https://github.com/verifyica-team/verifyica/issues
2. **Ask in Discussions:** https://github.com/verifyica-team/verifyica/discussions
3. **File a Bug Report:** https://github.com/verifyica-team/verifyica/issues/new

## Providing Debug Information

When asking for help, include:

```properties
# Enable debug logging
verifyica.engine.logger.level=DEBUG
```

Run tests and provide:

- Verifyica version
- Java version
- Test code (minimal reproduction)
- Full error messages and stack traces
- Configuration (verifyica.properties)
