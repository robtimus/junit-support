<head>
  <title>AbstractInjectExtension</title>
</head>

## AbstractInjectExtension

Class [AbstractInjectExtension](../apidocs/com/github/robtimus/junit/support/extension/AbstractInjectExtension.html) can be used as base class for JUnit extensions that can inject objects into fields, constructor parameters or method parameters based on an annotation.

To create such an extension:

* Add a no-argument constructor that passes the annotation type to the `AbstractInjectExtension` constructor.
* Implement `validateTarget`. This method takes the injection target (see below), the annotation, and the extension context, and should return an empty `Optional` if the injection target is properly annotated, or an `Optional` with a `JUnitException` otherwise. The exception should be provided by one of the injection target argument's `createException` methods.
* Implement `resolveValue`. This method takes the annotation, the injection target, and the extension context, and should return the value to inject.

The injection target is provided as an instance of class [InjectionTarget](../apidocs/com/github/robtimus/junit/support/extension/InjectionTarget.html), and provides an abstraction of the field, constructor parameter or method parameter. It offers methods to provide the injection target's type, find annotations, or create exceptions without having to know whether the target is a field, a constructor parameter or a method parameter.

For an example extension, see [TestResourceExtension](https://github.com/robtimus/junit-support/blob/master/src/main/java/com/github/robtimus/junit/support/extension/testresource/TestResourceExtension.java).
