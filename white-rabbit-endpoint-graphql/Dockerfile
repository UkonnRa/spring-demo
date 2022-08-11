## Cannot use `jdeps --print-module-deps` directly due to
## https://stackoverflow.com/questions/69943899/jdeps-cant-print-module-deps-due-to-a-multireleaseexception
## The latest jdk-18 fixed the issue
#FROM openjdk:19-alpine AS jdepsExtractor
#
#COPY white-rabbit-endpoint-graphql.jar app.jar
#
#RUN jar xvf app.jar
#
#RUN jdeps -cp 'BOOT-INF/lib/*' -recursive --multi-release 17 --ignore-missing-deps -q --print-module-deps app.jar > jdeps.info

# openjdk:17-alpine is very old
FROM eclipse-temurin:18-alpine AS builder

COPY build/libs/white-rabbit-endpoint-graphql.jar app.jar

COPY jdeps.info jdeps.info

## Due to the missing SSL related dependency, no idea why it is not in the jdeps
## https://stackoverflow.com/questions/55439599/sslhandshakeexception-with-jlink-created-runtime/55517159#comment97595874_55439599
## Article Related: https://mbien.dev/blog/entry/custom-java-runtimes-with-jlink
# RUN echo "$(cat jdeps.info),jdk.crypto.ec" > jdeps.info

RUN jlink --verbose --compress 2 --strip-java-debug-attributes --no-header-files --no-man-pages --output jre \
    --add-modules $(cat jdeps.info)

FROM alpine:latest

ARG USER=whiterabbit

ARG GROUP=wonderland

ARG UID=1000

ARG GID=1000

ARG HOME=/home/$USER

RUN addgroup -g $GID -S $GROUP

RUN adduser -h $HOME -g "" -G $GROUP -D $USER

USER $USER

WORKDIR $HOME

COPY --from=builder --chown=$USER:$GROUP jre jre

COPY --from=builder --chown=$USER:$GROUP app.jar app.jar

ENTRYPOINT [ "jre/bin/java", "-jar", "app.jar" ]
