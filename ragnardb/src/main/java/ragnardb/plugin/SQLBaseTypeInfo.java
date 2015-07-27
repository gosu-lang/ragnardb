package ragnardb.plugin;

import gw.lang.reflect.*;
import gw.lang.reflect.java.JavaTypes;

import java.sql.Types;
import java.util.*;

public abstract class SQLBaseTypeInfo extends BaseTypeInfo {
  protected List<IPropertyInfo> _propertiesList;
  protected Map<String, IPropertyInfo> _propertiesMap;
  protected MethodList _methodList;
  protected List<IConstructorInfo> _constructorList;

  public SQLBaseTypeInfo(IFileBasedType type) {
    super(type);
  }

  @Override
  public IFileBasedType getOwnersType()
  {
    return (IFileBasedType)super.getOwnersType();
  }

  /**
   * Sourced from "JDBC Types Mapped to Java Types" at https://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html
   * @param sqlType
   * @return
   */
  protected IType getGosuType(int sqlType) {
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

}
