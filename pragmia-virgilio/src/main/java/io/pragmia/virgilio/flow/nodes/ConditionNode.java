package io.pragmia.virgilio.flow.nodes;

import io.pragmia.api.node.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ConditionNode implements AuthFlowNode {
    private final SpelExpressionParser parser = new SpelExpressionParser();

    public String getNodeType()    { return "CONDITION"; }
    public String getCategory()    { return "Logic"; }
    public String getIcon()        { return "git-branch"; }
    public String getDescription() { return "Branch condizionale SpEL — porta 'true' o 'false'"; }
    public List<String> getOutputPorts() { return List.of("true", "false"); }
    public List<NodeProperty> getProperties() {
        return List.of(new NodeProperty("expression", "SpEL Expression", NodeProperty.PropertyType.EXPRESSION,
            true, null, "Es: #attributes['role'] == 'ADMIN'"));
    }

    public NodeExecutionResult execute(NodeContext ctx) {
        String expr = (String) ctx.getAllAttributes().get("expression");
        if (expr == null || expr.isBlank()) return NodeExecutionResult.failure("No expression configured");
        try {
            StandardEvaluationContext sc = new StandardEvaluationContext();
            sc.setVariable("attributes", ctx.getAllAttributes());
            sc.setVariable("username",   ctx.getUsername().orElse(""));
            Boolean res = parser.parseExpression(expr).getValue(sc, Boolean.class);
            return NodeExecutionResult.success(Boolean.TRUE.equals(res) ? "true" : "false");
        } catch (Exception e) {
            return NodeExecutionResult.failure("SpEL error: " + e.getMessage());
        }
    }
}
