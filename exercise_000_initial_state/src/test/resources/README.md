initial-state

# Exercise 0 > Initial State

- Before we start our Journey to the **Global Minumum**, we are given a set
  of so-called reduction rules that form the basis of 

- Use the `test` command to verify the initial state works as expected. You
  should see something like the following:

```scala
man [e] > global-mimimum > initial-state > test
[info] Compiling 1 Scala source to /Users/ericloots/Trainingen/LightbendTraining/GlobalMinimum/exercise_000_initial_state/target/scala-2.12/test-classes...
[info] ReductionRuleSpec:
[info] Applying reduction rules
[info] - should Eliminate values in isolated complete sets from occurrences in other cells (First reduction rule)
[info] - should Eliminate values in isolated complete sets of 5 values from occurrences in other cells (First reduction rule)
[info] - should Eliminate values in 2 isolated complete sets of 3 values from occurrences in other cells (First reduction rule)
[info] - should Eliminate values in shadowed complete sets from occurrences in same cells (Second reduction rule)
[info] - should Eliminate values in shadowed complete (6 value) sets from occurrences in same cells (Second reduction rule)
[info] Run completed in 966 milliseconds.
[info] Total number of tests run: 5
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 5, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 2 s, completed Nov 15, 2017 10:34:49 PM
```

- Use the `nextExercise` command to move to the next exercise.
