package ragnardb.runtime

uses gw.lang.reflect.IPropertyInfo
uses gw.lang.reflect.IType
uses gw.lang.reflect.features.IPropertyReference

uses java.lang.Iterable
uses java.util.Iterator

class SQLQuery<T> implements Iterable<T>{

  var _rootType : IType as readonly RootType
  var _joinExpr: JoinExpression as JoinExpr
  var _whereExpr: WhereExpression as WhereExpr
  var _pickBlock: IPropertyInfo as Pluck
  var _parent: SQLQuery as Parent
  var _metadata: ITypeToSQLMetadata as Metadata

  construct(me : ITypeToSQLMetadata) {
    _metadata = me
    _rootType = T
  }

  function pluck<U>( pi: IPropertyReference<T, U> ): SQLQuery<U>{
    return new SQLQuery<U>(Metadata){
        :Parent = this,
        :Pluck = pi.PropertyInfo
        }
  }

  function where(c: SQLConstraint ): SQLQuery<T>{
    return new SQLQuery<T>(Metadata){
        :Parent = this,
        :WhereExpr = new WhereExpression( c )
        }
  }

  function join( left: IPropertyReference, right: IPropertyReference ): SQLQuery<T>{
    return new SQLQuery<T>(Metadata){
        :Parent = this,
        :JoinExpr = new JoinExpression( left.PropertyInfo, right.PropertyInfo )
        }
  }

  override function iterator(): Iterator<T>{
    return execQuery().iterator()
  }

  private property get Root() : SQLQuery {
    if(_parent != null) {
      return _parent.Root
    } else {
      return this
    }
  }

  private function genSQL() : String {
    var select =  "SELECT *";
    var from = "FROM ${_metadata.getTableForType( Root.RootType )}"
    var where = _whereExpr == null ? "" : "WHERE " + _whereExpr.getSQL( _metadata )
    return "${select} ${from} ${where}"
  }

  private function getArgs() : List {
    return _whereExpr.getArgs();
  }

  private function execQuery(): Iterable<T>{
    return SQLRecord.select( genSQL(), getArgs(), T )
  }
}