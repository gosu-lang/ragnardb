package ragnardb.plugin;

import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.RefreshKind;
import gw.lang.reflect.RefreshRequest;
import gw.lang.reflect.gs.TypeName;
import gw.lang.reflect.module.IModule;

import java.net.URL;
import java.util.List;
import java.util.Set;

public class SQLPlugin implements ITypeLoader
{
    @Override
    public IModule getModule() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IType getType(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<? extends CharSequence> getAllTypeNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean showTypeNamesInIDE() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<? extends CharSequence> getAllNamespaces() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public URL getResource(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isCaseSensitive() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
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
    public boolean handlesFile(IFile iFile) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String[] getTypesForFile(IFile iFile) {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RefreshKind refreshedFile(IFile iFile, String[] strings, RefreshKind refreshKind) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void refreshedNamespace(String s, IDirectory iDirectory, RefreshKind refreshKind) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void refreshedTypes(RefreshRequest refreshRequest) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void refreshed() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean handlesDirectory(IDirectory iDirectory) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getNamespaceForDirectory(IDirectory iDirectory) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasNamespace(String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<TypeName> getTypeNames(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<String> computeTypeNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void shutdown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInited() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void init() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void uninit() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
