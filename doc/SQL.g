grammar SQL;
options { k = 1; }

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

table

    	:	'CREATE' ('TEMP' | 'TEMPORARY')?  'TABLE' ('IF' 'NOT' 'EXISTS')? ('database-name' '.')? 'table-name'
    	        '(' columndef (columndef ',')* ')' ('WITHOUT' 'ROWID')? 
    	 ;
columndef
        :	'column-name' ('type-name')? (columnconstraint)*
        ;
        
columnconstraint
        :	('CONSTRAINT' 'name')? 
                 (
                   'PRIMARY' 'KEY' ('ASC' 'DESC')? 'conflict-clause' ('AUTOINCREMENT')?
                   | 'NOT' 'NULL' 'conflict-clause'
                   | 'UNIQUE' conflictclause
                   | 'CHECK' '(' INT ')'
                   | 'DEFAULT' (signednumber | literalvalue | '(' INT ')'
                   | 'COLLLATE' 'collation-name'
                   | foreignkeyclause
                 )
                 )          
        ;
        
conflictclause
        :	('ON' 'CONFLICT' 
                     ('ROLLBACK' | 'ABORT' | 'FAIL' | 'IGNORE' | 'REPLACE')
                )?
         ;
  
foreignkeyclause
       	:	'REFERENCES' 'foreign-table' ('(' 'column-name' (',' 'column-name')* ')' )?
       	        ('MATCH' 'name' | 'ON' ('DELETE' | 'UPDATE') 
       	          (('SET' ('NULL' |  'DEFAULT' ))| 'CASCADE' | 'RESTRICT' | 'NO' 'ACTION')
       	        )*
       	        ( ('NOT')? 'DEFERRABLE' ('INITIALLY' ('DEFERRED' | 'IMMEDIATE'))? )?
       	;      
literalvalue
        :	'numeric-literal' | 'string-literal' | 'blob-literal' | 'NULL' | 'CURRENT_TIME' | 'CURRENT_DATE' | 'CURRENT_TIMESTAMP'
        ;
       	
signednumber 
	:	('+'|'-') 'numeric-literal' 
	;
       	
typename 
	:	('name')* ( '(' ( 'signed-number' ')' | '(' 'signed-number' ',' 'signed-number' ')'))?
	;
	
tableconstraint
  :  ('CONSTRAINTame')? ( ('PRIMARY' 'KEY' | 'UNIQUE') '(' indexedcolumn (',' indexedcolumn)* ')' conflictclause
  | 'CHECK' '(' expr ')'
  | 'FOREIGN' 'KEY' '(' 'columnname' (',' 'columnname' )* ')' foreignkeyclause)
  ;

indexedcolumn
      :  'column_name' ('COLLATE' 'collationname')? ('ASC' | 'DESC')?
      ;
      
selectstmt
	:	('WITH' ('RECURSIVE')? commontableexpression (',' commontableexpression)* )?
	        selectsub
	        (compoundoperator selectsub)*
	        ('ORDER' 'BY' orderingterm (',' orderingterm)*)?
	        ('LIMIT' INT 'OFFSET' (',' 'OFFSET')* INT)?
	;
	
selectsub
	:	'SELECT' ('DISTINCT' | 'ALL')? resultcolumn (',' resultcolumn)*
	         ('FROM' (tableorsubquery joinclause))?
	         ('WHERE' expr)?
	         ('GROUP' 'BY' expr (',' expr)* ('HAVING' expr)? )?
	         |
	         'VALUES' '(' expr (',' expr)* ')' (',' '(' expr (',' expr)* ')')*
	;       	
	
	tableorsubquery
                :               ('databasename' '.')? 'tablename' ('AS' 'tablealias')? (('INDEXED' 'BY' 'indexname') | ('NOT' 'INDEXED'))?
                |       '(' ((joinclause /* | (tableorsubquery (',' tableorsubquery)*)*/ ')') | (selectstmt ')'  (('AS')? 'tablealias')?)) 
                ;
         

resultcolumn
                :               '*'
                |              'tablename' '.' '*' //lookahead 2 to resolve tablename vs expression
                |              expr (('AS')? 'columnalias')?
                ;
                
orderingterm
                :               expr ('COLLATE' 'collationname')? ('ASC' | 'DESC')? //check collate before checking expression
                ;

joinoperator
                :               ','
                |              ('NATURAL')? (('LEFT' ('OUTER')?) | 'INNER' | 'CROSS')? 'JOIN'
                ;
                
joinconstraint
                :               'ON' expr
                |              'USING' '(' 'columnname' (',' 'columnname')* ')'
                ;

joinclause
                :               tableorsubquery (joinoperator tableorsubquery joinconstraint?)* 
                ;
                
compoundoperator
                :               'UNION' 'ALL'?
                |              'INTERSECT'
                |              'EXCEPT'
                ;
                
commontableexpression
                :               'tablename' ('(' 'columnname' (',' 'columnname')* ')')? 'AS' '(' selectstmt ')'
                ;

expr	:	andcondition ('OR' andcondition)*
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
	|	'columnname'
	;
	
case	:	expr ('WHEN' expr 'THEN' expr)+ ('ELSE' expr)? 'END'
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
	
compare	:	'comparator'
	;
	
