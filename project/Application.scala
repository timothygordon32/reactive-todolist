import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.Import.WebKeys._
import com.joescii.SbtJasminePlugin._
import sbt.Keys._
import sbt.Tests.{SubProcess, Group}
import sbt._
import com.typesafe.sbt.less.Import.LessKeys

trait Application {

  lazy val targetJvm = settingKey[String]("The version of the JVM the build targets")

  def appOrganization: String
  def appName: String
  def appVersion: String
  def appDependencies: Seq[ModuleID]
  def appResolvers: Seq[Resolver]
  def plugins: Seq[Plugins]
  def jvmSettings: Seq[Setting[_]]
  def scalaSettings: Seq[Setting[_]]

  def jasmineAdditionalSettings() = Seq(
    appJsDir := Seq(baseDirectory.value / "app/assets/javascripts"),
    appJsLibDir := Seq(baseDirectory.value  / "public/javascripts"),
    jasmineTestDir := Seq(baseDirectory.value  / "test/assets/javascripts"),
    jasmineConfFile := Seq(baseDirectory.value  / "test/assets/javascripts/test.dependencies.js"),
    (test in Test) <<= (test in Test) dependsOn jasmine
  )

  def testSettings() = Seq(
    fork in Test := false,
    parallelExecution in Test := true,
    addTestReportOption(Test)
  )

  def integrationTestSettings() = Seq(
    unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
    // Workaround so that integration tests can be run in an IDE
    unmanagedResourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "target" / "web" / "public" / "test")),
    // Ensure that ;clean; it:test can be run without running test
    unmanagedClasspath in IntegrationTest <<= (unmanagedClasspath in IntegrationTest, baseDirectory) map { (uc, base) =>
      Attributed.blank(base / "target" / "web" / "public" / "test") +: uc
    },
    fork in IntegrationTest := false,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := true,
    (compile in IntegrationTest) <<= (compile in IntegrationTest).dependsOn(assets in TestAssets)
  )

  private def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }

  private def addTestReportOption(conf: Configuration, directory: String = "test-reports") = {
    val testResultDir = "target/" + directory
    testOptions in conf += Tests.Argument("-o", "-u", testResultDir, "-h", testResultDir + "/html-report")
  }

  def lessSettings() = Seq(
    includeFilter in (Assets, LessKeys.less) := "*.less",
    LessKeys.compress := true
  )

  lazy val project = Project(appName, file("."))
    .enablePlugins(plugins:_ *)
    .settings(
      name := appName,
      version := appVersion,
      organization := appOrganization,
      isSnapshot := version.value.contains("SNAPSHOT"))
    .settings(jvmSettings: _*)
    .settings(scalaSettings: _*)
    .settings(resolvers := appResolvers)
    .settings(libraryDependencies ++= appDependencies)
    .settings(jasmineSettings : _*)
    .settings(jasmineAdditionalSettings() : _*)
    .settings(lessSettings() : _*)
    .settings(inConfig(Test)(Defaults.testSettings): _*)
    .settings(testSettings(): _*)
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(integrationTestSettings(): _*)
}
