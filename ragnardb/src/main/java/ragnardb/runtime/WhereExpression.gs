package ragnardb.runtime

class WhereExpression {

  private final var _constraint: SQLConstraint

  construct( c: SQLConstraint ){
    _constraint = c
  }

  internal function getSQL(metadata: ITypeToSQLMetadata) : String {
    return _constraint.getSQL(metadata)
  }

  function getArgs(): List<Object>{
    return _constraint.getArgs();
  }
}