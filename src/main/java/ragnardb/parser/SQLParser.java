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

  private void specialError(String s){
    error(currentToken, "Expecting " + s + " but found '" + currentToken.getText() + ".");
  }
  private void pass(TokenType passType){
    if(passType == currentToken.getType()){
      next();
    }
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

  private void matchNum() {
    TokenType currentType = currentToken.getType();
    if (currentType == TokenType.LONG || currentType == TokenType.DOUBLE) {
      next();
    } else {
      String name = currentType.getName();
      error(currentToken, "Expecting 'LONG' or 'DOUBLE' but found '" + name + "'.");
    }
  }


  private boolean isNum(){
    return (currentToken.getType() == TokenType.LONG || currentToken.getType() == TokenType.DOUBLE);
  }

  private void list(TokenType item){
    match(item);
    while(tokEquals(TokenType.COMMA)){
      next();
      match(item);
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
        if (tokEquals(TokenType.IDENT)) {
          parseColumnDef();
        } else {
          parseTableConstraint();
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
  private void parseTypeName(){
    match(TokenType.IDENT);
    while(tokEquals(TokenType.IDENT)){
      next();
    }
    if(tokEquals(TokenType.LPAREN)){
      next();
      if(currentToken.getType() == TokenType.LONG | currentToken.getType() == TokenType.DOUBLE){
        next();
        if(tokEquals(TokenType.COMMA)){
          next();
          if(currentToken.getType() == TokenType.LONG | currentToken.getType() == TokenType.DOUBLE){
            next();
            match(TokenType.RPAREN);
          } else {
            error(currentToken, "Expecting LONG or DOUBLE but found '" + currentToken.getType() + "'.");
          }
        } else{
          match(TokenType.RPAREN);
        }
      }else{
        error(currentToken, "Expecting LONG or DOUBLE but found '" + currentToken.getType() + "'.");
      }
    }
  }
  private boolean isConstraint(){

    return tokEquals(TokenType.CONSTRAINT) ||
            tokEquals(TokenType.PRIMARY) ||
            tokEquals(TokenType.UNIQUE) ||
            tokEquals(TokenType.CHECK) ||
            tokEquals(TokenType.FOREIGN) ;
  }

  private void parseColumnDef(){
    match(TokenType.IDENT);
    parseTypeName();
    if(tokEquals(TokenType.DEFAULT)) {
      next();
      parseExpr();
    }
    //TODO: Add 'as' clause when select statements work
    if(tokEquals(TokenType.NOT)){
      next();
      match(TokenType.NULL);
    }
    else if (tokEquals(TokenType.NULL)){
      next();
    }

    if(tokEquals(TokenType.AUTO_INCREMENT) || tokEquals(TokenType.IDENTITY)){
      next();
      if(tokEquals(TokenType.LPAREN)){
        next();
        matchNum();
        if (tokEquals(TokenType.COMMA)){
          next();
          matchNum();
        }
        match(TokenType.RPAREN);
        }
      }
      //Implement Selectivity, Comment?
    if(tokEquals(TokenType.UNIQUE)){
      next();
    }
    else if(tokEquals(TokenType.PRIMARY)){
      next();
      match(TokenType.KEY);
      pass(TokenType.HASH);
    }

    if(tokEquals(TokenType.CHECK)){
      next();
      parseCondition();
    }
  }

  private void parseConstraint()
  {
    if(tokEquals(TokenType.CONSTRAINT)){
      next();
      if(tokEquals(TokenType.IF)){
        next();
        match(TokenType.NOT);
        match(TokenType.EXISTS);
      }
      match(TokenType.IDENT);
    }
    if(tokEquals(TokenType.CHECK)){
      next();
      parseExpr();
    }
    else if (tokEquals(TokenType.UNIQUE)){
      next();
      match(TokenType.LPAREN);
      match(TokenType.IDENT);
      while(tokEquals(TokenType.COMMA)){
        next();
        match(TokenType.IDENT);
      }
      match(TokenType.RPAREN);
    }
    else if (tokEquals(TokenType.PRIMARY)){
      next();
      match(TokenType.KEY);
      pass(TokenType.HASH);
      match(TokenType.LPAREN);
      match(TokenType.IDENT);
      while(tokEquals(TokenType.COMMA)){
        next();
        match(TokenType.IDENT);
      }
      match(TokenType.RPAREN);
    }
    else if(tokEquals(TokenType.FOREIGN)){
      next();
      match(TokenType.KEY);
      match(TokenType.LPAREN);
      list(TokenType.IDENT);
      match(TokenType.RPAREN);
      match(TokenType.REFERENCES);
      pass(TokenType.IDENT);
      if(tokEquals(TokenType.LPAREN)){
        next();
        list(TokenType.IDENT);
        match(TokenType.RPAREN);
      }
      if(tokEquals(TokenType.ON)){
        next();
        if(tokEquals(TokenType.DELETE)||tokEquals(TokenType.UPDATE)){
          next();
          parseReferentialAction();
        }
        else{
          error(currentToken, "Expecting DELETE or UPDATE but found '" + currentToken.getText() + ".");
        }


      }

    }


  }

  private void parseReferentialAction(){

    switch(currentToken.getType()) {
      case CASCADE:
        next();
      case RESTRICT:
        next();
      case NO:
        next();
        match(TokenType.ACTION);
      case SET:
        if(tokEquals(TokenType.DEFAULT)||tokEquals(TokenType.NULL)){
          next();
        }
        else{
          specialError("DEFAULT or NULL");
        }
      default:
        specialError("CASCADE or RESTRICT or NO or SET");
    }
  }

    private void parseConflictClause(){
      if(tokEquals(TokenType.ON)) {
        next();
        match(TokenType.CONFLICT);
        if (currentToken.getType() == TokenType.ROLLBACK ||
                currentToken.getType() == TokenType.ABORT ||
                currentToken.getType() == TokenType.FAIL ||
                currentToken.getType() == TokenType.IGNORE ||
                currentToken.getType() == TokenType.REPLACE) {
          next();
        } else {
          error(currentToken, "Expecting conflict action but found '" + currentToken.getText() + "'.");
        }
      }
    }

    private void parseForeignKeyClause(){
      match(TokenType.REFERENCES);
      match(TokenType.IDENT);
      if(tokEquals(TokenType.LPAREN)) {
        next();
        match(TokenType.IDENT);
        while (tokEquals(TokenType.COMMA)) {
          next();
          match(TokenType.IDENT);
        }
        match(TokenType.RPAREN);
      }
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
          next();
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
          next();
          parseIndexedColumn();
        }
        match(TokenType.RPAREN);
        parseConflictClause();
        break;
      case CHECK:
        next();
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
        error(currentToken, "Expecting 'CONSTRAINT', 'PRIMARY', 'UNIQUE', 'CHECK' or 'FOREIGN' but found '" + currentToken.getType().toString() + "'.");
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
      error(currentToken, "Expecting 'DOUBLE' or 'LONG' or ' but found '" + currentToken.getType().toString());
    }
    next();
  }


}
