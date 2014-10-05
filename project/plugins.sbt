// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository - explicit here for Heroku
resolvers := Seq("Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.3")

// Jasmine plugin for running JavaScript tests
addSbtPlugin("com.joescii" % "sbt-jasmine-plugin" % "1.3.0")

// Less plugin for scoping page-specific styles in a single-page application
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.2")