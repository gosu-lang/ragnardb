package ragnardb.runtime

uses gw.lang.reflect.IPropertyInfo

class WhereExpression {

  private final var _propertyInfo: IPropertyInfo
  private final var _constraint: SQLConstraint

  construct( propertyInfo: IPropertyInfo, c: SQLConstraint ){
    _propertyInfo = propertyInfo
    _constraint = c
  }
}