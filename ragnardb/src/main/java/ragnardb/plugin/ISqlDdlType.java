package ragnardb.plugin;

import gw.lang.parser.IHasInnerClass;
import gw.lang.reflect.IFileBasedType;
import gw.lang.reflect.IType;
import ragnardb.parser.ast.CreateTable;

import java.io.IOException;
import java.util.List;

public interface ISqlDdlType extends IType, IHasInnerClass, IFileBasedType {
  String getSqlSource() throws IOException;

  /**
   * The names of all the SQL types available from this source
   * @return a non null set of SQL type names
   */
  List<CreateTable> getTables();

}
