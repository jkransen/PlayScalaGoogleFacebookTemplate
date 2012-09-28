import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "Workout.temp"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "org.apache.jena" % "jena-arq" % "2.9.3",
    "com.restfb" % "restfb" % "1.6.11")

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings( 
    // Add your own project settings here      
  )

}
