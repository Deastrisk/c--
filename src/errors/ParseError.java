package errors;

import token.Token;
import token.TokenType;

public class ParseError extends RuntimeException {

    private final Token token;

    public ParseError(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public String getMessage() {
        if (token.token() == TokenType.EOF) {
            return "[Line: " + token.line() + "] Error at end of file: " + super.getMessage();
        }

        return "[Line: " + token.line() + "] Error at '" + token.value() + "': " + super.getMessage();
    }
}
