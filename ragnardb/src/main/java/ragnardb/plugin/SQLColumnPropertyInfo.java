package ragnardb.plugin;

import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.PropertyInfoBase;
import ragnardb.runtime.IHasListenableProperties;
import ragnardb.runtime.IListenerAction;
import ragnardb.runtime.SQLRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SQLColumnPropertyInfo extends PropertyInfoBase implements IPropertyInfo, IHasListenableProperties
{
  private String _columnName;
  private String _propName;
  private IType _propType;
  private IPropertyAccessor _accessor;
  private final int _offset;
  private final int _length;

  protected SQLColumnPropertyInfo(String columnName, String propName, IType propertyType, SQLBaseTypeInfo container, int offset, int length)
  {
    super( container );
    _columnName = columnName;
    _propName = propName;
    _propType = propertyType;
    _accessor = new IPropertyAccessor()
    {
      @Override
      public Object getValue( Object obj ) {
        return ((SQLRecord) obj).getRawValue(_columnName);
      }

      @Override
      public void setValue( Object obj, Object val ) {
        //TODO look for listeners here
        fireListeners(obj);

//        ((ISQLTableType) getOwnersType()).fireListener(SQLColumnPropertyInfo.this);
        ((SQLRecord) obj).setRawValue(_columnName, val);
      }
    };
    _offset = offset;
    _length = length;
  }

  public String getColumnName()
  {
    return _columnName;
  }

  @Override
  public boolean isReadable()
  {
    return true;
  }

  @Override
  public boolean isWritable( IType iType )
  {
    return true;
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

  private List<IListenerAction> _actions = new ArrayList<>();

  @Override
  public void addListener(IListenerAction action) {
    _actions.add(action);
  }

  @Override
  public void fireListeners(Object ctx) {
    for(IListenerAction action : _actions) {
      action.action(ctx);
    }
  }

  @Override
  public void clearListeners() {
    _actions.clear();
  }

}
