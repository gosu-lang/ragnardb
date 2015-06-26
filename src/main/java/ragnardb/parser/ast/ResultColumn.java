package ragnardb.parser.ast;

import ragnardb.parser.Token;

import java.util.ArrayList;

/**
 * Created by klu on 6/25/2015.
 */
public class ResultColumn {
  private String result;
  private ArrayList<Expression> resultExpressions;
  /*This is to keep track of which tokens we have to swallow/pass through*/
  private ArrayList<Token> swallowedTokens = new ArrayList<>();

  public ResultColumn(){
    resultExpressions = new ArrayList<>();
    result = "";
  }

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

  public void addToken(Token t){swallowedTokens.add(t);}

  public ArrayList<Token> getSwallowedTokens(){return swallowedTokens;}

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
