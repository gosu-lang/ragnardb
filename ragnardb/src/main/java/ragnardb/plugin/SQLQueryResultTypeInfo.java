package ragnardb.plugin;

import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.MethodList;
import ragnardb.parser.ast.ResultColumn;
import ragnardb.parser.ast.SelectStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by klu on 7/9/2015.
 */
public class SQLQueryResultTypeInfo extends SQLBaseTypeInfo{

  public SQLQueryResultTypeInfo(ISQLQueryResultType type, SelectStatement statement, ISQLQueryType query) {
    super(type);
    decorateType(type, statement, query);
  }

  private void decorateType(ISQLQueryResultType type, SelectStatement statement, ISQLQueryType query){
    _propertiesMap = new HashMap<>();
    _propertiesList = new ArrayList<>();
    _constructorList = new ArrayList<>();
    _methodList = new MethodList();
    ISQLTableType table = query.getTable(statement.getTables().get(0).toLowerCase());
    List<ColumnDefinition> columnDefinitions =  table.getColumnDefinitions();
    ArrayList<ResultColumn> results = statement.getResultColumns();
    for(ResultColumn rc: results){
      for(ColumnDefinition cd: columnDefinitions){
        if(cd.getColumnName().equals(rc.getResult())){
          SQLColumnPropertyInfo col = new SQLColumnPropertyInfo(cd.getColumnName(), cd.getPropertyName(),
            getGosuType(cd.getSQLType()),table.getTypeInfo(), cd.getOffset(), cd.getLength());
          _propertiesMap.put(col.getName(), col);
          _propertiesList.add(col);
        }
      }
    }
  }
}
