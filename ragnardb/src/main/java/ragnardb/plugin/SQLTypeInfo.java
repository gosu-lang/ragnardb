package ragnardb.plugin;

import gw.lang.reflect.*;
import gw.lang.reflect.java.JavaTypes;
import ragnardb.runtime.*;

import java.io.IOException;
import java.sql.Types;
import java.util.*;

public class SQLTypeInfo extends BaseTypeInfo {
  private List<IPropertyInfo> _propertiesList;
  private Map<String, IPropertyInfo> _propertiesMap;
  private MethodList _methodList;
  private List<IConstructorInfo> _constructorList;

  public SQLTypeInfo(ISqlTableType type) {
    super(type);
    resolveProperties(type);
  }

  public SQLTypeInfo(ISqlDdlType type) {
    super(type);
    decorateDdlType(type);
  }

  private void resolveProperties( ISqlTableType type ) {
    _propertiesList = new ArrayList<>();
    _propertiesMap = new HashMap<>();

    List<ColumnDefinition> columns = type.getColumnDefinitions();
    for(ColumnDefinition column : columns) {
      SQLColumnPropertyInfo prop = new SQLColumnPropertyInfo(column.getColumnName(), column.getPropertyName(),
          getGosuType(column.getSQLType()), this, column.getOffset(), column.getLength());
      _propertiesMap.put(prop.getName(), prop);
      _propertiesList.add( prop );
    }
    _methodList = createMethodInfos();
    _constructorList = createConstructorInfos();
  }

  private List<IConstructorInfo> createConstructorInfos() {
    List<IConstructorInfo> constructorInfos = new ArrayList<>();

    IConstructorInfo constructorMethod = new ConstructorInfoBuilder()
            .withDescription( "Creates a new Table object" )
            .withParameters()
            .withConstructorHandler(( args ) -> new SQLRecord(((ISqlTableType) getOwnersType()).getTable().getTableName(),
                "id")).build(this);

    constructorInfos.add( constructorMethod );

    return constructorInfos;
  }

  /**
   * ISqlDdlType will have a single getter, SqlSource : String
   * @param type ISqlDdlType
   */
  private void decorateDdlType( ISqlDdlType type ) {
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
    _propertiesList.add(prop);
    _methodList = new MethodList();
    _constructorList = Collections.emptyList();
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
  public List<? extends IConstructorInfo> getConstructors() {
    return _constructorList;
  }

  @Override
  public IConstructorInfo getConstructor(IType... vars) {
    return FIND.constructor( getConstructors(), vars );
  }

  @Override
  public IConstructorInfo getCallableConstructor( IType... params )
  {
    return FIND.callableConstructor( getConstructors(), params );
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
    return FIND.callableMethod( getMethods(), strMethod, params );
  }

  @Override
  public IMethodInfo getMethod(CharSequence methodName, IType... params) {
    return FIND.method( getMethods(), methodName, params );
  }

  /**
   * create a "findBy***" method for each property/column
   * @return
   */
  private MethodList createMethodInfos() {  //MethodList#add(IMethodInfo)
    MethodList result = new MethodList();
    SQLMetadata md = new SQLMetadata();



    for(String propertyName : _propertiesMap.keySet()) {
      SQLColumnPropertyInfo prop = (SQLColumnPropertyInfo) _propertiesMap.get(propertyName);


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
            SQLQuery query = new SQLQuery(md, getOwnersType());
            SQLConstraint constraint = SQLConstraint.isEqualTo(prop, args[0]);
            query = query.where(constraint);
            return query.iterator().hasNext() ? query.iterator().next() : null;
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
            SQLQuery query = new SQLQuery(md, getOwnersType());
            SQLConstraint constraint = SQLConstraint.isEqualTo(prop, args[0]);
            query = query.where(constraint);
            return query;
          })
          .build(this);

      result.add(findAllByMethod);
    }

    //Now we add a create method to allow insertions
    IMethodInfo createMethod = new MethodInfoBuilder()
      .withName("create")
      .withDescription("Creates a new table entry")
      .withParameters()
      .withReturnType(this.getOwnersType())
      .withCallHandler((ctx, args) -> ((SQLRecord) ctx).create())
      .build(this);
    result.add(createMethod);

    IMethodInfo initMethod = new MethodInfoBuilder()
      .withName("init")
      .withDescription("Creates a new table entry")
      .withParameters()
      .withReturnType(this.getOwnersType())
        .withStatic(true)
      .withCallHandler((ctx, args) -> new SQLRecord(((ISqlTableType) getOwnersType()).getTable().getTableName(), "id"))
        .build(this);
    result.add(initMethod);

    IMethodInfo whereMethod = new MethodInfoBuilder()
      .withName( "where" )
      .withDescription( "Creates a new table query" )
      .withParameters( new ParameterInfoBuilder().withName( "condition" ).withType( TypeSystem.get( SQLConstraint.class ) ) )
      .withReturnType( JavaTypes.ITERABLE().getParameterizedType(this.getOwnersType() ) )
      .withStatic( true )
      .withCallHandler( ( ctx, args ) -> new SQLQuery<SQLRecord>( md, this.getOwnersType() ).where( (SQLConstraint)args[0] ) )
        .build( this );
    result.add(whereMethod);

    return result;
  }

}
