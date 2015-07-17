package ragnardb.runtime;

import gw.lang.reflect.IType;
import gw.lang.reflect.java.JavaTypes;
import gw.util.GosuExceptionUtil;
import ragnardb.plugin.ColumnDefinition;
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
//    System.out.println(statement + " @ExecutableQuery 27"); debugging logging info
  }

  public ExecutableQuery<T> setup(){
    ExecutableQuery<T> query = new ExecutableQuery<T>(_metadata, _rootType, this.statement, returnType, returnTable);
//    System.out.println(query.statement + " @ExecutableQuery 32");
    return query;
  }

  @Override
  public Iterator<T> iterator(){return this.execQuery().iterator();}

  private Iterable<T> execQuery(){
    try{
      List<T> results = new LinkedList<>();
      Iterable<SQLRecord> records;
      if(!(returnType instanceof ISQLQueryResultType || returnType instanceof ISQLTableType)){
        records = SQLRecord.executeStatement(statement, returnTable);
      } else {
        records = SQLRecord.executeStatement(statement, _rootType);
      }
//      System.out.println(this.statement + " @ExecutableQuery 49");
      if(returnType instanceof ISQLTableType){
        for(SQLRecord record: records){
          results.add((T) record);
        }
      } else if(returnType instanceof ISQLQueryResultType){
        for(SQLRecord record: records){
          results.add((T) record);
        }
      } else {
        List result;
        if(returnType == JavaTypes.STRING()){
          result = new LinkedList<String>();
          for(SQLRecord record: records){
            for(ColumnDefinition col: returnTable.getTable().getColumnDefinitions()){
              Object obj = record.getRawValue(col.getColumnName());
              if(obj != null){
                result.add((String) obj);
              }
            }
          }
        } else if(returnType == JavaTypes.CHARACTER()){
          result = new LinkedList<String>();
          for(SQLRecord record: records){
            for(ColumnDefinition col: returnTable.getTable().getColumnDefinitions()){
              Object obj = record.getRawValue(col.getColumnName());
              if(obj != null){
                result.add(((Character) obj).toString());
              }
            }
          }
        } else if(returnType == JavaTypes.INTEGER()){
          result = new LinkedList<Integer>();
          for(SQLRecord record: records){
            for(ColumnDefinition col: returnTable.getTable().getColumnDefinitions()){
              Object obj = record.getRawValue(col.getColumnName());
              if(obj != null){
                result.add((Integer) obj);
              }
            }
          }
        } else if(returnType == JavaTypes.LONG()){
          result = new LinkedList<Long>();
          for(SQLRecord record: records){
            for(ColumnDefinition col: returnTable.getTable().getColumnDefinitions()){
              Object obj = record.getRawValue(col.getColumnName());
              if(obj != null){
                result.add((Long) obj);
              }
            }
          }
        } else if(returnType == JavaTypes.DOUBLE()){
          result = new LinkedList<Double>();
          for(SQLRecord record: records){
            for(ColumnDefinition col: returnTable.getTable().getColumnDefinitions()){
              Object obj = record.getRawValue(col.getColumnName());
              if(obj != null){
                result.add((Double) obj);
              }
            }
          }
        } else if(returnType == JavaTypes.BOOLEAN()){
          result = new LinkedList<Boolean>();
          for(SQLRecord record: records){
            for(ColumnDefinition col: returnTable.getTable().getColumnDefinitions()){
              Object obj = record.getRawValue(col.getColumnName());
              if(obj != null){
                result.add((Boolean) obj);
              }
            }
          }
        } else {
          result = new LinkedList<>();
          for(SQLRecord record: records){
            for(ColumnDefinition col: returnTable.getTable().getColumnDefinitions()){
              Object obj = record.getRawValue(col.getColumnName());
              if(obj != null){
                result.add(obj);
              }
            }
          }
        }
        for(Object o : result){
          results.add((T) o);
        }
      }
      return results;
    }catch(SQLException e){
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

}
