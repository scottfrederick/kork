package com.netflix.spinnaker.kork.expressions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.mock.env.MockEnvironment;

class ExpressionTransformTest {
  private final ParserContext parserContext = new TemplateParserContext("${", "}");
  private final ExpressionParser parser = new SpelExpressionParser();
  private final MockEnvironment environment = new MockEnvironment();
  private final ExpressionTransform expressionTransform =
      new ExpressionTransform(parserContext, parser, environment, Function.identity());
  private final ExpressionEvaluationSummary summary = new ExpressionEvaluationSummary();

  @Test
  void evaluateCompositeExpression() {
    StandardEvaluationContext evaluationContext =
        new ExpressionsSupport(Trigger.class)
            .buildEvaluationContext(new Pipeline(new Trigger(100)), true);

    String evaluated =
        expressionTransform.transformString(
            "group:artifact:${trigger['buildNumber']}", evaluationContext, summary);

    assertThat(evaluated).isEqualTo("group:artifact:100");
    assertThat(summary.getFailureCount()).isEqualTo(0);
  }

  @Test
  void resolvePropertyPlaceholder() {
    StandardEvaluationContext evaluationContext =
        new ExpressionsSupport().buildEvaluationContext(new Object(), true);

    environment.setProperty("test.key", "a test value");

    String evaluated =
        expressionTransform.transformString(
            "group:artifact:${test.key}", evaluationContext, summary);

    assertThat(evaluated).isEqualTo("group:artifact:a test value");
    assertThat(summary.getFailureCount()).isEqualTo(0);
  }

  @Test
  void evaluateToInputString() {
    StandardEvaluationContext evaluationContext =
        new ExpressionsSupport().buildEvaluationContext(new Object(), true);

    String evaluated =
        expressionTransform.transformString(
            "group:artifact:${\"a test string\"}", evaluationContext, summary);

    assertThat(evaluated).isEqualTo("group:artifact:a test string");
    assertThat(summary.getFailureCount()).isEqualTo(0);
  }

  @AllArgsConstructor
  @Data
  static class Pipeline {
    Trigger trigger;
  }

  @AllArgsConstructor
  @Data
  static class Trigger {
    int buildNumber;
  }
}
