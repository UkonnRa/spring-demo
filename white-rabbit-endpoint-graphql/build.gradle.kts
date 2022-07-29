plugins {
  id("org.springframework.boot") version "2.7.2"
}

dependencies {
  implementation(project(":white-rabbit-core"))

  runtimeOnly("com.h2database:h2")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-graphql")
  implementation("com.graphql-java:graphql-java-extended-scalars:18.1")
  implementation("org.springframework.boot:spring-boot-starter-websocket")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("org.springframework.graphql:spring-graphql-test")
}
