#!/usr/bin/env groovy

pipeline {
  agent {
    kubernetes {
      defaultContainer 'java'
      yaml '''
        apiVersion: v1
        kind: Pod
        spec:
          containers:
          - name: java
            image: eclipse-temurin:17-alpine
            command:
            - cat
            tty: true
            resources:
              limits:
                memory: "1500Mi"
                cpu: "1"
              requests:
                memory: "700Mi"
                cpu: "500m"
        '''
    }
  }
  stages {
    stage('Check & Build') {
      steps {
        sh 'java -version'
        sh './gradlew check build'
      }
    }

    stage('Docker') {
      steps {
        sh 'java -version'
        sh 'ls -la white-rabbit-endpoint-graphql'
      }
    }
  }
}
