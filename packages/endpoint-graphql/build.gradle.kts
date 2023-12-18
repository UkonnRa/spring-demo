plugins {
  id("org.springframework.boot")
}

dependencies {
  implementation(project(":backend-core"))
  implementation("org.springframework.boot:spring-boot-starter-graphql")
}
