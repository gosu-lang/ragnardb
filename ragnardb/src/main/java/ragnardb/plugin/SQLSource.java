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

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class SQLSource extends PhysicalResourceImpl implements ISQLSource {

  private DDL parseTree;
  private IFile _file;

  public SQLSource(IFile file) {
    super( file.getPath(), FileFactory.instance().getDefaultPhysicalFileSystem() );
    _file = file;
    setParseTree();
  }

  public DDL getParseTree(){
    return parseTree;
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
    parseTree = p.parse();
  }

  @Override
  public List<CreateTable> getTables() {
    return parseTree.getList().stream().collect(Collectors.toList());
//    List<CreateTable> returnSet = new ArrayList<>();
//
//    for (CreateTable table : parseTree.getList()){
//          returnSet.add(table);
//    }
//    return returnSet;
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
