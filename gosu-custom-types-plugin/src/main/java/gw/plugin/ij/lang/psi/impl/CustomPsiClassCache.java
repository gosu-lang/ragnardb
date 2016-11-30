/*
 * Copyright 2014 Guidewire Software, Inc.
 */

package gw.plugin.ij.lang.psi.impl;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.PsiModificationTrackerImpl;
import gw.config.CommonServices;
import gw.fs.IFile;
import gw.lang.parser.IHasInnerClass;
import gw.lang.reflect.AbstractTypeSystemListener;
import gw.lang.reflect.IAttributedFeatureInfo;
import gw.lang.reflect.IConstructorInfo;
import gw.lang.reflect.IDefaultTypeLoader;
import gw.lang.reflect.IEnumData;
import gw.lang.reflect.IEnumType;
import gw.lang.reflect.IFeatureInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.IHasParameterInfos;
import gw.lang.reflect.ILocationInfo;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IParameterInfo;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IRelativeTypeInfo;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.MethodList;
import gw.lang.reflect.Modifier;
import gw.lang.reflect.RefreshRequest;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.GosuClassTypeLoader;
import gw.lang.reflect.gs.IGosuClass;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.java.JavaTypes;
import gw.lang.reflect.module.IModule;
import gw.plugin.ij.custom.JavaFacadePsiClass;
import gw.plugin.ij.filesystem.IDEAFile;
import gw.plugin.ij.util.FileUtil;
import java.net.URL;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CustomPsiClassCache extends AbstractTypeSystemListener
{
  private static final CustomPsiClassCache INSTANCE = new CustomPsiClassCache();

  @NotNull
  public static CustomPsiClassCache instance()
  {
    return INSTANCE;
  }

  private final ConcurrentHashMap<String, JavaFacadePsiClass> _psi2Class = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<IModule, Map<String, JavaFacadePsiClass>> _type2Class = new ConcurrentHashMap<>();

  private CustomPsiClassCache()
  {
    TypeSystem.addTypeLoaderListenerAsWeakRef( this );
  }

  public JavaFacadePsiClass getPsiClass( @NotNull IType type )
  {
    List<VirtualFile> typeResourceFiles = FileUtil.getTypeResourceFiles( type );
    if( typeResourceFiles.isEmpty() )
    {
      return null;
    }

    IModule module = type.getTypeLoader().getModule();
    String name = type.getName();
    Map<String, JavaFacadePsiClass> map = _type2Class.get( module );
    if( map == null )
    {
      map = new ConcurrentHashMap<>();
      _type2Class.put( module, map );
    }
    JavaFacadePsiClass psiFacadeClass = map.get( name );
    if( psiFacadeClass == null || !psiFacadeClass.isValid() )
    {
      PsiClass delegate = createPsiClass( type );
      psiFacadeClass = new JavaFacadePsiClass( delegate, type );
      map.put( name, psiFacadeClass );
      _psi2Class.put( type.getSourceFiles()[0].getPath().getPathString(), psiFacadeClass );
    }
    return psiFacadeClass;
  }

  @NotNull
  private PsiClass createPsiClass( @NotNull IType type )
  {
    PsiManager manager = PsiManagerImpl.getInstance( (Project)type.getTypeLoader().getModule().getExecutionEnvironment().getProject().getNativeProject() );
    String source = generateSource( type );
    final PsiJavaFile aFile = createDummyJavaFile( type, manager, source );
    final PsiClass[] classes = aFile.getClasses();
    return classes[0];
//    PsiJavaParserFacadeImpl psiJavaParserFacade = new PsiJavaParserFacadeImpl( manager );
//    String source = generateSource( type );
//    return psiJavaParserFacade.createClassFromText( source, null );
  }

  protected PsiJavaFile createDummyJavaFile( IType type, PsiManager manager, @NonNls final String text ) {
    final FileType fileType = JavaFileType.INSTANCE;
    return (PsiJavaFile)PsiFileFactory.getInstance( manager.getProject() ).createFileFromText( type.getName() + '.'  + JavaFileType.INSTANCE.getDefaultExtension(), fileType, text);
  }

  private String generateSource( IType type )
  {
    StringBuilder sb = new StringBuilder()
      .append( "package " ).append( type.getNamespace() ).append( ";\n\n" );
    ITypeInfo ti = safeGetTypeInfo( type );
    sb.append( "  @ClassInfoId(" ).append( 0 ).append( ", \"" )
      .append( type.getName() ).append( "\", " );
      appendLocationInfo( sb, ti )
      .append( ")\n" );
    generateClass( type, sb );
    return sb.toString();
  }

  private void generateClass( IType type, StringBuilder sb )
  {
    if( Modifier.isPublic( type.getModifiers() ) )
    {
      sb.append( "public " );
    }
    if( Modifier.isStatic( type.getModifiers() ) )
    {
      sb.append( "static " );
    }
    sb.append( type.isEnum() ? "enum " : "class " ).append( type.getRelativeName() ).append( " " ) ;
    IType supertype = type.getSupertype();
    if( supertype != null && supertype != JavaTypes.OBJECT() )
    {
      sb.append( "extends " ).append( supertype.getName() ).append( " " );
    }
    IType[] interfaces = type.getInterfaces();
    if( interfaces != null && interfaces.length > 0 )
    {
      sb.append( "implements " );
      for( int i = 0; i < interfaces.length; i++ )
      {
        if( i != 0 )
        {
          sb.append( ", " );
        }
        sb.append( interfaces[i].getName() );
      }
    }
    sb.append( " {\n" );
    generateProperties( type, sb );
    generateConstructors( type, sb );
    generateMethods( type, sb );
    generateInnerClasses( type, sb );
    sb.append( "}\n\n" );

  }

  private void generateInnerClasses( IType type, StringBuilder sb )
  {
    if( type instanceof IHasInnerClass )
    {
      int i = 0;
      for( IType innerClass : ((IHasInnerClass)type).getInnerClasses() )
      {
        ITypeInfo ti = safeGetTypeInfo( innerClass );
        sb.append( "  @InnerClassInfoId(" ).append( i++ ).append( ", \"" )
          .append( type.getName() ).append( "\", " );
          appendLocationInfo( sb, ti )
          .append( ")\n" );
        generateClass( innerClass, sb );
      }
    }
  }

  private void generateConstructors( IType type, StringBuilder sb )
  {
    ITypeInfo ti = safeGetTypeInfo( type );
    List<? extends IConstructorInfo> constructors;
    if( ti instanceof IRelativeTypeInfo )
    {
      constructors = ((IRelativeTypeInfo)ti).getConstructors( type );
    }
    else
    {
      constructors = ti.getConstructors();
    }
    int i = 0;
    if( constructors != null )
    {
      for( IConstructorInfo ci : constructors )
      {
        sb.append( "  @ConstructorInfoId(" ).append( i++ ).append( ", \"" ).append( ci.getName() ).append( "\", " );
          appendLocationInfo( sb, ci )
          .append( ")\n" )
          .append( "  " );
        generateModifiers( sb, ci );
        sb.append( " " );
        sb.append( type.getRelativeName() );
        sb.append( "(" );
        generateParameters( sb, ci );
        sb.append( ") {}\n" );
      }
    }
  }

  private ITypeInfo safeGetTypeInfo( IType type )
  {
    try
    {
      return type.getTypeInfo();
    }
    catch( Exception e )
    {
      e.printStackTrace();
      return TypeSystem.getErrorType().getTypeInfo();
    }
  }

  private void generateMethods( IType type, StringBuilder sb )
  {
    ITypeInfo ti = safeGetTypeInfo( type );
    MethodList methods;
    if( ti instanceof IRelativeTypeInfo )
    {
      methods = ((IRelativeTypeInfo)ti).getMethods( type );
    }
    else
    {
      methods = ti.getMethods();
    }
    int i = 0;
    if( methods != null )
    {
      for( IMethodInfo mi : methods )
      {
        if( mi.getDisplayName().charAt( 0 ) == '@' )
        {
          i++;
          continue;
        }
        sb.append( "  @MethodInfoId(" ).append( i++ ).append( ", \"" ).append( maybeQualifyWithType( mi ) ).append( mi.getName() ).append( "\", " );
          appendLocationInfo( sb, mi )
          .append( ")\n" )
          .append( "  " );
        generateModifiers( sb, mi );
        generateReturnType( sb, mi );
        sb.append( " " );
        sb.append( mi.getDisplayName() );
        sb.append( "(" );
        generateParameters( sb, mi );
        sb.append( ")" );
        generateMethodImplStub( sb, mi );
      }
    }
  }

  private void generateProperties( IType type, StringBuilder sb )
  {
    ITypeInfo ti = safeGetTypeInfo( type );
    List<? extends IPropertyInfo> properties;
    if( ti instanceof IRelativeTypeInfo )
    {
      properties = new ArrayList<>( ((IRelativeTypeInfo)ti).getProperties( type ) );
    }
    else
    {
      properties = new ArrayList<>( ti.getProperties() );
    }
    int i = 0;
    if( properties != null )
    {
      if( type.isEnum() )
      {
        generateEnumConstProperties( type, sb, properties, i );
      }
      for( IPropertyInfo pi : properties )
      {
        if( pi.isStatic() )
        {
          generatePropertyAsField( sb, i, pi );
        }
        else
        {
          generateInstanceProperty( sb, i, pi );
        }
        i++;
      }
    }
  }

  private void generateEnumConstProperties( IType type, StringBuilder sb, List<? extends IPropertyInfo> properties, int i )
  {
    for( Iterator<? extends IPropertyInfo> iter = properties.iterator(); iter.hasNext(); )
    {
      IPropertyInfo pi = iter.next();
      if( type instanceof IEnumType && ((IEnumData)type).getEnumValue( pi.getName() ) != null )
      {
        sb.append( "  @PropertyEnumInfoId(" ).append( i ).append( ", \"" ).append( pi.getName() ).append( "\", " );
          appendLocationInfo( sb, pi )
          .append( ")\n" )
          .append( "  " );
        sb.append( pi.getDisplayName() ).append( ",\n" );
        iter.remove();
      }
    }
    sb.append( ";\n" );
  }

  // Note, generate as Fields instead of get/set methods because the Ferrite Gosu plugin
  // doesn't know how to make properties from them for some reason
  private void generateInstanceProperty( StringBuilder sb, int i, IPropertyInfo pi )
  {
    sb.append( "  @PropertyFieldInfoId(" ).append( i ).append( ", \"" ).append( maybeQualifyWithType( pi ) ).append( pi.getName() ).append( "\", " );
      appendLocationInfo( sb, pi )
      .append( ")\n" )
      .append( "  " );
    generateFieldModifiers( sb, pi );
    sb.append( pi.getFeatureType().getName() ).append( " " ).append( pi.getDisplayName() ).append( ";\n" );
  }

  private String maybeQualifyWithType( IFeatureInfo fi )
  {
    IType ownersType = fi.getOwnersType();
    if( ownersType instanceof IGosuClass || ownersType instanceof IJavaType )
    {
      return TypeSystem.getPureGenericType( ownersType ).getName() + "#";
    }
    return "";
  }

//  private void generateInstanceProperty( StringBuilder sb, int i, IPropertyInfo pi )
//  {
//    if( pi.isReadable() )
//    {
//      sb.append( "  @PropertyGetInfoId(" ).append( i ).append( ", \"" ).append( pi.getName() ).append( "\", " );
//        appendLocationInfo( sb, pi )
//        .append( ")\n" )
//        .append( "  " );
//      generateModifiers( sb, pi );
//      sb.append( pi.getFeatureType().getName() );
//      sb.append( " " );
//      sb.append( "get" ).append( pi.getDisplayName() );
//      sb.append( "() {throw new RuntimeException();}\n" );
//    }
//    if( pi.isWritable( pi.getOwnersType() ) )
//    {
//      sb.append( "  @PropertySetInfoId(" ).append( i ).append( ", \"" ).append( pi.getName() ).append( "\", " );
//        appendLocationInfo( sb, pi )
//        .append( ")\n" )
//        .append( "  " );
//      generateModifiers( sb, pi );
//      sb.append( pi.getFeatureType().getName() );
//      sb.append( " " );
//      sb.append( "set" ).append( pi.getDisplayName() );
//      sb.append( "( " ).append( pi.getFeatureType().getName() ).append( " value ) {}\n" );
//    }
//  }

  private void generatePropertyAsField( StringBuilder sb, int i, IPropertyInfo pi )
  {
    if( pi.isReadable() )
    {
      sb.append( "  @PropertyFieldInfoId(" ).append( i ).append( ", \"" ).append( maybeQualifyWithType( pi ) ).append( pi.getName() ).append( "\", " );
        appendLocationInfo( sb, pi )
        .append( ")\n" )
        .append( "  " );
      generateFieldModifiers( sb, pi );
      sb.append( pi.getFeatureType().getName() ).append( " " ).append( pi.getDisplayName() ).append( ";\n" );
    }
  }

  private StringBuilder appendLocationInfo( StringBuilder sb, IFeatureInfo pi )
  {
    ILocationInfo loc = pi.getLocationInfo();
    int offset = loc.getOffset();
    if( offset < 0 && loc.getLine() > 0 )
    {
      IType type = pi.getOwnersType();
      VirtualFile virtualFile = FileUtil.getTypeResourceFiles( type ).get( 0 );
      URL fileUrl = loc.getFileUrl();
      if( fileUrl != null && virtualFile != null )
      {
        //## todo: handle types with multiple files e.g., the xsd type refers to elements in other types like Schema.xsd
        //## todo: For now just ignore typeinfo that comes from outside the immediate xsd
        IFile ifile = CommonServices.getFileSystem().getIFile( loc.getFileUrl() );
        VirtualFile vfile = ((IDEAFile)ifile).getVirtualFile();
        if( !vfile.equals( virtualFile ) )
        {
          virtualFile = null;
        }
      }
      if( virtualFile != null )
      {
        Document doc = FileDocumentManager.getInstance().getDocument( virtualFile );
        offset = doc.getLineStartOffset( loc.getLine() - 1 );
        if( loc.getColumn() > 0 )
        {
          offset += loc.getColumn() - 1;
        }
      }
    }
    return sb.append( offset ).append( ", " )
    .append( loc.getTextLength() ).append( ", " )
    .append( loc.getLine() ).append( ", " )
    .append( loc.getColumn() );
  }

  private void generateMethodImplStub( StringBuilder sb, IMethodInfo mi )
  {
    if( mi.isAbstract() )
    {
      sb.append( ";\n" );
    }
    else
    {
      sb.append( " {throw new RuntimeException();}\n" );
    }
  }

  private void generateReturnType( StringBuilder sb, IMethodInfo mi )
  {
    sb.append( mi.getReturnType().getName() );
  }

  private void generateParameters( StringBuilder sb, IHasParameterInfos mi )
  {
    IParameterInfo[] parameters = mi.getParameters();
    for( int i = 0; i < parameters.length; i++ )
    {
      IParameterInfo pi = parameters[i];
      if( i != 0 )
      {
        sb.append( "," );
      }
      sb.append( " " ).append( pi.getFeatureType() ).append( " " ).append( pi.getName() );
      if( i == parameters.length-1 )
      {
        sb.append( " " );
      }
    }
  }

  private void generateModifiers( StringBuilder sb, IAttributedFeatureInfo fi )
  {
    if( fi.isStatic() && !(fi instanceof IConstructorInfo) )
    {
      sb.append( "static " );
    }
    else if( fi.isAbstract() )
    {
      sb.append( "abstract " );
    }
    if( fi.isFinal() )
    {
      sb.append( "final " );
    }
    if( fi.isPrivate() )
    {
      sb.append( "private " );
    }
    else if( fi.isProtected() )
    {
      sb.append( "protected " );
    }
    else if( !fi.isInternal() )
    {
      sb.append( "public " );
    }
  }

  private void generateFieldModifiers( StringBuilder sb, IPropertyInfo pi )
  {
    if( pi.isStatic() )
    {
      sb.append( "static " );
    }
    if( !pi.isWritable() )
    {
      sb.append( "final " );
    }
    if( pi.isPrivate() )
    {
      sb.append( "private " );
    }
    else if( pi.isProtected() )
    {
      sb.append( "protected " );
    }
    else if( !pi.isInternal() )
    {
      sb.append( "public " );
    }
  }

  public Collection<? extends String> getAllClassNames()
  {
//    long t1 = System.nanoTime();

    Set<String> classes = new HashSet<>();
    for( ITypeLoader loader : TypeSystem.getAllTypeLoaders() )
    {
      if( !(loader instanceof GosuClassTypeLoader || loader instanceof IDefaultTypeLoader) )
      {
        IModule module = loader.getModule();
        TypeSystem.pushModule( module );
        try
        {
          for( CharSequence cs : loader.getAllTypeNames() )
          {
            String s = cs.toString();
            int i = s.lastIndexOf( '.' );
            if( i > 0 )
            {
              s = s.substring( i + 1 );
            }
            classes.add( s );
          }
        }
        finally
        {
          TypeSystem.popModule( module );
        }
      }
    }

//    System.out.println((System.nanoTime() - t1)*1e-6);
    return classes;
  }

  @NotNull
  public Collection<PsiClass> getByShortName( String shortName )
  {
    Set<PsiClass> classes = new HashSet<>();
    String prefix = "." + shortName;
    for( ITypeLoader loader : TypeSystem.getAllTypeLoaders() )
    {
      if( loader.showTypeNamesInIDE() && !(loader instanceof GosuClassTypeLoader || loader instanceof IDefaultTypeLoader) )
      {
        IModule module = loader.getModule();
        TypeSystem.pushModule( module );
        try
        {
          for( CharSequence cs : loader.getAllTypeNames() )
          {
            String typeName = cs.toString();
            if( typeName.endsWith( prefix ) )
            {
              IType type = TypeSystem.getByFullNameIfValid( typeName, module );
              PsiClass psiClass = getPsiClass( type );
              if( psiClass != null )
              {
                classes.add( psiClass );
              }
            }
          }
        }
        finally
        {
          TypeSystem.popModule( module );
        }
      }
    }
    return classes;
  }

  @Override
  public void refreshedTypes( RefreshRequest request )
  {
    Map<String, JavaFacadePsiClass> map = _type2Class.get( request.module );
    if( map != null )
    {
      for( String type : request.types )
      {
        map.remove( type );
      }
    }
    if( request.file != null )
    {
      String pathString = request.file.getPath().getPathString();
      JavaFacadePsiClass removedFacade = _psi2Class.remove( pathString );
      if( removedFacade != null )
      {
        ((PsiModificationTrackerImpl) removedFacade.getManager().getModificationTracker()).incCounter();
      }
    }
  }

  @Override
  public void refreshed()
  {
    _psi2Class.clear();
    _type2Class.clear();
  }
}
