package ragnardb.parser;

public class SQLParser {

  private SQLTokenizer _tokenizer;
  private Token currentToken;

  public SQLParser(SQLTokenizer tokenizer) {
    _tokenizer = tokenizer;
    next();
  }

  private void next() {
    currentToken = _tokenizer.get();
  }

  private void match(TokenType expectedType) {
    TokenType currentType = currentToken.getType();
    if(currentType == expectedType) {
      next();
    } else {
      String name = currentType.getName();
      if(currentType == TokenType.IDENT) {
        name = currentToken.getText();
      }
      error(currentToken, "Expecting '" + expectedType.getName() + "' but found '" + name + "'.");
    }
  }

  private void error(Token token, String message) {
    throw new SQLParseError("[" + token.getLine() + ", " + token.getCol() + "] - ERROR: " + message);
  }

  public void parse() {
    parseCreateTable();
  }

  private void parseCreateTable() {
    match(TokenType.CREATE);
    if(currentToken.getType() == TokenType.TEMP ||
      currentToken.getType() == TokenType.TEMPORARY ) {
      next();
    }
    match(TokenType.TABLE);
    match(TokenType.IDENT);
    match(TokenType.LPAREN);
    match(TokenType.RPAREN);
  }
}
