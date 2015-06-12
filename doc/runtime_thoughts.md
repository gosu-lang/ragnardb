http://guides.rubyonrails.org/active_record_basics.html

http://guides.rubyonrails.org/active_record_querying.html

http://guides.rubyonrails.org/association_basics.html

http://www.querydsl.com/

http://www.jooq.org/doc/3.6/manual-single-page/

http://stackoverflow.com/questions/5620985/is-there-any-good-dynamic-sql-builder-library-in-java

    var query = Person.whereCol( LastName, isNotNull() )
                      .or( { whereCol(), whereCol(), whereCol() }  )
    query.execute()