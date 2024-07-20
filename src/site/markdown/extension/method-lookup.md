<head>
  <title>MethodLookup</title>
</head>

## MethodLookup

Class [MethodLookup](../apidocs/com.github.robtimus.junit.support/com/github/robtimus/junit/support/extension/MethodLookup.html) can be used to find methods from references, in a similar way as [@MethodSource](https://junit.org/junit5/docs/current/api/org.junit.jupiter.params/org/junit/jupiter/params/provider/MethodSource.html).

Method references can come in the following formats:

| Format                                                   | Description                                                                       |
|----------------------------------------------------------|-----------------------------------------------------------------------------------|
|`<methodName>`                                            | A method in the test class itself                                                 |
|`<methodName(<parameterTypes>)`                           | A method in the test class itself, with the given comma separated parameter types |
|`<fullyQualifiedClassName>#<methodName>`                  | A method in the given class                                                       |
|`<fullyQualifiedClassName>#<methodName>(<parameterTypes>)`| A method in the given class, with the given comma separated parameter types       |

### Preferred lookup

To find a method based on one or more preferred sets of parameter types, use the following pattern:

```java
MethodLookup.Result result = MethodLookup.withParameterTypes(String.class, int.class)
        .orParameterTypes(String.class)
        .find(methodReference, context);
```

If the method reference has an explicit set of parameter types, the referred-to method is validated against the specified parameter combinations. If the method was not found, or if its parameter types do not match any of the specified parameter combinations, an error is thrown. Otherwise, the result contains the referred-to method and the index of the specified parameter combination that matched the method reference's parameter types. For instance, in the above example, the combination `(String.class, int.class)` has index 0, and `(String.class)` has index 1. This index can be used to easily determine how to invoke the method.

If on the other hand the method reference does not have an explicit set of parameter types, the method lookup attempts all specified parameter combinations in order until a match is found. If no matching method is found, an error is thrown. Otherwise, the result contains the method that was found and the index of the specified parameter combination used to find the method.

### @MethodSource style lookup

To find a method based only on the method reference, use the following pattern:

```java
Method method = MethodLookup.findMethod(methodReference, context);
```

If the method reference has an explicit set of parameter types, the referred-to method is returned. If the method was not found, an error is thrown instead.

If on the other hand the method reference does not have an explicit set of parameter types, the method will return the single method with the given name. If there is no method with the given name, or if there are multiple methods, an error is thrown instead.
