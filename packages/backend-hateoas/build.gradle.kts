plugins {
  id("java-library")
}

object Versions {
  const val SPRINGDOC = "2.4.0"
}

dependencies {
  api(project(":backend-core"))

  implementation("org.springframework.boot:spring-boot-starter-hateoas")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${Versions.SPRINGDOC}")
}
