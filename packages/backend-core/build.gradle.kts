plugins {
  id("java-library")
}

dependencies {
  api("org.springframework.boot:spring-boot-starter")
  api("org.springframework.boot:spring-boot-starter-data-jpa")
  api("org.springframework.boot:spring-boot-starter-validation")
  api("org.springframework.boot:spring-boot-starter-json")
  implementation("org.springframework.boot:spring-boot-starter-actuator")

  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok")
  annotationProcessor("org.hibernate.orm:hibernate-jpamodelgen")
}
