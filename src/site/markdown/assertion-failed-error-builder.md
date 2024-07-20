<head>
  <title>AssertionFailedErrorBuilder</title>
</head>

## AssertionFailedErrorBuilder

JUnit 5.9.0 introduced [AssertionFailureBuilder](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/AssertionFailureBuilder.html). That's quite useful, but it's a bit strict in its definition on actual and expected.

Class [AssertionFailedErrorBuilder](apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/AssertionFailedErrorBuilder.html) is like an extended version of `AssertionFailureBuilder`. It adds the following additional features:
* Providing prefixes for both expected and actual values, e.g. `expected: caused by <expected> but was: caused by <actual>`
* Providing multiple expected and actual values, e.g. `expected: one of <expected1>, <expected2> but was: <actual>`
* Providing non-formatted text for "expected" values, e.g. `expected: matching the predicate but was: <actual>`
* Building reasons with value formatting
