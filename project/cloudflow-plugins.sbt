resolvers += Resolver.url("lightbend-oss", url("https://lightbend.bintray.com/cloudflow"))(Resolver.ivyStylePatterns)
resolvers += "Akka Snapshots" at "https://repo.akka.io/snapshots/"

addSbtPlugin("com.lightbend.cloudflow" % "sbt-cloudflow" % "2.0.5")