package ragnardb.plugin;

public class ColumnDefinition {
  private String _columnName;
  private int _sqlType; // see java.sql.Types: http://docs.oracle.com/javase/8/docs/api/index.html?java/sql/Types.html
  private boolean notNull;
  private boolean _null;
  private boolean auto_Increment;
  private boolean identity;
  private int startInt;
  private int incrementInt;
  private boolean primaryKey;
  private boolean hash;
  private boolean unique;


  public ColumnDefinition(String columnName, int sqlType) {
    _columnName = columnName;
    _sqlType = sqlType;
  }

  public String getColumnName() {
    return _columnName;
  }

  public void setColumnName(String columnName) {
    _columnName = columnName;
  }

  public int getSQLType() {
    return _sqlType;
  }

  public void setSQLType(int sqlType) {
    _sqlType = sqlType;
  }
  public void setNotNull(boolean in){
    notNull = in;
  }

  public void setNull(boolean in){
    _null = in;
  }
  public void setAutoIncrement(boolean in){
    auto_Increment = in;
  }
  public void setIdentity(boolean in){
    identity = in;
  }
  public void setStartInt(int in){
    startInt = in;
  }
  public void setIncrementInt(int in){
    incrementInt = in;
  }
  public void setPrimaryKey(boolean in){
    primaryKey = in;
  }
  public void setHash(boolean in){
    hash = in;
  }
  public void setUnique(boolean in){
    unique = in;
  }

  public boolean getNotNull(){
    return notNull;
  }
  public boolean getNull(){
    return _null;
  }
  public boolean getAutoIncrement(){
    return auto_Increment;
  }
  public boolean getIdentity(){
    return identity;
  }
  public int getStartInt(){
    return startInt;
  }
  public int getIncrementInt(){
    return incrementInt;
  }
  public boolean getPrimaryKey(){
    return primaryKey;
  }
  public boolean getHash(){
    return hash;
  }
  public boolean getUnique(){
    return unique;
  }

}
