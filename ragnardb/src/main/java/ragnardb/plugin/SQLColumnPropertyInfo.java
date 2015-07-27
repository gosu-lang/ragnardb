package ragnardb.plugin;

import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.ILocationInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.LocationInfo;
import gw.lang.reflect.PropertyInfoBase;
import ragnardb.runtime.IHasListenableProperties;
import ragnardb.runtime.IListenerAction;
import ragnardb.runtime.IPropertyIntermediateAccessor;
import ragnardb.runtime.SQLRecord;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLColumnPropertyInfo extends PropertyInfoBase implements IPropertyInfo, IHasListenableProperties
{
  private String _columnName;
  private String _propName;
  private IType _propType;
  private IPropertyAccessor _accessor;
  private final ILocationInfo _location;

  protected SQLColumnPropertyInfo(String columnName, String propName, IType propertyType, SQLBaseTypeInfo container, int offset, int length)
  {
    super( container );
    _columnName = columnName;
    _propName = propName;
    _propType = propertyType;
    _accessor = new IPropertyIntermediateAccessor() {

      private Object _intermediateValue = null;

      @Override
      public Object getValue( Object obj ) {
        return _intermediateValue != null ? _intermediateValue : ((SQLRecord) obj).getRawValue(_columnName);
      }

      /**
       * Allows multiple mutations of a property without a db commit; prevents recursive loops
       * @param val
       */
      @Override
      public void setIntermediateValue( Object val ) {
        _intermediateValue = val;
      }

      @Override
      public void setValue( Object obj, Object val ) {
        _intermediateValue = val;
        fireListeners(obj);
        ((SQLRecord) obj).setRawValue(_columnName, _intermediateValue);
        _intermediateValue = null;
      }
    };
    try
    {
      _location = new LocationInfo( offset, length, -1, -1, container.getOwnersType().getSourceFiles()[0].toURI().toURL() );
    }
    catch( MalformedURLException e )
    {
      throw new RuntimeException( e );
    }
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
  public ILocationInfo getLocationInfo()
  {
    return _location;
  }

  //unbound actions will have a null key
  private Map<Object, List<IListenerAction>> _actions = new HashMap<>();

  @Override
  public void addListener(Object ctx, IListenerAction action) {
    List<IListenerAction> toBeAdded = new ArrayList<>();
    List<IListenerAction> existingActions = _actions.get(ctx);
    if(existingActions != null) {
      toBeAdded.addAll(existingActions);
    }
    toBeAdded.add(action);
    _actions.put(ctx, toBeAdded);
  }

  @Override
  public void fireListeners(Object ctx) {
    List<IListenerAction> unboundListeners = _actions.get(null);
    List<IListenerAction> boundListeners = _actions.get(ctx);

    //fire unbound listeners first
    if(unboundListeners !=null && !unboundListeners.isEmpty()) {
      for (IListenerAction action : unboundListeners) {
        ((IPropertyIntermediateAccessor) _accessor).setIntermediateValue(action.execute(ctx));
      }
    }
    //fire bound listeners
    if(boundListeners != null && !boundListeners.isEmpty()) {
      for (IListenerAction action : boundListeners) {
        ((IPropertyIntermediateAccessor) _accessor).setIntermediateValue(action.execute(ctx));
      }
    }
  }

  @Override
  public void clearListeners(Object ctx) {
    _actions.put(ctx, Collections.emptyList());
  }

  @Override
  public void clearAllListeners() {
    _actions.clear();
  }

}
