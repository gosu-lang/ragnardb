package ragnardb.plugin;

import gw.lang.reflect.ConstructorInfoBuilder;
import gw.lang.reflect.IAttributedFeatureInfo;
import gw.lang.reflect.IConstructorHandler;
import gw.lang.reflect.IConstructorInfo;
import gw.lang.reflect.IFileBasedType;
import gw.lang.reflect.ILocationInfo;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.LocationInfo;
import gw.lang.reflect.MethodInfoBuilder;
import gw.lang.reflect.MethodList;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.PropertyInfoBuilder;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.IGosuClass;
import gw.lang.reflect.java.JavaTypes;
import ragnardb.parser.ast.Constraint;
import ragnardb.parser.ast.CreateTable;
import ragnardb.runtime.SQLConstraint;
import ragnardb.runtime.SQLMetadata;
import ragnardb.runtime.SQLQuery;
import ragnardb.runtime.SQLRecord;
import ragnardb.utils.NounHandler;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SQLTableTypeInfo extends SQLBaseTypeInfo {
  private SQLMetadata _md = new SQLMetadata();
  private ISQLTableType _parent;
  private ISQLDdlType _system;
  private CreateTable _table;
  private String _classTableName;
  private IGosuClass _domainLogic;
  private IConstructorHandler _constructor;
  private final ILocationInfo _location;

  public SQLTableTypeInfo(ISQLTableType type, CreateTable table,  ISQLDdlType system) {
    super(type);
    _parent = type;
    _system = system;
    _table = table;
    resolveProperties(type);
    _classTableName = type.getName();
    try
    {
      _location = new LocationInfo( getOwnersType().getTable().getOffset(), getOwnersType().getTable().getTypeName().length(), -1, -1, type.getSourceFiles()[0].toURI().toURL() );
    }
    catch( MalformedURLException e )
    {
      throw new RuntimeException( e );
    }
  }

  private void resolveProperties( ISQLTableType type ) {


    _propertiesList = new ArrayList<>();
    _propertiesMap = new HashMap<>();


    //Get column References
    List<ColumnDefinition> columns = type.getColumnDefinitions();
    for(ColumnDefinition column : columns) {
      SQLColumnPropertyInfo prop = new SQLColumnPropertyInfo(column.getColumnName(), column.getPropertyName(),
        getGosuType(column.getSQLType()), this, column.getOffset(), column.getLength());
      _propertiesMap.put(prop.getName(), prop);
      _propertiesList.add( prop );
    }

    //Adding Foreign Key References that this table does

    for( Constraint c : _table.getConstraints()) {
      if (c.getType() == Constraint.constraintType.FOREIGN) {
        String keyName = c.getColumnNames().get(0);
        ColumnDefinition referer = _table.getColumnDefinitionByName(keyName);

        CreateTable foreignTable = null;
        for (CreateTable possibleForiegnTable : _system.getTables()) {
          if (possibleForiegnTable.getTableName().equals(c.getReferentialName())) {
            foreignTable = possibleForiegnTable;
          }
        }

        String foreignName = c.getReferentialColumnNames().get(0);
        ColumnDefinition referee = foreignTable.getColumnDefinitionByName(foreignName);


        SQLReferencePropertyInfo refProp = new SQLReferencePropertyInfo(referer.getColumnName(), referee.getPropertyName(),
          foreignTable.getTypeName(),
          _system,
          JavaTypes.getGosuType(SQLQuery.class).getParameterizedType(this.getOwnersType()),
          this, referer.getOffset(), referer.getLength());

        _propertiesMap.put(refProp.getName(), refProp);
        _propertiesList.add(refProp);


      }
    }

      for(CreateTable foreignTable : _system.getTables()){

        for( Constraint cons : foreignTable.getConstraints()) {

          if (cons.getType() == Constraint.constraintType.FOREIGN) {
            if (cons.getReferentialName().equals(_table.getTableName())) {

              String localKey = cons.getReferentialColumnNames().get(0);
              ColumnDefinition referee = _table.getColumnDefinitionByName(localKey);

              String foreignName = cons.getColumnNames().get(0);
              ColumnDefinition referer = foreignTable.getColumnDefinitionByName(foreignName);


              if (referee != null && referer != null) {
                SQLReferencePropertyInfo refProp = new SQLReferencePropertyInfo(referee.getColumnName(), referer.getPropertyName(),
                  foreignTable.getTypeName(),
                  _system,
                  JavaTypes.getGosuType(SQLQuery.class).getParameterizedType(this.getOwnersType()),
                  this, referer.getOffset(), referer.getLength());

                _propertiesMap.put(refProp.getName(), refProp);
                _propertiesList.add(refProp);
              }
              else{
                System.err.println("Error: Foreign Key Declaration does not match with table columns");
              }
            }
          }
        }
    }

    IPropertyInfo allProp = generateAllProperty();
    _propertiesList.add( allProp );
    _propertiesMap.put( allProp.getName(), allProp );

    // Adding Foreign Key References TO this table (The reverse query) TODO

    _domainLogic = maybeGetDomainLogic();
    createMethodInfos();
    createConstructorInfos();
  }

  private void createConstructorInfos() {
    List<IConstructorInfo> constructorInfos = new ArrayList<>();

    final String tableName = getOwnersType().getTable().getTableName();
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
      .withConstructorHandler( _constructor ).build(this);

    constructorInfos.add(constructorMethod);

    _constructorList = constructorInfos;
  }

  private void createMethodInfos() {
    MethodList methodList = new MethodList();

    for (String propertyName : _propertiesMap.keySet()) {
      IPropertyInfo prop = _propertiesMap.get(propertyName);

      methodList.add(generateFindByMethod(prop));
      methodList.add(generateFindByAllMethod(prop));
    }

    methodList.add(generateCreateMethod());
    methodList.add(generateWhereMethod());
    methodList.add(generateSelectMethod());

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
        .withName("findBy" + propertyName)
        .withDescription("Find single match based on the value of the " + propertyName + " column.")
        .withParameters(new ParameterInfoBuilder()
          .withName(propertyName)
          .withType(prop.getFeatureType())
          .withDescription("Performs strict matching on this argument"))
        .withReturnType(this.getOwnersType())
        .withStatic(true)
        .withCallHandler((ctx, args) -> {
          SQLQuery query = new SQLQuery(_md, getOwnersType());
          SQLConstraint constraint = SQLConstraint.isComparator(prop, args[0], "=");
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
        .withCallHandler((ctx, args) -> {
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
        .withCallHandler((ctx, args) -> ((SQLRecord) ctx).create())
        .build(this);
  }

  private IMethodInfo generateWhereMethod() {
    return new MethodInfoBuilder()
        .withName("where")
        .withDescription("Creates a new table query")
        .withParameters(new ParameterInfoBuilder().withName("condition").withType(TypeSystem.get(SQLConstraint.class)))
        .withReturnType(JavaTypes.ITERABLE().getParameterizedType(this.getOwnersType()))
        .withStatic(true)
        .withCallHandler((ctx, args) -> new SQLQuery<SQLRecord>(_md, this.getOwnersType()).where((SQLConstraint) args[0]))
        .build(this);
  }

  private IPropertyInfo generateAllProperty()
  {
    return new PropertyInfoBuilder()
      .withName( "All" )
      .withDescription( "Returns All " + getOwnersType() + "s" )
      .withType( TypeSystem.get( SQLQuery.class ).getParameterizedType( this.getOwnersType() ) )
      .withStatic( true )
      .withWritable( false )
      .withAccessor( new IPropertyAccessor()
      {
        @Override
        public Object getValue( Object ctx )
        {
          return new SQLQuery<SQLRecord>(_md, getOwnersType());
        }

        @Override
        public void setValue( Object ctx, Object value )
        {
          //ignore
        }
      } )
      .build( this );
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

  public ISQLTableType getOwnersType() {
    return (ISQLTableType) super.getOwnersType();
  }

  private IGosuClass maybeGetDomainLogic() {
    ISQLTableType tableType = getOwnersType();
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
          .collect(Collectors.toList());
    }
    else
    {
      return Collections.emptyList();
    }
  }

}
