package ragnardb.runtime;

import gw.lang.reflect.IPropertyInfo;

import java.util.Map;

public interface IHasListenableProperties {

  void addListener(IListenerAction action);

  void fireListeners(Object ctx);

  void clearListeners();

}
