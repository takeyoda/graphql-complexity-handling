package io.github.takeyoda.querycomplexity.config;

import graphql.execution.AbortExecutionException;
import graphql.execution.instrumentation.Instrumentation;
import graphql.scalars.ExtendedScalars;
import io.github.takeyoda.querycomplexity.context.CallerContextLifter;
import io.github.takeyoda.querycomplexity.graphql.DefaultFieldComplexityCalculator;
import io.github.takeyoda.querycomplexity.graphql.QueryComplexityMonitoringInstrumentation;
import io.github.takeyoda.querycomplexity.graphql.QueryComplexityMonitoringInstrumentation.OnQueryComplexityCalculatedListener;
import io.github.takeyoda.querycomplexity.model.Client;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import reactor.core.Fuseable;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@Slf4j
public class Config {

  private static final String REACTOR_KEY = Config.class.getName();

  @PostConstruct
  public void init() {
    Hooks.onEachOperator(
        REACTOR_KEY,
        Operators.liftPublisher(
            (publisher, coreSubscriber) -> {
              if (publisher instanceof Fuseable.ScalarCallable) {
                return coreSubscriber;
              }
              return new CallerContextLifter<>(coreSubscriber);
            }));
  }

  @PreDestroy
  public void destroy() {
    Hooks.resetOnEachOperator(REACTOR_KEY);
  }

  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return builder -> {
      builder.scalar(ExtendedScalars.PositiveInt);
      builder.scalar(ExtendedScalars.GraphQLLong);
    };
  }

  @Bean
  public Instrumentation queryComplexityMonitoringInstrumentation() {
    return new QueryComplexityMonitoringInstrumentation(
        new DefaultFieldComplexityCalculator(), queryComplexityCalculatedListener());
  }

  private OnQueryComplexityCalculatedListener queryComplexityCalculatedListener() {
    return event -> {
      final Client client = event.context().get(Client.class);
      log.info(
          "client={}, operationType={}, depth={}, complexity={}",
          client.apiKey(),
          event.operationType(),
          event.depth(),
          event.complexity());
      if (event.depth() > client.maxDepth()) {
        throw new AbortExecutionException(
            "Maximum query depth exceeded " + event.depth() + " > " + client.maxDepth());
      }
      if (event.complexity() > client.maxComplexity()) {
        throw new AbortExecutionException(
            "Maximum query complexity exceeded "
                + event.complexity()
                + " > "
                + client.maxComplexity());
      }
    };
  }
}
