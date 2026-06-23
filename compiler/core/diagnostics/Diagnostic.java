package compiler.core.diagnostics;

public class Diagnostic {

    public enum Level {
        INFO,
        WARNING,
        ERROR
    }

    private final Level level;
    private final String code;
    private final String message;

    public Diagnostic(Level level, String code, String message) {
        this.level = level;
        this.code = code;
        this.message = message;
    }

    public Level getLevel() {
        return level;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
