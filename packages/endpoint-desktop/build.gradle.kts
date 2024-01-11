plugins {
  id("org.springframework.boot")
  id("org.graalvm.buildtools.native")
}

dependencies {
  implementation(platform("org.springframework.shell:spring-shell-dependencies:3.2.0"))

  implementation(project(":backend-core"))
  implementation("org.hibernate.orm:hibernate-community-dialects")
  implementation("org.springframework.shell:spring-shell-starter")

  runtimeOnly("com.h2database:h2")
}
