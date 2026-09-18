package org.example.model;

public record JudgeOptions(
    String modelName,
    Double temperature
) {
    public static JudgeOptions defaults() {
        return new JudgeOptions(null, 0.0);
    }
}
