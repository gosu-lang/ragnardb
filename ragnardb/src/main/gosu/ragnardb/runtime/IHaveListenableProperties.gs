package ragnardb.runtime

uses gw.lang.reflect.IPropertyInfo

interface IHaveListenableProperties {

  function addListener(prop : IPropertyInfo, listener : block(Object):Object)

}