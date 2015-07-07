package ragnardb.parser.ast;

import java.util.ArrayList;

/**
 * Created by klu on 7/6/2015.
 */
public class TableOrSubquery {
  private String name;
  private String alias;
  private String index;
  private SelectStatement subquery;
  private ArrayList<JoinClause> jcs;

  public TableOrSubquery(String n){
    name = n;
    alias = null;
    subquery = null;
    jcs = null;
  }

  public TableOrSubquery(String n, String a){
    name = n;
    alias = a;
    subquery = null;
    jcs = null;
  }

  public TableOrSubquery(SelectStatement s){
    subquery = s;
    jcs = null;
    name = null;
    index = null;
  }

  public TableOrSubquery(JoinClause jc){
    subquery = null;
    name = null;
    index = null;
    alias = null;
    jcs = new ArrayList<>();
    jcs.add(jc);
  }

  public String getName(){
    return name;
  }

  public boolean hasAlias(){
    return alias!=null;
  }

  public void setIndex(String i){
    index = i;
  }

  public void addJoinClause(JoinClause jc){
    jcs.add(jc);
  }

  public void setAlias(String s){
    alias = s;
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder("<Table-Or-Subquery>\n");
    if(subquery == null && jcs == null){
      sb.append("\t"+name);
      if(alias != null){
        sb.append(" AS " + alias);
      }
      if(index != null){
        sb.append(" INDEXED BY " + index + "\n");
      }
      sb.append('\n');
      return sb.toString();
    } else if(jcs == null){
      return "<Table-Or-Subquery>" + subquery.toString();
    } else{
      for(JoinClause jc : jcs){
        sb.append(jc);
      }
      return sb.toString();
    }
  }

  protected String toString(String initial){
    StringBuilder sb = new StringBuilder(initial + "<Table-Or-Subquery>\n");
    if(subquery == null && jcs == null){
      sb.append(initial+"\t"+name);
      if(alias != null){
        sb.append(" AS " + alias);
      }
      if(index != null){
        sb.append(" INDEXED BY " + index + "\n");
      }
      sb.append('\n');
      return sb.toString();
    } else if(jcs == null){
      return initial + "\t<Table-Or-Subquery>\n" + subquery.toString(initial+"\t");
    } else{
      for(JoinClause jc : jcs){
        sb.append(jc.toString(initial+"\t"));
      }
      return sb.toString();
    }
  }
}
