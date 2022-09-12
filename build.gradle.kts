plugins {
  id("java")
  id("idea")
  id("checkstyle")
  id("jacoco")
  id("jacoco-report-aggregation")

  id("com.github.spotbugs") version "5.0.10"
  id("com.diffplug.spotless") version "6.10.0"
  id("com.github.ben-manes.versions") version "0.42.0"
  id("io.freefair.lombok") version "6.5.1"

  id("io.spring.dependency-management") version "1.0.13.RELEASE"
}

group = "com.ukonnra.wonderland"
version = "0.0.1-SNAPSHOT"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

allprojects {
  repositories {
    mavenCentral()
    maven("https://repo.spring.io/release")
  }
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "idea")
  apply(plugin = "checkstyle")
  apply(plugin = "jacoco")
  apply(plugin = "jacoco-report-aggregation")

  apply(plugin = "com.github.spotbugs")
  apply(plugin = "com.diffplug.spotless")
  apply(plugin = "io.freefair.lombok")

  apply(plugin = "io.spring.dependency-management")

  dependencyManagement {
    imports {
      mavenBom("org.testcontainers:testcontainers-bom:1.17.3")
    }
  }

  dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
  }

  checkstyle {
    toolVersion = "10.3.1"
  }

  spotbugs {
    toolVersion.set("4.7.1")
    excludeFilter.set(file("$rootDir/config/spotbugs/exclude.xml"))
  }

  tasks.spotbugsMain {
    reports.create("xml") {
      required.set(true)
    }

    reports.create("html") {
      required.set(true)
    }
  }

  spotless {
    java {
      importOrder()
      removeUnusedImports()
      googleJavaFormat()
    }
  }

  configurations {
    compileOnly {
      extendsFrom(configurations.annotationProcessor.get())
    }

    implementation {
      exclude(module = "spring-boot-starter-tomcat")
    }
  }

  tasks.clean {
    delete("out", "logs")
  }

  tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    useJUnitPlatform()
    testLogging.apply {
      events("passed", "skipped", "failed")
      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
      showStackTraces = true
    }
  }

  tasks.check {
    dependsOn(tasks.testCodeCoverageReport)
  }
}
