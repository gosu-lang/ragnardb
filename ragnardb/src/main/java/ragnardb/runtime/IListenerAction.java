package ragnardb.runtime;

@FunctionalInterface
public interface IListenerAction {

  <T> void action(T type); //TODO rename to execute()? fire()? go()?

}
