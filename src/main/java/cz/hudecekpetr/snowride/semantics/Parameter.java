package cz.hudecekpetr.snowride.semantics;

public class Parameter {
    public String text;
    public ParameterKind kind;

    public Parameter(String text, ParameterKind kind) {
        this.text = text;
        this.kind = kind;
    }
}
