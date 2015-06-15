package ragnardb.plugin;

import gw.fs.FileFactory;
import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.fs.IResource;
import gw.lang.reflect.*;
import gw.lang.reflect.gs.TypeName;
import gw.lang.reflect.module.IModule;

import java.io.File;
import java.net.URL;
import java.util.*;

public class SQLPlugin extends TypeLoaderBase {

    private static final String FILE_EXTENSION = "ddl";

    private Set<IResource> _sources; //TODO populate set of all DDL files on disk? Use IFile?
    private Map<ISQLSource, Set<String>> _sqlTypeNames = new HashMap<>();

    public SQLPlugin(IModule module) {
      super(module);
      _sources = new HashSet<>();
      FileFactory ff = FileFactory.instance();
      _sources.add(ff.getIFile(new File("src/test/resources/Foo/Users.ddl"))); //TODO unhack me

      //populate _sqlTypes
      for(IResource source : _sources) {
        ISQLSource newGuy = new SQLSource(source);
        _sqlTypeNames.put(newGuy, newGuy.getTypeNames());


      }

    }

    @Override
    public IType getType(String name) {
      for(ISQLSource source : _sqlTypeNames.keySet()) {
        //Set<String> namedTypes = source.getTypeNames();
        IType result = TypeSystem.getTypeReference(new SQLType(this, name));
        return result;
//        Set<IType> typesInSource = source.getTypes();
//        for(IType theType : typesInSource) {
//          if(name == theType.getName()) {
//            return theType;
//          }
//        }
      }
      return null;
    }

  @Override
  public Set<? extends CharSequence> getAllNamespaces() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public List<String> getHandledPrefixes() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean handlesNonPrefixLoads() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void refreshedNamespace(String s, IDirectory iDirectory, RefreshKind refreshKind) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean hasNamespace(String s) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Set<String> computeTypeNames() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }


}
