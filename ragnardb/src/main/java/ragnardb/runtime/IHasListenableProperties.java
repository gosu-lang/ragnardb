package ragnardb.runtime;

import gw.lang.reflect.IPropertyInfo;

import java.util.Map;

public interface IHasListenableProperties {

  void addListener(IPropertyInfo prop, IListenerAction action);

  void fireListener(IPropertyInfo prop);

}
