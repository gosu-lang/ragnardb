package ragnardb.plugin;

import gw.fs.IFile;
import gw.fs.IResource;
import gw.lang.reflect.module.IModule;
import ragnardb.parser.ast.CreateTable;
import ragnardb.parser.ast.DDL;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public interface ISQLSource extends IResource {

  Reader getReader() throws IOException;

  String getTypeName( IModule module );

  DDL getParseTree();

  IFile getFile();
}
