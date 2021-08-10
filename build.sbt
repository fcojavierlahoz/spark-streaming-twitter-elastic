import AssemblyKeys._ // put this at the top of the file

name := "Twitter-ES"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming" % "2.2.1",
  "org.apache.bahir" %% "spark-streaming-twitter" % "2.0.0",
  "org.apache.hadoop" % "hadoop-client" % "2.7.1",
  "org.apache.hadoop" % "hadoop-hdfs" % "2.7.1",
  "org.elasticsearch" % "elasticsearch-spark-20_2.11" % "6.1.0",
  "org.json" % "json" % "20090211",
  "org.glassfish.hk2" % "hk2-utils" % "2.2.0-b27",
  "org.glassfish.hk2" % "hk2-locator" % "2.2.0-b27",
  "javax.validation" % "validation-api" % "1.1.0.Final"
).map(_.excludeAll(ExclusionRule("org.glassfish.hk2"),ExclusionRule("javax.validation"))) 

resolvers ++= Seq(
  "clojars" at "https://clojars.org/repo",
  "conjars" at "http://conjars.org/repo"
)


resourceDirectory in Compile := baseDirectory.value / "resources"

assemblySettings

mergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case "log4j.properties"                                  => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf"                                    => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}
