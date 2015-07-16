package ragnardb.plugin;

import gw.lang.reflect.*;
import ragnardb.parser.ast.ResultColumn;
import ragnardb.parser.ast.SelectStatement;
import ragnardb.parser.ast.Statement;
import ragnardb.runtime.SQLRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by klu on 7/9/2015.
 */
public class SQLQueryResultTypeInfo extends SQLBaseTypeInfo{
  private IConstructorHandler _constructor;

  public SQLQueryResultTypeInfo(ISQLQueryResultType type, Statement statement, ISQLQueryType query) {
    super(type);
    decorateType(type, statement, query);
  }

  public SQLQueryResultTypeInfo(ArrayList<SQLColumnPropertyInfo> propInfos, ISQLQueryResultType type){
    super(type);
    decorateType(type, propInfos);
  }

  private void decorateType(ISQLQueryResultType type, Statement statement, ISQLQueryType query){
    _propertiesMap = new HashMap<>();
    _propertiesList = new ArrayList<>();
    _methodList = new MethodList();
    ISQLTableType table = query.getTable(statement.getTables().get(0).toLowerCase());
    createConstructorInfos(type.getRelativeName());
    List<ColumnDefinition> columnDefinitions =  table.getColumnDefinitions();
    ArrayList<ResultColumn> results = statement.getResultColumns();
    for(ResultColumn rc: results){
      for(ColumnDefinition cd: columnDefinitions){
        if(cd.getColumnName().toLowerCase().equals(rc.getResult().toLowerCase())){
          SQLColumnPropertyInfo col = new SQLColumnPropertyInfo(cd.getColumnName(), cd.getPropertyName(),
            getGosuType(cd.getSQLType()),this, cd.getOffset(), cd.getLength());
          _propertiesMap.put(col.getName(), col);
          _propertiesList.add(col);
        }
      }
    }
  }

  private void decorateType(ISQLQueryResultType type, ArrayList<SQLColumnPropertyInfo> propInfos){
    _propertiesList = new ArrayList<>();
    _propertiesMap = new HashMap<>();
    _methodList = new MethodList();
    createConstructorInfos(type.getRelativeName());
    for(SQLColumnPropertyInfo propInfo: propInfos){
      _propertiesMap.put(propInfo.getName(), propInfo);
      _propertiesList.add(propInfo);
    }
  }


  private void createConstructorInfos(String table) {
    List<IConstructorInfo> constructorInfos = new ArrayList<>();

    _constructor = ( args ) -> {return new SQLRecord(table, "id");};

    IConstructorInfo constructor = new ConstructorInfoBuilder()
      .withDescription("Creates a new query result object")
      .withParameters()
      .withConstructorHandler( _constructor ).build(this);

    constructorInfos.add(constructor);
    _constructorList = constructorInfos;

  }
}
