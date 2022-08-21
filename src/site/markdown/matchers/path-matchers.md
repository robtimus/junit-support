<head>
  <title>PathMatchers</title>
</head>

## PathMatchers

Similar to how Hamcrest's [FileMatchers](https://hamcrest.org/JavaHamcrest/javadoc/2.2/org/hamcrest/io/FileMatchers.html) provides matchers for `File`, [PathMatchers](../apidocs/com/github/robtimus/junit/support/matchers/nio/file/PathMatchers.html) provides matchers for `Path`. These can be used instead of using `assertTrue` with custom messages, or `assertEquals` after extracting attributes from `Path` instances.
