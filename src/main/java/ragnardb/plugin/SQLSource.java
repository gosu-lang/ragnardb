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
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

public class SQLSource extends PhysicalResourceImpl implements ISQLSource {

  public SQLSource(ResourcePath path, IPhysicalFileSystem backingFileSystem) {
    super(path, backingFileSystem);
  }

  public SQLSource(ResourcePath path) {
    this(path, FileFactory.instance().getDefaultPhysicalFileSystem());
  }

  @Override
  public Set<String> getTypeNames() { //TODO incorporate parser output here
    Set<String> returnSet = new HashSet<>();
    SQLParser p = null;
    try {
      p = new SQLParser(new SQLTokenizer( new FileReader(this._path.getFileSystemPathString())));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    DDL DDLfile = p.parse();
    for (CreateTable table: DDLfile.getList()){
          String name = table.getName();
          returnSet.add(Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }
    return returnSet;
  }

}
