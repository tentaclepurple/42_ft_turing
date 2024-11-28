val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "ft_turing",
    version := "0.1.0",
    scalaVersion := scala3Version,
    
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.10.0-RC9",
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )