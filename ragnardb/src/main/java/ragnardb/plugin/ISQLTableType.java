package ragnardb.plugin;

import gw.lang.reflect.IType;
import ragnardb.parser.ast.CreateTable;

import java.util.List;

public interface ISQLTableType extends IType {

  CreateTable getTable();

  List<ColumnDefinition> getColumnDefinitions();

  void deleteAll( boolean confirm );
}
