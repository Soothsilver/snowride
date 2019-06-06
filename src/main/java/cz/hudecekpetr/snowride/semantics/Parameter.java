package cz.hudecekpetr.snowride.semantics;

public class Parameter {
    public String text;
    public ParameterKind kind;

    public Parameter(String text, ParameterKind kind) {
        this.text = text;
        if (kind == ParameterKind.VARARGS) {
            this.text = "[varargs] " + this.text.replace("*", "");
        }
        this.kind = kind;
    }
}
