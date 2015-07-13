package ragnardb.foo.BarExt

uses ragnardb.runtime.SQLRecord

public class BazExt extends SQLRecord {

  public function sayHi(arg : String) {
    print("Hi, ${arg}")
  }

  property get MeaningOfLife() : int {
    return 42
  }

}