package ragnardb.plugin;

import gw.lang.reflect.*;
import gw.lang.reflect.java.JavaTypes;
import gw.util.GosuExceptionUtil;
import ragnardb.RagnarDB;
import ragnardb.parser.ast.Expression;
import ragnardb.runtime.SQLConstraint;
import ragnardb.runtime.SQLRecord;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SQLDdlTypeInfo extends SQLBaseTypeInfo {
  public SQLDdlTypeInfo(ISQLDdlType type) {
    super(type);
    decorateDdlType(type);
  }

  /**
   * ISQLDdlType will have a single getter, SqlSource : String
   * @param type ISQLDdlType
   */
  private void decorateDdlType( ISQLDdlType type ) {
    _propertiesList = new ArrayList<>();
    _propertiesMap = new HashMap<>();

    IPropertyInfo sqlSource = new PropertyInfoBuilder()
      .withName("SqlSource")
      .withDescription("Returns the source of this ISQLDdlType")
      .withStatic()
      .withWritable(false)
      .withType(JavaTypes.STRING())
      .withAccessor(new IPropertyAccessor() {
        @Override
        public Object getValue( Object ctx ) {
          try {
            return ((ISQLDdlType) getOwnersType()).getSqlSource();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void setValue( Object ctx, Object value ) {
          throw new IllegalStateException("Calling setter on readonly property");
        }
      })
      .build(this);
    _propertiesMap.put( sqlSource.getName(), sqlSource );
    _propertiesList.add(sqlSource);

    IPropertyInfo tables = new PropertyInfoBuilder()
      .withName("Tables")
      .withDescription("Returns the source of this ISQLDdlType")
      .withStatic()
      .withWritable( false )
      .withType( JavaTypes.LIST().getParameterizedType( TypeSystem.get( ISQLTableType.class ) ) )
      .withAccessor( new IPropertyAccessor()
      {
        @Override
        public Object getValue( Object ctx )
        {
          return ((ISQLDdlType)getOwnersType()).getTableTypes();
        }

        @Override
        public void setValue( Object ctx, Object value )
        {
          throw new IllegalStateException( "Calling setter on readonly property" );
        }
      } )
      .build( this );
    _propertiesMap.put(tables.getName(), tables);
    _propertiesList.add(tables);

    _propertiesMap.put(tables.getName(), tables);
    _propertiesList.add(tables);

    _methodList = new MethodList();
    _constructorList = Collections.emptyList();


    _methodList.add(new MethodInfoBuilder()
      .withName("transaction")
      .withDescription("Runs enclosed code in a transaction")
      .withParameters(new ParameterInfoBuilder().withName("condition")
        .withType(TypeSystem.get(Runnable.class)))
      .withReturnType(JavaTypes.VOID())
      .withStatic(true)
      .withCallHandler((ctx, args) -> {
        Connection con = null;
        Savepoint save1 = null;
        try {
          con = RagnarDB.getConnection();
          con.setAutoCommit(false);

          save1 = con.setSavepoint();
          Runnable exec = (Runnable) args[0];

          exec.run();
          con.commit();

        } catch (Exception e) {
          try {
            con.rollback(save1);
            System.out.println("Rolling Back");
          } catch (Exception ee) {
            System.err.println("Error in SQLRollback");
            throw GosuExceptionUtil.forceThrow(ee);
          }
          throw GosuExceptionUtil.forceThrow(e);
        } finally {
          try {
            con.setAutoCommit(true);
          } catch (Exception e) {
            System.err.println("Error in SQLAutoCommit change");
            throw GosuExceptionUtil.forceThrow(e);
          }
        }

        return null;

      })
      .build(this));
        }

  }
