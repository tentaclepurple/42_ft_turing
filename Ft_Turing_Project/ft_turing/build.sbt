val scala3Version = "3.5.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Ft_Turing",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    Compile / mainClass := Some("turing.TuringMachineValidator"),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "com.typesafe.play" %% "play-json" % "2.10.0-RC5"
    )
  )