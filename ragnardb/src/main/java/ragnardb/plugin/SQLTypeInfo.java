package ragnardb.plugin;

import gw.lang.reflect.*;
import gw.lang.reflect.java.JavaTypes;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLTypeInfo extends BaseTypeInfo {
  private List<IPropertyInfo> _propertiesList;
  private Map<String, IPropertyInfo> _propertiesMap;
  private MethodList _methodList;

  public SQLTypeInfo(ISqlTableType type) {
    super(type);
    resolveProperties(type);
  }

  private void resolveProperties(ISqlTableType type) {
    _propertiesList = new ArrayList<>();
    _propertiesMap = new HashMap<>();

    List<ColumnDefinition> columns = type.getColumnDefinitions();
    for(ColumnDefinition column : columns) {
      SQLColumnPropertyInfo prop = new SQLColumnPropertyInfo(column.getColumnName(),
          makePropertyName(column.getColumnName()),
          getGosuType(column.getSQLType()), this, column.getOffset(), column.getLength());
      _propertiesMap.put(prop.getName(), prop);
      _propertiesList.add(prop);
      _methodList = createMethodInfos();
    }
  }

  /**
   * Sourced from "JDBC Types Mapped to Java Types" at https://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html
   * @param sqlType
   * @return
   */
  private IType getGosuType(int sqlType) {
    switch(sqlType) {
      case (Types.CHAR):
      case (Types.NCHAR):
      case (Types.VARCHAR):
      case (Types.NVARCHAR):
      case (Types.LONGVARCHAR):
      case (Types.LONGNVARCHAR):
        return JavaTypes.STRING();
      case (Types.DECIMAL):
      case (Types.NUMERIC):
        return JavaTypes.BIG_DECIMAL();
      case (Types.BIT):
        return JavaTypes.pBOOLEAN();
      case (Types.TINYINT):
        return JavaTypes.pBYTE();
      case (Types.SMALLINT):
        return JavaTypes.pSHORT();
      case (Types.INTEGER):
        return JavaTypes.pINT();
      case (Types.BIGINT):
        return JavaTypes.pLONG();
      case (Types.REAL):
        return JavaTypes.pFLOAT();
      case (Types.FLOAT):
      case (Types.DOUBLE):
        return JavaTypes.pDOUBLE();
//      case (Types.DATE):
//        return ??
//      case (Types.TIME):
//        return ??
//      case (Types.TIMESTAMP):
//        return ??
      default:
        return JavaTypes.OBJECT();
    }
  }

  private String makePropertyName(String columnName) {
    return columnName;
  }

  @Override
  public List<? extends IPropertyInfo> getProperties() {
    return _propertiesList;
  }

  @Override
  public IPropertyInfo getProperty(CharSequence propName) {
    return _propertiesMap.get(propName.toString());
  }

  @Override
  public int getOffset() {
    return ((ISqlTableType)getOwnersType()).getTable().getOffset();
  }

  @Override
  public int getTextLength() {
    return ((ISqlTableType) getOwnersType()).getTable().getName().length();
  }

  @Override
  public MethodList getMethods() {
    return _methodList;
  }

  @Override
  public IMethodInfo getCallableMethod(CharSequence strMethod, IType... params) {
    return FIND.callableMethod(getMethods(), strMethod, params);
  }

  @Override
  public IMethodInfo getMethod(CharSequence methodName, IType... params) {
    return FIND.method(getMethods(), methodName, params);
  }

  /**
   * create a "findBy***" method for each property/column
   * @return
   */
  private MethodList createMethodInfos() {  //MethodList#add(IMethodInfo)
    MethodList result = new MethodList();
    for(String propertyName : _propertiesMap.keySet()) {
      SQLColumnPropertyInfo prop = (SQLColumnPropertyInfo) _propertiesMap.get(propertyName);

      IMethodInfo findByMethod = new MethodInfoBuilder()
          .withName("findBy" + propertyName)
          .withDescription("Find single match based on the value of the " + propertyName + " column.")
          .withParameters(new ParameterInfoBuilder()
              .withName(propertyName)
              .withType(prop.getFeatureType())
              .withDescription("Performs strict matching on this argument"))
          .withReturnType(this.getOwnersType())
          .withStatic(true)
          .withCallHandler((ctx, args) -> null) // as opposed to { return null; }
          .build(this);

      result.add(findByMethod);

      //Now we add the findAllBy
      IMethodInfo findAllByMethod = new MethodInfoBuilder()
          .withName("findAllBy" + propertyName)
          .withDescription("Find all matches based on the value of the " + propertyName + " column.")
          .withParameters(new ParameterInfoBuilder()
              .withName(propertyName)
              .withType(prop.getFeatureType())
              .withDescription("Performs strict matching on this argument"))
          .withReturnType(JavaTypes.ITERABLE().getParameterizedType(this.getOwnersType()))
          .withStatic(true)
          .withCallHandler((ctx, args) -> null)
          .build(this);

      result.add(findAllByMethod);
    }

    return result;
  }

}
