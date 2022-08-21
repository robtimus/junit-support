<head>
  <title>Disabling pre-defined tests</title>
</head>

## Disabling pre-defined tests

Sometimes it's necessary to disable a test, e.g. because it doesn't apply to the class to test. An example is testing a partial sub list of an empty list; the only sub list to return is the full list. To disable a test, simply override it and don't apply any JUnit annotation to the method; this will make JUnit ignore the test.
