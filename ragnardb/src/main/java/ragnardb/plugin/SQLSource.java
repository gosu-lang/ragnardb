package ragnardb.plugin;

import gw.fs.FileFactory;
import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.fs.ResourcePath;
import gw.fs.physical.PhysicalResourceImpl;
import gw.lang.reflect.module.IModule;
import ragnardb.parser.SQLParser;
import ragnardb.parser.SQLTokenizer;
import ragnardb.parser.ast.CreateTable;
import ragnardb.parser.ast.DDL;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

public class SQLSource extends PhysicalResourceImpl implements ISQLSource {

  private DDL _parseTree;
  private IFile _file;

  public SQLSource(IFile file) {
    super( file.getPath(), FileFactory.instance().getDefaultPhysicalFileSystem() );
    _file = file;
    setParseTree();
  }

  public DDL getParseTree(){
    return _parseTree;
  }

  @Override
  public IFile getFile() {
    return _file;
  }

  private void setParseTree(){
    SQLParser p = null;
    try {
      p = new SQLParser(new SQLTokenizer(getReader()));
    } catch (Exception e) {
      throw new RuntimeException( e );
    }
    _parseTree = p.parse();
  }

  @Override
  public String getTypeName( IModule module ) {
    ResourcePath path = getPath();
    for( IDirectory dir: module.getSourcePath() ) {
      if( dir.getPath().isDescendant( path ) ) {
        String rawName = dir.getPath().relativePath(path);
        rawName = rawName.substring(0, rawName.lastIndexOf('.'));
        rawName = rawName.replace(File.separator, ".");
        return rawName;
      }
    }
    throw new IllegalStateException( "Expected to have name" );
  }

  @Override
  public Reader getReader() throws IOException {
    return new InputStreamReader( _file.openInputStream() );
  }

}
