package ragnardb.plugin;

import gw.fs.IFile;
import gw.fs.IResource;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SQLSource implements ISQLSource {

  private IResource _file;

  public SQLSource(IResource file) {
    _file = file;


  }

  @Override
  public Set<String> getTypeNames() {
    return Collections.singleton(_file.getName());
  }

  @Override
  public IType getTypeByName(String name) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public IFile getFile(String name) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
