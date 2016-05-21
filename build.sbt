lazy val root = (project in file("."))
  .settings(
    sbtPlugin := true,
    name := "sbt-service-deployer",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.10.6",
    libraryDependencies ++= Seq(
      "org.slf4s" %% "slf4s-api" % "1.7.12",
      "com.vast.sbt" %% "sbt-slf4j" % "0.2.1"
    ),
    organization := "net.pishen",
    publishMavenStyle := false,
    licenses += ("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    homepage := Some(url("https://github.com/pishen/sbt-service-deployer")),
    pomExtra := (
      <scm>
        <url>https://github.com/pishen/sbt-service-deployer.git</url>
        <connection>scm:git:git@github.com:pishen/sbt-service-deployer.git</connection>
      </scm>
    )
  )
