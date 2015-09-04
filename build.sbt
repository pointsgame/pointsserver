import scalariform.formatter.preferences._

val sprayRepo = "spray" at "http://repo.spray.io"

val scalatest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"
val scalamock = "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.12.4" % "test"
val nscalaTime = "com.github.nscala-time" %% "nscala-time" % "2.2.0"
val scalaz = "org.scalaz" %% "scalaz-core" % "7.1.3"
val shapeless = "com.chuusai" %% "shapeless" % "2.2.5"
val akka = "com.typesafe.akka" %% "akka-actor" % "2.3.12"
val argonaut = "io.argonaut" %% "argonaut" % "6.1"
val sprayCan = "io.spray" %% "spray-can" % "1.3.3"
val sprayRouting = "io.spray" %% "spray-routing-shapeless2" % "1.3.3"
val sprayWebsocket = "com.wandoulabs.akka" %% "spray-websocket" % "0.1.4"
val slick = "com.typesafe.slick" %% "slick" % "3.0.2"
val sqliteJdbc = "org.xerial" % "sqlite-jdbc" % "3.8.11.1"
val slickJodaMapper = "com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0"

val commonSettings = scalariformSettings ++ Seq(
  version := "1.0.0-SNAPSHOT",
  scalaVersion := "2.11.7",
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-optimise",
    "-encoding", "utf8",
    "-Xfuture",
    "-Xlint"
  ),
  preferences := preferences.value
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DoubleIndentClassDeclaration, false),
  wartremoverWarnings ++= Seq(
    Wart.Any2StringAdd,
    Wart.Null,
    Wart.Return
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    sprayRepo
  ),
  libraryDependencies ++= Seq(
    scalatest,
    scalamock,
    scalacheck,
    scalaz
  )
)

lazy val `paper` = project.in(file("./modules/paper"))
  .settings(commonSettings: _*)

lazy val `server` = project.in(file("./modules/server"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(akka, sprayCan, sprayRouting, sprayWebsocket, argonaut, slick, sqliteJdbc))
  .dependsOn(domain, db)

lazy val `domain` = project.in(file("./modules/domain"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(nscalaTime, argonaut))

lazy val `db` = project.in(file("./modules/db"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(nscalaTime, slick, slickJodaMapper))
  .dependsOn(domain)

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .aggregate(paper, server, domain, db)
