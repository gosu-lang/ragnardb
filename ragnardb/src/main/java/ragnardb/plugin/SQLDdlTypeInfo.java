package ragnardb.plugin;

import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.MethodList;
import gw.lang.reflect.PropertyInfoBuilder;
import gw.lang.reflect.java.JavaTypes;

import java.io.IOException;
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

    IPropertyInfo prop = new PropertyInfoBuilder()
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

    _propertiesMap.put(prop.getName(), prop);
    _propertiesList.add(prop);
    _methodList = new MethodList();
    _constructorList = Collections.emptyList();
  }

}
