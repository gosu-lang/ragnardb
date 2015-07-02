package ragnardb.plugin;

import gw.lang.reflect.*;
import gw.lang.reflect.java.JavaTypes;
import ragnardb.runtime.*;

import java.io.IOException;
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

  public SQLTypeInfo(ISqlDdlType type) {
    super(type);
    resolveProperties(type);
  }

  private void resolveProperties(ISqlTableType type) {
    _propertiesList = new ArrayList<>();
    _propertiesMap = new HashMap<>();

    List<ColumnDefinition> columns = type.getColumnDefinitions();
    for(ColumnDefinition column : columns) {
      SQLColumnPropertyInfo prop = new SQLColumnPropertyInfo(column.getColumnName(), column.getPropertyName(),
          getGosuType(column.getSQLType()), this, column.getOffset(), column.getLength());
      _propertiesMap.put(prop.getName(), prop);
      _propertiesList.add( prop );
      _methodList = createMethodInfos();
    }
  }

  /**
   * ISqlDdlType will have a single getter, SqlSource : String
   * @param type ISqlDdlType
   */
  private void resolveProperties(ISqlDdlType type) {
    _propertiesList = new ArrayList<>();
    _propertiesMap = new HashMap<>();

    IPropertyInfo prop = new PropertyInfoBuilder()
        .withName("SqlSource")
        .withDescription("Returns the source of this ISqlDdlType")
        .withStatic()
        .withWritable(false)
        .withType(JavaTypes.STRING())
        .withAccessor(new IPropertyAccessor() {
          @Override
          public Object getValue( Object ctx ) {
            try {
              return ((ISqlDdlType) getOwnersType()).getSqlSource();
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

    _propertiesMap.put(prop.getName(), prop);
    _propertiesList.add( prop );
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
    if(getOwnersType() instanceof ISqlTableType) {
      return ((ISqlTableType) getOwnersType()).getTable().getOffset();
    }
    return super.getOffset();
  }

  @Override
  public int getTextLength() {
    if(getOwnersType() instanceof ISqlTableType) {
      return ((ISqlTableType) getOwnersType()).getTable().getTypeName().length();
    }
    return super.getTextLength();
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
      SQLMetadata md = new SQLMetadata();

      String name = "findBy" + prop.getName();
      IMethodInfo findByMethod = new MethodInfoBuilder()
          .withName( name )
          .withDescription("Find single match based on the value of the " + propertyName + " column.")
          .withParameters(new ParameterInfoBuilder()
            .withName(propertyName)
            .withType(prop.getFeatureType())
            .withDescription("Performs strict matching on this argument"))
          .withReturnType(this.getOwnersType())
          .withStatic(true)
          .withCallHandler((ctx, args) -> {
            return new IMethodCallHandler() {
              @Override
              public Object handleCall(Object ctx, Object... args) {
                SQLQuery query = new SQLQuery(md, getOwnersType());
                SQLConstraint constraint = SQLConstraint.isEqualTo(prop, args[0]);
                query = query.where(constraint);
                return query.iterator().hasNext() ? query.iterator().next() : null;
              }
            };
          }) // as opposed to { return null; }
          .build(this);

      result.add(findByMethod);

      //Now we add the findAllBy
      IMethodInfo findAllByMethod = new MethodInfoBuilder()
          .withName("findAllBy" + propertyName.substring(0,1).toUpperCase()+propertyName.substring(1))
          .withDescription("Find all matches based on the value of the " + propertyName + " column.")
          .withParameters(new ParameterInfoBuilder()
            .withName(propertyName)
            .withType(prop.getFeatureType())
            .withDescription("Performs strict matching on this argument"))
          .withReturnType(JavaTypes.ITERABLE().getParameterizedType(this.getOwnersType()))
          .withStatic(true)
          .withCallHandler((ctx, args) -> {
            return new IMethodCallHandler() {
              @Override
              public Object handleCall(Object ctx, Object... args) {
                SQLQuery query = new SQLQuery(md, getOwnersType());
                SQLConstraint constraint = SQLConstraint.isEqualTo(prop, args[0]);
                query = query.where(constraint);
                return query.iterator();
              }
            };
          })
          .build(this);

      result.add(findAllByMethod);
    }

    return result;
  }

}
