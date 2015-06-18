package ragnardb.demo

class Scratch implements IQueryStuff {

  function foo() {
    var query = Person.whereCol( LastName, isNotNull() )
                      .or( { whereCol(), whereCol(), whereCol() }  )

    query.execute()
  }


}