package compiler.core.errors;

public class CompilerException extends RuntimeException {

    private final String code;

    public CompilerException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
