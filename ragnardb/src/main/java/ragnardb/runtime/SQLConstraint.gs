package ragnardb.runtime

uses java.lang.*

abstract class SQLConstraint {

  static function isEqualTo( o: Object ): SQLConstraint{
    return new IsEqualToConstraint(o);
  }

  static function isIn( l: List<Object> ): SQLConstraint{
    return new IsInConstraint(l);
  }

  static function isLike( s: String ): SQLConstraint{
    return new IsLikeConstraint(s);
  }

  private static class IsEqualToConstraint extends SQLConstraint {
    var _obj : Object
    construct(obj : Object) {
      _obj = obj
    }
  }

  private static class IsInConstraint extends SQLConstraint {
    var _lst : List
    construct(l : List) {
      _lst = l
    }
  }

  private static class IsLikeConstraint extends SQLConstraint {
    var _str: Object
    construct( str: Object) {
      _str = str
    }
  }

}