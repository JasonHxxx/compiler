lexer grammar CmmLexer;

//1.Tokens
//最长优先匹配，八进制和十六进制要写在数字的前面   （->不必要，因为int不会匹配0开头的）
//8进制和16进制都要显示为int类型，在main函数里处理
Hexadecimal:('0x'|'0X')[0-9a-fA-F]+;//用+，后面至少一个符合要求的数字
Octal:'0'[0-7]+;//用+，后面至少一个符合要求的数字

INT:'0'|([1-9]DIGIT*);//[0-9]+匹配连续的数字，要用+，至少有一个可以匹配  这里匹配int：'0'|[1-9]DIGIT* 这样不加括号是错的
//无符号小数！！！！！！！！！！！！！！！！！！
FLOAT:(FRAC EXPO)|(SEQ'.'SEQ);//***************小数点的前后必须有数字
SEMI:';';
COMMA:',';
ASSIGNOP:'=';
RELOP:'>'|'<'|'>='|'<='|'=='|'!=';//可？最长优先匹配
PLUS:'+';
MINUS:'-';
STAR:'*';
DIV:'/';
AND:'&&';//单引号可行
OR:'||';
DOT:'.';
NOT:'!';
TYPE:'int'|'float';
LP:'(';
RP:')';
LB:'[';
RB:']';
LC:'{';
RC:'}';
STRUCT:'struct';
RETURN:'return';
IF:'if';
ELSE:'else';
WHILE:'while';



//2.跳过空格与换行！！！！！！！！！！！
WS:[ \t\r\n]+ -> skip;
//3.注释匹配
SLCOMMENT:'//'.*?'\n'->skip;
MLCOMMENT:'/*'.*?'*/'->skip;//匹配距离最近的*/

ID:(LETTER|'_')WORD*;//(LETTER|'_')(LETTER|'_'|DIGIT)*   放在最后，否则return等都会匹配为ID

//for 浮点数
fragment
SIGN:[+-];
fragment
DIGIT:[0-9];
fragment
SEQ:DIGIT+;
fragment
EXPO:[eE]SIGN?SEQ;
fragment
FRAC:(SEQ'.')|(SEQ?'.'SEQ);
fragment
SUR:[fFlL];
//for 词法单元ID
fragment
LETTER:[a-zA-Z];
fragment
WORD:[a-zA-Z0-9_];
