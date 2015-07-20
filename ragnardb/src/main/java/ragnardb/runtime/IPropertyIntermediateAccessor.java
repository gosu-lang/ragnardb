package ragnardb.runtime;

import gw.lang.reflect.IPropertyAccessor;

/**
 * Created by kmoore on 7/20/15.
 */
public interface IPropertyIntermediateAccessor extends IPropertyAccessor {

  void setIntermediateValue(Object val);
}
