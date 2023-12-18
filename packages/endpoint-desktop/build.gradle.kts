plugins {
  id("org.springframework.boot")
  id("org.graalvm.buildtools.native")
}

dependencies {
  implementation(project(":backend-core"))
  implementation("org.springframework.boot:spring-boot-starter-graphql")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  runtimeOnly("org.xerial:sqlite-jdbc")
}

graalvmNative {
  binaries.all {
    buildArgs.addAll("-H:+UnlockExperimentalVMOptions", "-H:DashboardDump=endpoint-desktop", "-H:+DashboardAll")
  }
}
