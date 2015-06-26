package ragnardb.parser.ast;

import ragnardb.parser.Token;

import java.util.ArrayList;

/**
 * Created by klu on 6/22/2015.
 */
public abstract class Term {

  private ArrayList<Token> swallowedTokens = new ArrayList<>();

  @Override
  public abstract String toString();

  public abstract void setNegative(boolean isNeg);

  protected abstract String toString(String initial);

  public String getName(){return "This is not a variable :(";}

  public String getType(){return "This is not a variable :(";}

  public void addToken(Token t){swallowedTokens.add(t);}

  public ArrayList<Token> getSwallowedTokens(){return swallowedTokens;}
}
