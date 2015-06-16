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
  private Map<String, TokenType> keyword2TokType;
  private String[] keywords = { "abort", "action", "add", "after", "all", "alter", "analyze", "and", "as", "asc",
                                "attach", "autoincrement", "before", "begin", "between", "by", "cascade", "case",
                                "cast", "check", "collate", "column", "commit", "conflict", "constraint", "create",
                                "cross", "current_date", "current_time", "current_timestamp", "database", "default",
                                "deferrable", "deferred", "delete", "desc", "detach", "distinct", "drop", "each",
                                "else", "end", "escape", "except", "exclusive", "exists", "explain", "fail", "for",
                                "foreign", "from", "full", "glob", "group", "having", "if", "ignore", "immediate", "in",
                                "index", "indexed", "initially", "inner", "insert", "instead", "intersect", "into", "is",
                                "isnull", "join", "key", "left", "like", "limit", "match", "natural", "no", "not",
                                "notnull", "null", "of", "offset", "on", "or", "order", "outer", "plan", "pragma",
                                "primary", "query", "raise", "recursive", "references", "regexp", "reindex", "release",
                                "rename", "replace", "restrict", "right", "rollback", "row", "savepoint", "select",
                                "set", "table", "temp", "temporary", "then", "to", "transaction", "trigger", "union",
                                "unique", "update", "using", "vacuum", "values", "view", "virtual", "when", "where",
                                "with", "without"};


  public SQLTokenizer(Reader r) {
    reader = new BufferedReader(r);
    line = 1;
    col = 0;
    EOF = false;
    keyword2TokType = populateKeywordTable(keywords);
    next();
  }

  private Map<String, TokenType> populateKeywordTable(String[] keywords) {
    HashMap<String, TokenType> map = new HashMap<String, TokenType>();
    for(String kw : keywords) {
      map.put(kw, TokenType.valueOf(kw.toUpperCase()));
    }
    return map;
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
    } else if(isIdent(ch)) {
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

    while(isIdent(ch)) {
      sb.append(ch);
      next();
    }
    String s = sb.toString().toLowerCase();
    Token tok;
    TokenType type = keyword2TokType.get(s);
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
