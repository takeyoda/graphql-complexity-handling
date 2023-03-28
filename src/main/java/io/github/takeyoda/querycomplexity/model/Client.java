package io.github.takeyoda.querycomplexity.model;

import lombok.Builder;

@Builder
public record Client(String apiKey, int maxDepth, int maxComplexity) {}
