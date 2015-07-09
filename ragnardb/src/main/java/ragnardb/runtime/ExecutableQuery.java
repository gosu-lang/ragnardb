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

  public ExecutableQuery(ITypeToSQLMetadata md, IType rootType){
    super(md, rootType);
  }

  public ExecutableQuery<T> setup(String string){
    ExecutableQuery<T> query = new ExecutableQuery<T>(_metadata, _rootType);
    statement = string;
    return query;
  }

  @Override
  public Iterator<T> iterator(){return execQuery().iterator();}

  private Iterable<T> execQuery(){
    try{
      return SQLRecord.select(statement, Collections.emptyList(),_rootType);
    }catch(SQLException e){
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

}
