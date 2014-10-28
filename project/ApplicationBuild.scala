import sbt.Keys._
import sbt._

object ApplicationBuild extends Build with Application {

  val appOrganization = "com.livingbreathingcode"
  val appName = "reactive-todolist"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "ws.securesocial" %% "securesocial" % "3.0-M1",
    "com.typesafe.play" %% "play-ws" % "2.3.3" % "test,it",
    "com.typesafe.play" %% "play-test" % "2.3.3" % "test,it")

  val appResolvers = Seq(
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/")

  lazy val plugins = Seq(
    play.PlayScala)

  lazy val jvmSettings = Seq(
    targetJvm := "jvm-1.7")

  lazy val scalaSettings = Seq(
    scalaVersion := "2.11.2",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-Xlint",
      "-language:_",
      "-target:" + targetJvm.value,
      "-Xmax-classfile-name", "100",
      "-encoding", "UTF-8"))
}


