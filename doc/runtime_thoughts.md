# Runtime Ideas/Concepts

## Basic Behaviors

Take a lot from Active Record:  http://guides.rubyonrails.org/active_record_basics.html

Things I love from Active Record:

* Raw string setter/getter API for working with HTTP requests (very pragmatic)
* Previous property value is available before save
* Callbacks in transaction life cycle
* Efficient, pleasant API for avoiding N+1 queries
* getOrCreate methods
* Single field finders

## Query Builder Syntaxes Out There

http://guides.rubyonrails.org/active_record_querying.html

http://guides.rubyonrails.org/association_basics.html

http://www.querydsl.com/

http://www.jooq.org/doc/3.6/manual-single-page/

http://stackoverflow.com/questions/5620985/is-there-any-good-dynamic-sql-builder-library-in-java

### Potential Syntax Proposal

* A mix of builder syntax on DDL objects and enhancement mixin
* IQueryStuff interface + IQueryStuffEnhancement gives you the is* methods via The Keefer Gambit

Some possible syntax:

    var query = Person.whereCol( Person#LastName, isNotNull() )
    query.execute()

    var query = Person.whereCol( Person#LastName, isEqualTo("Foo") ) 
    query.execute()

    var query = Person.whereCol( Person#LastName, isLike("%Foo%") ) 
    query.execute()

    var query = Person.whereCol( Person#LastName, isIn({"Foo", "Bar"})) 
    query.execute()
    
    // multiple columns
    var query = Person.whereCol( Person#LastName, isIn("Foo", "Bar") ) 
                      .whereCol( Person#Age, isBetween(22, 44) )
    query.execute()
    
    // order by
    var query = Person.whereCol( Person#LastName, isIn("Foo", "Bar") ) 
                      .whereCol( Person#Age, isBetween(22, 44) )
                      .orderBy( Person#Age, Descending ) // Again, enum constants
    query.execute()
    
    // join (how to express 
    var query = Person.whereCol( Person#LastName, isIn("Foo", "Bar") ) 
                      .whereCol( Person#Age, isBetween(22, 44) )
                      .join( Person#CompanyId, Company#Id )
                      .whereCol( Company#Name, isEqualTo( "Guidewire" ) )
                      .orderBy( Person#Age, Descending ) // Again, enum constants
    query.execute()
    
    // raw API as well
    var query = Person.where( ":min <= age OR age <= :max ", {"min" => 22, "max" => 44}) 
    query.execute()

## Where Does Domain Logic Go?

Options so far considered:

### Domain logic goes on enhancments of DDL-derived types 

In this case, just use enhancements to add domain methods to the DDL-based type. 

Pros: 

* Simple
* Can use structures in place of interfaces

Cons: 

* Can't implement interfaces
* Can't override property setters (maybe define a convention?)

### Gosu class acts as implementation via a naming convention

A Gosu class located in a particular place relative to the DDL file could be used (e.g. /model/application/impl/PersonImpl.gs)

It is tricky to know exactly what the relationship between the DDL type (model.application.Person) and the gosu 
class (model.application.impl.PersonImpl).  Would the DDL type be the super class?  Can't be, right?  It's not a byte-code
type.  Would the gosu class be the parent? Or would it be a custom merge of the two into a single type?

I think it has to be a merge, where the Person.gs gosu class become the actual instance type at runtime, and then
the DDL type takes type info from the impl class (including interfaces implemented, etc.)

Pros: 

* Can implement interfaces
* Full gosu functionality in your model (mix-ins, etc.)
* Natural API for overriding validations (override a method on parent class)
* Natural place to express more complicated entity relationships (e.g. joinds)


Cons: 

* More complicated merging of type info, but fun!


#### Implementation Questions

Question: How does an impl class refer to a property on the DDL type (which extends the *impl*!)?

Answer: Feature Literals

      property set FirstName( name : String ) {
        if(name == null) {
          throw IllegalArgumentException()
        }
        setValue(Person#FirstName, name) // in place of super.FirstName = name
      }

Question: How are array-like properties specified?

Answer: Regular old properties + query syntax

      // in CompanyImpl.gs
      property get Employees() : ResultSet<Employee> {
        return load(Employee)  // use "natural" join columns (Company.ID, Employee.CompanyID)
        // return load(Employee, Employee#BusinessID) // join on a different column
        // return load(Employee, Employee#BusinessID, Company#BusinessID) // join on a different column on both tables
      }
      
      