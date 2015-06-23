package ragnardb.parser.ast;

/**
 * Created by klu on 6/22/2015.
 */
public class StringTerm implements Term{
  private String val;

  public StringTerm(){val = "";}

  public StringTerm(String s){val = s;}

  public void setString(String s){val = s;}

  public String getString(){return val;}

  public void setNegative(boolean isNeg){}

  public String toString(){return "<Term> " + val + "\n";}
}
