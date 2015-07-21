package ragnardb.runtime;

import gw.lang.reflect.IPropertyInfo;

import java.util.Map;

public interface IHasListenableProperties {

  void addListener(Object ctx, IListenerAction action);

  void fireListeners(Object ctx);

  void clearListeners(Object ctx);

  void clearAllListeners();

}
