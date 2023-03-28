package io.github.takeyoda.querycomplexity.graphql;

import graphql.ExecutionResult;
import graphql.GraphQLContext;
import graphql.analysis.FieldComplexityCalculator;
import graphql.analysis.FieldComplexityEnvironment;
import graphql.analysis.QueryTraverser;
import graphql.analysis.QueryVisitorFieldEnvironment;
import graphql.analysis.QueryVisitorStub;
import graphql.execution.ExecutionContext;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;
import graphql.language.OperationDefinition;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

/**
 * @see graphql.analysis.MaxQueryComplexityInstrumentation
 * @see graphql.analysis.MaxQueryDepthInstrumentation
 */
@RequiredArgsConstructor
public class QueryComplexityMonitoringInstrumentation extends SimpleInstrumentation {
  public interface OnQueryComplexityCalculatedListener {
    void onQueryComplexityCalculated(QueryComplexityCalculatedEvent event);
  }

  private final FieldComplexityCalculator fieldComplexityCalculator;
  private final OnQueryComplexityCalculatedListener listener;

  @Override
  public InstrumentationContext<ExecutionResult> beginExecuteOperation(
      InstrumentationExecuteOperationParameters parameters) {
    QueryTraverser queryTraverser = newQueryTraverser(parameters.getExecutionContext());
    final int depth =
        queryTraverser.reducePreOrder(
            (env, acc) -> Math.max(getPathLength(env.getParentEnvironment()), acc), 0);

    Map<QueryVisitorFieldEnvironment, Integer> valuesByParent = new LinkedHashMap<>();
    queryTraverser.visitPostOrder(
        new QueryVisitorStub() {
          @Override
          public void visitField(QueryVisitorFieldEnvironment env) {
            int childsComplexity = valuesByParent.getOrDefault(env, 0);
            int value = calculateComplexity(env, childsComplexity);

            valuesByParent.compute(
                env.getParentEnvironment(),
                (key, oldValue) -> Optional.ofNullable(oldValue).orElse(0) + value);
          }
        });
    final OperationDefinition.Operation operationType =
        parameters.getExecutionContext().getOperationDefinition().getOperation();
    final int totalComplexity = valuesByParent.getOrDefault(null, 0);

    GraphQLContext context = parameters.getExecutionContext().getGraphQLContext();
    listener.onQueryComplexityCalculated(
        QueryComplexityCalculatedEvent.builder()
            .operationType(operationType)
            .depth(depth)
            .complexity(totalComplexity)
            .context(context)
            .build());

    return SimpleInstrumentationContext.noOp();
  }

  private QueryTraverser newQueryTraverser(ExecutionContext executionContext) {
    return QueryTraverser.newQueryTraverser()
        .schema(executionContext.getGraphQLSchema())
        .document(executionContext.getDocument())
        .operationName(executionContext.getExecutionInput().getOperationName())
        .coercedVariables(executionContext.getCoercedVariables())
        .build();
  }

  private int getPathLength(QueryVisitorFieldEnvironment path) {
    int length = 1;
    while (path != null) {
      path = path.getParentEnvironment();
      length++;
    }
    return length;
  }

  private int calculateComplexity(
      QueryVisitorFieldEnvironment queryVisitorFieldEnvironment, int childsComplexity) {
    if (queryVisitorFieldEnvironment.isTypeNameIntrospectionField()) {
      return 0;
    }
    FieldComplexityEnvironment fieldComplexityEnvironment =
        convertEnv(queryVisitorFieldEnvironment);
    return fieldComplexityCalculator.calculate(fieldComplexityEnvironment, childsComplexity);
  }

  private FieldComplexityEnvironment convertEnv(
      QueryVisitorFieldEnvironment queryVisitorFieldEnvironment) {
    FieldComplexityEnvironment parentEnv = null;
    if (queryVisitorFieldEnvironment.getParentEnvironment() != null) {
      parentEnv = convertEnv(queryVisitorFieldEnvironment.getParentEnvironment());
    }
    return new FieldComplexityEnvironment(
        queryVisitorFieldEnvironment.getField(),
        queryVisitorFieldEnvironment.getFieldDefinition(),
        queryVisitorFieldEnvironment.getFieldsContainer(),
        queryVisitorFieldEnvironment.getArguments(),
        parentEnv);
  }

  @Builder
  public record ComplexityCalculatedEvent() {}
}
