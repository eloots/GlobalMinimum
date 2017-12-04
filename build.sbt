
lazy val base = (project in file("."))
  .aggregate(
    common,
    exercise_000_initial_state,
    exercise_001_some_actors,
    exercise_002_sudoku_solver
 )
  .settings(CommonSettings.commonSettings: _*)


lazy val common = project.settings(CommonSettings.commonSettings: _*)

lazy val exercise_000_initial_state = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_001_some_actors = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")

lazy val exercise_002_sudoku_solver = project
  .settings(CommonSettings.commonSettings: _*)
  .dependsOn(common % "test->test;compile->compile")
