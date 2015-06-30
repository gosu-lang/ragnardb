package ragnardb.runtime

enhancement IQueryStuffEnhancements : IQueryStuff{

  function isEqualTo( o : Object ) : SQLConstraint {
    return SQLConstraint.isEqualTo(o)
  }

  function isIn( l : List<Object> ) : SQLConstraint {
    return SQLConstraint.isIn( l )
  }

  function isLike( s : String ) : SQLConstraint {
    return SQLConstraint.isLike(s)
  }
}
