import scalariform.formatter.preferences._

val scalazRepo = "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
val scalamock = "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test"
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"

val commonSettings = scalariformSettings ++ Seq(
  version := "1.0.0-SNAPSHOT",
  organization := "net.pointsgame",
  scalaVersion := "2.12.3",
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-encoding", "utf8",
    "-Xfuture",
    "-Xlint"
  ),
  scalariformPreferences := scalariformPreferences.value
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DoubleIndentClassDeclaration, false),
  wartremoverWarnings ++= Seq(
    Wart.ToString,
    Wart.StringPlusAny,
    Wart.Null,
    Wart.Return
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    scalazRepo
  ),
  libraryDependencies ++= Seq(
    scalatest,
    scalamock,
    scalacheck
  )
)

lazy val paper = project.in(file("./modules/paper"))
  .settings(commonSettings: _*)

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .aggregate(paper)
