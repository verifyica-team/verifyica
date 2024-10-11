# Filters

Verifyica allows for test class filtering. By design, test method filtering is not supported.

## Configuration

To use test class filtering you need to configure the filter YAML file property.

```properties
verifyica.engine.filter.definitions.filename=filters.yaml
```

## Filter Types

There are four filter types:

### `IncludeClass`

- includes a test class

### `ExcludeClass`

- excludes a test class

### `IncludeTaggedClass`

- includes a tagged test class

### `ExcludeTaggedClass`

- excludes a tagged test class

## Filters YAML

Example File:

```yaml
#############################################################
# All test classes are included by default in the working set                      #
#
# Filters are executed against the discovery set, including
# excluding them from the working set
#############################################################

# Remove all test classes
- type: ExcludeClass
  enabled: false
  classRegex: ".*"

# Include a specific package of tests classes
- type: IncludeClass
  enabled: false
  classRegex: "org.verifyica.test.argument"

# Include a specific test class
- type: IncludeClass
  enabled: false
  classRegex: "TagTest"

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

Copyright (C) 2024-present Verifyica project authors and contributors