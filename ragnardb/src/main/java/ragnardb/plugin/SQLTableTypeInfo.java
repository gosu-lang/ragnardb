package ragnardb.plugin;

import gw.lang.reflect.ConstructorInfoBuilder;
import gw.lang.reflect.IConstructorInfo;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.MethodInfoBuilder;
import gw.lang.reflect.MethodList;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.JavaTypes;
import ragnardb.runtime.SQLConstraint;
import ragnardb.runtime.SQLMetadata;
import ragnardb.runtime.SQLQuery;
import ragnardb.runtime.SQLRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SQLTableTypeInfo extends SQLBaseTypeInfo {
  private String _classTableName;

  public SQLTableTypeInfo(ISQLTableType type) {
    super(type);
    resolveProperties(type);
    _classTableName = type.getName();
  }

  private void resolveProperties( ISQLTableType type ) {
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

  @Override
  public int getOffset() {
    return ((ISQLTableType) getOwnersType()).getTable().getOffset();
  }

  @Override
  public int getTextLength() {
    return ((ISQLTableType) getOwnersType()).getTable().getTypeName().length();
  }

  private List<IConstructorInfo> createConstructorInfos() {
    List<IConstructorInfo> constructorInfos = new ArrayList<>();

    IConstructorInfo constructorMethod = new ConstructorInfoBuilder()
      .withDescription( "Creates a new Table object" )
      .withParameters()
      .withConstructorHandler((args) -> new SQLRecord(((ISQLTableType) getOwnersType()).getTable().getTableName(),
        "id")).build(this);

    constructorInfos.add( constructorMethod );

    return constructorInfos;
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
      .withCallHandler((ctx, args) -> new SQLRecord(((ISQLTableType) getOwnersType()).getTable().getTableName(), "id"))
      .build(this);
    result.add(initMethod);

    IMethodInfo selectMethod = new MethodInfoBuilder()
      .withName("select")
      .withDescription("Creates a new table query")
      .withParameters()
      .withReturnType(JavaTypes.getGosuType(SQLQuery.class).getParameterizedType(this.getOwnersType()))
      .withStatic(true)
      .withCallHandler((ctx, args) -> new SQLQuery<SQLRecord>(md, this.getOwnersType()))
      .build(this);
    result.add(selectMethod);

    IMethodInfo whereMethod = new MethodInfoBuilder()
      .withName("where")
      .withDescription("Creates a new table query")
      .withParameters(new ParameterInfoBuilder().withName("condition").withType(TypeSystem.get(SQLConstraint.class)))
      .withReturnType(JavaTypes.getGosuType(SQLQuery.class).getParameterizedType(this.getOwnersType()))
      .withStatic(true)
      .withCallHandler((ctx, args) -> new SQLQuery<SQLRecord>(md, this.getOwnersType()).where((SQLConstraint) args[0]))
      .build(this);
    result.add(whereMethod);


    IMethodInfo getName = new MethodInfoBuilder()
      .withName("getName")
      .withDescription("Returns Table Name")
      .withParameters()
      .withReturnType(JavaTypes.STRING())
      .withStatic(true)
      .withCallHandler((ctx, args) -> _classTableName)
      .build(this);
    result.add(getName);

    return result;
  }

}
