inThisBuild(
  List(
    organization := "com.kubukoz",
    homepage := Some(url("https://github.com/kubukoz/http4s-oauth2")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    // Paying respects to upstream devs
    developers := List(
      Developer(
        "majk-p",
        "Michał Pawlik",
        "michal.pawlik@ocado.com",
        url("https://michalp.net")
      ),
      Developer(
        "kubukoz",
        "Jakub Kozłowski",
        "kubukoz@gmail.com",
        url("https://github.com/kubukoz")
      ),
      Developer(
        "tplaskowski",
        "Tomek Pląskowski",
        "t.plaskowski@ocado.com",
        url("https://github.com/tplaskowski")
      )
    ),
    versionScheme := Some("early-semver")
  )
)

def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(x.cross(CrossVersion.full))

val Scala212 = "2.12.13"
val Scala213 = "2.13.5"

val GraalVM11 = "graalvm-ce-java11@21.0.0"

ThisBuild / scalaVersion := Scala213
ThisBuild / crossScalaVersions := Seq(Scala212, Scala213)
ThisBuild / githubWorkflowJavaVersions := Seq(GraalVM11)
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("test", "mimaReportBinaryIssues"))
)

//sbt-ci-release settings
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  // the default is master - https://github.com/djspiewak/sbt-github-actions/issues/41
  RefPredicate.Equals(Ref.Branch("main")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)
ThisBuild / githubWorkflowPublishPreamble := Seq(WorkflowStep.Use(UseRef.Public("olafurpg", "setup-gpg", "v3")))
ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))
ThisBuild / githubWorkflowEnv ++= List("PGP_PASSPHRASE", "PGP_SECRET", "SONATYPE_PASSWORD", "SONATYPE_USERNAME").map { envKey =>
  envKey -> s"$${{ secrets.$envKey }}"
}.toMap

val mimaSettings = mimaPreviousArtifacts := Set(
  // organization.value %% name.value % "0.3.0" // TODO Define a process for resetting this after release
)

lazy val oauth2 = project.settings(
  name := "http4s-oauth2",
  libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-circe" % "1.0.0-M21",
    "org.http4s" %% "http4s-client" % "1.0.0-M21",
    "io.circe" %% "circe-literal" % "0.14.0-M5" % Test,
    "org.scalatest" %% "scalatest" % "3.2.6" % Test,
    compilerPlugin("org.typelevel" % "kind-projector" % "0.11.3" cross CrossVersion.full),
    compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  ),
  mimaSettings,
  scalacOptions -= "-Xfatal-warnings"
)

val root = project
  .in(file("."))
  .settings(
    publish / skip := true,
    mimaPreviousArtifacts := Set.empty
  )
  // after adding a module remember to regenerate ci.yml using `sbt githubWorkflowGenerate`
  .aggregate(oauth2)
