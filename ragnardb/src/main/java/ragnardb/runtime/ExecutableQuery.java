package ragnardb.runtime;

import gw.lang.reflect.IType;
import gw.util.GosuExceptionUtil;
import ragnardb.plugin.ISQLQueryResultType;
import ragnardb.plugin.ISQLTableType;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by klu on 7/8/2015.
 */
public class ExecutableQuery<T> extends SQLQuery<T>{
  private String statement;
  private IType returnType;
  private ISQLTableType returnTable;

  public ExecutableQuery(ITypeToSQLMetadata md, IType rootType, String s, IType ret, ISQLTableType rT){
    super(md, rootType);
    statement = s;
    returnType = ret;
    returnTable = rT;
  }

  public ExecutableQuery<T> setup(){
    ExecutableQuery<T> query = new ExecutableQuery<T>(_metadata, _rootType, this.statement, returnType, returnTable);
    return query;
  }

  @Override
  public Iterator<T> iterator(){return this.execQuery().iterator();}

  private Iterable<T> execQuery(){
    try{
      List<T> results = new LinkedList<>();
      Iterable<SQLRecord> records = SQLRecord.executeStatement(statement, _rootType);
      if(returnType instanceof ISQLTableType){
        for(SQLRecord record: records){
          results.add((T) record);
        }
      } else if(returnType instanceof ISQLQueryResultType){
        for(SQLRecord record: records){
          results.add((T) record);
        }
      }
      return results;
    }catch(SQLException e){
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

}
