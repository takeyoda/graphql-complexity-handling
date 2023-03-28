package io.github.takeyoda.querycomplexity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder.SpringWebBlockHoundIntegration;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    BlockHound.builder().with(new SpringWebBlockHoundIntegration()).install();
    SpringApplication.run(Application.class, args);
  }
}
