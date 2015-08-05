package ragnardb.plugin;

import gw.lang.parser.resources.Res;
import gw.lang.reflect.*;
import gw.lang.reflect.java.JavaTypes;
import ragnardb.RagnarDB;
import ragnardb.parser.ast.*;
import ragnardb.runtime.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class SQLQueryTypeInfo extends SQLBaseTypeInfo {
  private ArrayList<JavaVar> coalescedVars;
  private ITypeToSQLMetadata _md;

  public SQLQueryTypeInfo(ISQLQueryType type) {
    super(type);
    decorateQuery(type);
  }

  private void decorateQuery(ISQLQueryType type){
    _propertiesList = new ArrayList<>();
    _propertiesMap = new HashMap<>();

    IPropertyInfo prop = new PropertyInfoBuilder()
      .withName("SqlSource")
      .withDescription("Returns the source of this query.")
      .withWritable(false)
      .withStatic()
      .withType(JavaTypes.STRING())
      .withAccessor(new IPropertyAccessor() {
        @Override
        public Object getValue( Object ctx ) {
          try {
            return ((ISQLQueryType) getOwnersType()).getSqlSource();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void setValue( Object ctx, Object value ) {
          throw new IllegalStateException("Calling setter on readonly property");
        }
      })
      .build(this);

    _propertiesList.add(prop);
    _propertiesMap.put(prop.getName(), prop);
    _methodList = getMethods(type);
    _constructorList = Collections.emptyList();
  }

  private MethodList getMethods(ISQLQueryType type){
    MethodList result = new MethodList();
    Statement tree = (Statement) type.getParseTree();
    setCoalescedVars(tree.getVariables());
    if(!(tree instanceof SelectStatement)){
      return handleNonSelect(tree, type);
    }
    IType returnType = returnType(tree, type);
    if(returnType instanceof ISQLQueryResultType){
      ((ISQLQueryType) this.getOwnersType()).setResultType((ISQLQueryResultType) returnType);
    }
    ISQLTableType table = type.getTable(tree.getTables().get(0).toLowerCase());
    IMethodInfo execute = new MethodInfoBuilder()
      .withName("execute")
      .withDescription("Executes the following query with replacement of variables")
      .withParameters(getParams())
      .withReturnType(JavaTypes.ITERABLE().getParameterizedType(returnType))
      .withStatic(true)
      .withCallHandler((ctx, args) -> {
        ISQLQueryType owner = (ISQLQueryType) getOwnersType();
        Statement statement = (Statement) owner.getParseTree();
        ArrayList<JavaVar> variables = statement.getVariables();
        List<Object> vars = new ArrayList<>();
        try {
          String rawSQL = owner.getSqlSource();
          String[] places = rawSQL.split("\n");
          for (int j = 0; j < places.length; j++) {
            int additionalBuffer = 0;
            for (int i = 0; i < coalescedVars.size(); i++) {
              for (JavaVar var : variables) {
                if (var.equals(coalescedVars.get(i)) && (var.getLine()-1)== j) {
                  String currentLine = places[j];
                  Object rep = args[i];
                  String replacement = "?";
                  vars.add(rep);
                  String finalLine = currentLine.substring(0, var.getCol() - (j==0?1:2) + additionalBuffer)
                    + replacement + currentLine.substring(var.getCol() - 1 + var.getSkiplen() + additionalBuffer);
                  places[j] = finalLine;
                  additionalBuffer += replacement.length();
                  additionalBuffer -= var.getSkiplen();
                }
              }
            }
          }
          String finalSQL = String.join("\n", places);
          finalSQL = finalSQL.replace(";", "");
//          System.out.println(finalSQL + " @SQLQueryTypeInfo 105"); debugging logging info
          if (returnType instanceof ISQLQueryResultType) {
            _md = new SQLQueryResultMetadata((ISQLQueryResultType) returnType);
          } else if (returnType instanceof ISQLTableType) {
            _md = new SQLMetadata();
          } else {
            _md = new SQLQueryResultMetadata(owner.getTable(statement.getTables().get(0)));
          }
          ExecutableQuery query = new ExecutableQuery(_md, returnType, finalSQL, returnType, table, vars);
          query = query.setup();
          return query;
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      })
      .build(this);
    result.add(execute);
    return result;
  }

  private void setCoalescedVars(ArrayList<JavaVar> init){
    coalescedVars = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    for(JavaVar v: init){
      if(!names.contains(v.getVarName())){
        coalescedVars.add(v);
        names.add(v.getVarName());
      }
    }
  }

  private ParameterInfoBuilder[] getParams(){
    ArrayList<ParameterInfoBuilder> params = new ArrayList<>();
    for(JavaVar v: coalescedVars){
      ParameterInfoBuilder param = new ParameterInfoBuilder()
        .withName(v.getVarName())
        .withType(TypeSystem.getByFullNameIfValid(v.getVarType()))
        .withDescription("Performs strict matching on this argument");
      params.add(param);
    }
    ParameterInfoBuilder[] parameters = new ParameterInfoBuilder[params.size()];
    for(int i=0 ; i<parameters.length; i++){
      parameters[i] = params.get(i);
    }
    return parameters;
  }

  private IType returnType(Statement select, ISQLQueryType type){
    ArrayList<ResultColumn> resultColumns = select.getResultColumns();
    ArrayList<String> tableNames = select.getTables();
    if(tableNames.size() > 1){
      return handleMultipleTables(select, type);
    }
    if(resultColumns.size() == 1){
      ResultColumn col = resultColumns.get(0);
      String finalChar = Character.toString(col.getName().charAt(col.getName().length() - 1));
      if(finalChar.equals("*")){
        if(tableNames.size() == 1){
          return type.getTable(tableNames.get(0).toLowerCase());
        }
      } else { //now we presume that we are dealing with a single column
        String column = col.getName();
        return type.getColumn(column, tableNames.get(0).toLowerCase());
      }
    }
    return type.getResults(select, type);
  }

  private IType handleMultipleTables(Statement select, ISQLQueryType type){
    ArrayList<ResultColumn> resultColumns = select.getResultColumns();
    ArrayList<String> tableNames = select.getTables();
    ArrayList<SQLColumnPropertyInfo> columns = new ArrayList<>();
    ArrayList<String> columnNames = new ArrayList<>();
    if(resultColumns.size() == 1){
      ResultColumn col = resultColumns.get(0);
      String finalChar = Character.toString(col.getName().charAt(col.getName().length() - 1));
      if(finalChar.equals("*")){
        ArrayList<ISQLTableType> tables = new ArrayList<>();
        for(String tName: tableNames){
          tables.add(type.getTable(tName.toLowerCase()));
        }
        for(ISQLTableType table: tables){
          for(ColumnDefinition column: table.getColumnDefinitions()){
            SQLColumnPropertyInfo propertyInfo = type.getColumnProperty(column.getColumnName(),table.getTable().getTableName().toLowerCase());
            if(!columnNames.contains(propertyInfo.getColumnName())){
              columns.add(propertyInfo);
              columnNames.add(propertyInfo.getColumnName());
            }
          }
        }
        return type.getResults(columns, select);
      } else {
        String[] resultInfo = col.getName().split("\\.");
        return type.getColumn(resultInfo[1], resultInfo[0].toLowerCase());
      }
    } else {
      for(ResultColumn rc: resultColumns){
        String[] split = rc.getName().split("\\.");
        columns.add(type.getColumnProperty(split[1], split[0].toLowerCase()));
      }
      return type.getResults(columns, select);
    }
  }

  private MethodList handleNonSelect(Statement statement, ISQLQueryType type){
    MethodList result = new MethodList();
    IMethodInfo execute = new MethodInfoBuilder()
      .withName("execute")
      .withDescription("Executes the following query with replacement of variables")
      .withParameters(getParams())
      .withReturnType(JavaTypes.INTEGER())
      .withStatic(true)
      .withCallHandler((ctx, args) -> {
        ArrayList<JavaVar> variables = statement.getVariables();
        List<Object> vars = new ArrayList<>();
        try {
          String rawSQL = type.getSqlSource();
          String[] places = rawSQL.split("\n");
          for (int j = 0; j < places.length; j++) {
            int additionalBuffer = 0;
            for (int i = 0; i < coalescedVars.size(); i++) {
              for (JavaVar var : variables) {
                if (var.equals(coalescedVars.get(i)) && (var.getLine()-1)== j) {
                  String currentLine = places[j];
                  Object rep = args[i];
                  String replacement = "?";
                  vars.add(rep);
                  String finalLine = currentLine.substring(0, var.getCol() - (j==0?1:2) + additionalBuffer)
                    + replacement + currentLine.substring(var.getCol() - 1 + var.getSkiplen() + additionalBuffer);
                  places[j] = finalLine;
                  additionalBuffer += replacement.length();
                  additionalBuffer -= var.getSkiplen();
                }
              }
            }
          }
          String finalSQL = String.join("\n", places);
          finalSQL = finalSQL.replace(";", "");
          PreparedStatement p = RagnarDB.prepareStatement(finalSQL, vars);
          return p.executeUpdate();
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      })
      .build(this);
    result.add(execute);
    return result;
  }
}
