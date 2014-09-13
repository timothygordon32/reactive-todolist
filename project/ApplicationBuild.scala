import com.joescii.SbtJasminePlugin._
import sbt.Keys._
import sbt.Tests.{SubProcess, Group}
import sbt._

object ApplicationBuild extends Build with Application {

  val appOrganization = "com.livingbreathingcode"
  val appName = "reactive-todolist"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.reactivemongo" %% "reactivemongo" % "0.10.5.akka23-SNAPSHOT",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT",
    "com.typesafe.play" %% "play-ws" % "2.3.3" % IntegrationTest,
    "com.typesafe.play" %% "play-test" % "2.3.3" % IntegrationTest)

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

  def jasmineAdditionalSettings() = Seq(
    appJsDir <+= baseDirectory / "app/assets/javascripts",
    appJsLibDir <+= baseDirectory / "public/javascripts",
    jasmineTestDir <+= baseDirectory / "test/assets/javascripts",
    jasmineConfFile <+= baseDirectory / "test/assets/javascripts/test.dependencies.js",
    // link jasmine to the standard 'sbt test' action. Now when running 'test' jasmine tests will be run, and if they pass
    // then other Play tests will be executed.
    (test in Test) <<= (test in Test) dependsOn (jasmine)
  )

  def testSettings() = Seq(
    fork in Test := false,
    parallelExecution in Test := false,
    addTestReportOption(Test)
  )

  def integrationTestSettings() = Seq(
    unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
    unmanagedResourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "target" / "web" / "public" / "test")),
    fork in IntegrationTest := false,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false)

  lazy val project = Project(appName, file("."))
    .enablePlugins(play.PlayScala)
    .settings(
      name := appName,
      version := appVersion,
      organization := appOrganization,
      isSnapshot := version.value.contains("SNAPSHOT"))
    .settings(jvmSettings: _*)
    .settings(scalaSettings: _*)
    .settings(
      libraryDependencies ++= appDependencies)
    .settings(jasmineSettings : _*)
    .settings(jasmineAdditionalSettings() : _*)
    .settings(inConfig(TemplateTest)(Defaults.testSettings): _*)
    .settings(testSettings(): _*)
    .configs(IntegrationTest)
    .settings(inConfig(TemplateItTest)(Defaults.itSettings): _*)
    .settings(integrationTestSettings(): _*)
    .settings(
      resolvers ++= appResolvers)

}

trait Application {

  lazy val targetJvm = settingKey[String]("The version of the JVM the build targets")

  val allPhases = "tt->test;test->test;test->compile;compile->compile"
  val allItPhases = "tit->it;it->it;it->compile;compile->compile"

  lazy val TemplateTest = config("tt") extend Test
  lazy val TemplateItTest = config("tit") extend IntegrationTest

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }

  def addTestReportOption(conf: Configuration, directory: String = "test-reports") = {
    val testResultDir = "target/" + directory
    testOptions in conf += Tests.Argument("-o", "-u", testResultDir, "-h", testResultDir + "/html-report")
  }

}
