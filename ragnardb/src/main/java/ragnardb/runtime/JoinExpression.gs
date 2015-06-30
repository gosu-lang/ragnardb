package ragnardb.runtime

uses gw.lang.reflect.IPropertyInfo

class JoinExpression {

  private final var _left: IPropertyInfo
  private final var _right: IPropertyInfo

  construct( left: IPropertyInfo, right: IPropertyInfo ){
    _left = left
    _right = right
  }
}