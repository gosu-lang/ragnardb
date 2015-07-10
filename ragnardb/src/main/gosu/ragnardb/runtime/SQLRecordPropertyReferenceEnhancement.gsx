package ragnardb.runtime

uses gw.lang.reflect.features.IPropertyReference

enhancement SQLRecordPropertyReferenceEnhancement : IPropertyReference<SQLRecord, Object> {

  function isEqualTo( o : Object ) : SQLConstraint {
    return SQLConstraint.isComparator(this.PropertyInfo, o, "=")
  }

  function isLessThan( o : Object ) : SQLConstraint {
    return SQLConstraint.isComparator(this.PropertyInfo, o, "<")
  }

  function isGreaterThan( o : Object ) : SQLConstraint {
    return SQLConstraint.isComparator(this.PropertyInfo, o, ">")
  }

  function isLessOrEqual( o : Object ) : SQLConstraint {
    return SQLConstraint.isComparator(this.PropertyInfo, o, "<=")
  }

  function isGreaterOrEqual( o : Object ) : SQLConstraint {
    return SQLConstraint.isComparator(this.PropertyInfo, o, ">=")
  }

  function isNotEqualTo( o : Object ) : SQLConstraint {
    return SQLConstraint.isComparator(this.PropertyInfo, o, "<>")
  }

  function isIn( l : List<Object> ) : SQLConstraint {
    return SQLConstraint.isIn(this.PropertyInfo,  l)
  }

  function isInQuery( s : SQLQuery<Object> ) : SQLConstraint {
    return SQLConstraint.isIn(this.PropertyInfo,  s)
  }


  function isLike( s : String ) : SQLConstraint {
    return SQLConstraint.isLike(this.PropertyInfo, s)
  }








}
