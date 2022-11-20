import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class CmmSemanticVisitor<T> extends CmmParserBaseVisitor<T> {
    Deque<Type> stack = new ArrayDeque<>();
    public Map<String, Type> structureMemberlist=new HashMap<String, Type>();
    public Map<String, Type> allStructureMemberlist=new HashMap<String, Type>();

    @Override
    public T visit(ParseTree tree) {
        return super.visit(tree);
    }

    public String getLineNum(ParserRuleContext ctx){
        Token startToken=((ParserRuleContext)ctx).getStart();
        String lineNum=String.valueOf(startToken.getLine());
        return lineNum;
    }

    @Override
    public T visitChildren(RuleNode node) {
        T result = this.defaultResult();
        int n = node.getChildCount();


        for(int i = 0; i < n && this.shouldVisitNextChild(node, result); ++i) {
            ParseTree c = node.getChild(i);
            T childResult = c.accept(this);
            result = this.aggregateResult(result, childResult);
        }

        return result;
    }

    @Override
    public T visitTerminal(TerminalNode node) {
        Token symbol = node.getSymbol();
        String ruleName=CmmParser.VOCABULARY.getSymbolicName(symbol.getType());
        if(!ruleName.equals("ID")){//type(int/float)或struct
            switch (ruleName){
                case "INT":
                    Int newInt=new Int();
                    //含有struct和数组的加减还没有解决
                    newInt.notLeftValue=true;
                    stack.push(newInt);
                    break;
                case  "FLOAT":
                    Float newFloat=new Float();
                    newFloat.notLeftValue=true;
                    stack.push(newFloat);
                    break;
            }
        }

        //服务于j=j+1 获得左边的类型
        String s=symbol.getText();
        if(ruleName.equals("ID") ){
            if(Main.symbolTable.get(s)==null){
                System.err.println("Error type 1 at Line "+symbol.getLine()+": Undefined variable \"j\".");
                Type nullType=new NullType();
                stack.push(nullType);
            }
            else{
                stack.push(Main.symbolTable.get(s));
            }
        }
        return null;
    }

    @Override
    public T visitProgram(CmmParser.ProgramContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitFunctionDef(CmmParser.FunctionDefContext ctx) {
        Function function=new Function();

        //先判断是不是重新定义，重新定义直接丢弃
        function.name=ctx.funDec().children.get(0).getText();
        if(Main.symbolTable.get(function.name)!=null){
            String lineNum=getLineNum(ctx);
            System.err.println("Error type 4 at Line "+lineNum+": Redefined function \"func\".");
            ctx.children.clear();//*这个struct里要不要填内容？
            return null;
        }
        int stackSize0=stack.size();
        super.visitFunctionDef(ctx);
        int stackSize1=stack.size();
        int funcCount=stackSize1-stackSize0;
        Deque<Type> stack0 = new ArrayDeque<>();
        for(int i=0;i<funcCount;i++){
            stack0.push(stack.pop());
        }
        function.returnType= stack0.pop();
        while(stack0.size()!=0){
            function.paramList.add(stack0.pop());
        }
        Main.symbolTable.put(function.name,function);
        return null;
    }

    @Override
    public T visitStructDef(CmmParser.StructDefContext ctx) {

        return super.visitStructDef(ctx);
    }

    @Override
    public T visitGlobalVarsDef(CmmParser.GlobalVarsDefContext ctx) {
        return super.visitGlobalVarsDef(ctx);
    }

    @Override
    public T visitExtDecList(CmmParser.ExtDecListContext ctx) {
        visitChildren(ctx);
        stack.pop();
        return null;
    }

    @Override
    public T visitStructType_Description(CmmParser.StructType_DescriptionContext ctx) {
        return super.visitStructType_Description(ctx);
    }

    @Override
    public T visitBasicDataType_Description(CmmParser.BasicDataType_DescriptionContext ctx) {
        if(ctx.getText().charAt(0)=='i') {
            Int newInt=new Int();
            stack.push(newInt);
        }
        else {
            Float newfloat=new Float();
            stack.push(newfloat);
        }
        return super.visitBasicDataType_Description(ctx);
    }

    @Override
    public T visitStructType_Description2Undefined(CmmParser.StructType_Description2UndefinedContext ctx) {
        Structure structure=new Structure();
        //判断是否重名
        structure.name=ctx.children.get(1).getText();
        boolean defined=false;
        if(structure.name!=null){
            if(Main.symbolTable.get(structure.name)!=null){
                defined=true;
            }
        }
        if(defined){
            System.err.println("Error type 16 at Line "+getLineNum(ctx)+": Duplicated structure name \"Position\".");
            ctx.children.clear();//*这个struct里要不要填内容？
            return null;
        }

        Map<String, Type> structureMemberlist0=new HashMap<String, Type>();
        Set<Map.Entry<String, Type>> entrySet= structureMemberlist.entrySet();
        //foreach遍历
        for(Map.Entry<String,Type> entry:entrySet){
            structureMemberlist0.put(entry.getKey(), entry.getValue());
        }
        int stackSize0=stack.size();
        ctx.children.remove(0);//两次remove0，第一次remove后会往前移动
        ctx.children.remove(0);
        super.visitStructType_Description2Undefined(ctx);
        int stackSize1=stack.size();
        int structParamCount=stackSize1-stackSize0;

        //新的变量加入struct
        Set<Map.Entry<String, Type>> entrySet1= structureMemberlist.entrySet();
        for(Map.Entry<String,Type> entry:entrySet1){
            if (structureMemberlist0.get(entry.getKey()) == null) {
                structure.memberlist.put(entry.getKey(), entry.getValue());
            }
        }

        //移除新的变量
        Map<String, Type> structureMemberlist1=new HashMap<String, Type>();
        Set<Map.Entry<String, Type>> entrySet2= structureMemberlist.entrySet();
        for(Map.Entry<String,Type> entry:entrySet2){
            if (structureMemberlist0.get(entry.getKey()) != null) {
                structureMemberlist1.put(entry.getKey(), entry.getValue());
            }
        }
        structureMemberlist.clear();
        Set<Map.Entry<String, Type>> entrySet3= structureMemberlist1.entrySet();
        for(Map.Entry<String,Type> entry:entrySet3){
            structureMemberlist.put(entry.getKey(), entry.getValue());
        }
        //将struct加入
        if(structure.name!=null) {
            structureMemberlist.put(structure.name,structure);
        }

        //加入符号表
        if(structure.name!=null) {
            Main.symbolTable.put(structure.name,structure);
        }
        if(!ctx.inDefine){
            stack.push(structure);
        }

        return null;
    }

    @Override
    public T visitStructType_Description2Defined(CmmParser.StructType_Description2DefinedContext ctx) {
        String name=ctx.tag().getText();
        if(Main.symbolTable.get(name)==null){
            System.err.println("Error type 17 at Line "+getLineNum(ctx)+": Undefined structure \"Position\".");
            Type nullType=new NullType();
            stack.push(nullType);
            return null;
        }else if(Main.symbolTable.get(name).kind!=Kind.STRUCTURE){
            System.err.println("Error type 17 at Line "+getLineNum(ctx)+": Undefined structure \"Position\".");
            Type nullType=new NullType();
            stack.push(nullType);
            return null;
        }else if(!((Structure)(Main.symbolTable.get(name))).name.equals(name)){
            System.err.println("Error type 17 at Line "+getLineNum(ctx)+": Undefined structure \"Position\".");
            Type nullType=new NullType();
            stack.push(nullType);
            return null;
        } else {
            stack.push(Main.symbolTable.get(name));
            return null;
        }
    }

    @Override
    public T visitOptTag(CmmParser.OptTagContext ctx) {
        return super.visitOptTag(ctx);
    }

    @Override
    public T visitTag(CmmParser.TagContext ctx) {
        return super.visitTag(ctx);
    }

    @Override
    public T visitVarDec(CmmParser.VarDecContext ctx) {
        String name=ctx.children.get(0).getText();
        Type type=stack.pop();
        stack.push(type);
        if(type.kind==Kind.NULLKIND){
            if(ctx.inDecOrParamDec)
                stack.push(type);
            return null;
        }
        if(Main.symbolTable.get(name)!=null) {
            String lineNum=getLineNum(ctx);
            //暂时不考虑struct嵌套定义
            if(allStructureMemberlist.get(name)==null)
                System.err.println("Error type 3 at Line "+lineNum+": Redefined variable \"i\".");
            else
                System.err.println("Error type 15 at Line "+getLineNum(ctx)+": Redefined field \"x\".");
            if(ctx.inDecOrParamDec)
                stack.push(type);
            return null;
        }
        if(ctx.getChildCount()!=1){
            Array array=new Array();
            array.elementType=type;

            //计算传入的参数个数
            int stackSize0=stack.size();
            ctx.children.remove(0);
            visitChildren(ctx);
            int stackSize1=stack.size();
            int params=stackSize1-stackSize0;

            //加入维度信息
            int i=0;
            array.dimention=0;
            while (i<params){
                stack.pop();
                array.dimention++;
                i++;
            }

            Main.symbolTable.put(name,array);
            if(ctx.inStruct){
                structureMemberlist.put(name,array);
                allStructureMemberlist.put(name,array);
            }
            if(ctx.inDecOrParamDec)
                stack.push(array);
        }else{
            Main.symbolTable.put(name,type);
            if(ctx.inStruct) {
                structureMemberlist.put(name,type);
                allStructureMemberlist.put(name,type);
            }
            if(ctx.inDecOrParamDec)
                stack.push(type);
        }
        return null;
    }

    @Override
    public T visitFunDec1(CmmParser.FunDec1Context ctx) {
        ctx.children.remove(0);
        return super.visitFunDec1(ctx);
    }

    @Override
    public T visitFundec2(CmmParser.Fundec2Context ctx) {
        ctx.children.remove(0);
        return super.visitFundec2(ctx);
    }

    @Override
    public T visitVarList(CmmParser.VarListContext ctx) {
        return super.visitVarList(ctx);
    }

    @Override
    public T visitParamDec(CmmParser.ParamDecContext ctx) {
        return super.visitParamDec(ctx);
    }

    @Override
    public T visitCompSt(CmmParser.CompStContext ctx) {
        return super.visitCompSt(ctx);
    }

    @Override
    public T visitStmtList(CmmParser.StmtListContext ctx) {
        return super.visitStmtList(ctx);
    }

    @Override
    public T visitExpression_Clause(CmmParser.Expression_ClauseContext ctx) {
        visitChildren(ctx);
        stack.pop();
        return null;
    }

    @Override
    public T visitCompSt_Clause(CmmParser.CompSt_ClauseContext ctx) {
        return super.visitCompSt_Clause(ctx);
    }

    @Override
    public T visitReturn_Clause(CmmParser.Return_ClauseContext ctx) {
        visitChildren(ctx);
        Type returnType=stack.pop();
        if(returnType.kind!=Kind.NULLKIND) {
            Type needed = stack.getLast();
            if (returnType.kind != needed.kind) {
                System.err.println("Error type 8 at Line " + getLineNum(ctx) + ": Type mismatched for return.");
            }
        }
        return null;
    }

    @Override
    public T visitIf_Clause(CmmParser.If_ClauseContext ctx) {
        ParseTree parseTree=ctx.children.get(4);
        ctx.children.remove(4);
        visitChildren(ctx);
        int a=0;
        Type compareResult=stack.pop();
        if(compareResult.kind!=Kind.NULLKIND){
            ctx.children.add(parseTree);
            ctx.children.remove(0);
            ctx.children.remove(0);
            ctx.children.remove(0);
            visitChildren(ctx);
        }else{

        }

        return null;
    }

    @Override
    public T visitIfElse_Clause(CmmParser.IfElse_ClauseContext ctx) {
        return super.visitIfElse_Clause(ctx);
    }

    @Override
    public T visitWhile_Clause(CmmParser.While_ClauseContext ctx) {
        return super.visitWhile_Clause(ctx);
    }

    @Override
    public T visitDefList(CmmParser.DefListContext ctx) {
        return super.visitDefList(ctx);
    }

    @Override
    public T visitDef(CmmParser.DefContext ctx) {
        return super.visitDef(ctx);
    }

    @Override
    public T visitDecList(CmmParser.DecListContext ctx) {
        visitChildren(ctx);
        stack.pop();//将类型pop掉
        return null;
    }

    @Override
    public T visitDec(CmmParser.DecContext ctx) {
        super.visitDec(ctx);
        int count=ctx.getChildCount();
        if(ctx.getChildCount()>2){
            Type type_right=stack.pop();
            Type type_left=stack.pop();
            stack.push(type_left);
            if(type_left.kind!=type_right.kind){
                //...
            }
        }else{
            //donothing
        }
        return null;
    }

    @Override
    public T visitOct(CmmParser.OctContext ctx) {
        return super.visitOct(ctx);
    }

    @Override
    public T visitOr(CmmParser.OrContext ctx) {
        return super.visitOr(ctx);
    }

    @Override
    public T visitMinusNot(CmmParser.MinusNotContext ctx) {
        return super.visitMinusNot(ctx);
    }

    @Override
    public T visitStructvisit(CmmParser.StructvisitContext ctx) {
        String name=ctx.children.get(0).getText();
        boolean success=true;
        Type nullType;
        if(Main.symbolTable.get(name)==null){
            System.err.println("Error type 1 at Line "+getLineNum(ctx)+": Undefined variable \"j\".");
            success=false;
        }else if(Main.symbolTable.get(name).kind!=Kind.STRUCTURE){
            System.err.println("Error type 13 at Line "+getLineNum(ctx)+": Illegal use of \".\".");
            success=false;
        }
        if(!success){//不成功，直接不需要访问参数列表
            nullType=new NullType();
            stack.push(nullType);
            return null;
        }



        String domainName=ctx.children.get(2).getText();
        Structure structure=(Structure)(Main.symbolTable.get(name));
        if(structure.memberlist.get(domainName)==null){
            System.err.println("Error type 14 at Line "+getLineNum(ctx)+": Non-existent field \"n\".");
            success=false;
        }

        if(!success){
            nullType=new NullType();
            stack.push(nullType);
            return null;
        }else{
            stack.push(structure.memberlist.get(domainName));
            return null;
        }
    }

    @Override
    public T visitInt(CmmParser.IntContext ctx) {
        return super.visitInt(ctx);
    }

    @Override
    public T visitFunctionvisit2(CmmParser.Functionvisit2Context ctx) {
        String visitFuncName=ctx.children.get(0).getText();
        boolean success=true;
        Type nullType;
        if(Main.symbolTable.get(visitFuncName)==null){
            System.err.println("Error type 2 at Line "+getLineNum(ctx)+": Undefined function \"inc\".");
            success=false;
        }else {
            if(Main.symbolTable.get(visitFuncName).kind!=Kind.FUNCTION){
                System.err.println("Error type 11 at Line "+getLineNum(ctx)+": \"i\" is not a function.");
                success=false;
            }
        }
        if(!success){//不成功，直接不需要访问参数列表
            nullType=new NullType();
            stack.push(nullType);
            return null;
        }

        Function function=(Function) (Main.symbolTable.get(visitFuncName));
        if(function.paramList.size()!=0){
            success=false;
        }

        if(!success){
            nullType=new NullType();
            stack.push(nullType);
            System.err.println("Error type 9 at Line "+getLineNum(ctx)+": Function \"sub(int)\" is not applicable for arguments \"(int, int)\".");
            return null;
        }else{
            stack.push(function.returnType);
            return null;
        }
    }

    @Override
    public T visitPlusMinus(CmmParser.PlusMinusContext ctx) {
        super.visitPlusMinus(ctx);
        Type type1=stack.pop();
        Type type2=stack.pop();
        if(type1.kind==type2.kind){
            stack.push(type1);
        }else{
            System.err.println("Error type 7 at Line "+getLineNum(ctx)+": Type mismatched for operands.");
            Type nullType=new NullType();
            stack.push(nullType);
        }
        return null;
    }

    @Override
    public T visitFunctionvisit1(CmmParser.Functionvisit1Context ctx) {
        String visitFuncName=ctx.children.get(0).getText();
        boolean success=true;
        Type nullType;
        if(Main.symbolTable.get(visitFuncName)==null){
            System.err.println("Error type 2 at Line "+getLineNum(ctx)+": Undefined function \"inc\".");
            success=false;
        }else {
            if(Main.symbolTable.get(visitFuncName).kind!=Kind.FUNCTION){
                System.err.println("Error type 11 at Line "+getLineNum(ctx)+": \"i\" is not a function.");
                success=false;
            }
        }
        if(!success){//不成功，直接不需要访问参数列表
            nullType=new NullType();
            stack.push(nullType);
            return null;
        }


        //计算传入的参数个数
        int stackSize0=stack.size();
        ctx.children.remove(0);
        visitChildren(ctx);
        int stackSize1=stack.size();
        int params=stackSize1-stackSize0;

        Function function=(Function) (Main.symbolTable.get(visitFuncName));
        if(params!=function.paramList.size()){
            success=false;
        }else{
            for(int i=function.paramList.size()-1;i>=0;i--){
                Type type1=stack.pop();
                stack.push(type1);
                if(function.paramList.get(i).kind!=type1.kind){
                    success=false;
                    break;
                }
            }
        }
        //参数出栈
        for(int i=0;i<params;i++){
            stack.pop();
        }
        if(!success){
            nullType=new NullType();
            stack.push(nullType);
            System.err.println("Error type 9 at Line "+getLineNum(ctx)+": Function \"sub(int)\" is not applicable for arguments \"(int, int)\".");
            return null;
        }else{
            stack.push(function.returnType);
            return null;
        }
    }

    @Override
    public T visitFloat(CmmParser.FloatContext ctx) {
        return super.visitFloat(ctx);
    }

    @Override
    public T visitYuanKuoHao(CmmParser.YuanKuoHaoContext ctx) {
        return super.visitYuanKuoHao(ctx);
    }

    @Override
    public T visitAnd(CmmParser.AndContext ctx) {
        return super.visitAnd(ctx);
    }

    @Override
    public T visitHex(CmmParser.HexContext ctx) {
        return super.visitHex(ctx);
    }

    @Override
    public T visitCompare(CmmParser.CompareContext ctx) {
        visitChildren(ctx);
        Type type_right=stack.pop();
        Type type_left=stack.pop();
        if(type_right.kind!=Kind.NULLKIND && type_left.kind!=Kind.NULLKIND) {

        }else{
            Type nullType=new NullType();
            stack.push(nullType);
        }
        return null;
    }

    @Override
    public T visitId(CmmParser.IdContext ctx) {
        String name=ctx.children.get(0).getText();
        boolean success=true;
        Type nullType=new NullType();
        if(Main.symbolTable.get(name)==null){
            System.err.println("Error type 1 at Line "+getLineNum(ctx)+": Undefined variable \"j\".");
            success=false;
        }
        if(!success){
            nullType=new NullType();
            stack.push(nullType);
            return null;
        }else{
            stack.push(Main.symbolTable.get(name));
            return null;
        }
    }

    @Override
    public T visitStarDiv(CmmParser.StarDivContext ctx) {
        return super.visitStarDiv(ctx);
    }

    @Override
    public T visitAssignop(CmmParser.AssignopContext ctx) {
        visitChildren(ctx);
        Type type_right=stack.pop();
        Type type_left=stack.pop();
        if(type_right.kind!=Kind.NULLKIND && type_left.kind!=Kind.NULLKIND) {
            if(type_left.notLeftValue){
                System.err.println("Error type 6 at Line "+getLineNum(ctx)+": The left-hand side of an assignment must be a variable.");
                Type nullType=new NullType();
                stack.push(nullType);//有错误时同样要push
                return null;
            }else if (type_left.kind != type_right.kind) {
                System.err.println("Error type 5 at Line " + getLineNum(ctx) + ": Type mismatched for assignment.");
                Type nullType=new NullType();
                stack.push(nullType);//有错误时同样要push
            }else{
                stack.push(type_left);
            }
        }else{
            Type nullType=new NullType();
            stack.push(nullType);
        }
        return null;
    }

    @Override
    public T visitArrayvisit(CmmParser.ArrayvisitContext ctx) {
        String s=ctx.getText();
        visitChildren(ctx);
        String name=ctx.children.get(0).getText();
        boolean success=true;
        Type nullType;
        Type type_right=stack.pop();
        Type type_left=stack.pop();
        if(type_left.kind==Kind.ARRAY && type_right.kind==Kind.INT && ((Array)(type_left)).dimention>0){
            //success
        }else if(type_left.kind!=Kind.ARRAY){
            System.err.println("Error type 10 at Line "+getLineNum(ctx)+": \"i\" is not an array.");
            success=false;
        }else if(type_right.kind!=Kind.INT){
            System.err.println("Error type 12 at Line "+getLineNum(ctx)+": \"1.5\" is not an integer.");
            success=false;
        }
//        if(Main.symbolTable.get(name)==null){
//            System.err.println("Error type 1 at Line "+getLineNum(ctx)+": Undefined variable \"j\".");
//            success=false;
//        }else if(Main.symbolTable.get(name).kind!=Kind.ARRAY){
//            System.err.println("Error type 10 at Line "+getLineNum(ctx)+": \"i\" is not an array.");
//            success=false;
//        }
        if(!success){//不成功，直接不需要访问参数列表
            nullType=new NullType();
            stack.push(nullType);
            return null;
        }else {
            int dimention=((Array)(type_left)).dimention;
            dimention--;
            if(dimention!=0) {
                Array array=new Array();
                array.dimention=dimention;
                array.elementType=((Array)(type_left)).elementType;
                stack.push(array);
            }
            else{
                stack.push(((Array)(type_left)).elementType);
            }
            return null;
        }

//        //计算传入的参数个数
//        int stackSize0=stack.size();
//        ctx.children.remove(0);
//        visitChildren(ctx);
//        int stackSize1=stack.size();
//        int params=stackSize1-stackSize0;
//
//        Array array=(Array) (Main.symbolTable.get(name));
////        if(params!=array.dimention){
////
////        }
//        for(int i=0;i<params;i++){
//            Type type=stack.pop();
//            stack.push(type);
//            if(type.kind!=Kind.INT){
//                System.err.println("Error type 12 at Line "+getLineNum(ctx)+": \"1.5\" is not an integer.");
//                success=false;
//                break;
//            }
//        }
//        for(int i=0;i<params;i++){
//            stack.pop();
//        }
//        if(!success){
//            nullType=new NullType();
//            stack.push(nullType);
//            return null;
//        }else{
//            stack.push(array.elementType);
//            return null;
//        }
    }

    @Override
    public T visitArgs(CmmParser.ArgsContext ctx) {
        return super.visitArgs(ctx);
    }
}
