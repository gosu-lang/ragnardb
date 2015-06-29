package ragnardb.parser.ast;

/**
 * Created by klu on 6/25/2015.
 */
public class JavaVar {
  private String _varName;
  private String _varType;

  public JavaVar(){}

  public JavaVar(String s){_varName = s;}

  public JavaVar(String s, String t){
    _varName = s;
    _varType = t;
  }

  public String getVarName(){return _varName;}

  public String getVarType(){return _varType;}

  public void setVarName(String s){_varName = s;}

  public void setVarType(String s){_varType = s;}
}
