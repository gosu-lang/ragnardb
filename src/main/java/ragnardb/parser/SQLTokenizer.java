package ragnardb.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class SQLTokenizer {
  private BufferedReader reader;
  private int line;
  private int col;
  private char ch;
  private boolean EOF;

  public SQLTokenizer(Reader r) {
    reader = new BufferedReader(r);
    line = 1;
    col = 0;
    EOF = false;
    next();
  }

  private boolean isBlank(char c) {
    return c == ' ' || c == '\t' || c == '\n' || c == '\r';
  }

  private boolean isIdent(char c) {
    return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';
  }

  public Token get() {
    Token tok;

    while(!EOF && isBlank(ch)) {
      next();
    }

    if(EOF) {
      tok = new Token(TokenType.EOF, line, col);
    } else if(isIdent(ch) ) {
      tok = identifier();
    } else if(isNumber(ch)) {
      tok = number();
    } else {
      tok = new Token(TokenType.UNKNOWN, line, col);
    }
    return tok;
  }

  private boolean isNumber(char c) {
    return c >= '0' && c <= '9';
  }

  private Token number() {
    Token tok;
    int l = line;
    int c = col;
    long intNum = 0;
    double decNum = 0.0;
    boolean isDecimal = false;

    while(isNumber(ch)) {
      intNum = intNum * 10 + (ch - '0');
      next();
    }
    if(ch == '.') {
      next();
      isDecimal = true;
      int e = 0;
      while(isNumber(ch)) {
        intNum = intNum * 10 + (ch - '0');
        next();
        e--;
      }
      decNum = intNum * Math.pow(10, e);
    }
    if(isDecimal) {
      tok = new Token(TokenType.DOUBLE, l, c);
      tok.setDoubleNumber(decNum);
    } else {
      tok = new Token(TokenType.LONG, l, c);
      tok.setLongNumber(intNum);
    }
    return tok;
  }

  private Token identifier() {
    StringBuilder sb = new StringBuilder();
    int l = line;
    int c = col;


    sb.append(ch);
    next();

    while(isIdent(ch) || isNumber(ch)  ) {
      sb.append(ch);
      next();
    }
    String s = sb.toString().toLowerCase();
    Token tok;
    TokenType type = TokenType.find(s);
    if(type != null) {
      tok = new Token(type, l, c);
    } else {
      tok = new Token(TokenType.IDENT, l, c);
      tok.setText(s);
    }
    return tok;
  }

  private void next() {
    int c;

    c = read();
    if(c == '\r') {
      c = read();
    }
    if(c == '\n') {
      col = 1;
      line++;
    } else if(c != -1) {
      col++;
    } else {
      EOF = true;
      c = 0;
    }
    ch = (char) c;
  }

  private int read() {
    int c;
    try {
      c = reader.read();
    } catch(IOException e) {
      c = -1;
    }
    return c;
  }
}
