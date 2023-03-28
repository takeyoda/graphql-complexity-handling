package io.github.takeyoda.querycomplexity.graphql;

import graphql.analysis.FieldComplexityCalculator;
import graphql.analysis.FieldComplexityEnvironment;
import graphql.schema.GraphQLImplementingType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedOutputType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLOutputType;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

/**
 * クエリの複雑度計算器。<br>
 * 以下のルールで複雑度を計算する。
 *
 * <ul>
 *   <li>アクセスするフィールド 1 つあたり <strong>+1</strong>
 *   <li>List フィールドは <strong>{子要素の合計複雑度} * 10</strong>
 *   <li>ただし、<a href="https://relay.dev/graphql/connections.htm#sec-Connection-Types>Connection</a>
 *       の取得件数 (N) が指定されている場合 <strong>{子要素の合計複雑度} * N</strong>
 * </ul>
 *
 * example:
 *
 * <pre>
 *   query {
 *     users(first: 5) {    # (2 + 1 + 121) + 1 = 125
 *       pageInfo {         # 1 + 1 = 2
 *         hasNextPage      # +1
 *       }
 *       totalCount         # +1
 *       edges {            # 24 * 5 + 1 = 121
 *         node {           # (1 + 1 + 21) + 1 = 24
 *           id             # +1
 *           name           # +1
 *           favorites {    # (1 + 1) * 10 + 1 = 21
 *             name         # +1
 *             description  # +1
 *           }
 *         }
 *       }
 *     }
 *   }
 * </pre>
 *
 * @see <a href="https://relay.dev/graphql/connections.htm">GraphQL Cursor Connections
 *     Specification</a>
 */
@Slf4j
public class DefaultFieldComplexityCalculator implements FieldComplexityCalculator {

  private final int defaultListWeight;

  public DefaultFieldComplexityCalculator() {
    this(10);
  }

  public DefaultFieldComplexityCalculator(int defaultListWeight) {
    this.defaultListWeight = defaultListWeight;
  }

  @Override
  public int calculate(FieldComplexityEnvironment environment, int childComplexity) {
    final int weight = calculateWeight(environment);
    if (log.isDebugEnabled()) {
      final String fieldName = environment.getField().getName();
      GraphQLOutputType type = environment.getFieldDefinition().getType();
      if (type instanceof GraphQLNonNull) {
        type = (GraphQLOutputType) ((GraphQLNonNull) type).getWrappedType();
      }
      final String typeName;
      if (type instanceof GraphQLNamedOutputType) {
        typeName = ((GraphQLNamedOutputType) type).getName();
      } else {
        typeName = null;
      }
      log.debug(
          "{}: {}({}) --> {}c * {}w + 1 = {}",
          fieldName,
          typeName,
          type.getClass().getSimpleName(),
          childComplexity,
          weight,
          childComplexity * weight + 1);
    }
    return childComplexity * weight + 1;
  }

  protected int calculateWeight(FieldComplexityEnvironment environment) {
    GraphQLOutputType type = environment.getFieldDefinition().getType();
    if (type instanceof GraphQLNonNull) {
      type = (GraphQLOutputType) ((GraphQLNonNull) type).getWrappedType();
    }
    if (!(type instanceof GraphQLList)) {
      return 1;
    }
    if ("edges".equals(environment.getField().getName())) {
      FieldComplexityEnvironment parentEnvironment = environment.getParentEnvironment();
      GraphQLOutputType parentType = parentEnvironment.getFieldDefinition().getType();
      if (isImplementsInterfaceNamed(parentType, "Connection")) {
        Map<String, Object> parentArgs = parentEnvironment.getArguments();
        return IntStream.of(
                // for Cursor based Connection
                Objects.requireNonNullElse((Integer) parentArgs.get("first"), -1),
                Objects.requireNonNullElse((Integer) parentArgs.get("after"), -1),
                // for Offset based Connection
                Objects.requireNonNullElse((Integer) parentArgs.get("limit"), -1))
            .filter(i -> i >= 0)
            .max()
            .orElse(defaultListWeight);
      }
    }
    return defaultListWeight;
  }

  protected static boolean isImplementsInterfaceNamed(
      GraphQLOutputType type, String interfaceName) {
    if (type instanceof GraphQLNonNull) {
      type = (GraphQLOutputType) ((GraphQLNonNull) type).getWrappedType();
    }
    if (!(type instanceof GraphQLImplementingType)) {
      return false;
    }
    return ((GraphQLImplementingType) type)
        .getInterfaces().stream().anyMatch(e -> interfaceName.equals(e.getName()));
  }
}
