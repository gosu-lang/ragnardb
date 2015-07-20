package ragnardb.runtime;

@FunctionalInterface
public interface IListenerAction<R, T> {

  T execute(R type);

}
