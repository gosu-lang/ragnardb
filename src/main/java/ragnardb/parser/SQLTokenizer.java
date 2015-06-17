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
    } else if(isSpecial(ch)) {
      tok = special();
    } else {
      tok = new Token(TokenType.UNKNOWN, line, col);
    }
    return tok;
  }

  private boolean isSpecial(char c)
    { return c == '('  ||
      c == ')'  ||
      c == '+'  ||
      c == '-'  ||
      c == '.'  ||
      c == ','  ||
      c == ';' ;
    }

  private Token special()
  {
    Token tok;
    if(ch == '('){tok = new Token(TokenType.LPAREN, line, col);}
    else if(ch == ')'){tok = new Token(TokenType.RPAREN, line, col);}
    else if(ch == '+'){tok = new Token(TokenType.PLUS, line, col);}
    else if(ch == '-'){tok = new Token(TokenType.MINUS, line, col);}
    else if(ch == '.'){tok = new Token(TokenType.DOT, line, col);}
    else if(ch == ','){tok = new Token(TokenType.COMMA, line, col);}
    else if(ch == ';'){tok = new Token(TokenType.SEMI, line, col);}
    else{tok = new Token(TokenType.UNKNOWN, line, col);}

    next();
    return tok;

  }

  private boolean isNumber(char c) {
    return (c >= '0' && c <= '9') || c == '.';
  }

  private Token number() {
    Token tok;
    int l = line;
    int c = col;
    long intNum = 0;
    double decNum;
    boolean isDecimal = false;
    boolean isExponential = false;

    /*Set exponent*/
    int e = 0;

    while(isNumber(ch) || ch == 'e') {
      if(ch == '.') {
        isDecimal = true;
        next();
        continue;
      }
      if(ch == 'e') {
        isExponential = true;
        next();
        break;
      }
      intNum = intNum * 10 + (ch - '0');
      if(isDecimal){ e--; }
      next();
    }
    if(isExponential){
      isDecimal = true;
      /*Used to deal with the event a negative exponential is used*/
      int negativeExp = 1;
      int expNum = 0;
      if(ch == '+') {
        next();
      }
      if(ch == '-') {
        negativeExp = -1;
        next();
      }
      while(isNumber(ch) && ch != '.') {
        expNum = expNum * 10 + (ch - '0');
        next();
      }
      e += negativeExp*expNum;
    }
    decNum = intNum * Math.pow(10, e);
    if(isDecimal) {
      tok = new Token(TokenType.DOUBLE, 1, c);
      tok.setDoubleNumber(decNum);
    } else {
      tok = new Token(TokenType.LONG, 1, c);
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
