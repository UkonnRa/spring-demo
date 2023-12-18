rootProject.name = "spring-electron-test"

file("packages").walk().maxDepth(1).filter {
  it.list()?.contains("build.gradle.kts") ?: false
}.forEach {
  include(it.name)
  project(":${it.name}").projectDir = it
}
