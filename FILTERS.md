# Filters

Verifyica allows for various test class / test method filtering scenarios using a YAML file.

## Configuration

To use test class / test method filtering you need to configure the filter YAML file property.

```properties
verifyica.engine.filter.definitions.filename=filters.yaml
```

## Filter Types

There are four filter types:

### `IncludeClass`

- includes a test class
- includes specific test methods

### `ExcludeClass`

- excludes a test class
- excludes all test methods

### `IncludeTaggedClass`

- includes a tagged test class
- includes specific test methods

### `ExcludeTaggedClass`

- excludes a tagged test class
- excludes all test methods

## Filters YAML

Example File:

```yaml
###################################################
# All test classes / test methods are included by #
# default in the working set                      #
#                                                 #
# Filters are executed against the discovery set, #
# including / excluding them from the working set #
###################################################

# Remove all test classes and test methods
- type: ExcludeClass
  enabled: false
  classRegex: ".*"
  methodRegex: ".*"

# Include a specific package of tests classes and all test methods
- type: IncludeClass
  enabled: false
  classRegex: "org.antublue.verifyica.test.argument"
  methodRegex: ".*"

# Include a specific test class and all test methods
- type: IncludeClass
  enabled: false
  classRegex: "TagTest"
  methodRegex: ".*"

# Include all tagged classes
- type: IncludeTaggedClass
  enabled: false
  classTagRegex: ".*"

# Include classes tagged with "Foo"
- type: IncludeTaggedClass
  enabled: false
  classTagRegex: "Foo"

# Include classes tagged with "Bar"
- type: IncludeTaggedClass
  enabled: false
  classTagRegex: "Bar"

# Exclude classes tagged with "Bar"
- type: ExcludeTaggedClass
  enabled: false
  classTagRegex: "Bar"

# Include classes tagged with "Tag1" or "Tag2"
- type: IncludeTaggedClass
  enabled: false
  classTagRegex: "Tag1|Tag2"
```

---

Copyright (C) 2024 The Verifyica project authors