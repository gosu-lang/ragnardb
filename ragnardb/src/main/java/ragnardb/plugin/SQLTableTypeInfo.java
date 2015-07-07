package ragnardb.plugin;

import gw.lang.reflect.*;
import gw.lang.reflect.java.JavaTypes;
import ragnardb.runtime.SQLConstraint;
import ragnardb.runtime.SQLMetadata;
import ragnardb.runtime.SQLQuery;
import ragnardb.runtime.SQLRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SQLTableTypeInfo extends SQLBaseTypeInfo {

  private SQLMetadata _md = new SQLMetadata();

  public SQLTableTypeInfo(ISQLTableType type) {
    super(type);
    resolveProperties(type);
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
    createMethodInfos();
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

  private void createMethodInfos() {
    MethodList methodList = new MethodList();

    for (String propertyName : _propertiesMap.keySet()) {
      SQLColumnPropertyInfo prop = (SQLColumnPropertyInfo) _propertiesMap.get(propertyName);

      methodList.add(generateFindByMethod(prop));
      methodList.add(generateFindByAllMethod(prop));
    }

    methodList.add(generateCreateMethod());
    methodList.add(generateInitMethod());
    methodList.add(generateWhereMethod());

    List<? extends IMethodInfo> domainMethods = maybeGetDomainMethods();
    List<? extends IPropertyInfo> domainProperties = maybeGetDomainProperties();

    for(IMethodInfo domainMethod : domainMethods) {
      methodList.add(domainMethod);
    }

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
          SQLConstraint constraint = SQLConstraint.isEqualTo(prop, args[0]);
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
          SQLConstraint constraint = SQLConstraint.isEqualTo(prop, args[0]);
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

  private IMethodInfo generateInitMethod() {
    return new MethodInfoBuilder()
        .withName("init")
        .withDescription("Creates a new table entry")
        .withParameters()
        .withReturnType(this.getOwnersType())
        .withStatic(true)
        .withCallHandler(( ctx, args ) -> new SQLRecord(((ISQLTableType) getOwnersType()).getTable().getTableName(), "id"))
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

  /**
   * TODO singularize fqn properly
   * @return
   */
  private IType maybeGetDomainLogic() {
    ISQLTableType tableType = (ISQLTableType) getOwnersType();
    final String domainLogicPackageSuffix = "Extensions."; //TODO make constant
    final String domainLogicTableSuffix = "Ext"; //TODO make constant
    final String domainLogicFqn = tableType.getNamespace() + domainLogicPackageSuffix + tableType.getRelativeName() + domainLogicTableSuffix;
    return TypeSystem.getByFullNameIfValid(domainLogicFqn);
  }

  private List<? extends IMethodInfo> maybeGetDomainMethods() {
    List<IMethodInfo> methodList = Collections.emptyList();

    final IType domainLogic = maybeGetDomainLogic();

    if (domainLogic != null) {
      methodList = new ArrayList<>();
      final IRelativeTypeInfo domainLogicTypeInfo = (IRelativeTypeInfo) domainLogic.getTypeInfo();
      List<? extends IMethodInfo> domainMethods = domainLogicTypeInfo.getDeclaredMethods()
          .stream()
          .filter(IAttributedFeatureInfo::isPublic)
          .filter(method -> !method.getName().startsWith("@"))
          .collect(Collectors.toList());

      for (IMethodInfo method : domainMethods) {
        final IParameterInfo[] params = method.getParameters();
        ParameterInfoBuilder[] paramInfos = new ParameterInfoBuilder[params.length];
        for(int i = 0; i < params.length; i++) {
          IParameterInfo param = params[i];
          paramInfos[i] = new ParameterInfoBuilder().like(param);
        }
        IMethodInfo syntheticMethod = new MethodInfoBuilder()
            .withName(method.getDisplayName())
            .withDescription(method.getDescription())
            .withParameters(paramInfos)
            .withReturnType(method.getReturnType())
            .withStatic(method.isStatic())
            .withCallHandler(method.getCallHandler())
            .build(this);

        methodList.add(syntheticMethod);
      }
    }
    return methodList;
  }

  private List<? extends IPropertyInfo> maybeGetDomainProperties() {
    List<IPropertyInfo> propertyList = Collections.emptyList();

    final IType domainLogic = maybeGetDomainLogic();

    if (domainLogic != null) {
      propertyList = new ArrayList<>();
      final IRelativeTypeInfo domainLogicTypeInfo = (IRelativeTypeInfo) domainLogic.getTypeInfo();
      List<? extends IPropertyInfo> domainProperties = domainLogicTypeInfo.getDeclaredProperties()
          .stream()
          .filter(IAttributedFeatureInfo::isPublic)
          .collect(Collectors.toList());

      for (IPropertyInfo prop : domainProperties) {
        IPropertyInfo syntheticProperty = new PropertyInfoBuilder()
            .withName(prop.getName())
            .withDescription(prop.getDescription())
            .withStatic(prop.isStatic())
            .withWritable(prop.isWritable())
            .withType(prop.getFeatureType())
            .withAccessor(prop.getAccessor())
            .build(this);

        propertyList.add(syntheticProperty);
      }
    }
    return propertyList;
  }

}
