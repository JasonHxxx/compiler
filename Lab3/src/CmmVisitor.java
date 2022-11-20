import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;

public class CmmVisitor<T> extends CmmParserBaseVisitor<T>{
    static int count=0;//用于作为打印空格的计数
    @Override
    public T visitChildren(RuleNode node) {
        T result = this.defaultResult();
        int n = node.getChildCount();

        String ruleName= Trees.getNodeText(node, Arrays.asList(CmmParser.ruleNames));
        if(ruleName.equals("program")&&n==1){
            //do nothing
        }else{
            if(n!=0){//test1里defList没有匹配到的情况
                for(int j=0;j<count;j++)
                    System.err.print("  ");//不能写成println！！！！！！！！！！！！
                char[] arr = ruleName.toCharArray();
                arr[0] = Character.toUpperCase(arr[0]);
                ruleName=new String(arr);
                System.err.print(ruleName);
                //打印行号
                Token startToken=((ParserRuleContext)node).getStart();
                String lineNum=String.valueOf(startToken.getLine());
                System.err.println(" "+"("+lineNum+")");
            }
        }


//        String temp=((RuleContext)node).;

        CmmVisitor.count++;
        for(int i = 0; i < n && this.shouldVisitNextChild(node, result); ++i) {
            ParseTree c = node.getChild(i);
            T childResult = c.accept(this);
            result = this.aggregateResult(result, childResult);
        }

        CmmVisitor.count--;
        return result;
    }

    @Override
    public T visitTerminal(TerminalNode node) {


//这个会将括号直接打印而不是用字母表示
//        String ruleName= Trees.getNodeText(node, Arrays.asList(CmmParser.ruleNames));
//        ruleName=ruleName.toUpperCase();

        Token symbol = node.getSymbol();
        String ruleName=CmmParser.VOCABULARY.getSymbolicName(symbol.getType());//用和visitChildren一样的方法会打印括号而不是LP这种的
        String tokenText= symbol.getText();
        if(!ruleName.equals("EOF")){
            for(int j=0;j<count;j++)
                System.err.print("  ");//不能写成println！！！！！！！！！！！！

            switch (ruleName) {
                case "FLOAT":
                    double re = Double.parseDouble(tokenText);
                    DecimalFormat df = new DecimalFormat("#.000000");
                    df.setRoundingMode(RoundingMode.HALF_UP);//是四舍五入的
                    tokenText = df.format(re);
                    if (tokenText.charAt(0) == '.') {
                        tokenText = "0" + tokenText;
                    }
                    break;
                case "Octal":
                    ruleName = "INT";
                    tokenText = symbol.getText().substring(1);
                    tokenText = String.valueOf(Long.parseLong(tokenText, 8));
                    break;
                case "Hexadecimal":
                    ruleName = "INT";
                    tokenText = symbol.getText().substring(2);
                    tokenText = String.valueOf(Long.parseLong(tokenText, 16));
                    break;
            }

            if(ruleName.equals("ID") || ruleName.equals("TYPE") || ruleName.equals("INT")
                    || ruleName.equals("Octal") || ruleName.equals("Hexadecimal") || ruleName.equals("FLOAT")){//这里提示不会等于Octal或Hex...是错误的
                System.err.println(ruleName+": "+tokenText);
            }
            else{
                System.err.println(ruleName);
            }
        }

        return this.defaultResult();
    }

    @Override
    public T visitErrorNode(ErrorNode node) {
        return super.visitErrorNode(node);
    }
}
