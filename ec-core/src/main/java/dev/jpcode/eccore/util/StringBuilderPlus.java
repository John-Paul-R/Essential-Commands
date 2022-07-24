package dev.jpcode.eccore.util;

public class StringBuilderPlus {

    private final StringBuilder sb;

    public StringBuilderPlus() {
        sb = new StringBuilder();
    }

    public StringBuilderPlus append(String str)
    {
        sb.append(str != null ? str : "");
        return this;
    }

    public StringBuilderPlus appendLine(String str)
    {
        sb.append(str != null ? str : "").append(System.getProperty("line.separator"));
        return this;
    }

    public String toString()
    {
        return sb.toString();
    }
}
