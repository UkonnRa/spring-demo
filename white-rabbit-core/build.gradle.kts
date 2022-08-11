plugins {
  id("java-library")

  id("org.springframework.boot") version "2.7.2" apply false
}

dependencyManagement {
  imports {
    mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
  }
}

dependencies {
  api("org.springframework.boot:spring-boot-starter")
  api("org.springframework.boot:spring-boot-starter-actuator")
  api("org.springframework.boot:spring-boot-starter-data-jpa")
  api("org.springframework.boot:spring-boot-starter-validation")
  api("org.springframework.boot:spring-boot-starter-json")
  api("org.springframework.boot:spring-boot-starter-security")

  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok")

  api("com.querydsl:querydsl-jpa:5.0.0")
  annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jpa")
  annotationProcessor("org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1.0.2.Final")

  api("io.projectreactor:reactor-core")

  testImplementation(project(":white-rabbit-test-suite"))
  testImplementation("org.springframework.security:spring-security-test")
}
