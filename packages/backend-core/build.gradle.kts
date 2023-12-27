import kotlin.math.truncate

plugins {
  id("java-library")
}

dependencies {
  api("org.springframework.boot:spring-boot-starter")
  annotationProcessor("org.hibernate:hibernate-jpamodelgen:${dependencyManagement.managedVersions["org.hibernate.orm:hibernate-core"]}")
  api("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  api("org.springframework.boot:spring-boot-starter-validation")
  api("org.springframework.boot:spring-boot-starter-json")

  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok")
}
