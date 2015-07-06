package ragnardb.parser.ast;

import ragnardb.parser.Token;
import java.util.ArrayList;

/**
 * Created by klu on 6/22/2015.
 */
public class SelectStatement {
  private boolean isRecursive, isValues;
  private ArrayList<CommonTableExpression> recursiveTables;
  private ArrayList<ResultColumn> results;
  private ArrayList<JoinClause> tables;

  /*This is to keep track of which tokens we have to swallow/pass through*/
  private ArrayList<Token> swallowedTokens = new ArrayList<>();

  private class SelectExpression {
    private Expression containedExpression;
    private String containedType;

    private SelectExpression(Expression e, String s){
      containedExpression = e;
      containedType = s;
    }
  }

  private ArrayList<SelectExpression> expressions;

  public SelectStatement(){
    isRecursive = false;
    isValues = false;
    recursiveTables = new ArrayList<>();
    results = new ArrayList<>();
    expressions = new ArrayList<>();
    tables = new ArrayList<>();
  }

  public SelectStatement(CommonTableExpression cte){
    isRecursive = true;
    recursiveTables = new ArrayList<>();
    recursiveTables.add(cte);
    results = new ArrayList<>();
    expressions = new ArrayList<>();
    tables = new ArrayList<>();
  }

  public SelectStatement(ResultColumn rc){
    recursiveTables = new ArrayList<>();
    results = new ArrayList<>();
    results.add(rc);
    expressions = new ArrayList<>();
    tables = new ArrayList<>();
  }

  public SelectStatement(CommonTableExpression cte, ResultColumn rc){
    isRecursive = true;
    recursiveTables = new ArrayList<>();
    recursiveTables.add(cte);
    results = new ArrayList<>();
    results.add(rc);
    expressions = new ArrayList<>();
    tables = new ArrayList<>();
  }

  public SelectStatement(boolean b, Expression e, String s){
    isValues = b;
    recursiveTables = new ArrayList<>();
    results = new ArrayList<>();
    expressions = new ArrayList<>();
    expressions.add(new SelectExpression(e, s));
    tables = new ArrayList<>();
  }

  public void setValues(boolean b){isValues = b;}

  public void addExpression(Expression e, String s){
    expressions.add(new SelectExpression(e,s));
  }

  public void addCommonTableExpression(CommonTableExpression cte){
    recursiveTables.add(cte);
  }

  public void addResultColumn(ResultColumn rc){
    results.add(rc);
  }

  public ArrayList<ResultColumn> getResultColumns(){return results;}

  public ArrayList<CommonTableExpression> getCommonTableExpressions(){return recursiveTables;}

  public ArrayList<Expression> getExpressionsOfClause(String clause){
    ArrayList<Expression> _expressions = new ArrayList<>();
    for(SelectExpression se: expressions){
      if(se.containedType.equals(clause)){
        _expressions.add(se.containedExpression);
      }
    }
    return _expressions;
  }

  public void addToken(Token t){swallowedTokens.add(t);}

  public ArrayList<Token> getSwallowedTokens(){return swallowedTokens;}

  public void addTable(JoinClause jc){
    tables.add(jc);
  }

  public String toString(){
    StringBuilder sb = new StringBuilder("<Select>\n");
    if(isRecursive){
      sb.append("(RECURSIVE)\n");
      for(CommonTableExpression cte: recursiveTables){
        sb.append(cte.toString("\t"));
      }
      sb.append("(END RECURSIVE)\n");
    }
    if(isValues){
      sb.append("<Values>\n");
      for(SelectExpression se: expressions){
        sb.append("\t" + se.containedType + "\n");
        sb.append(se.containedExpression.toString("\t"));
      }
    } else {
      sb.append("\tSELECT\n");
      for(ResultColumn rc: results){
        sb.append(rc.toString("\t"));
      }
      sb.append("\tFROM\n");
      for(JoinClause jc: tables){
        sb.append(jc.toString("\t"));
      }
      for(SelectExpression se: expressions){
        sb.append("\t" + se.containedType + "\n");
        sb.append(se.containedExpression.toString("\t"));
      }
    }
    return sb.toString();
  }

  protected String toString(String initial){
    StringBuilder sb = new StringBuilder(initial+"<Select>\n");
    if(isRecursive){
      sb.append(initial+"(RECURSIVE)\n");
      for(CommonTableExpression cte: recursiveTables){
        sb.append(cte.toString(initial+"\t"));
      }
      sb.append(initial+"(END RECURSIVE)\n");
    }
    if(isValues){
      sb.append(initial+"<Values>\n");
      for(SelectExpression se: expressions){
        sb.append(initial+"\t" + se.containedType + "\n");
        sb.append(se.containedExpression.toString(initial+"\t"));
      }
    } else {
      sb.append(initial+"\tSELECT\n");
      for(ResultColumn rc: results){
        sb.append(rc.toString(initial+"\t"));
      }
      sb.append(initial+"\tFROM\n");
      for(JoinClause jc: tables){
        sb.append(jc.toString(initial+"\t"));
      }
      for(SelectExpression se: expressions){
        sb.append(initial+"\t" + se.containedType + "\n");
        sb.append(se.containedExpression.toString(initial+"\t"));
      }
    }
    return sb.toString();
  }




}
