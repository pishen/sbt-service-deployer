lazy val root = (project in file("."))
  .settings(
    sbtPlugin := true,
    name := "sbt-api-deployer",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.10.6",
    addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3"),
    libraryDependencies ++= Seq(
      "org.slf4s" %% "slf4s-api" % "1.7.12",
      "com.vast.sbt" %% "sbt-slf4j" % "0.2.1",
      "com.amazonaws" % "aws-java-sdk-ec2" % "1.10.76",
      "com.amazonaws" % "aws-java-sdk-elasticloadbalancing" % "1.10.76"
    ),
    organization := "net.pishen",
    publishMavenStyle := false,
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    homepage := Some(url("https://github.com/pishen/sbt-api-deployer")),
    pomExtra := (
      <scm>
        <url>https://github.com/pishen/sbt-api-deployer.git</url>
        <connection>scm:git:git@github.com:pishen/sbt-api-deployer.git</connection>
      </scm>
    )
  )
