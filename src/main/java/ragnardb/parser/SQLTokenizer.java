package ragnardb.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

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

  private void hComment() {
    while(ch != '\n') {
      next();
    }
    next();
  }

  private void comment() {
    boolean exiting = false;
    while(!exiting && !EOF) {
      if(ch != '*') {
          next();
      }
      else {
        next();
        if(ch == '/') {
          next();
          exiting = true;
        }
      }
    }
  }

  private Token temp;
  public Token peek(){
    if (temp==null){
      temp = get();
    }
    return temp;
  }
  public Token get() {
    Token tok;
    if(temp!= null){
      tok = temp;
      temp = null;
      return tok;
    }
    while(!EOF && isBlank(ch)) {
      next();
    }

    if(EOF) {
      tok = new Token(TokenType.EOF, line, col);
    } else if(ch == '/') { //Need to add in // Comments
      next();
      if(ch == '*') {
        comment();
        return get();
      } else {
        tok = new Token(TokenType.SLASH, line, col - 1);
      }
    } else if(ch == '-') {
      next();
      if(ch == '-') {
        hComment();
        return get();
      } else {
        tok = new Token(TokenType.MINUS, line, col - 1);
      }
    } else if(ch == '"') {
      tok = stringLiteralIdentifier();
    } else if(isIdent(ch)) {
      tok = identifier();
    } else if(isNumberOrDot(ch)) {
      tok = numberOrDot();
    } else if(ch == '@'){
      tok = new Token(TokenType.AT, line, col);
      next();
    } else if(ch == ':'){
      tok = new Token(TokenType.COLON, line, col);
      next();
    } else if(ch == '(') {
      tok = new Token(TokenType.LPAREN, line, col);
      next();
    } else if(ch == ')') {
      tok = new Token(TokenType.RPAREN, line, col);
      next();
    } else if(ch == '+') {
      tok = new Token(TokenType.PLUS, line, col);
      next();
    } else if(ch == ',') {
      tok = new Token(TokenType.COMMA, line, col);
      next();
    } else if(ch == ';') {
      tok = new Token(TokenType.SEMI, line, col);
      next();
    } else if(ch == '*') {
      tok = new Token(TokenType.TIMES, line, col);
      next();
    } else if(ch == '%') {
      tok = new Token(TokenType.MOD, line, col);
      next();
    } else if(ch == '<') {
      next();
      if(ch == '>'){
        tok = new Token(TokenType.NEQ,line,col);
        next();
      }
      else if(ch == '='){
        tok = new Token(TokenType.GTE,line,col);
        next();
      }
      else{
        tok = new Token(TokenType.GT,line,col-1);
      }
    } else if(ch == '>') {
      next();
      if(ch == '='){
        tok = new Token(TokenType.LTE,line,col);
        next();
      }
      else{
        tok = new Token(TokenType.LT,line-1,col);
      }
    } else if(ch == '!'){
      next();
      if(ch == '='){
        tok = new Token(TokenType.NEQ,line,col);
        next();
      }
      else{
        tok = new Token(TokenType.UNKNOWN,line,col);
      }
    } else if(ch == '=') {
      tok = new Token(TokenType.EQ, line, col);
      next();
    } else if(ch == '&'){
      next();
      if(ch == '&'){
        tok = new Token(TokenType.OVL, line, col);
        next();
      }
      else{
        tok = new Token(TokenType.UNKNOWN, line, col);
      }
    } else if(ch == '|'){
      next();
      if(ch == '|'){
        tok = new Token(TokenType.BAR, line, col);
        next();
      }
      else{
        tok = new Token(TokenType.UNKNOWN, line, col);
      }
    } else {
      tok = new Token(TokenType.UNKNOWN, line, col);
      next();
    }
    return tok;
  }

  private boolean isNumberOrDot(char c) {
    return (c >= '0' && c <= '9') || c == '.';
  }

  private Token numberOrDot() {
    Token tok;
    int l = line;
    int c = col;
    long intNum = 0;
    double decNum;
    boolean isDecimal = false;

    /*Set exponent*/
    int e = 0;

    while(isNumberOrDot(ch)) {

      if(ch == '.' && !isDecimal) {
        isDecimal = true;
        next();
        if(!isNumberOrDot(ch) && intNum == 0) {
          return new Token(TokenType.DOT, line, col - 1);
        }
      }

      if(!EOF) {
        intNum = intNum * 10 + (ch - '0');
        if(isDecimal) {
          e--;
        }
        next();
      }

      if(ch == '.' && isDecimal) {
        decNum = intNum * Math.pow(10, e);
        tok = new Token(TokenType.DOUBLE, l, c);
        tok.setDoubleNumber(decNum);
        return tok;
      }

      /*Adds date/time handling*/
      /*if((ch == ':' || ch == '-') && intNum != 0){
        return dateTime((int)intNum, l, c);
      }*/
    }

    if(ch == 'e' || ch == 'E') {
      if(intNum == 0) {
        intNum = 1;
      }
      next();
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

      while(isNumberOrDot(ch) && ch != '.') {
        expNum = expNum * 10 + (ch - '0');
        next();
      }

      if(ch == '.' || (!isNumberOrDot(ch) && !(EOF || isBlank(ch)))) {
        e += negativeExp * expNum;
        tok = new Token(TokenType.DOUBLE, l, c);
        decNum = intNum * Math.pow(10, e);
        tok.setDoubleNumber(decNum);
        return tok;
      }

      e += negativeExp * expNum;
    }
    decNum = intNum * Math.pow(10, e);
    if(isDecimal) {
      tok = new Token(TokenType.DOUBLE, l, c);
      tok.setDoubleNumber(decNum);
    } else {
      tok = new Token(TokenType.LONG, l, c);
      tok.setLongNumber(intNum);
    }
    return tok;
  }

  private Token dateTime(int start, int l, int c){
    StringBuilder sb = new StringBuilder(start);
    Token output = new Token(TokenType.LONG, l, c);
    if(ch == ':'){
      while((isNumberOrDot(ch) && ch != '.') || ch == ':'){
        sb.append(ch);
        next();
      }
      output.setLongNumber(0);
      output.setText(sb.toString());
    }
    else{
      while((isNumberOrDot(ch) && ch != '.') || ch == '-'){
        sb.append(ch);
        next();
      }
      output.setLongNumber(0);
      output.setText(sb.toString());
    }
    return output;
  }

  private Token identifier() {
    StringBuilder sb = new StringBuilder();
    int l = line;
    int c = col;


    sb.append(ch);
    next();

    while(isIdent(ch) || (isNumberOrDot(ch) && ch != '.') ) {
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


  private Token stringLiteralIdentifier() {
    StringBuilder sb = new StringBuilder();
    int l = line;
    int c = col;

    next();

    while(ch != '"') {
      sb.append(ch);
      next();
    }
    next();
    String s = sb.toString();
    Token tok;
    tok = new Token(TokenType.IDENT, l, c);
    tok.setText(s);
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
