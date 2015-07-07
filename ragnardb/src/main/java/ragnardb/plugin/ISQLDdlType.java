package ragnardb.plugin;

import gw.lang.parser.IHasInnerClass;

import ragnardb.parser.ast.CreateTable;

import java.util.List;

public interface ISQLDdlType extends IHasInnerClass, ISQLTypeBase {
  List<CreateTable> getTables();
}
