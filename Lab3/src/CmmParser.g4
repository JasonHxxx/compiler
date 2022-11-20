parser grammar CmmParser;
options {
 tokenVocab=CmmLexer;
}
//右键test rule ***进行相应的rule规则的测试
program: extDef* EOF;
//定义高层语法
extDef: specifier[false] funDec compSt #FunctionDef//函数定义
 | specifier[true] SEMI #StructDef//结构体定义struct {...};
 | specifier[false] extDecList SEMI #GlobalVarsDef//全局变量 int global1,global2;
 ;
extDecList: varDec[true,false] (COMMA varDec[true,false])*;

//类型描述符，基本数据类型或struct类型
specifier[boolean inDefine]: structSpecifier[$inDefine] #StructType_Description
 | TYPE #BasicDataType_Description
 ;
structSpecifier[boolean inDefine]: STRUCT optTag LC defList[true] RC #StructType_Description2Undefined
 | STRUCT tag #StructType_Description2Defined
 ;

//可选的tag（空或id）
optTag: ID?;
tag: ID;

varDec[boolean inDecOrParamDec,boolean inStruct]: ID (LB (INT | ({notifyErrorListeners("array size must be an integer constant, not float");} FLOAT) | ({notifyErrorListeners("array size must be an integer constant, not n");} ID)) RB)*;//别的都是直接右递归，这个是直接左递归  a或a[3][...]...

//函数头的定义：函数名+形参列表 func(...)
funDec:ID LP varList RP #FunDec1
 | ID LP RP #Fundec2
 ;
varList: paramDec (COMMA paramDec)*;
paramDec: specifier[false] varDec[true,false];

compSt: LC defList[false] stmtList RC;
stmtList: stmt*;
stmt: exp SEMI #Expression_Clause
 | compSt #CompSt_Clause
 | RETURN exp SEMI #Return_Clause
 | IF LP exp RP stmt # If_Clause
 | IF LP exp RP stmt ELSE stmt #IfElse_Clause
 | WHILE LP exp RP stmt # While_Clause
 ;

defList[boolean inStruct]: def[$inStruct]*;//int a; float b, c=10; int d[10];
def[boolean inStruct]: specifier[false] decList[$inStruct] SEMI;//int a;
decList[boolean inStruct]: dec[$inStruct] (COMMA dec[$inStruct])*;//a,b.c=10...
dec[boolean inStruct]: varDec[true,$inStruct] (ASSIGNOP exp)?;

//antlr4只能处理直接左递归，不能处理相互左递归：也就是a:b;b:a;
//如果对于第一优先级梯队的产生式加括号，就相当于将args写在了第一条，这是不行的
//写在同一行不加括号，优先级还是不一样的
//这里的优先级，加减乘除之前的可能有问题
exp: LP exp RP #YuanKuoHao| ID LP args RP #Functionvisit1| ID LP RP #Functionvisit2| exp LB exp RB #Arrayvisit| exp DOT ID #Structvisit
 | <assoc=right> (MINUS | NOT) exp #MinusNot//(<assoc=right> MINUS exp | <assoc=right> NOT exp)  这样写还是不能保持优先级一样！！！！！！！！ 1692->2000分
 | exp op=(STAR | DIV) exp #StarDiv
 | exp op=(PLUS | MINUS) exp #PlusMinus
 | exp RELOP exp #Compare
 | exp AND exp #And
 | exp OR exp #Or
 | <assoc=right> exp ASSIGNOP exp #Assignop
 | INT #Int
 | Hexadecimal #Hex
 | Octal #Oct
 | FLOAT #Float
 | ID #Id
 ;
args: exp (COMMA exp)*;
