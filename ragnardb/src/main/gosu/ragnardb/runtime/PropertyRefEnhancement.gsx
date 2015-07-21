package ragnardb.runtime

uses gw.lang.reflect.features.PropertyReference

enhancement PropertyRefEnhancement<R extends SQLRecord, T> : PropertyReference<R, T> {

  function addListener(action : IListenerAction<R, T>) {
    (this.getPropertyInfo() as IHasListenableProperties).addListener(null, action)
  }

  function clearListeners() {
    (this.getPropertyInfo() as IHasListenableProperties).clearListeners(null)
  }

}
