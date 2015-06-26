package ragnardb.parser.ast;

import java.util.ArrayList;

/**
 * Created by klu on 6/25/2015.
 */
public class ResultColumn {
  private String result;
  private ArrayList<Expression> resultExpressions;

  public ResultColumn(String s){
    result = s;
    resultExpressions = null;
  }

  public ResultColumn(Expression e){
    result = null;
    resultExpressions = new ArrayList<>();
    resultExpressions.add(e);
  }

  public void addExpression(Expression e){
    resultExpressions.add(e);
  }

  public ArrayList<Expression> getResultExpressions(){return resultExpressions;}

  public String toString(){
    if(resultExpressions == null){
      return result+ "\n";
    } else {
      StringBuilder sb = new StringBuilder();
      for(Expression e: resultExpressions){
        sb.append(e);
      }
      return sb.toString();
    }
  }

  protected String toString(String initial){
    if(resultExpressions == null){
      return initial+result+"\n";
    } else {
      StringBuilder sb = new StringBuilder();
      for(Expression e: resultExpressions){
        sb.append(e.toString(initial));
      }
      return sb.toString();
    }
  }

}
