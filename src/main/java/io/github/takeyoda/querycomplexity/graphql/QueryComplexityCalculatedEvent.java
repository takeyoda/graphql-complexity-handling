package io.github.takeyoda.querycomplexity.graphql;

import graphql.GraphQLContext;
import graphql.language.OperationDefinition;
import lombok.Builder;

@Builder
public record QueryComplexityCalculatedEvent(
    OperationDefinition.Operation operationType,
    int depth,
    int complexity,
    GraphQLContext context) {}
