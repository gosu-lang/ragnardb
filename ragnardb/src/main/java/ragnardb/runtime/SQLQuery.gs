package ragnardb.runtime

uses gw.lang.reflect.IPropertyInfo
uses gw.lang.reflect.IType
uses gw.lang.reflect.features.IPropertyReference

uses java.lang.Iterable
uses java.util.Iterator

class SQLQuery<T> implements Iterable<T>{

  var _table: IType as Table
  var _joinExpr: JoinExpression as JoinExpr
  var _whereExprs: WhereExpression as WhereExpr
  var _pickBlock: IPropertyInfo as Pick
  var _parent: SQLQuery as Parent

  function pluck<U>( pi: IPropertyReference<T, U> ): SQLQuery<U>{
    return new SQLQuery<U>(){
        :Parent = this,
        :Pick = pi.PropertyInfo
        }
  }

  function where( pi: IPropertyReference, c: SQLConstraint ): SQLQuery<T>{
    return new SQLQuery<T>(){
        :Parent = this,
        :WhereExpr = new WhereExpression( pi.PropertyInfo, c )
        }
  }

  function join( left: IPropertyReference, right: IPropertyReference ): SQLQuery<T>{
    return new SQLQuery<T>(){
        :Parent = this,
        :JoinExpr = new JoinExpression( left.PropertyInfo, right.PropertyInfo )
        }
  }

  override function iterator(): Iterator<T>{
    return execQuery().iterator()
  }

  private function execQuery(): Iterable<T>{
    return {}
  }
}