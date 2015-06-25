package ragnardb.plugin;

import gw.fs.FileFactory;
import gw.fs.ResourcePath;
import gw.fs.physical.IPhysicalFileSystem;
import gw.fs.physical.PhysicalResourceImpl;
import ragnardb.parser.SQLParser;
import ragnardb.parser.SQLTokenizer;
import ragnardb.parser.ast.CreateTable;
import ragnardb.parser.ast.DDL;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

public class SQLSource extends PhysicalResourceImpl implements ISQLSource {

  private DDL parseTree;

  private void setParseTree(){
    SQLParser p = null;
    try {
     p = new SQLParser(new SQLTokenizer(getReader()));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    parseTree = p.parse();
  }

  public SQLSource(ResourcePath path, IPhysicalFileSystem backingFileSystem) {
    super(path, backingFileSystem);
    setParseTree();
  }

  public DDL getParseTree(){
    return parseTree;
  }

  public SQLSource(ResourcePath path) {
    this(path, FileFactory.instance().getDefaultPhysicalFileSystem());
  }

  @Override
  public Set<String> getTypeNames() {
    Set<String> returnSet = new HashSet<>();

    for (CreateTable table: parseTree.getList()){
          String name = table.getName();
          returnSet.add(Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }
    return returnSet;
  }

  @Override
  public Reader getReader() throws FileNotFoundException {
    return new FileReader(this._path.getFileSystemPathString());
  }

}
