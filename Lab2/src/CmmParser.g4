parser grammar CmmParser;
options {
 tokenVocab=CmmLexer;
}
//右键test rule ***进行相应的rule规则的测试
program: extDef* EOF;
//定义高层语法
extDef: specifier funDec compSt #FunctionDef//函数定义
 | specifier SEMI #StructDef//结构体定义struct {...};
 | specifier extDecList SEMI #GlobalVarsDef//全局变量 int global1,global2;
 ;
extDecList: varDec (COMMA varDec)*;

//类型描述符，基本数据类型或struct类型
specifier: structSpecifier #StructType_Description
 | TYPE #BasicDataType_Description
 ;
structSpecifier: STRUCT optTag LC defList RC #StructType_Description2Undefined
 | STRUCT tag #StructType_Description2Defined
 ;

//可选的tag（空或id）
optTag: ID?;
tag: ID;

varDec: ID (LB (INT | ({notifyErrorListeners("array size must be an integer constant, not float");} FLOAT) | ({notifyErrorListeners("array size must be an integer constant, not n");} ID)) RB)*;//别的都是直接右递归，这个是直接左递归  a或a[3][...]...

//函数头的定义：函数名+形参列表 func(...)
funDec:ID LP varList RP #FunDec1
 | ID LP RP #Fundec2
 ;
varList: paramDec (COMMA paramDec)*;
paramDec: specifier varDec;

compSt: LC defList stmtList RC;
stmtList: stmt*;
stmt: exp SEMI #Expression_Clause
 | compSt #CompSt_Clause
 | RETURN exp SEMI #Return_Clause
 | IF LP exp RP stmt # If_Clause
 | IF LP exp RP stmt ELSE stmt #IfElse_Clause
 | WHILE LP exp RP stmt # While_Clause
 ;

defList: def*;//int a; float b, c=10; int d[10];
def: specifier decList SEMI;//int a;
decList: dec (COMMA dec)*;//a,b.c=10...
dec: varDec (ASSIGNOP exp)?;

//antlr4只能处理直接左递归，不能处理相互左递归：也就是a:b;b:a;
//如果对于第一优先级梯队的产生式加括号，就相当于将args写在了第一条，这是不行的
//写在同一行不加括号，优先级还是不一样的
//这里的优先级，加减乘除之前的可能有问题
exp: LP exp RP | ID LP args RP | ID LP RP | exp LB exp RB | exp DOT ID
 | <assoc=right> (MINUS | NOT) exp//(<assoc=right> MINUS exp | <assoc=right> NOT exp)  这样写还是不能保持优先级一样！！！！！！！！ 1692->2000分
 | exp op=(STAR | DIV) exp
 | exp op=(PLUS | MINUS) exp
 | exp RELOP exp
 | exp AND exp
 | exp OR exp
 | <assoc=right> exp ASSIGNOP exp
 | INT
 | Hexadecimal
 | Octal
 | FLOAT
 | ID
 ;
args: exp (COMMA exp)*;
