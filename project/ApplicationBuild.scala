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
    "com.typesafe.play" %% "play-ws" % "2.3.3" excludeAll ExclusionRule(organization = "org.apache.httpcomponents"),
    "ws.securesocial" %% "securesocial" % "3.0-M1",
    "org.apache.httpcomponents" % "httpclient" % "4.3.1",
    "org.subethamail" % "subethasmtp" % "3.1.7" % "test,it" exclude("javax.mail", "mail"),
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


