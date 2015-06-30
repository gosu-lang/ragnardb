package ragnardb.runtime

uses gw.lang.reflect.features.IPropertyReference

uses java.lang.*

abstract class SQLConstraint {

  var _propertyReference : IPropertyReference as readonly PropertyReference

  construct(pr : IPropertyReference) {
    _propertyReference = pr
  }

  static function isEqualTo( pr: IPropertyReference, o: Object ): SQLConstraint{
    return new IsEqualToConstraint(pr, o);
  }

  static function isIn( pr: IPropertyReference, l: List<Object> ): SQLConstraint{
    return new IsInConstraint(pr, l);
  }

  static function isLike( pr: IPropertyReference, s: String ): SQLConstraint{
    return new IsLikeConstraint(pr, s);
  }

  abstract function getSQL( metadata: ITypeToSQLMetadata ): String

  abstract function getArgs(): List<Object>

  private static class IsEqualToConstraint extends SQLConstraint {
    var _obj : Object
    construct(pr : IPropertyReference, obj : Object) {
      super(pr)
      _obj = obj
    }
    function getSQL( metadata: ITypeToSQLMetadata ): String {
      return metadata.getColumnForProperty( PropertyReference.PropertyInfo  ) + "=?"
    }
    function getArgs() : List<Object> {
      return {_obj}
    }
  }

  private static class IsInConstraint extends SQLConstraint {
    var _lst : List
    construct(pr : IPropertyReference, l : List) {
      super(pr)
      _lst = l
    }
    function getSQL( metadata: ITypeToSQLMetadata  ): String {
      throw "Not implemented"
    }
    function getArgs() : List<Object> {
      return {}
    }
  }

  private static class IsLikeConstraint extends SQLConstraint {
    var _str: Object
    construct( pr : IPropertyReference, str: Object) {
      super(pr)
      _str = str
    }
    function getSQL( metadata: ITypeToSQLMetadata  ): String {
      throw "Not implemented"
    }
    function getArgs() : List<Object> {
      return {}
    }
  }

}