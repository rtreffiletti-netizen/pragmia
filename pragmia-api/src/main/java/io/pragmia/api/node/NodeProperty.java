package io.pragmia.api.node;

public record NodeProperty(
    String name,
    String label,
    PropertyType type,
    boolean required,
    Object defaultValue,
    String description
) {
    public enum PropertyType { STRING, PASSWORD, INTEGER, BOOLEAN, ENUM, EXPRESSION, DURATION }
}
