package ragnardb.plugin;

import gw.fs.FileFactory;
import gw.fs.ResourcePath;
import gw.fs.physical.IPhysicalFileSystem;
import gw.fs.physical.PhysicalResourceImpl;

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
    switch(this._path.getName()) {
      case("Users.ddl"):
        returnSet.add("Contacts");
        break;
      case("Vehicles.ddl"):
        returnSet.add("Cars");
        returnSet.add("Motorcycles");
        break;
      default: //noop
    }
    return returnSet;
  }

}
