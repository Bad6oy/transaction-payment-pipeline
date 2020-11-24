import sbt.Keys._
import sbt._

val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10"
val log4j2Version    = "2.14.0"
//val log4jApi         = "org.apache.logging.log4j" % "log4j-api" % log4j2Version
//val log4jSlf4j       = "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4j2Version
//val log4jCore        = "org.apache.logging.log4j" % "log4j-core" % log4j2Version
//val disruptor        = "com.lmax" % "disruptor" % "3.4.2"
val scalaTest        = "org.scalatest" %% "scalatest" % "3.0.8" % "test"
val log4j            = "ch.qos.logback" % "logback-classic" % "1.2.3"
val alpakkaFile      = "com.lightbend.akka"     %% "akka-stream-alpakka-file"  % "2.0.2"
val akkaSlf4j        = "com.typesafe.akka" %% "akka-slf4j" % "2.6.6"
val protobuf         = "com.typesafe.akka" %% "akka-protobuf" % "2.6.6"
val akkaStream       = "com.typesafe.akka" %% "akka-stream" % "2.6.6"

lazy val root = {
  Project(id = "translation-payment-pipeline", base = file("."))
    .settings(
      name := "translation-payment-pipeline",
      skip in publish := true,
      version := "0.2"
    )
    .withId("translation-payment-pipeline")
    .settings(commonSettings)
    .aggregate(
      cloudflowPipeline,
      paymentChecker,
      paymentFileIngress,
      paymentParticipantInitializer,
      paymentProcessing,
      paymentLogger,
      datamodel,
      messageUtility
    )
}

lazy val cloudflowPipeline = appModule("cloudflow-pipeline")
  .enablePlugins(CloudflowApplicationPlugin)
  .settings(commonSettings)
  .settings(
    name := "cloudflow-pipeline",
    libraryDependencies ++= Seq(
    akkaSlf4j,
    log4j,
    protobuf
    )
  )

lazy val paymentChecker = appModule("payment-checker-flink")
  .enablePlugins(CloudflowFlinkPlugin)
  .settings(commonSettings)
  .settings(
    name := "payment-checker-flink",
    libraryDependencies ++= Seq(
      //log4jApi,
      //log4jSlf4j,
      //log4jCore,
      //disruptor,
      log4j,
      scalaTest
    )
  )
  .dependsOn(datamodel, messageUtility)

lazy val paymentFileIngress = appModule("payment-file-ingress")
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(commonSettings)
  .settings(
    name := "payment-file-ingress",
    libraryDependencies ++= Seq(
      alpakkaFile,
      //log4jApi,
      //log4jSlf4j,
      //log4jCore,
      //disruptor,
      log4j,
      scalaTest
    )
  )
  .dependsOn(datamodel)

lazy val paymentLogger = appModule("payment-logger")
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(commonSettings)
  .settings(
    name := "payment-logger",
    libraryDependencies ++= Seq(
      //log4jApi,
      //log4jSlf4j,
      //log4jCore,
      //disruptor,
      log4j,
      scalaTest
    )
  )
  .dependsOn(datamodel)

lazy val paymentParticipantInitializer = appModule("payment-participant-initializer")
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(commonSettings)
  .settings(
    name := "payment-participant-initializer",
    libraryDependencies ++= Seq(
      akkaHttpSprayJson,
      //log4jApi,
      //log4jSlf4j,
      //log4jCore,
      //disruptor,
      log4j,
      scalaTest
    )
  )
  .dependsOn(datamodel)

lazy val paymentProcessing = appModule("payment-processing-flink")
  .enablePlugins(CloudflowFlinkPlugin)
  .settings(commonSettings)
  .settings(
    name := "payment-processing-flink",
    libraryDependencies ++= Seq(
      //log4jApi,
      //log4jSlf4j,
      //log4jCore,
      //disruptor,
      log4j,
      scalaTest
    )
  )
  .dependsOn(datamodel, messageUtility)

lazy val datamodel = appModule("data-model")
  .enablePlugins(CloudflowLibraryPlugin)
  .settings(
    commonSettings,
    (sourceGenerators in Compile) += (avroScalaGenerateSpecific in Test).taskValue
  )


lazy val messageUtility = appModule("message-utility")
  .settings(commonSettings)
  .dependsOn(datamodel)

lazy val commonSettings = Seq(
  scalaVersion := "2.12.12",
  scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8", // Specify character encoding used by source files.
    "-Xlog-reflective-calls",
    "-Xlint",
    "-explaintypes", // Explain type errors in more detail.
    "-feature",      // Emit warning and location for usages of features that should be imported explicitly.
    "-language:_",
    "-unchecked",                       // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit",                      // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings",                 // Fail the compilation if there are any warnings.
    "-Xfuture",                         // Turn on future language features.
    "-Xlint:adapted-args",              // Warn if an argument list is modified to match the receiver.
    "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
    "-Xlint:constant",                  // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select",        // Selecting member of DelayedInit.
    "-Xlint:doc-detached",              // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible",              // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any",                 // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator",      // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override",          // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit",              // Warn when nullary methods return Unit.
    "-Xlint:option-implicit",           // Option.apply used implicit view.
    "-Xlint:package-object-classes",    // Class or object defined in package object.
    "-Xlint:poly-implicit-overload",    // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow",            // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align",               // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow",     // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match",             // Pattern match may not be typesafe.
    "-Yno-adapted-args",                // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification",            // Enable partial unification in type constructor inference
    "-Ywarn-dead-code",                 // Warn when dead code is identified.
    "-Ywarn-extra-implicit",            // Warn when more than one implicit parameter section is defined.
    "-Ywarn-inaccessible",              // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any",                 // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override",          // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit",              // Warn when nullary methods return Unit.
    "-Ywarn-unused",
    "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports",   // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals",    // Warn if a local definition is unused.
    "-Ywarn-unused:params",    // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars",   // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates"   // Warn if a private member is unused.
  )
)

def appModule(moduleId: String): Project = {
  Project(id = moduleId, base = file(moduleId))
    .settings(
      name := moduleId
    )
    .withId(moduleId)
    .settings(commonSettings)
}