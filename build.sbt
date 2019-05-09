name := "doobieTestProject"

version := "0.1"

organization := "org.me"

scalaVersion := "2.12.8"

scalacOptions += "-Ypartial-unification" // 2.11.9+

lazy val doobieVersion = "0.6.0"

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium"   %  "selenium-server"     % "3.141.59",
  "postgresql"                %  "postgresql"          % "9.1-901-1.jdbc4",
  "org.scalaj"               %% "scalaj-http"          % "2.4.1",
  "org.json4s"               %% "json4s-native"        % "3.6.5",
  "org.json4s"               %% "json4s-jackson"       % "3.6.5",
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2"   % doobieVersion
)
