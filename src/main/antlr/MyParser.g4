grammar MyParser;

program: stmt* EOF;

stmt
    : IF expr block (ELSE stmt)? # IfStmt
    | PRINT expr terminator # PrintStmt // 允许 print expr 结尾无分号
    | VAR ID EQ expr terminator # VarDecl // 允许 var x = 1 结尾无分号
    | FUN ID LP argumentList? RP block # FunDecl
    | FOR LP (forInit | SEMI) expr? SEMI forUpdate? RP block # ForLoop
    | WHILE expr block # WhileLoop
    | block # BlockStmt
    | expr terminator # ExprStmt // 表达式语句允许结尾无分号
    | SEMI # EmptyStmt
    ;

argumentList
    : (ID ':' type)*
    ;

// 保留 terminator 规则
terminator
    : NEWLINE
    | SEMI
    | EOF
    ;

block
    : '{' stmt* '}'
    ;

forInit
    : stmt
    ;

forUpdate
    : expr
    ;

type
    : type DOT ID # TypeDotAccess // <--- 语法左递归，生成左结合树
    | ID          # SimpleType
    ;

expr
    : primaryExpr           # Primary
    | PLUS expr        # UnaryPlus
    | MINUS expr       # UnaryMinus
    | expr (MUL|DIV) expr    # MulDivExpr
    | expr (PLUS|MINUS) expr    # AddSubExpr
    | expr (EQEQ|INEQ) expr # Equality
    | expr (GREATER|GREATEREQ|LESS|LESSEQ) expr # Comparsion
    | expr LP (expr (COMMA expr)*)? RP  # CallExpr
    | ID EQ expr          # AssignExpr
    | LP expr RP           # GroupExpr
    | expr DOT ID          # DotAccess // <--- 添加这行来支持点访问
    ;

primaryExpr
    : ID
    | INT
    | STRING
    | FLOAT
    | TRUE
    | FALSE
    ;

// --- 关键字 (Keywords) ---
// 将所有关键字规则放在这里，放在 ID 规则之前
IF: 'if';
ELSE: 'else';
WHILE: 'while';
FOR: 'for';
PRINT: 'print';
VAR: 'var';
FUN: 'fun';
TRUE: 'true';
FALSE: 'false';

// --- 字面量 (Literals) ---
INT: [0-9]+;
FLOAT: [0-9]+'.'[0-9]+;
// STRING 规则保持不变
STRING
    : '"' (~["\\\r\n] | '\\' .)* '"'
    | '\'' (~['\\\r\n] | '\\' .)* '\''
    ;

// --- 操作符 (Operators) ---
PLUS: '+';
MINUS: '-';
MUL: '*';
DIV: '/';

EQEQ: '==';
INEQ: '!=';

LESS: '<';
LESSEQ: '<=';

GREATER: '>';
GREATEREQ: '>=';

EQ: '=';
LP: '(';
RP: ')';
SEMI: ';';
DOT: '.';
COMMA: ',';

// --- 标识符 (Identifier) ---
// 必须放在所有关键字之后！
ID
    : [\p{L}\p{M}]                  // 所有语言文字
      [\p{L}\p{N}\p{M}_]*              // 后续字符：文字、数字、标记、下划线
    ;

// --- 空白字符 (Whitespace) ---
NEWLINE: ('\r'? '\n' | '\r');
WS: [ \t]+ -> skip;