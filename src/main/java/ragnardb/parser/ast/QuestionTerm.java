package ragnardb.parser.ast;

/**
 * Created by klu on 6/22/2015.
 */
public class QuestionTerm implements Term{
  private long intNum;

  public QuestionTerm(){}

  public QuestionTerm(long l){intNum = l;}

  public void setIntNum(int i){intNum = (long)i;}

  public void setIntNum(long i){intNum = i;}

  public long getIntNum(){return intNum;}

  public String toString(){
    return "<Term> ? " + intNum + "\n";
  }

  public void setNegative(boolean isNeg){}
}
