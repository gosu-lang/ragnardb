package ragnardb.runtime;

public class RagnarExt<T> extends SQLRecord
{
  public RagnarExt( String tableName, String idColumn )
  {
    super( tableName, idColumn );
  }

  protected T getSelf() {
    return (T) this;
  }
}