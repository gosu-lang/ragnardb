package ragnardb.parser;

import ragnardb.parser.ast.*;
import ragnardb.plugin.ColumnDefinition;
import ragnardb.utils.NounHandler;

import java.sql.Types;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  private String match(TokenType expectedType) {
    TokenType currentType = currentToken.getType();
    String s;
    if (currentType == expectedType) {
      if(currentType == TokenType.IDENT){
        s = currentToken.getText();
      } else {
        s = currentToken.toString();
      }
      next();
    } else {
      String name = currentType.getName();
      if (currentType == TokenType.IDENT) {
        name = currentToken.getText();
      }
      error(currentToken, "Expecting '" + expectedType.getName() + "' but found '" + name + "'.");
      return name;
    }
    return s;
  }

  private void matchNum() {
    TokenType currentType = currentToken.getType();
    if (currentType == TokenType.LONG || currentType == TokenType.INTERNALDOUBLE) {
      next();
    } else {
      String name = currentType.getName();
      error(currentToken, "Expecting 'LONG' or 'INTERNALDOUBLE' but found '" + name + "'.");
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
    return (currentToken.getType() == TokenType.LONG || currentToken.getType() == TokenType.INTERNALDOUBLE);
  }

  private ArrayList<String> list(TokenType item) {
    ArrayList<String> items = new ArrayList<String>();
    String _item = match(item);
    items.add(_item);
    while (tokEquals(TokenType.COMMA)) {
      next();
      _item = match(item);
      items.add(_item);
    }
    return items;
  }

  private void error(Token token, String message) {
    throw new SQLParseError("[" + token.getLine() + ", " + token.getCol() + "] - ERROR: " + message);
  }

  public DDL parse() {
    if(tokEquals(TokenType.CREATE) || tokEquals(TokenType.EOF)) {
      DDL statements = new DDL();
      while (true) {
        if (tokEquals(TokenType.EOF)) {

          return statements;
        }
        statements.append(parseCreateTable());
        if (!(tokEquals(TokenType.EOF) | tokEquals(TokenType.SEMI))) {
          error(currentToken, "Expecting 'SEMI' or 'EOF but found " + currentToken.getType().getName());
        }
        if (tokEquals(TokenType.SEMI)) {
          match(TokenType.SEMI);
        }
      }
    } else { //for now we will treat everything else as a select statement
      parseSelect();
      return null;
    }
  }

  private SelectStatement parseSelect() {
    SelectStatement _ss = new SelectStatement();
    if (tokEquals(TokenType.WITH)) {
      _ss.addToken(currentToken);
      next();
      if (tokEquals(TokenType.RECURSIVE)) {
        _ss.addToken(currentToken);
        next();
      }
      CommonTableExpression cte = parseCommonTableExpression();
      _ss.addCommonTableExpression(cte);
      while (tokEquals(TokenType.COMMA)) {
        next();
        cte = parseCommonTableExpression();
        _ss.addCommonTableExpression(cte);
      }
    }
    if(_ss == null){
      _ss = new SelectStatement();
    }
    parseSelectSub(_ss);
    while (matchIn(TokenType.UNION, TokenType.INTERSECT, TokenType.EXCEPT)) {
      if (tokEquals(TokenType.UNION)) {
        _ss.addToken(currentToken);
        next();
        if (tokEquals(TokenType.ALL)) {
          _ss.addToken(currentToken);
          next();
        }
      } else {
        _ss.addToken(currentToken);
        next();
      }
      parseSelectSub(_ss);
    }
    if (tokEquals(TokenType.ORDER)) {
      _ss.addToken(currentToken);
      next();
      _ss.addToken(currentToken);
      match(TokenType.BY);
      parseOrderingTerm(_ss);
      while (tokEquals(TokenType.COMMA)) {
        next();
        parseOrderingTerm(_ss);
      }
    }
    if (tokEquals(TokenType.LIMIT)) {
      _ss.addToken(currentToken);
      next();
      match(TokenType.LONG);
      if(tokEquals(TokenType.OFFSET) || tokEquals(TokenType.COMMA)){
        next();
        match(TokenType.LONG);
      }
    }
    return _ss;
  }
  private CreateTable parseCreateTable() {
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

    CreateTable table = new CreateTable(currentToken.getText());
    match(TokenType.IDENT);

    if (currentToken.getType() == TokenType.DOT) {
      next();
      match(TokenType.IDENT);
    }
    if (tokEquals(TokenType.LPAREN)) {
      next();

      if(tokEquals(TokenType.IDENT)) {
        table.append(parseColumnDef());
      } else{
        table.append(parseConstraint());

      }
      while (currentToken.getType() == TokenType.COMMA) {
        next();
        if (tokEquals(TokenType.IDENT)) {
          table.append(parseColumnDef());
        } else {
          table.append(parseConstraint());
        }
      }
      match(TokenType.RPAREN);
    }
    if (tokEquals(TokenType.WITHOUT)) {
      next();
      match(TokenType.ROWID);
    }
    return table;

  }

  private ColumnDefinition parseTypeName(String name) {

    int datatype;

    if(currentToken.getType()!=TokenType.IDENT){
      error(currentToken, "Expecting IDENT (datatype) but found '" + currentToken.getType() + "'.");
    }

    datatype = ColumnDefinition.lookUp.get(currentToken.getText());
    ColumnDefinition column = new ColumnDefinition(name,datatype);
    next();

    if(tokEquals(TokenType.IDENT) && "precision".equals(currentToken.getText())){
      next();
    }

    if((datatype == Types.NVARCHAR || datatype == Types.NCHAR || datatype == Types.BLOB || datatype == Types.CLOB || datatype == Types.DECIMAL )
            && tokEquals(TokenType.LPAREN)){
      next();
      column.setStartInt( (int) currentToken.getLongNumber());
      matchNum();
      if( datatype == Types.DECIMAL){
        if(tokEquals(TokenType.COMMA)){
          next();
          column.setIncrementInt((int) currentToken.getLongNumber());
          matchNum();
        }
      }
      match(TokenType.RPAREN);
    }
    return column;

  }

  private void parseSelectSub(SelectStatement current) {
    if(tokEquals(TokenType.SELECT)) {
      current.addToken(currentToken);
      next();
      current.setValues(false);
      if (tokEquals(TokenType.DISTINCT) || tokEquals(TokenType.ALL)) {
        current.addToken(currentToken);
        next();
      }
      ResultColumn rc = parseResultColumn(current);
      current.addResultColumn(rc);
      while (tokEquals(TokenType.COMMA)) {
        next();
        rc = parseResultColumn(current);
        current.addResultColumn(rc);
      }
      parseSelectSub2(current);
    } else if(tokEquals(TokenType.VALUES)){
      current.addToken(currentToken);
      next();
      current.setValues(true);
      match(TokenType.LPAREN);
      Expression e = parseExpr();
      current.addExpression(e, "VALUES");
      while(tokEquals(TokenType.COMMA)){
        next();
        e = parseExpr();
        current.addExpression(e, "VALUES");
      }
      match(TokenType.RPAREN);
      while(tokEquals(TokenType.COMMA)){
        next();
        match(TokenType.LPAREN);
        e = parseExpr();
        current.addExpression(e, "VALUES");
        while(tokEquals(TokenType.COMMA)){
          next();
          e = parseExpr();
          current.addExpression(e, "VALUES");
        }
        match(TokenType.RPAREN);
      }
    } else {
      error(currentToken, "Expecting 'select' or 'values' but found '" + currentToken.getType() + "'.");
    }
  }

  private void parseSelectSub2(SelectStatement current){
    if (tokEquals(TokenType.FROM)) {
      current.addToken(currentToken);
      next();
      parseJoinClause();
    }
    if (tokEquals(TokenType.WHERE)) {
      current.addToken(currentToken);
      next();
      Expression e = parseExpr();
      current.addExpression(e,"WHERE");
    }
    if (tokEquals(TokenType.GROUP)) {
      current.addToken(currentToken);
      next();
      current.addToken(currentToken);
      match(TokenType.BY);
      Expression e = parseExpr();
      current.addExpression(e, "GROUP BY");
      while (tokEquals(TokenType.COMMA)) {
        next();
        e = parseExpr();
        current.addExpression(e, "GROUP BY");
      }
      if (tokEquals(TokenType.HAVING)) {
        current.addToken(currentToken);
        next();
        e = parseExpr();
        current.addExpression(e, "HAVING");
      }
    }
  }

  private CommonTableExpression parseCommonTableExpression() {
    CommonTableExpression _cte;
    String s = match(TokenType.IDENT);
    _cte = new CommonTableExpression(s);
    if(tokEquals(TokenType.LPAREN)){
      _cte.addToken(currentToken);
      next();
      ArrayList<String> sx = list(TokenType.IDENT);
      for(String ss: sx){
        _cte.addColumn(ss);
      }
      _cte.addToken(currentToken);
      match(TokenType.RPAREN);
    }
    match(TokenType.AS);
    match(TokenType.LPAREN);
    _cte.addToken(currentToken);
    SelectStatement _select = parseSelect();
    _cte.setSelect(_select);
    _cte.addToken(currentToken);
    match(TokenType.RPAREN);
    return _cte;
  }

  private void parseOrderingTerm(SelectStatement current) {
    Expression er = parseExpr();
    current.addExpression(er, "ORDER BY");
    if(tokEquals(TokenType.COLLATE)){
      current.addToken(currentToken);
      next();
      match(TokenType.IDENT);
    }
    if(matchIn(TokenType.ASC, TokenType.DESC)){
      current.addToken(currentToken);
      next();
    }
  }

  private ResultColumn parseResultColumn(SelectStatement current) {
    ResultColumn _rc = null;
    if(tokEquals(TokenType.TIMES)){
      next();
    } else if(tokEquals(TokenType.IDENT)){
      String tempname = match(TokenType.IDENT);
      if(tokEquals(TokenType.DOT)){
        next();
        if(tokEquals(TokenType.TIMES)){
          next();
          _rc = new ResultColumn(tempname+".*");
        } else if(tokEquals(TokenType.IDENT)){
          _rc = new ResultColumn(tempname+"."+currentToken.getText());
          next();
        } else {
          error(currentToken, "Expecting tablename.(columnname) but found '" + currentToken.getType() + "'.");
        }
      } else if(tokEquals(TokenType.FROM)){
        _rc = new ResultColumn(tempname);
        parseSelectSub2(current);
      } else {
        error(currentToken, "Expecting tablename.* or column but found '" + currentToken.getType() + "'.");
      }
    } else if(tokEquals(TokenType.LPAREN)){
      _rc = new ResultColumn();
      _rc.addToken(currentToken);
      next();
      Expression e = parseExpr();
      _rc.addExpression(e);
      while(tokEquals(TokenType.COMMA)){
        next();
        e = parseExpr();
        _rc.addExpression(e);
      }
      _rc.addToken(currentToken);
      match(TokenType.RPAREN);
    } else {
      Expression e = parseExpr();
      _rc = new ResultColumn(e);
      if(tokEquals(TokenType.AS)){
        next();
      }
      match(TokenType.IDENT);
    }
    return _rc;
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
            error(currentToken, "Expecting join operation or ',' but found '" + currentToken.getType() + "'.");
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

  private boolean isComparator() {
    return tokEquals(TokenType.EQ) ||
      tokEquals(TokenType.NEQ) ||
      tokEquals(TokenType.LT) ||
      tokEquals(TokenType.GT) ||
      tokEquals(TokenType.GTE) ||
      tokEquals(TokenType.LTE) ||
      tokEquals(TokenType.OVL);
  }

  private String toCamelCase(String str){
    Pattern p = Pattern.compile("_(.)");
    Matcher m = p.matcher(str);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(sb, m.group(1).toUpperCase());
    }
    m.appendTail(sb);
    return sb.toString();
  }

  private ColumnDefinition parseColumnDef() {
    String typeName = currentToken.getCasedText();
    typeName = toCamelCase(typeName);
    match(TokenType.IDENT);
    ColumnDefinition column = parseTypeName(typeName);
    if (tokEquals(TokenType.DEFAULT)) {
      next();
      if(tokEquals(TokenType.LONG) || tokEquals(TokenType.INTERNALDOUBLE) || tokEquals(TokenType.IDENT)
        || tokEquals(TokenType.NULL) || tokEquals(TokenType.CURRENT_DATE) || tokEquals(TokenType.CURRENT_TIME)
        || tokEquals(TokenType.CURRENT_TIMESTAMP)){ //Limited parse expressions
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
      column.setNotNull(true);
    } else if (tokEquals(TokenType.NULL)) {
      next();
      column.setNull(true);
    }

    if (tokEquals(TokenType.AUTO_INCREMENT) || tokEquals(TokenType.IDENTITY)) {
      if(tokEquals(TokenType.AUTO_INCREMENT)){
        column.setAutoIncrement(true);
      }
      else{
        column.setIdentity(true);
      }
      next();
      if (tokEquals(TokenType.LPAREN)) {
        next();
        column.setStartInt((int) currentToken.getLongNumber());
        matchNum();

        if (tokEquals(TokenType.COMMA)) {
          next();
          column.setIncrementInt((int) currentToken.getLongNumber());
          matchNum();
        }
        match(TokenType.RPAREN);
      }
    }
    //Implement Selectivity, Comment?
    if (tokEquals(TokenType.UNIQUE)) {
      column.setUnique(true);
      next();
    } else if (tokEquals(TokenType.PRIMARY)) {
      column.setPrimaryKey(true);
      next();
      match(TokenType.KEY);
      if(tokEquals(TokenType.HASH)){
        next();
        column.setHash(true);
      }
    }

    if (tokEquals(TokenType.CHECK)) {
      next();
      parseCondition();
    }
    return column;
  }

  private Constraint parseConstraint() {
    Constraint _constraint = new Constraint();
    String s = "";
    if (tokEquals(TokenType.CONSTRAINT)) {
      next();
      if (tokEquals(TokenType.IF)) {
        next();
        match(TokenType.NOT);
        match(TokenType.EXISTS);
      }
      _constraint.setName(match(TokenType.IDENT));
    }
    if (tokEquals(TokenType.CHECK)) {
      next();
      _constraint.setType(Constraint.constraintType.CHECK);
      Expression e = parseExpr();
      //_constraint.setExpression(e);
    } else if (tokEquals(TokenType.UNIQUE)) {
      String t;
      next();
      match(TokenType.LPAREN);
      t = match(TokenType.IDENT);
      _constraint.appendColumn(t);
      _constraint.setType(Constraint.constraintType.UNIQUE);
      while (tokEquals(TokenType.COMMA)) {
        next();
        t = match(TokenType.IDENT);
        _constraint.appendColumn(t);
      }
      match(TokenType.RPAREN);
    } else if (tokEquals(TokenType.PRIMARY)) {
      next();
      match(TokenType.KEY);
      if(tokEquals(TokenType.HASH)){
        next();
        _constraint.setType(Constraint.constraintType.PRIMARYHASH);
      }
      else{
        _constraint.setType(Constraint.constraintType.PRIMARY);
      }
      match(TokenType.LPAREN);
      String t = match(TokenType.IDENT);
      _constraint.appendColumn(t);

      while (tokEquals(TokenType.COMMA)) {
        next();
        t = match(TokenType.IDENT);
        _constraint.appendColumn(t);
      }
      match(TokenType.RPAREN);
    } else if (tokEquals(TokenType.FOREIGN)) {
      _constraint.setType(Constraint.constraintType.FOREIGN);
      next();
      match(TokenType.KEY);
      match(TokenType.LPAREN);
      ArrayList<String> fKs = list(TokenType.IDENT);
      _constraint.setColumnNames(fKs);
      match(TokenType.RPAREN);
      match(TokenType.REFERENCES);
      if(tokEquals(TokenType.IDENT)){
        _constraint.setReferentialName(currentToken.getText());
        next();
      }
      if (tokEquals(TokenType.LPAREN)) {
        next();
        ArrayList<String> refs = list(TokenType.IDENT);
        for(String r: refs){
          _constraint.appendReferentialColumn(r);
        }
        match(TokenType.RPAREN);
      }
      while (tokEquals(TokenType.ON)) {
        next();
        if (tokEquals(TokenType.DELETE) || tokEquals(TokenType.UPDATE)) {
          next();
          parseReferentialAction();
        } else {
          error(currentToken, "Expecting DELETE or UPDATE but found '" + currentToken.getText() + ".");
        }
      }
    }
    return _constraint;
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
        next();
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

  public Expression parseExpr() {
    Expression expression;
    AndCondition condition = parseAndCondition();
    expression = new Expression(condition);
    while (tokEquals(TokenType.OR)) {
      expression.addToken(currentToken);
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
      andCondition.addToken(currentToken);
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
      condition = new Condition();
      condition.addToken(currentToken);
      next();
      condition.addToken(currentToken);
      match(TokenType.LPAREN);
      SelectStatement ss = parseSelect();
      condition.setFirst(new Operand(new Summand(new Factor(new GeneralTerm(ss)))));
      condition.addToken(currentToken);
      match(TokenType.RPAREN);
    } else {
      Operand o = parseOperand();
      condition = new Condition(o);
      if (tokEquals(TokenType.IS) || tokEquals(TokenType.BETWEEN) || tokEquals(TokenType.IN) || tokEquals(TokenType.NOT)
        || tokEquals(TokenType.LIKE) || tokEquals(TokenType.REGEXP) || isComparator()) {
        condition.addToken(currentToken);
        parseConditionRHS(condition);
      }
    }
    return condition;
  }

  private void parseConditionRHS(Condition existing) {

    if (isComparator()) {
      next();
      if (tokEquals(TokenType.ALL) || tokEquals(TokenType.ANY) || tokEquals(TokenType.SOME)) {
        existing.addToken(currentToken);
        next();
        match(TokenType.LPAREN);
        SelectStatement ss = parseSelect();
        Operand o = new Operand(new Summand(new Factor(new GeneralTerm(ss))));
        existing.setSecond(o);
        existing.addToken(currentToken);
        match(TokenType.RPAREN);
      } else {
        Operand o = parseOperand();
        existing.setSecond(o);
      }
    } else {
      switch (currentToken.getType()) {
        case IS:
          existing.addToken(currentToken);
          next();
          if (tokEquals(TokenType.NOT)) {
            existing.addToken(currentToken);
            next();
          }
          if (tokEquals(TokenType.NULL)) {
            existing.addToken(currentToken);
            next();
          } else {
            existing.addToken(currentToken);
            match(TokenType.DISTINCT);
            existing.addToken(currentToken);
            match(TokenType.FROM);
            Operand o = parseOperand();
            existing.setSecond(o);
          }
          break;
        case BETWEEN:
          existing.addToken(currentToken);
          next();
          Operand o = parseOperand();
          existing.setSecond(o);
          match(TokenType.AND);
          break;
        case IN:
          existing.addToken(currentToken);
          next();
          existing.addToken(currentToken);
          match(TokenType.LPAREN);
          if (tokEquals(TokenType.WITH) || tokEquals(TokenType.RECURSIVE) || tokEquals(TokenType.SELECT)
            || tokEquals(TokenType.VALUES)) {
            SelectStatement ss = parseSelect();
            existing.setSecond(new Operand(new Summand(new Factor(new GeneralTerm(ss)))));
          } else {
            Expression e = parseExpr();
            existing.setSecond(new Operand(new Summand(new Factor(new GeneralTerm(e)))));
            while (tokEquals(TokenType.COMMA)) {
              next();
              parseExpr();
              //TODO: FIX
            }
          }
          existing.addToken(currentToken);
          match(TokenType.RPAREN);
          break;
        case NOT:
          existing.addToken(currentToken);
          next();
        case LIKE:
          existing.addToken(currentToken);
          next();
          o = parseOperand();
          existing.setSecond(o);
          if (tokEquals(TokenType.ESCAPE)) {
            existing.addToken(currentToken);
            next();
            match(TokenType.IDENT);
          }
          break;
        case REGEXP:
          existing.addToken(currentToken);
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
      operand.addToken(currentToken);
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
        String x = match(TokenType.IDENT);
        if(tokEquals(TokenType.DOT)){
          next();
          x += ".";
          x += match(TokenType.IDENT);
        }
        t = new StringTerm(x);
        break;
      case AT:
        next();
        String name = match(TokenType.IDENT);
        JavaVar variable = new JavaVar(name);
        if(tokEquals(TokenType.COLON)){
          next();
          String _type = match(TokenType.IDENT);
          while(tokEquals(TokenType.DOT)){
            _type += '.';
            next();
            _type += match(TokenType.IDENT);
          }
          variable.setVarType(_type);
        }
        t = new VariableTerm(variable);
        break;
      case LONG:
      case INTERNALDOUBLE:
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
    }else if(tokEquals(TokenType.INTERNALDOUBLE)){
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
