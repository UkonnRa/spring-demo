package com.ukonnra.wonderland.springelectrontest.desktop;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.protobuf.services.ProtoReflectionService;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GrpcServer implements InitializingBean, DisposableBean {
  private final Server server;

  public GrpcServer(@Value("${server.port:8080}") int port, GreeterImpl greeter) {
    this.server =
        Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
            .addService(ProtoReflectionService.newInstance())
            .addService(greeter)
            .build();
  }

  @Override
  public void afterPropertiesSet() throws IOException {
    this.server.start();
    final var awaitThread =
        new Thread(
            () -> {
              try {
                log.info("Grpc Server is listening {}", this.server.getPort());
                this.server.awaitTermination();
              } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
    awaitThread.setDaemon(false);
    awaitThread.start();
  }

  @Override
  public void destroy() throws InterruptedException {
    log.info("Ready to destroy Grpc Server");
    this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
  }
}
