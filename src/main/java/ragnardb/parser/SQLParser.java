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

  private boolean tokEquals(TokenType expectedType) {
    return expectedType == currentToken.getType();
  }

  private void match(TokenType expectedType) {
    TokenType currentType = currentToken.getType();
    if (currentType == expectedType) {
      next();
    } else {
      String name = currentType.getName();
      if (currentType == TokenType.IDENT) {
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
    if (currentToken.getType() == TokenType.TEMP ||
            currentToken.getType() == TokenType.TEMPORARY) {
      next();
    }
    match(TokenType.TABLE);
    if (currentToken.getType() == TokenType.IF) {
      next();
      match(TokenType.NOT);
      match(TokenType.EXISTS);
    }

    match(TokenType.IDENT);

    if (currentToken.getType() == TokenType.DOT) {
      next();
      match(TokenType.IDENT);
    }
    if(tokEquals(TokenType.LPAREN)) {
      next();

      parseColumnDef();
      while (currentToken.getType() == TokenType.COMMA) {
        next();
        if (tokEquals(TokenType.CONSTRAINT)) {
          next();
          parseTableConstraint();
        } else {
          parseColumnDef();
        }
      }
      match(TokenType.RPAREN);
    }
    if (tokEquals(TokenType.WITHOUT)) {
      next();
      match(TokenType.ROWID);
    }
    if (!tokEquals(TokenType.EOF)) {
      error(currentToken, "The statement has not terminated but the grammar has been exhausted.");
    }

  }

  private void parseColumnDef() { //Also does constraint
    match(TokenType.IDENT);
    match(TokenType.IDENT);
    while (currentToken.getType() == TokenType.CONSTRAINT) {
      next();
      match(TokenType.IDENT);
      if (currentToken.getType() == TokenType.PRIMARY) {
        match(TokenType.KEY);
        if (currentToken.getType() == TokenType.ASC || currentToken.getType() == TokenType.DESC) {
          next();
        }
        parseConflictClause();
        if (currentToken.getType() == TokenType.AUTOINCREMENT) {
          next();
        }
      } else if (currentToken.getType() == TokenType.NOT) {
        next();
        match(TokenType.NULL);
        parseConflictClause();
      } else if (currentToken.getType() == TokenType.UNIQUE){
        next();
        parseConflictClause();
      } else if (currentToken.getType() == TokenType.CHECK){
        next();
        match(TokenType.LPAREN);
        parseExpr();
        match(TokenType.RPAREN);
      } else if (currentToken.getType() == TokenType.DEFAULT)
      {
        next();
        if(currentToken.getType() == TokenType.IDENT || currentToken.getType() == TokenType.DOUBLE
                || currentToken.getType() == TokenType.LONG)
        {
          next();
        } else if(currentToken.getType() == TokenType.LPAREN){
          next();
          parseExpr();
          match(TokenType.RPAREN);
        } else {
          error(currentToken, "Expecting LONG DOUBLE RPAREN or IDENT but found '" + currentToken.getText() + "'.");
        }
      } else if(currentToken.getType() == TokenType.COLLATE)
      {
        next();
        match(TokenType.IDENT);
      }
      else if(currentToken.getType() == TokenType.REFERENCES) //First keyword of foreign key clause
      {
        parseForeignKeyClause();
      }
      else{
        error(currentToken, "Expecting Constraint but found '" + currentToken.getText() + "'.");
      }
      }
    }

    private void parseConflictClause(){
      match(TokenType.ON);
      match(TokenType.CONFLICT);
      if(currentToken.getType() == TokenType.ROLLBACK ||
              currentToken.getType() == TokenType.ABORT  ||
              currentToken.getType() == TokenType.FAIL  ||
              currentToken.getType() == TokenType.IGNORE  ||
              currentToken.getType() == TokenType.REPLACE  ){
        next();
      }
      else{
        error(currentToken, "Expecting Conflict but found '" + currentToken.getText() + "'.");
      }
    }

    private void parseForeignKeyClause(){
      match(TokenType.REFERENCES);
      match(TokenType.IDENT);
      if(tokEquals(TokenType.LPAREN)){
        next();
        match(TokenType.IDENT);
        while(tokEquals(TokenType.IDENT)){
          next();
          match(TokenType.COMMA);
        }
        match(TokenType.RPAREN);
        while(tokEquals(TokenType.ON) || tokEquals(TokenType.MATCH)){
          if (tokEquals(TokenType.ON)){
            next();
            if (!(tokEquals(TokenType.DELETE)||tokEquals(TokenType.UPDATE))){
              error(currentToken, "Expecting Update/Delete but found '" + currentToken.getText() + "'.");
            }
            next();
            if(tokEquals(TokenType.SET)){
              next();
              if (!(tokEquals(TokenType.NULL)||tokEquals(TokenType.DEFAULT))){
                error(currentToken, "Expecting NULL/DEFAULT but found '" + currentToken.getText() + "'.");
              }
              next();
            } else if (tokEquals(TokenType.CASCADE) || tokEquals(TokenType.RESTRICT)){
              next();
            } else if (tokEquals(TokenType.NO)){
              next();
              match(TokenType.ACTION);
            } else{
              error(currentToken, "Expecting foreign key value but found '" + currentToken.getText() + "'.");
            }
          } else if (tokEquals(TokenType.MATCH)){
            next();
            match(TokenType.IDENT);
          }
          else{
            error(currentToken, "Expecting ON/MATCH but found '" + currentToken.getText() + "'.");
          }

          }
        //I checked and am pretty certain that there are no contexts in which 'not' would be appropriate
        //Other than for this clause
        if(tokEquals(TokenType.NOT)){
          next();
          if (!tokEquals(TokenType.DEFERRABLE))
          {
            error(currentToken, "Expecting DEFERRABLE but found '" + currentToken.getText() + "'.");
          }
        }

        if(tokEquals(TokenType.DEFERRABLE)){
          next();
          if(tokEquals(TokenType.INITIALLY)){
            next();
            if(tokEquals(TokenType.DEFERRED) || tokEquals(TokenType.IMMEDIATE)){
              next();
            }else{
              error(currentToken, "Expecting DEFERRED/IMMEDIATE but found '" + currentToken.getText() + "'.");
            }
          }

        }

      }
    }
  private void parseTableConstraint() {
    if(currentToken.getType() == TokenType.CONSTRAINT){
      next();
      match(TokenType.IDENT);
    }
    switch(currentToken.getType()){
      case PRIMARY:
        next();
        match(TokenType.KEY);
        match(TokenType.LPAREN);
        parseIndexedColumn();
        while(currentToken.getType() == TokenType.COMMA){
          parseIndexedColumn();
        }
        match(TokenType.RPAREN);
        parseConflictClause();
        break;
      case UNIQUE:
        next();
        match(TokenType.LPAREN);
        parseIndexedColumn();
        while(currentToken.getType() == TokenType.COMMA){
        parseIndexedColumn();
      }
      match(TokenType.RPAREN);
      parseConflictClause();
      break;
      case CHECK:
        match(TokenType.LPAREN);
        parseExpr();
        match(TokenType.RPAREN);
        break;
      case FOREIGN:
        next();
        match(TokenType.KEY);
        match(TokenType.LPAREN);
        match(TokenType.IDENT);
        while(currentToken.getType() == TokenType.COMMA){
          next();
          match(TokenType.IDENT);
        }
        match(TokenType.RPAREN);
        parseForeignKeyClause();
        break;
      default:
        error(currentToken, "Expecting 'CONSTRAINT', 'PRIMARY', 'UNIQUE', 'CHECK' or 'FOREIGN' but found '" + currentToken.getType().toString());
        break;
    }
  }

  private void parseIndexedColumn() {
    match(TokenType.IDENT);
    if (currentToken.getType() == TokenType.COLLATE) {
      next();
      match(TokenType.IDENT);
    }
    if(currentToken.getType() == TokenType.ASC || currentToken.getType() == TokenType.DESC){
      next();
    }
  }

  private void parseRaiseFunction() {
    match(TokenType.RAISE);
    match(TokenType.LPAREN);
    switch (currentToken.getType()){
      case IGNORE:
        next();
        match(TokenType.RPAREN);
        break;
      case ROLLBACK:
      case ABORT:
      case FAIL:
        next();
        match(TokenType.COMMA);
        match(TokenType.IDENT);
        match(TokenType.RPAREN);
        break;
    }
  }

  private void parseExpr(){
    parseAndCondition();
    if(tokEquals(TokenType.OR)){
      next();
      parseExpr();
    }
  }
  private void parseAndCondition(){
    parseCondition();
    if(tokEquals(TokenType.AND)){
      next();
      parseExpr();
    }
  }
  private void parseCondition(){ //Ommiting EXISTS (select)
    while(tokEquals(TokenType.NOT)){
      next();
    }
    parseOperand();


  }
  private void parseOperand(){
    parseSummand();
    if(tokEquals(TokenType.BAR)){
      next();
      parseOperand();
      //TODO: Add Condition Right Hand Side
    }
  }

  private void parseSummand(){
    parseFactor();
    if(tokEquals(TokenType.PLUS) || tokEquals(TokenType.MINUS)){
      next();
      parseSummand();
    }
  }

  private void parseFactor(){
    parseTerm();
    if(tokEquals(TokenType.SLASH)||tokEquals(TokenType.TIMES)||tokEquals(TokenType.MOD)){
      next();
      parseFactor();
    }
  }

  private void parseTerm(){
    if(!(tokEquals(TokenType.DOUBLE)||tokEquals(TokenType.LONG))){
      error(currentToken, "Expecting 'DOUBLE' or 'LONG' but found '" + currentToken.getType().toString());
    }
  }


}
