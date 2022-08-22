<head>
  <title>ArgumentsCombiner</title>
</head>

## ArgumentsCombiner

[JUnit Pioneer](https://junit-pioneer.org/) has [@CartesianTest](https://junit-pioneer.org/docs/cartesian-product/) to provide the Cartesian product of sets of arguments. Using `@CartesianTest.MethodFactory` allows you to create argument sets programmatically. It does not provide the possibility to filter out combinations though. Class [ArgumentsCombiner](apidocs/com/github/robtimus/junit/support/params/ArgumentsCombiner.html) works like JUnit Pioneer's `ArgumentSets` class but allows filtering out combinations. For instance, to create a set of all possible month-day combinations in non-leap years in an `ArgumentsProvider`:

```
class MonthDayArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return ArgumentsCombiner.with(EnumSet.allOf(Month.class))
                .crossJoin(() -> IntStream.rangeClosed(1, 31).boxed())
                .excludeCombinations(arguments -> Month.FEBRUARY.equals(arguments[0]) && (int) arguments[1] > 28)
                .excludeCombination(Month.APRIL, 31)
                .excludeCombination(Month.JUNE, 31)
                .excludeCombination(Month.SEPTEMBER, 31)
                .excludeCombination(Month.NOVEMBER, 31)
                .stream();
    }
}
```
