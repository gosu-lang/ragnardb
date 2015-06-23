package ragnardb.parser;

import ragnardb.parser.ast.*;

import java.util.ArrayList;

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

  private void specialError(String s) {
    error(currentToken, "Expecting " + s + " but found '" + currentToken.getText() + ".");
  }

  private void pass(TokenType passType) {
    if (passType == currentToken.getType()) {
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

  private boolean matchIn(TokenType...list){
    for(TokenType t: list){
      if(currentToken.getType() == t){
        return true;
      }
    }
    return false;
  }

  private boolean isNum() {
    return (currentToken.getType() == TokenType.LONG || currentToken.getType() == TokenType.DOUBLE);
  }

  private void list(TokenType item) {
    match(item);
    while (tokEquals(TokenType.COMMA)) {
      next();
      match(item);
    }
  }

  private void error(Token token, String message) {
    throw new SQLParseError("[" + token.getLine() + ", " + token.getCol() + "] - ERROR: " + message);
  }

  public void parse() {
    while(true) {
      if (tokEquals(TokenType.EOF)) {
        return; //Success state;
      }
      parseCreateTable();
      if(!(tokEquals(TokenType.EOF)|tokEquals(TokenType.SEMI))){
        error(currentToken, "Expecting 'SEMI' or 'EOF but found " + currentToken.getType().getName());
      }
      if(tokEquals(TokenType.SEMI)){
        match(TokenType.SEMI);
      }
    }
  }

  private void parseSelect() {
    if(tokEquals(TokenType.WITH)){
      next();
      if(tokEquals(TokenType.RECURSIVE)){
        next();
      }
      parseCommonTableExpression();
      while(tokEquals(TokenType.COMMA)){
        next();
        parseCommonTableExpression();
      }
    }
    parseSelectSub();
    while(matchIn(TokenType.UNION, TokenType.INTERSECT, TokenType.EXCEPT)){
      if(tokEquals(TokenType.UNION)){
        next();
        if(tokEquals(TokenType.ALL)){
          next();
        }
      } else {
        next();
      }
      parseSelectSub();
    }
    if(tokEquals(TokenType.ORDER)){
      next();
      match(TokenType.BY);
      parseOrderingTerm();
      while(tokEquals(TokenType.COMMA)){
        next();
        parseOrderingTerm();
      }
    }
    if(tokEquals(TokenType.LIMIT)){
      next();
      match(TokenType.LONG);
      list(TokenType.OFFSET);
      match(TokenType.LONG);

    }
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
    if (tokEquals(TokenType.LPAREN)) {
      next();

      if(tokEquals(TokenType.IDENT)) {
        parseColumnDef();
      } else{
        parseTableConstraint();
      }
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

  }

  private void parseTypeName() {
    match(TokenType.IDENT);
    while (tokEquals(TokenType.IDENT)) {
      next();
    }
    if (tokEquals(TokenType.LPAREN)) {
      next();
      if (currentToken.getType() == TokenType.LONG | currentToken.getType() == TokenType.DOUBLE) {
        next();
        if (tokEquals(TokenType.COMMA)) {
          next();
          if (currentToken.getType() == TokenType.LONG | currentToken.getType() == TokenType.DOUBLE) {
            next();
            match(TokenType.RPAREN);
          } else {
            error(currentToken, "Expecting LONG or DOUBLE but found '" + currentToken.getType() + "'.");
          }
        } else {
          match(TokenType.RPAREN);
        }
      } else {
        error(currentToken, "Expecting LONG or DOUBLE but found '" + currentToken.getType() + "'.");
      }
    }
  }

  private void parseSelectSub() {
    if(tokEquals(TokenType.SELECT)) {
      next();
      if (tokEquals(TokenType.DISTINCT) || tokEquals(TokenType.ALL)) {
        next();
        parseResultColumn();
        while (tokEquals(TokenType.COMMA)) {
          next();
          parseResultColumn();
        }
      }
      if (tokEquals(TokenType.FROM)) {
        next();
        parseTableOrSubquery();
        parseJoinClause();
      }
      if (tokEquals(TokenType.WHERE)) {
        next();
        parseExpr();
      }
      if (tokEquals(TokenType.GROUP)) {
        next();
        match(TokenType.BY);
        parseExpr();
        while (tokEquals(TokenType.COMMA)) {
          next();
          parseExpr();
        }
        if (tokEquals(TokenType.HAVING)) {
          next();
          parseExpr();
        }
      }
    } else if(tokEquals(TokenType.VALUES)){
      next();
      match(TokenType.LPAREN);
      parseExpr();
      while(tokEquals(TokenType.COMMA)){
        next();
        parseExpr();
      }
      match(TokenType.RPAREN);
      while(tokEquals(TokenType.COMMA)){
        next();
        match(TokenType.LPAREN);
        parseExpr();
        while(tokEquals(TokenType.COMMA)){
          next();
          parseExpr();
        }
        match(TokenType.RPAREN);
      }
    } else {
      error(currentToken, "Expecting 'select' or 'values' but found '" + currentToken.getType() + "'.");
    }
  }

  private void parseCommonTableExpression() {
    match(TokenType.IDENT);
    if(tokEquals(TokenType.LPAREN)){
      next();
      list(TokenType.IDENT);
      match(TokenType.RPAREN);
    }
    match(TokenType.AS);
    match(TokenType.LPAREN);
    parseSelect();
    match(TokenType.RPAREN);
  }

  private void parseOrderingTerm() {
    parseExpr();
    if(tokEquals(TokenType.COLLATE)){
      next();
      match(TokenType.IDENT);
    }
    if(matchIn(TokenType.ASC, TokenType.DESC)){
      next();
    }
  }

  private void parseResultColumn() {
    if(tokEquals(TokenType.TIMES)){
      next();
    } else if(tokEquals(TokenType.IDENT)){
      next();
      match(TokenType.DOT);
      match(TokenType.TIMES);
    } else {
      parseExpr();
      if(tokEquals(TokenType.AS)){
        next();
      }
      match(TokenType.IDENT);
    }
  }

  private void parseTableOrSubquery(){
    if(tokEquals(TokenType.LPAREN)){
      if(matchIn(TokenType.WITH, TokenType.RECURSIVE, TokenType.SELECT, TokenType.VALUES)){
        parseSelect();
        match(TokenType.RPAREN);
        if(tokEquals(TokenType.AS)){
          next();
          match(TokenType.IDENT);
        }
      } else {
        parseJoinClause();
        match(TokenType.RPAREN);
      }
    } else if(tokEquals(TokenType.IDENT)){
      next();
      if(tokEquals(TokenType.DOT)){
        next();
        match(TokenType.IDENT);
      }
      if(tokEquals(TokenType.AS)){
        next();
        match(TokenType.IDENT);
      }
      if(tokEquals(TokenType.INDEXED)){
        next();
        match(TokenType.BY);
        match(TokenType.IDENT);
      } else if(tokEquals(TokenType.NOT)){
        next();
        match(TokenType.INDEXED);
      }
    } else {
      error(currentToken, "Expecting Table Name or '(' but found '" + currentToken.getType() + "'.");
    }
  }

  private void parseJoinClause(){
    parseTableOrSubquery();
    while(matchIn(TokenType.COMMA, TokenType.NATURAL, TokenType.LEFT, TokenType.INNER, TokenType.CROSS)) {
      if (tokEquals(TokenType.COMMA)) {
        next();
      } else {
        if (tokEquals(TokenType.NATURAL)) {
          next();
        }
        switch (currentToken.getType()) {
          case LEFT:
            next();
            if (tokEquals(TokenType.OUTER)) {
              next();
            }
            break;
          case INNER:
          case CROSS:
            next();
            break;
          default:
            error(currentToken, "Expection join operation or ',' but found '" + currentToken.getType() + "'.");
            break;
        }
        match(TokenType.JOIN);
      }
      parseTableOrSubquery();
      if(tokEquals(TokenType.ON)){
        next();
        parseExpr();
      } else if(tokEquals(TokenType.USING)){
        next();
        match(TokenType.LPAREN);
        list(TokenType.IDENT);
        match(TokenType.RPAREN);
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

  private boolean isComparator() {
    return tokEquals(TokenType.EQ) ||
      tokEquals(TokenType.NEQ) ||
      tokEquals(TokenType.LT) ||
      tokEquals(TokenType.GT) ||
      tokEquals(TokenType.GTE) ||
      tokEquals(TokenType.LTE) ||
      tokEquals(TokenType.OVL);
  }

  private void parseColumnDef() { //Also does constraint
    match(TokenType.IDENT);
    parseTypeName();
    if (tokEquals(TokenType.DEFAULT)) {
      next();
      if(tokEquals(TokenType.LONG) || tokEquals(TokenType.DOUBLE) || tokEquals(TokenType.IDENT)
        || tokEquals(TokenType.NULL) || tokEquals(TokenType.CURRENT_DATE) || tokEquals(TokenType.CURRENT_TIME)
        || tokEquals(TokenType.CURRENT_TIMESTAMP)){ //Limited parse expression
        next();
      }
    }

    if(tokEquals(TokenType.CONSTRAINT)){
      next();
      match(TokenType.IDENT);
    }
    //TODO: Add 'as' clause when select statements work
    if (tokEquals(TokenType.NOT)) {
      next();
      match(TokenType.NULL);
    } else if (tokEquals(TokenType.NULL)) {
      next();
    }

    if (tokEquals(TokenType.AUTO_INCREMENT) || tokEquals(TokenType.IDENTITY)) {
      next();
      if (tokEquals(TokenType.LPAREN)) {
        next();
        matchNum();
        if (tokEquals(TokenType.COMMA)) {
          next();
          matchNum();
        }
        match(TokenType.RPAREN);
      }
    }
    //Implement Selectivity, Comment?
    if (tokEquals(TokenType.UNIQUE)) {
      next();
    } else if (tokEquals(TokenType.PRIMARY)) {
      next();
      match(TokenType.KEY);
      pass(TokenType.HASH);
    }

    if (tokEquals(TokenType.CHECK)) {
      next();
      parseCondition();
    }
  }

  private void parseConstraint() {
    if (tokEquals(TokenType.CONSTRAINT)) {
      next();
      if (tokEquals(TokenType.IF)) {
        next();
        match(TokenType.NOT);
        match(TokenType.EXISTS);
      }
      match(TokenType.IDENT);
    }
    if (tokEquals(TokenType.CHECK)) {
      next();
      parseExpr();
    } else if (tokEquals(TokenType.UNIQUE)) {
      next();
      match(TokenType.LPAREN);
      match(TokenType.IDENT);
      while (tokEquals(TokenType.COMMA)) {
        next();
        match(TokenType.IDENT);
      }
      match(TokenType.RPAREN);
    } else if (tokEquals(TokenType.PRIMARY)) {
      next();
      match(TokenType.KEY);
      pass(TokenType.HASH);
      match(TokenType.LPAREN);
      match(TokenType.IDENT);
      while (tokEquals(TokenType.COMMA)) {
        next();
        match(TokenType.IDENT);
      }
      match(TokenType.RPAREN);
    } else if (tokEquals(TokenType.FOREIGN)) {
      next();
      match(TokenType.KEY);
      match(TokenType.LPAREN);
      list(TokenType.IDENT);
      match(TokenType.RPAREN);
      match(TokenType.REFERENCES);
      pass(TokenType.IDENT);
      if (tokEquals(TokenType.LPAREN)) {
        next();
        list(TokenType.IDENT);
        match(TokenType.RPAREN);
      }
      if (tokEquals(TokenType.ON)) {
        next();
        if (tokEquals(TokenType.DELETE) || tokEquals(TokenType.UPDATE)) {
          next();
          parseReferentialAction();
        } else {
          error(currentToken, "Expecting DELETE or UPDATE but found '" + currentToken.getText() + ".");
        }
      }
    }
  }

  private void parseReferentialAction() {

    switch (currentToken.getType()) {
      case CASCADE:
        next();
        break;
      case RESTRICT:
        next();
        break;
      case NO:
        next();
        match(TokenType.ACTION);
        break;
      case SET:
        if (tokEquals(TokenType.DEFAULT) || tokEquals(TokenType.NULL)) {
          next();
        } else {
          specialError("DEFAULT or NULL");
        }
        break;
      default:
        specialError("CASCADE or RESTRICT or NO or SET");
    }
  }

  private void parseConflictClause() {
    if (tokEquals(TokenType.ON)) {
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

  private void parseForeignKeyClause() {
    match(TokenType.REFERENCES);
    match(TokenType.IDENT);
    if (tokEquals(TokenType.LPAREN)) {
      next();
      match(TokenType.IDENT);
      while (tokEquals(TokenType.COMMA)) {
        next();
        match(TokenType.IDENT);
      }
      match(TokenType.RPAREN);
    }
    while (tokEquals(TokenType.ON) || tokEquals(TokenType.MATCH)) {
      if (tokEquals(TokenType.ON)) {
        next();
        if (!(tokEquals(TokenType.DELETE) || tokEquals(TokenType.UPDATE))) {
          error(currentToken, "Expecting Update/Delete but found '" + currentToken.getText() + "'.");
        }
        next();
        if (tokEquals(TokenType.SET)) {
          next();
          if (!(tokEquals(TokenType.NULL) || tokEquals(TokenType.DEFAULT))) {
            error(currentToken, "Expecting NULL/DEFAULT but found '" + currentToken.getText() + "'.");
          }
          next();
        } else if (tokEquals(TokenType.CASCADE) || tokEquals(TokenType.RESTRICT)) {
          next();
        } else if (tokEquals(TokenType.NO)) {
          next();
          match(TokenType.ACTION);
        } else {
          error(currentToken, "Expecting foreign key value but found '" + currentToken.getText() + "'.");
        }
      } else if (tokEquals(TokenType.MATCH)) {
        next();
        match(TokenType.IDENT);
      } else {
        error(currentToken, "Expecting ON/MATCH but found '" + currentToken.getText() + "'.");
      }

    }
    //I checked and am pretty certain that there are no contexts in which 'not' would be appropriate
    //Other than for this clause
    if (tokEquals(TokenType.NOT)) {
      next();
      if (!tokEquals(TokenType.DEFERRABLE)) {
        error(currentToken, "Expecting DEFERRABLE but found '" + currentToken.getText() + "'.");
      }
    }

    if (tokEquals(TokenType.DEFERRABLE)) {
      next();
      if (tokEquals(TokenType.INITIALLY)) {
        next();
        if (tokEquals(TokenType.DEFERRED) || tokEquals(TokenType.IMMEDIATE)) {
          next();
        } else {
          error(currentToken, "Expecting DEFERRED/IMMEDIATE but found '" + currentToken.getText() + "'.");
        }
      }
    }
  }

  private void parseTableConstraint() {
    if (currentToken.getType() == TokenType.CONSTRAINT) {
      next();
      match(TokenType.IDENT);
    }
    switch (currentToken.getType()) {
      case PRIMARY:
        next();
        match(TokenType.KEY);
        match(TokenType.LPAREN);
        parseIndexedColumn();
        while (currentToken.getType() == TokenType.COMMA) {
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
        while (currentToken.getType() == TokenType.COMMA) {
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
        while (currentToken.getType() == TokenType.COMMA) {
          next();
          match(TokenType.IDENT);
        }
        match(TokenType.RPAREN);
        parseForeignKeyClause();
        break;
      default:
        error(currentToken, "Expecting 'CONSTRAINT', 'PRIMARY', 'UNIQUE', 'CHECK' or 'FOREIGN' but found '"
          + currentToken.getType().toString() + "'.");
        break;
    }
  }

  private void parseIndexedColumn() {
    match(TokenType.IDENT);
    if (currentToken.getType() == TokenType.COLLATE) {
      next();
      match(TokenType.IDENT);
    }
    if (currentToken.getType() == TokenType.ASC || currentToken.getType() == TokenType.DESC) {
      next();
    }
  }


  private void parseRaiseFunction() {
    match(TokenType.RAISE);
    match(TokenType.LPAREN);
    switch (currentToken.getType()) {
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

  private Expression parseExpr() {
    Expression expression;
    AndCondition condition = parseAndCondition();
    expression = new Expression(condition);
    while (tokEquals(TokenType.OR)) {
      next();
      condition = parseAndCondition();
      expression.addCondition(condition);
    }
    return expression;
  }

  private AndCondition parseAndCondition() {
    AndCondition andCondition;
    Condition c = parseCondition();
    andCondition = new AndCondition(c);
    while (tokEquals(TokenType.AND)) {
      next();
      c = parseCondition();
      andCondition.addCondition(c);
    }
    return andCondition;
  }

  private Condition parseCondition() {
    Condition condition;
    if (tokEquals(TokenType.NOT)) {
      next();
      return parseCondition();
    }
    if(tokEquals(TokenType.EXISTS)){
      next();
      match(TokenType.LPAREN);
      parseSelect();
      condition = null; //TODO: Deal with this BS too
      match(TokenType.RPAREN);
    } else {
      Operand o = parseOperand();
      condition = new Condition(o);
      if (tokEquals(TokenType.IS) || tokEquals(TokenType.BETWEEN) || tokEquals(TokenType.IN) || tokEquals(TokenType.NOT)
        || tokEquals(TokenType.LIKE) || tokEquals(TokenType.REGEXP) || isComparator()) {
        parseConditionRHS(condition);
      }
    }
    return condition;
  }

  private void parseConditionRHS(Condition existing) {

    if (isComparator()) {
      next();
      if (tokEquals(TokenType.ALL) || tokEquals(TokenType.ANY) || tokEquals(TokenType.SOME)) {
        next();
        match(TokenType.LPAREN);
        parseSelect();
        //TODO: fix this
        match(TokenType.RPAREN);
      } else {
        Operand o = parseOperand();
        existing.setSecond(o);
      }
    } else {
      switch (currentToken.getType()) {
        case IS:
          next();
          if (tokEquals(TokenType.NOT)) {
            next();
          }
          if (tokEquals(TokenType.NULL)) {
            next();
          } else {
            match(TokenType.DISTINCT);
            match(TokenType.FROM);
            Operand o = parseOperand();
            existing.setSecond(o);
          }
          break;
        case BETWEEN:
          next();
          Operand o = parseOperand();
          existing.setSecond(o);
          match(TokenType.AND);
          break;
        case IN:
          next();
          match(TokenType.LPAREN);
          if (tokEquals(TokenType.WITH) || tokEquals(TokenType.RECURSIVE) || tokEquals(TokenType.SELECT)
            || tokEquals(TokenType.VALUES)) {
            parseSelect();
            //TODO: FIX
          } else {
            parseExpr();
            //TODO: FIX
            while (tokEquals(TokenType.COMMA)) {
              next();
              parseExpr();
              //TODO: FIX
            }
          }
          match(TokenType.RPAREN);
          break;
        case NOT:
          next();
        case LIKE:
          next();
          o = parseOperand();
          existing.setSecond(o);
          if (tokEquals(TokenType.ESCAPE)) {
            next();
            match(TokenType.IDENT);
          }
          break;
        case REGEXP:
          next();
          o = parseOperand();
          existing.setSecond(o);
          break;
        default:
          specialError("Comparator, IS, BETWEEN, IN, NOT, LIKE, or REGEXP");
          break;
      }
    }
  }

  private Operand parseOperand() {
    Operand operand;
    Summand s = parseSummand();
    operand = new Operand(s);
    while (tokEquals(TokenType.BAR)) {
      next();
      s = parseSummand();
      operand.addSummand(s);
    }
    return operand;
  }

  private Summand parseSummand() {
    Summand summand;
    Factor f = parseFactor();
    summand = new Summand(f);
    while (tokEquals(TokenType.PLUS) || tokEquals(TokenType.MINUS)) {
      String nextOp = currentToken.toString();
      next();
      f = parseFactor();
      summand.add(nextOp, f);
    }
    return summand;
  }

  private Factor parseFactor() {
    Factor factor;
    Term t = parseTerm();
    factor = new Factor(t);
    while (tokEquals(TokenType.SLASH) || tokEquals(TokenType.TIMES) || tokEquals(TokenType.MOD)) {
      String nextOp = currentToken.toString();
      next();
      t = parseTerm();
      factor.add(nextOp, t);
    }
    return factor;
  }

  private Term parseTerm() {
    Term t = null;
    switch (currentToken.getType()) {
      case IDENT:
        t = new StringTerm(currentToken.getText());
        next();
        break;
      case LONG:
      case DOUBLE:
        t = currentToken.getLongNumber()!=0?new AlgebraicTerm(currentToken.getLongNumber())
          :new AlgebraicTerm(currentToken.getDoubleNumber());
        next();
        break;
      case QUESTION:
        next();
        long l;
        if(tokEquals(TokenType.MINUS)){
          l = -1*currentToken.getLongNumber();
          next();
          t = new QuestionTerm(l);
        } else if(tokEquals(TokenType.PLUS)){
          next();
          t = new QuestionTerm(currentToken.getLongNumber());
          next();
        } else if(tokEquals(TokenType.LONG)){
          t = new QuestionTerm(currentToken.getLongNumber());
          next();
        } else {
          t = new QuestionTerm();
        }
        break;
      case PLUS:
        next();
        t = parseLimitedTerm(false);
        break;
      case MINUS:
        next();
        t = parseLimitedTerm(true);
        break;
      case LPAREN:
        next();
        if (tokEquals(TokenType.WITH) || tokEquals(TokenType.RECURSIVE) || tokEquals(TokenType.SELECT)
          || tokEquals(TokenType.VALUES)) {
          parseSelect();
          t = new GeneralTerm(new SelectStatement()); //TODO: Fix this BS
        } else {
          Expression e = parseExpr();
          t = new GeneralTerm(e);
        }
        match(TokenType.RPAREN);
        break;
      case CASE:
        next();
        Case c;
        if (tokEquals(TokenType.WHEN)) {
          next();
          c = parseCaseWhen();
        } else {
          c = parseCase();
        }
        t = new CaseTerm(c);
        break;
      default:
        specialError("Any term");
        break;
    }
    return t;
  }

  private Term parseLimitedTerm(boolean isNegative) {
    Term t = null;
    if(tokEquals(TokenType.LONG)){
      t = new AlgebraicTerm(currentToken.getLongNumber());
      next();
    }else if(tokEquals(TokenType.DOUBLE)){
      t = new AlgebraicTerm(currentToken.getDoubleNumber());
      next();
    }else if(tokEquals(TokenType.LPAREN)){
      Expression e = parseExpr();
      t = new GeneralTerm(e);
    }else{
      specialError("Algebraic Term");
      return null;
    }
    t.setNegative(isNegative);
    return t;
  }

  private Case parseCase() {
    Case _case = new Case();
    Expression init = parseExpr();
    _case.setInitial(init);
    parseCaseWhen(_case);
    return _case;
  }

  private Case parseCaseWhen() {
    Case _case = new Case();
    match(TokenType.WHEN);
    Expression when = parseExpr();
    match(TokenType.THEN);
    Expression then = parseExpr();
    _case.addWhenThen(when, then);
    if (tokEquals(TokenType.WHEN)) {
      parseCaseWhen(_case);
    } else {
      if (tokEquals(TokenType.ELSE)) {
        next();
        Expression fin = parseExpr();
        _case.setElse(fin);
      }
      match(TokenType.END);
    }
    return _case;
  }

  private void parseCaseWhen(Case _case) {
    match(TokenType.WHEN);
    Expression when = parseExpr();
    match(TokenType.THEN);
    Expression then = parseExpr();
    _case.addWhenThen(when, then);
    if (tokEquals(TokenType.WHEN)) {
      parseCaseWhen(_case);
    } else {
      if (tokEquals(TokenType.ELSE)) {
        next();
        Expression fin = parseExpr();
        _case.setElse(fin);
      }
      match(TokenType.END);
    }
  }
}
