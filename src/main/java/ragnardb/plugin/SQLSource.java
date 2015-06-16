package ragnardb.plugin;

import gw.fs.IDirectory;
import gw.fs.IResource;
import gw.fs.ResourcePath;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class SQLSource implements ISQLSource { // TODO extend gw.fs.physical.PhysicalResourceImpl and get rid of useless overrides?

  private IResource _file;

  public SQLSource(IResource file) {
    _file = file;

  }

  @Override
  public Set<String> getTypeNames() {
    return Collections.singleton("Contacts"); //TODO incorporate parser output here
  }

  @Override
  public IDirectory getParent() {
    return _file.getParent();
  }

  @Override
  public String getName() {
    return _file.getName();
  }

  @Override
  public boolean exists() {
    return _file.exists();
  }

  @Override
  public boolean delete() throws IOException {
    return _file.delete();
  }

  @Override
  public URI toURI() {
    return _file.toURI();
  }

  @Override
  public ResourcePath getPath() {
    return _file.getPath();
  }

  @Override
  public boolean isChildOf(IDirectory iDirectory) {
    return _file.isChildOf(iDirectory);
  }

  @Override
  public boolean isDescendantOf(IDirectory iDirectory) {
    return _file.isDescendantOf(iDirectory);
  }

  @Override
  public File toJavaFile() {
    return _file.toJavaFile();
  }

  @Override
  public boolean isJavaFile() {
    return _file.isJavaFile();
  }

  @Override
  public boolean isInJar() {
    return _file.isInJar();
  }

  @Override
  public boolean create() {
    return _file.create();
  }
}
