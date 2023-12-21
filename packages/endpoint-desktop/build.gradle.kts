plugins {
  id("org.springframework.boot")
  id("org.graalvm.buildtools.native")
}

dependencies {
  implementation(project(":backend-core"))
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  runtimeOnly("com.h2database:h2")
}
