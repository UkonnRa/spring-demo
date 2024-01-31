plugins {
  id("org.springframework.boot")
  id("org.graalvm.buildtools.native")
}

dependencies {
  implementation(project(":backend-hateoas"))
  implementation("org.hibernate.orm:hibernate-community-dialects")

  runtimeOnly("com.h2database:h2")
}

springBoot {
  buildInfo()
}

graalvmNative {
  binaries.all {
    // Fix unknown error for: `Error: Classes that should be initialized at run time got initialized during image building`
    buildArgs.add("--initialize-at-build-time=org.apache.catalina.connector.RequestFacade,org.apache.catalina.connector.ResponseFacade")
  }
}
