name := "reactive-todolist"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.10.0",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2"
)     

play.Project.playScalaSettings

unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base =>  Seq(base / "it"))

lazy val root =
  Project("root", file("."))
    .configs( IntegrationTest )
    .settings( Defaults.itSettings : _*)
    .settings(libraryDependencies += "com.typesafe.play" %% "play-test" % "2.2.2" % "it")