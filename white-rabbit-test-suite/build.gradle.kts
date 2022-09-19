plugins {
  id("org.springframework.boot") version "2.7.3"
}

dependencies {
  implementation(project(":white-rabbit-core"))
  implementation("org.springframework.boot:spring-boot-starter-test")
  implementation("net.datafaker:datafaker:1.6.0")
  runtimeOnly("com.h2database:h2")
}
