plugins {
  id("org.springframework.boot")
  id("org.graalvm.buildtools.native")
  id("com.google.protobuf") version "0.9.4"
}

object Versions {
  const val JAVAX_ANNOTATION_VERSION = "1.3.2"
  const val GRPC_VERSION = "1.61.0"
  const val PROTOC_VERSION = "3.25.1"
}

dependencies {
  implementation(platform("io.grpc:grpc-bom:${Versions.GRPC_VERSION}"))
  implementation(platform("com.google.protobuf:protobuf-bom:${Versions.PROTOC_VERSION}"))

  implementation(project(":backend-core"))
  implementation("org.hibernate.orm:hibernate-community-dialects")
  implementation("io.grpc:grpc-protobuf")
  implementation("io.grpc:grpc-stub")
  implementation("io.grpc:grpc-services")

  compileOnly("javax.annotation:javax.annotation-api:${Versions.JAVAX_ANNOTATION_VERSION}")

  runtimeOnly("io.grpc:grpc-netty")
  runtimeOnly("com.h2database:h2")
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:${Versions.PROTOC_VERSION}"
  }
  plugins {
    create("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:${Versions.GRPC_VERSION}"
    }
  }
  generateProtoTasks {
    all().forEach {
      it.plugins {
        create("grpc")
      }
    }
  }
}
