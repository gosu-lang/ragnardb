package ragnardb.runtime

uses gw.lang.reflect.features.BoundPropertyReference

enhancement BoundPropertyRefEnhancement<R extends SQLRecord, T> : BoundPropertyReference<R, T> {

  function addListener(action : IListenerAction<R, T>) {
    (this.getPropertyInfo() as IHasListenableProperties).addListener(this.Ctx, action)
  }

  function clearListeners() {
    (this.getPropertyInfo() as IHasListenableProperties).clearListeners(this.Ctx)
  }

}
