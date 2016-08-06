name := "marissa-core"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "ch.qos.logback" % "logback-core" % "1.1.3",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "commons-io" % "commons-io" % "2.4",
  "rocks.xmpp" % "xmpp-core-client" % "0.5.1",
  "rocks.xmpp" % "xmpp-extensions-client" % "0.5.1",
  "rocks.xmpp" % "xmpp-debug" % "0.5.0"
)