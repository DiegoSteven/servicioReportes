package com.example.Util;

import java.io.Serializable;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ExpressionEvaluatorFactory {
/* 

    private final Map<String, Serializable> compiledExpressions = new ConcurrentHashMap<>();

    public Object evaluate(String expression, Map<String, Object> context) {
        return MVEL.executeExpression(getCompiled(expression), context);
    }

    public boolean evaluateBoolean(String expression, Map<String, Object> context) {
        Object result = evaluate(expression, context);
        return result instanceof Boolean && (Boolean) result;
    }

    private Serializable getCompiled(String expression) {
        return compiledExpressions.computeIfAbsent(expression, MVEL::compileExpression);
    }
        */
}