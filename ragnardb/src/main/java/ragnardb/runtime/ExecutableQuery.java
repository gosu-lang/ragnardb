package ragnardb.runtime;

import gw.lang.reflect.IType;
import gw.util.GosuExceptionUtil;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by klu on 7/8/2015.
 */
public class ExecutableQuery<T> extends SQLQuery<T>{
  private String statement;

  public ExecutableQuery(ITypeToSQLMetadata md, IType rootType, String s){
    super(md, rootType);
    statement = s;
  }

  public ExecutableQuery<T> setup(){
    ExecutableQuery<T> query = new ExecutableQuery<T>(_metadata, _rootType, this.statement);
    return query;
  }

  @Override
  public Iterator<T> iterator(){return this.execQuery().iterator();}

  private Iterable<T> execQuery(){
    try{
      return SQLRecord.select(statement, Collections.emptyList(),_rootType);
    }catch(SQLException e){
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

}
