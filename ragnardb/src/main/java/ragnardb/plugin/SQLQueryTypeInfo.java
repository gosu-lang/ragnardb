package ragnardb.plugin;

import gw.lang.reflect.*;
import gw.lang.reflect.java.JavaTypes;
import ragnardb.parser.ast.JavaVar;
import ragnardb.parser.ast.JoinClause;
import ragnardb.parser.ast.ResultColumn;
import ragnardb.parser.ast.SelectStatement;

import java.io.IOException;
import java.util.*;

public class SQLQueryTypeInfo extends SQLBaseTypeInfo {
  private ArrayList<JavaVar> coalescedVars;

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
    SelectStatement tree = (SelectStatement) type.getParseTree();
    setCoalescedVars(tree.getVariables());
    IMethodInfo execute = new MethodInfoBuilder()
      .withName("execute")
      .withDescription("Executes the following query with replacement of variables")
      .withParameters(getParams())
      .withReturnType(JavaTypes.ITERABLE().getParameterizedType(returnType(tree, type)))
      .withStatic(true)
      .withCallHandler((ctx, args) -> {
        return null;
      })
      .build(this);
    result.add(execute);
    return result;
  }

  private void setCoalescedVars(ArrayList<JavaVar> init){
    coalescedVars = new ArrayList<>();
    Set<JavaVar> c = new HashSet<>(init);
    for(JavaVar v: c){
      coalescedVars.add(v);
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

  private IType returnType(SelectStatement select, ISQLQueryType type){
    ArrayList<ResultColumn> resultColumns = select.getResultColumns();
    ArrayList<String> tableNames = select.getTables();

    if(resultColumns.size() == 1){
      ResultColumn col = resultColumns.get(0);
      String finalChar = col.getResult().substring(col.getResult().length()-1);
      if(finalChar.equals("*")){
        if(tableNames.size() == 1){
          return type.getTable(tableNames.get(0).toLowerCase());
        }
      }
    }
    return null;
  }
}
