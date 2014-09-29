import sbt.Keys._
import sbt._

object ApplicationBuild extends Build with Application {

  val appOrganization = "com.livingbreathingcode"
  val appName = "reactive-todolist"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.reactivemongo" %% "reactivemongo" % "0.10.5.akka23-SNAPSHOT",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "com.typesafe.play" %% "play-ws" % "2.3.3" % "test,it",
    "com.typesafe.play" %% "play-test" % "2.3.3" % "test,it")

  val appResolvers = Seq(
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/")

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


