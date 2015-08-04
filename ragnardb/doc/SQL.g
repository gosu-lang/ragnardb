grammar SQL;
options { k = 1; }

/* Grammar used by the parser ragnardb. Satisfies H2 grammar EXCEPT with following exceptions:
1) database.table is allowed in more places; specifically, all dml statements support this (this is compatible with JDBC, easy to implement, and useful)
2) resultcolumns (the results from the select statement) only support columnnames; expression support does not allow us to generate typeinfo. Furthermore, expression support leads to unusual behavior in JDBC regarding returned results.
3) <> Comparator is not supported
4) Datatype support: please check parser for this; not all datatypes are supported
5) Select statements: ordering and limit by are only permitted once per query in the case of joins. In order to limit results in several queries in joins, please use TOP.
   The reason for this limitation is the resolving LL(1) conflicts; we cannot allow more than one ORDER BY and LIMIT clause per statement. This does not mean that you can
   only order by the tables/columns used in the first query; the grammar still allows you to access every query within a join.
6) We do not allow column definitions as select statements. This would wildly complicate our plugin.
7) Disallowing comma joins on tables. Bad practice, can lead to issues with our plugin and column resolution.

This grammar is LL(1) expect for one case: database.table vs table. This is extremely trivially resolved in the parser. If this error is encountered as a result of the construction (ID '.')? ID, please ignore.
*/

ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

INT :	'0'..'9'+
    ;

FLOAT
    :   ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
    |   '.' ('0'..'9')+ EXPONENT?
    |   ('0'..'9')+ EXPONENT
    ;

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;

STRING
    :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    ;

CHAR:  '\'' ( ESC_SEQ | ~('\''|'\\') ) '\''
    ;

fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;
	

tablename
	:	(ID '.')? ID;
	
queryname
	:	ID;
	
columnname
	:	ID;
	
constraintname
	:	ID;

createtable
    	:	'CREATE' ('TEMP' | 'TEMPORARY')?  'TABLE' ('IF' 'NOT' 'EXISTS')? tablename '(' (columndef | constraint) (',' (columndef | constraint))* ')'
    	;
    	
columndef
        :	columnname typename ('DEFAULT' term)? ('NOT'? 'NULL')? (('AUTO_INCREMENT'|'IDENTITY') ('(' INT (',' INT)? ')')?)? ('UNIQUE' | 'PRIMARY' 'KEY' 'HASH'?)? ('CHECK' condition)?
        ;
        
constraint
	:	('CONSTRAINT' ('IF' 'NOT' 'EXISTS' )? constraintname)? 
	(	'CHECK' expr
	|	'UNIQUE' '(' columnname (',' columnname)* ')'
	|	'FOREIGN' 'KEY' '(' columnname (',' columnname)* ')' 'REFERENCES' tablename? ('(' columnname (',' columnname)* ')')? ('ON' ('DELETE' referentialaction ('ON' 'UPDATE' referentialaction)? | 'UPDATE' referentialaction))?
	|	'PRIMARY' 'KEY' 'HASH'? '(' columnname (',' columnname)* ')'
	)
	;
        
       	
referentialaction
	:	'CASCADE'
	|	'RESTRICT'
	|	'NO' 'ACTION'
	|	'SET' ('DEFAULT' | 'NULL')
	;      
     
literalvalue
        :	INT | FLOAT | ID | STRING | 'blob-literal' | 'NULL' | 'CURRENT_TIME' | 'CURRENT_DATE' | 'CURRENT_TIMESTAMP'
        ;
       	
signednumber 
	:	('+'|'-')? (INT|FLOAT) 
	;
       	
typename 
	:	(ID)* ('(' signednumber (',' signednumber)? ')')?
	;

recursivequery
	:	'WITH' 'RECURSIVE' queryname '(' columnname (',' columnname)* ')' 'AS' '(' simpleselect 'UNION' 'ALL' simpleselect ')' simpleselect ('ORDER' 'BY' orderingterm (',' orderingterm)*)? ('LIMIT' expr ('OFFSET' expr)?)?
	;
      
selectstmt
	:	simpleselect ((('UNION' 'ALL'?)|'MINUS'|'EXCEPT'|'INTERSECT') simpleselect)* ('ORDER' 'BY' orderingterm (',' orderingterm)*)? ('LIMIT' expr ('OFFSET' expr)?)?
	;
	
simpleselect
	:	'SELECT' ('TOP' term)? ('DISTINCT'|'ALL')? (resultcolumn (',' resultcolumn)*| '(' resultcolumn (',' resultcolumn)* ')') ('AS' ID)? 'FROM' tableorsubquery ('WHERE' expr)? (('GROUP' 'BY' expr) (',' 'GROUP' 'BY' expr)*)? ('HAVING' expr)?
	;
	
insertstmt
	:	'INSERT' 'INTO' tablename 
	(	('(' columnname (',' columnname)? ')')? (valuesexpression | 'DIRECT'? 'SORTED'? selectstmt)
	|	'SET' columnname '=' expr (',' columnname '=' expr)*	)
	;
	
updatestmt
	:	'UPDATE' tablename ('AS'? ID)? ('SET' columnname '=' expr (',' columnname '=' expr)* | '(' columnname (',' columnname)* ')' '=' '(' selectstmt ')') ('WHERE' expr)? ('LIMIT' expr)?
	;
	
deletestmt
	:	'DELETE' 'FROM' tablename ('WHERE' expr)? ('LIMIT' term)?
	;
	     	
valuesexpression
	:	'VALUES' '(' expr (',' expr)* ')' (',' '(' expr (',' expr)* ')')* ;
	
tableorsubquery
        :       basictableorsubquery ('ON' expr)?
        ;
        
basictableorsubquery
	:	(tablename
        |	'(' selectstmt ')'
        |	valuesexpression
        ) ('AS'? ID)? (((((('LEFT'|'RIGHT')('OUTER')?|'INNER'|'CROSS'|'NATURAL')?) 'JOIN')) basictableorsubquery)?
	;        

resultcolumn
        :       (ID '.')? ('*'
        |	columnname )
        ;
                
orderingterm
        :       expr ('ASC' | 'DESC')? ('NULLS' ('FIRST'|'LAST'))? 
        ;

expr	:	andcondition ('OR' andcondition)* | 'DEFAULT'
	;
	
andcondition
	:	condition ('AND' condition)*
	;
	
condition
	:	operand (conditionrightside)?
	|	'NOT' condition
	|	'EXISTS' '(' selectstmt ')'
	;
	
operand	:	summand ('||' summand)*
	;


summand	:	factor (('+'|'-') factor)*
	;
	
factor	:	term (('*'|'/'|'%') term)*
	;

term	:	literalvalue
	|	'?' INT
	|	'(' (expr|selectstmt) ')'
	|	'CASE' (case|casewhen)
	;
	
case	:	expr casewhen
	;
	
casewhen:	('WHEN' expr 'THEN' expr)+ ('ELSE' expr)? 'END'
	;

conditionrightside
	:	compare (operand|('ALL'|'ANY'|'SOME') '(' selectstmt ')')
	|	'IS' 'NOT'? ('DISTINCT' 'FROM')? operand
	|	'BETWEEN' operand 'AND' operand
	|	'IN' '(' (selectstmt|expr (',' expr)*) ')'
	|	'NOT'? ('LIKE' operand ('ESCAPE' ID)?|'REGEXP' operand)
	;
	
compare	:	'>=' | '<=' | '=' | '<' | '>' | '!=' | '&&'
	;
	
