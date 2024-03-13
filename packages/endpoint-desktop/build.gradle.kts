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
