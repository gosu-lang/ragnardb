package ragnardb.plugin;

import gw.lang.reflect.*;
import gw.lang.reflect.features.PropertyReference;
import gw.lang.reflect.gs.IGosuClass;
import gw.lang.reflect.java.JavaTypes;
import ragnardb.runtime.SQLConstraint;
import ragnardb.runtime.SQLMetadata;
import ragnardb.runtime.SQLQuery;
import ragnardb.runtime.SQLRecord;
import ragnardb.utils.NounHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SQLTableTypeInfo extends SQLBaseTypeInfo {
  private SQLMetadata _md = new SQLMetadata();
  private String _classTableName;
  private IGosuClass _domainLogic;
  private IConstructorHandler _constructor;

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
    _domainLogic = maybeGetDomainLogic();
    createMethodInfos();
    createConstructorInfos();
  }

  @Override
  public int getOffset() {
    return ((ISQLTableType) getOwnersType()).getTable().getOffset();
  }

  @Override
  public int getTextLength() {
    return ((ISQLTableType) getOwnersType()).getTable().getTypeName().length();
  }

  private void createConstructorInfos() {
    List<IConstructorInfo> constructorInfos = new ArrayList<>();

    final String tableName = ((ISQLTableType) getOwnersType()).getTable().getTableName();
    final String idColumn = "id";

    final IConstructorInfo domainCtor = _domainLogic == null ? null : _domainLogic.getTypeInfo().getConstructor( JavaTypes.STRING(), JavaTypes.STRING() );
    _constructor = ( args ) -> {
      //reflectively instantiate the domain logic class, if it exists
      if( domainCtor != null )
      {
        return domainCtor.getConstructor().newInstance( tableName, idColumn );
      }
      else
      {
        return new SQLRecord( tableName, idColumn );
      }
    };

    IConstructorInfo constructorMethod = new ConstructorInfoBuilder()
      .withDescription( "Creates a new Table object" )
      .withParameters()
      .withConstructorHandler( _constructor ).build( this );

    constructorInfos.add( constructorMethod );

    _constructorList = constructorInfos;
  }

  private void createMethodInfos() {
    MethodList methodList = new MethodList();

    for (String propertyName : _propertiesMap.keySet()) {
      SQLColumnPropertyInfo prop = (SQLColumnPropertyInfo) _propertiesMap.get(propertyName);

      methodList.add(generateFindByMethod(prop));
      methodList.add(generateFindByAllMethod(prop));
    }

    methodList.add(generateCreateMethod());
    methodList.add(generateWhereMethod());
    methodList.add(generateSelectMethod());
    methodList.add(generateGetNameMethod());

    List<? extends IMethodInfo> domainMethods = maybeGetDomainMethods();
    List<? extends IPropertyInfo> domainProperties = maybeGetDomainProperties();

    methodList.addAll(domainMethods);

    _methodList = methodList;

    for(IPropertyInfo domainProperty : domainProperties) {
      _propertiesMap.put(domainProperty.getName(), domainProperty);
      _propertiesList.add(domainProperty);
    }

  }

  private IMethodInfo generateFindByMethod(IPropertyInfo prop) {
    final String propertyName = prop.getName();
    return new MethodInfoBuilder()
        .withName( "findBy" + propertyName )
        .withDescription("Find single match based on the value of the " + propertyName + " column.")
        .withParameters(new ParameterInfoBuilder()
            .withName(propertyName)
            .withType(prop.getFeatureType())
            .withDescription("Performs strict matching on this argument"))
        .withReturnType(this.getOwnersType())
        .withStatic(true)
        .withCallHandler(( ctx, args ) -> {
          SQLQuery query = new SQLQuery(_md, getOwnersType());
          SQLConstraint constraint = SQLConstraint.isComparator(prop, args[0],"=");
          query = query.where(constraint);
          return query.iterator().hasNext() ? query.iterator().next() : null;
        })
        .build(this);
  }

  private IMethodInfo generateFindByAllMethod(IPropertyInfo prop) {
    final String propertyName = prop.getName();
    return new MethodInfoBuilder()
        .withName("findAllBy" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1))
        .withDescription("Find all matches based on the value of the " + propertyName + " column.")
        .withParameters(new ParameterInfoBuilder()
            .withName(propertyName)
            .withType(prop.getFeatureType())
            .withDescription("Performs strict matching on this argument"))
        .withReturnType(JavaTypes.ITERABLE().getParameterizedType(this.getOwnersType()))
        .withStatic(true)
        .withCallHandler(( ctx, args ) -> {
          SQLQuery query = new SQLQuery(_md, getOwnersType());
          SQLConstraint constraint = SQLConstraint.isComparator(prop, args[0], "=");
          query = query.where(constraint);
          return query;
        })
        .build(this);
  }

  private IMethodInfo generateCreateMethod() {
    return new MethodInfoBuilder()
        .withName("create")
        .withDescription("Creates a new table entry")
        .withParameters()
        .withReturnType(this.getOwnersType())
        .withCallHandler(( ctx, args ) -> ((SQLRecord) ctx).create())
        .build(this);
  }

  private IMethodInfo generateWhereMethod() {
    return new MethodInfoBuilder()
        .withName("where")
        .withDescription("Creates a new table query")
        .withParameters(new ParameterInfoBuilder().withName("condition").withType(TypeSystem.get(SQLConstraint.class)))
        .withReturnType(JavaTypes.ITERABLE().getParameterizedType(this.getOwnersType()))
        .withStatic(true)
        .withCallHandler(( ctx, args ) -> new SQLQuery<SQLRecord>(_md, this.getOwnersType()).where((SQLConstraint) args[0]))
        .build(this);
  }

  private IMethodInfo generateSelectMethod() {
    return new MethodInfoBuilder()
        .withName("select")
        .withDescription("Creates a new table query")
        .withParameters()
        .withReturnType(JavaTypes.getGosuType(SQLQuery.class).getParameterizedType(this.getOwnersType()))
        .withStatic(true)
        .withCallHandler((ctx, args) -> new SQLQuery<SQLRecord>(_md, this.getOwnersType()))
        .build(this);
  }

  private IMethodInfo generateSingleObjectSelectMethod() {
    return new MethodInfoBuilder()
      .withName("select")
      .withDescription("Creates a new table query")
      .withParameters(new ParameterInfoBuilder().withName("Column").withType(TypeSystem.get(PropertyReference.class)))
        .withReturnType(JavaTypes.getGosuType(SQLQuery.class).getParameterizedType(this.getOwnersType()))
        .withStatic(true)
        .withCallHandler((ctx, args) -> new SQLQuery<SQLRecord>(_md, this.getOwnersType()))
        .build(this);
  }

  private IMethodInfo generateJoinMethod() {
    return new MethodInfoBuilder()
      .withName("join")
      .withDescription("Creates a join statement form table (Not a full query)")
      .withParameters(new ParameterInfoBuilder().withName("Table").withType(TypeSystem.get(IType.class)))
      .withReturnType(JavaTypes.getGosuType(SQLQuery.class).getParameterizedType(this.getOwnersType()))
      .withStatic(true)
      .withCallHandler((ctx, args) -> new SQLQuery<SQLRecord>(_md, this.getOwnersType()))
      .build(this);
  }

  private IMethodInfo generateGetNameMethod() {
    return new MethodInfoBuilder()
        .withName("getName")
        .withDescription("Returns Table Name")
        .withParameters()
        .withReturnType(JavaTypes.STRING())
        .withStatic(true)
        .withCallHandler((ctx, args) -> _classTableName)
        .build(this);
  }

  private IMethodInfo generateDeleteAllMethod() {
    return new MethodInfoBuilder()
        .withName( "deleteAll" )
        .withDescription( "Deletes all records in table" )
        .withParameters( new ParameterInfoBuilder().withName( "confirm" ).withType( JavaTypes.pBOOLEAN() ) )
        .withStatic( true )
        .withCallHandler( ( ctx, args ) -> {
          getOwnersType().deleteAll( (Boolean)args[0] );
          return null;
        } )
        .build( this );
  }

  public ISQLTableType getOwnersType() {
    return (ISQLTableType) super.getOwnersType();
  }

  private IGosuClass maybeGetDomainLogic() {
    ISQLTableType tableType = (ISQLTableType) getOwnersType();
    ISQLDdlType ddlType = (ISQLDdlType) tableType.getEnclosingType();
    final String singularizedDdlType = new NounHandler(ddlType.getRelativeName()).getSingular();
    final String domainLogicPackageSuffix = "Ext.";
    final String domainLogicTableSuffix = "Ext";
    final String domainLogicFqn = ddlType.getNamespace() + '.' +
        singularizedDdlType + domainLogicPackageSuffix + tableType.getRelativeName() + domainLogicTableSuffix;

    IType correctlyNamedDomainLogic = TypeSystem.getByFullNameIfValid(domainLogicFqn);

    IType sqlRecord = TypeSystem.getByFullName( "ragnardb.runtime.SQLRecord" ); //ok to throw here if we can't find SQLRecord

    if( correctlyNamedDomainLogic != null )
    {
      if( sqlRecord.isAssignableFrom( correctlyNamedDomainLogic ) )
      {
        if( correctlyNamedDomainLogic instanceof IGosuClass )
        {
          return (IGosuClass)correctlyNamedDomainLogic;
        }
        else
        {
          System.out.println( "Ragnar extension classes must be Gosu classes" );
        }
      }
      else
      {
        System.out.println( "Ragnar extension classes must extend SQLRecord" );
      }
    }

    return null;
  }

  private List<? extends IMethodInfo> maybeGetDomainMethods() {
    if ( _domainLogic != null)
    {
      return _domainLogic.getTypeInfo().getDeclaredMethods()
        .stream()
        .filter(IAttributedFeatureInfo::isPublic)
        .collect( Collectors.toList() );
    }
    else
    {
      return Collections.emptyList();
    }
  }

  private List<? extends IPropertyInfo> maybeGetDomainProperties() {
    if ( _domainLogic != null)
    {
      return _domainLogic.getTypeInfo().getDeclaredProperties()
          .stream()
          .filter(IAttributedFeatureInfo::isPublic)
          .collect( Collectors.toList() );
    }
    else
    {
      return Collections.emptyList();
    }
  }

}
