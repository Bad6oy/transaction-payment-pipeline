import sbt.Keys.libraryDependencies

lazy val pipeline =  (project in file("."))
  .enablePlugins(CloudflowApplicationPlugin, CloudflowAkkaPlugin, CloudflowFlinkPlugin)
  .settings(
    scalaVersion := "2.12.12",
    runLocalConfigFile := Some("src/main/resources/local.conf"),
    //runLocalLog4jConfigFile := Some("src/main/resources/log4j.xml"),
    name := "translations-payment-pipeline",
    libraryDependencies ++= Seq(
      "com.lightbend.akka"     %% "akka-stream-alpakka-file"  % "2.0.2",
      "com.typesafe.akka"      %% "akka-stream"               % "2.5.31",
      "com.typesafe.akka"      %% "akka-http-spray-json"      % "10.1.12",
      "ch.qos.logback"         %  "logback-classic"           % "1.2.3",
      "com.typesafe.akka"      %% "akka-http-testkit"         % "10.1.12" % "test",
      "org.scalatest"          %% "scalatest"                 % "3.0.8"  % "test"
    )
  )