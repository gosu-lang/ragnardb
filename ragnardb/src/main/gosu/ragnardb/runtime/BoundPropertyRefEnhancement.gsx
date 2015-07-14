package ragnardb.runtime

uses gw.lang.reflect.features.BoundPropertyReference

enhancement BoundPropertyRefEnhancement<T> : BoundPropertyReference<IHasListenableProperties, IListenerAction> {

  function addListener(blk : IListenerAction) {
    var root = this.Ctx as IHasListenableProperties
    root.addListener(this.PropertyInfo, blk)
  }

}