import token.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Lexer implements Closeable {

    private final FileReader reader;
    private int line = 1;

    public Lexer(File file) throws FileNotFoundException {
        reader = new FileReader(file);
    }

    public List<Token> tokenize() throws IOException {
        return tokenize(false);
    }

    private List<Token> tokenize(boolean alreadyInsideStrField) throws IOException {
        int content;
        StringBuilder builder = new StringBuilder();
        List<Token> tokens = new ArrayList<>();
        Token prevTok = null;

        boolean isEscaped = false;
        boolean insideChar = false;
        boolean insideStr = false;

        boolean insideStrField = false;

        boolean singleLineComment = false;

        while ((content = reader.read()) != -1) {
            char ch = (char) content;

            if (ch == '\n') {
                line++;
                singleLineComment = false;
            }

            if (alreadyInsideStrField && ch == '}') {
                Token token = findTokenType(builder.toString());
                if (token != null) {
                    tokens.add(token);
                }
                tokens.add(new Token(TokenType.STR_FIELD_END, null, line));
                break;
            }

            // skip inline comments
            if (singleLineComment) {
                continue;
            }

            // handle string
            if (insideStr) {

                // after char is escaped, finish escape sequence
                if (isEscaped) {
                    builder.append(ch);
                    isEscaped = false;
                }

                // next char is escaped
                else if (ch == '\\') {
                    builder.append(ch);
                    isEscaped = true;
                }

                else if (ch == '{') {
                    Token token = getStringToken(builder.toString() + '"');
                    if (token != null) {
                        tokens.add(token);
                    }

                    builder.setLength(0);
                    builder.append('"');
                    tokens.add(new Token(TokenType.STR_FIELD_START, null, line));

                    List<Token> fieldTokens = tokenize(true);
                    tokens.addAll(fieldTokens);
                }

                // end string
                // found un-escaped "
                else if (ch == '"') {
                    builder.append(ch);
                    Token token = getStringToken(builder.toString());
                    if (token != null) {
                        tokens.add(token);
                    }

                    builder.setLength(0);
                    insideStr = false;
                }

                else {
                    builder.append(ch);
                }
                continue;
            }

            // handle char
            if (insideChar) {
                builder.append(ch);
                
                // stop escape sequence
                if (isEscaped) {
                    isEscaped = false;
                }

                // start escape sequence
                else if (ch == '\\') {
                    isEscaped = true;
                }

                // end char
                // found un-escaped '
                else if (ch == '\'') {
                    Token token = getCharToken(builder.toString());
                    if (token != null) {
                        tokens.add(token);
                    }
                    builder.setLength(0);
                    insideChar = false;
                }
                continue;
            }

            // starts string
            if (ch == '"') {
                if (prevTok != null) {
                    tokens.add(prevTok);
                    prevTok = null;
                }
                insideStr = true;
                builder.setLength(0);
                builder.append(ch);
                continue;
            }

            // starts char
            if (ch == '\'') {
                if (prevTok != null) {
                    tokens.add(prevTok);
                    prevTok = null;
                }
                insideChar = true;
                builder.setLength(0);
                builder.append(ch);
                continue;
            }

            // skip whitespace
            if (Character.isWhitespace(ch)) {
                // previous token is found
                if (prevTok != null) {

                    // add to list then reset
                    tokens.add(prevTok);
                    prevTok = null;
                }

                // reset string builder
                builder.setLength(0);
                continue;
            }

            // starts inline comments
            if (ch == '#') {
                // previous token is found
                if (prevTok != null) {

                    // add to list then reset
                    tokens.add(prevTok);
                    prevTok = null;
                }

                singleLineComment = true;

                // reset string builder
                builder.setLength(0);
                continue;
            }

            builder.append(ch);

            Token token = findTokenType(builder.toString());

            if (token == null && prevTok != null) {
                tokens.add(prevTok);
                builder.setLength(0);
                builder.append(ch);
                prevTok = findTokenType(String.valueOf(ch));
                continue;
            }

            prevTok = token;
        }

        // check unclosed string error
        if (insideStr && !alreadyInsideStrField) {
            String raw = builder.toString();
            String modified = raw;
            if (raw.length() > 150) {
                modified = raw.substring(0, 150) + "...";
            }
            Main.error(line, "Unclosed string (\"" + modified + "\"");
        }

        // add final token if exists to the tokens list
        if (prevTok != null && !alreadyInsideStrField) {
            tokens.add(prevTok);
        }

        // add end of file token
        if (!alreadyInsideStrField) {
            tokens.add(eof());
        }

        return tokens;
    }

    private Token eof() {
        return new Token(TokenType.EOF, null, line);
    }

    private Token findTokenType(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        // handle keyword tokens
        Token token = getKeywordToken(value);
        if (token != null) {
            return token;
        }

        // handle punctuation tokens
        token = getPunctuationToken(value);
        if (token != null) {
            return token;
        }

        // handle operator tokens
//        System.out.println(value);
        token = getOperatorToken(value);
        if (token != null) {
            return token;
        }

        // handle literals
        token = getLiteralToken(value);
        if (token != null) {
            return token;
        }

        // handle identifiers
        if (isValidIdentifier(value)) {
            return new Token(TokenType.IDENTIFIER, value, line);
        }

        return null;
    }

    private Token getLiteralToken(String value) {

        // handle chars
        if (value.charAt(0) == '\'') {
            Token token = getCharToken(value);
            if (token != null) {
                return token;
            }
        }

        // handle strings
        if (value.charAt(0) == '"') {
            Token token = getStringToken(value);
            if (token != null) {
                return token;
            }
        }

        // handle numbers
        if (Util.isDecimal(value)) {
            return new Token(TokenType.NUMBER_LITERAL, Double.parseDouble(value), line);
        }

        return null;
    }

    private Token getStringToken(String value) {
        if (value.length() < 2) {
            return null;
        }

        if (value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"') {
            return null;
        }

        // remove the double quotes at the beginning and the end
        String content = value.substring(1, value.length() - 1);
        if (content.isEmpty()) {
            return new Token(TokenType.STRING_LITERAL, "", line);
        }

        // remove all escaped sequences
        content = removeEscapeSequence(content);
        if (content == null) {

            // content contains invalid escape sequence or
            // contains illegal non escaped characters (", ')
            return null;
        }

        return new Token(TokenType.STRING_LITERAL, content, line);
    }

    private String removeEscapeSequence(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        boolean sequenceStarted = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            // handle started sequence
            if (sequenceStarted) {
                if (Strings.isEscapedChar(ch)) {
                    char escaped = Strings.getEscapedChar(ch);
                    sb.append(escaped);

                    // stops sequence
                    sequenceStarted = false;
                } else {
                    return null;
                }

                continue;
            }

            // illegal unescaped double quotes (") exists within the str
            if (ch == '"') {
                return null;
            }

            // starts sequence
            if (ch == '\\') {
                sequenceStarted = true;
                continue;
            }

            // appends normally if not an escaped sequence
            sb.append(ch);
        }

        return sb.toString();
    }

    private Token getCharToken(String value) {
        if (value.charAt(0) != '\'' || value.charAt(value.length() - 1) != '\'') {
            return null;
        }

        if (value.length() == 3) {
            return new Token(TokenType.CHAR_LITERAL, value.charAt(1), line);
        } else if (value.length() > 3) {
            String escapedCh = removeEscapeSequence(value.substring(1, value.length() - 1));
            if (escapedCh == null || escapedCh.length() != 1) {
                return null;
            }
            return new Token(TokenType.CHAR_LITERAL, escapedCh.charAt(0), line);
        }

        return null;
    }

    public boolean isValidIdentifier(String s) {
        if (!Util.isAlpha(s.charAt(0)) && s.charAt(0) != '_') {
            return false;
        }

        for (int i = 1; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (!Util.isAlpha(ch) && !Util.isNumeric(ch) && ch != '_') {
                return false;
            }
        }

        return true;
    }

    private Token getPunctuationToken(String value) {
        Punctuation punct = Punctuation.find(value);
        if (punct == null) {
            return null;
        }
        return new Token(punct, punct.getValue(), line);
    }

    private Token getOperatorToken(String value) {
        Operator op = Operator.find(value);
        if (op == null) {
            return null;
        }
        return new Token(op, op.getValue(), line);
    }

    private Token getKeywordToken(String value) {
        Keyword keyword = Keyword.find(value);
        if (keyword == null) {
            return null;
        }
        return new Token(keyword, keyword, line);
    }

    @Override
    public final void close() throws IOException {
        reader.close();
    }

}
