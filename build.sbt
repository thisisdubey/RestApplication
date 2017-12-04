name := "RestApplication"
 
version := "1.0" 
      
lazy val `restapplication` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
scalaVersion := "2.11.11"

libraryDependencies ++= Seq( jdbc , cache , filters , specs2 % Test,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.7.play24",
  "io.swagger" %% "swagger-play2" % "1.5.1")

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      