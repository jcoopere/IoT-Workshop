name := "iiot-demo"

version := "1.0"

scalaVersion := "2.11.8"

val sparkVersion = "2.2.0-cdh6.0.1"
val kuduVersion = "1.6.0-cdh6.0.1"
val kafkaVersion = "2.2.0-cdh6.0.1"

libraryDependencies ++= Seq(
	"org.scalatest" %% "scalatest" % "2.2.1" % "test",
//	"com.thesamet.scalapb" %% "sparksql-scalapb" % "0.7.0",
	"org.apache.spark" %% "spark-core" % sparkVersion % "provided",
	"org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
	"org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
	"org.apache.spark" %% "spark-streaming-kafka-0-10-assembly" % kafkaVersion % "provided",
	"org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
	"org.apache.kudu" %% "kudu-spark2" % kuduVersion,
	"org.apache.kudu" % "kudu-client" % kuduVersion,
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

PB.protocVersion := "-v261"

resolvers ++= Seq(
	"Cloudera Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
//	"scala-tools" at "https://oss.sonatype.org/content/groups/scala-tools",
//	"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
//	"Second Typesafe repo" at "http://repo.typesafe.com/typesafe/maven-releases/",
//	"Mesosphere Public Repository" at "http://downloads.mesosphere.io/maven",
//	"Bintray sbt plugin releases" at "http://dl.bintray.com/sbt/sbt-plugin-releases/",
	Resolver.sonatypeRepo("public")
)

assemblyMergeStrategy in assembly := {
    case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
    case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
    case PathList("org", "apache", xs @ _*) => MergeStrategy.last
    case PathList("com", "google", xs @ _*) => MergeStrategy.last
    case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
    case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
    case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
    case PathList(ps @_*) if ps.last endsWith ".proto" => MergeStrategy.last
    case "about.html" => MergeStrategy.rename
    case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
    case "META-INF/mailcap" => MergeStrategy.last
    case "META-INF/mimetypes.default" => MergeStrategy.last
    case "plugin.properties" => MergeStrategy.last
    case "log4j.properties" => MergeStrategy.last
    case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
}

assemblyShadeRules in assembly := Seq(
	ShadeRule.rename("io.netty.handler.**" -> "shadeio.io.netty.handler.@1").inAll,
	ShadeRule.rename("io.netty.channel.**" -> "shadeioi.io.netty.channel.@1").inAll,
	ShadeRule.rename("io.netty.util.**" -> "shadeio.io.netty.util.@1").inAll,
	ShadeRule.rename("io.netty.bootstrap.**" -> "shadeio.io.netty.bootstrap.@1").inAll,
	ShadeRule.rename("com.google.common.**" -> "shade.com.google.common.@1").inAll,
	ShadeRule.rename("com.google.protobuf.**" -> "shadeproto.@1").inAll
)
