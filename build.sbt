import scalariform.formatter.preferences._

val scalazRepo = "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
val scalamock = "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test"
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
val nscalaTime = "com.github.nscala-time" %% "nscala-time" % "2.16.0"
val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.15"
val scalazConcurrent = "org.scalaz" %% "scalaz-concurrent" % "7.2.15"
val shapeless = "com.chuusai" %% "shapeless" % "2.2.5"
val akka = "com.typesafe.akka" %% "akka-actor" % "2.5.4"
val argonaut = "io.argonaut" %% "argonaut" % "6.2"
val slick = "com.typesafe.slick" %% "slick" % "3.2.0"
val sqliteJdbc = "org.xerial" % "sqlite-jdbc" % "3.20.0"
val slickJodaMapper = "com.github.tototoshi" %% "slick-joda-mapper" % "2.3.0"
val http4sDsl = "org.http4s" %% "http4s-dsl" % "0.15.16"
val http4sBlaze = "org.http4s" %% "http4s-blaze-server" % "0.15.16"
val http4sArgonaut = "org.http4s" %% "http4s-argonaut" % "0.15.16"

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

lazy val server = project.in(file("./modules/server"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(http4sDsl, http4sBlaze, http4sArgonaut, argonaut, slick, sqliteJdbc))
  .dependsOn(domain, db)

lazy val domain = project.in(file("./modules/domain"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(nscalaTime, argonaut, akka, scalaz, scalazConcurrent))

lazy val db = project.in(file("./modules/db"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(nscalaTime, slick, slickJodaMapper))
  .dependsOn(domain)

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .aggregate(paper, server, domain, db)
