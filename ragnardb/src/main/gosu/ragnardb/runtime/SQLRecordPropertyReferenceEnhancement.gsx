package ragnardb.runtime

uses gw.lang.reflect.features.IPropertyReference

enhancement SQLRecordPropertyReferenceEnhancement : IPropertyReference<SQLRecord, Object> {

  function isEqualTo( o : Object ) : SQLConstraint {
    return SQLConstraint.isEqualTo(this.PropertyInfo, o)
  }

  function isIn( l : List<Object> ) : SQLConstraint {
    return SQLConstraint.isIn(this.PropertyInfo,  l)
  }

  function isLike( s : String ) : SQLConstraint {
    return SQLConstraint.isLike(this.PropertyInfo, s)
  }






}
