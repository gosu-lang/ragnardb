package ragnardb.plugin;
import gw.lang.reflect.*;
import gw.lang.reflect.features.PropertyReference;
import ragnardb.parser.ast.SQL;
import ragnardb.runtime.*;

import java.util.*;

import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.PropertyInfoBase;
import ragnardb.runtime.SQLRecord;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by pjennings on 7/14/2015.
 */



public class SQLReferencePropertyInfo extends PropertyInfoBase implements IPropertyInfo
{
  private String _tableName;
  private String _refColumnName;
  private String _idColumnName;
  private String _propName;
  private IType _propType;
  private IPropertyAccessor _accessor;
  private final int _offset;
  private final int _length;

  protected SQLReferencePropertyInfo(String refColumnName, String idColumnName, String foreignTableName, ISQLDdlType system,  IType propertyType, ITypeInfo container, int offset, int length)
  {
    super( container );
    _propName = foreignTableName;
    _propType = propertyType;
    _refColumnName = refColumnName;
    _idColumnName = idColumnName;
    _accessor = new IPropertyAccessor()
    {
      @Override
      public Object getValue( Object obj )
      {
        //Finding foreign table
        ISQLTableType foreignTable = null;
        for(ISQLTableType t : system.getTableTypes()){
          if(t.getRelativeName().equals(foreignTableName)){
            foreignTable = t;
          }
        }



        Object ref = ((SQLRecord) obj).getRawValue(_refColumnName);
        List args = new ArrayList<>();
        args.add(ref);
        SQLQuery query =  new SQLQuery<SQLRecord>(new SQLMetadata(),foreignTable);
        return query.where(SQLConstraint.raw(_idColumnName + " = ? ", args));

        //Is making a new metadata ok?
        //return ((SQLRecord)obj).getRawValue( _columnName );
      }

      @Override
      public void setValue(Object ctx, Object value) {

      }

    };
    _offset = offset;
    _length = length;
  }


  @Override
  public boolean isReadable()
  {
    return true;
  }

  @Override
  public boolean isWritable( IType iType )
  {
    return false;
  }

  @Override
  public IPropertyAccessor getAccessor()
  {
    return _accessor;
  }

  @Override
  public List<IAnnotationInfo> getDeclaredAnnotations()
  {
    return Collections.emptyList();
  }

  @Override
  public String getName()
  {
    return _propName;
  }

  @Override
  public IType getFeatureType()
  {
    return _propType;
  }

  @Override
  public int getOffset() {
    return _offset;
  }

  @Override
  public int getTextLength() {
    return _length;
  }
}
