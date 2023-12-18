import com.diffplug.gradle.spotless.SpotlessTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
  id("java")
  id("idea")
  id("checkstyle")
  id("jacoco")

  id("com.github.spotbugs") version "6.0.4"
  id("com.diffplug.spotless") version "6.23.3"
  id("com.github.ben-manes.versions") version "0.50.0"
  id("io.freefair.lombok") version "8.4"
  id("org.sonarqube") version "4.4.1.3373"

  id("org.springframework.boot") version "3.2.0" apply false
  id("io.spring.dependency-management") version "1.1.4"
  id("org.graalvm.buildtools.native") version "0.9.28" apply false
}

group = "com.ukonnra.wonderland"
version = "0.1.0"

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

allprojects {
  repositories {
    mavenCentral()
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
  apply(plugin = "org.sonarqube")

  apply(plugin = "io.spring.dependency-management")

  dependencyManagement {
    imports {
      mavenBom(SpringBootPlugin.BOM_COORDINATES)
    }
  }

  dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
  }

  checkstyle {
    toolVersion = "10.12.5"
  }

  tasks.withType<Checkstyle> {
    exclude {
      val path = it.file.absolutePath
      path.contains("aot") || path.contains("generated")
    }
  }

  spotbugs {
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
      targetExclude("**/generated/**")
      importOrder()
      removeUnusedImports()
      googleJavaFormat()
    }
  }

  sonarqube {
    properties {
      property(
        "sonar.coverage.jacoco.xmlReportPaths",
        "${projectDir.parentFile.path}/build/reports/jacoco/codeCoverageReport/codeCoverageReport.xml"
      )
    }
  }

  configurations {
    compileOnly {
      extendsFrom(configurations.annotationProcessor.get())
    }
  }

  tasks.clean {
    delete("out", "logs")
  }

  tasks.test {
    useJUnitPlatform()
    testLogging.apply {
      exceptionFormat = TestExceptionFormat.FULL
      showStackTraces = true
    }
  }
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
}

// Aggregate test coverages from subprojects to the root.
// https://github.com/SonarSource/sonar-scanning-examples/blob/master/sonarqube-scanner-gradle/gradle-multimodule-coverage/build.gradle
tasks.register<JacocoReport>("codeCoverageReport") {
  subprojects {
    plugins.withType<JacocoPlugin>().configureEach {
      this@subprojects.tasks.matching { it.extensions.findByType<JacocoTaskExtension>() != null }.configureEach {
        if (extensions.getByType<JacocoTaskExtension>().isEnabled) {
          sourceSets(this@subprojects.sourceSets.main.get())
          executionData(this)
        } else {
          logger.warn("Jacoco extension is disabled for test task \'${name}\' in project \'${this@subprojects.name}\'. this test task will be excluded from jacoco report.")
        }
      }

      this@subprojects.tasks.matching { it.extensions.findByType<JacocoTaskExtension>() != null }.forEach {
        this@register.dependsOn(it)
      }
    }
  }

  reports {
    xml.required.set(true)
    html.required.set(true)
  }
}
