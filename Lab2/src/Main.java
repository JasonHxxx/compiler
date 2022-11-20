import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
//import org.antlr.v4.runtime.Token;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
public class Main
{
    public static int lastLine=-1;
    public static List<Integer> list = new ArrayList();
    public static boolean judgement=true;//judgement为设置为true，没有错误，在报错分支里设置为false，执行完成之后恢复为true
    public static void main(String[] args) throws Exception{


//        String inputFileName="tests/test1.cmm";
//        CharStream input=CharStreams.fromFileName(inputFileName);
        InputStream is=System.in;
        if(args.length>0){
            String inputFileName=args[0];
            is=new FileInputStream(inputFileName);
        }else{
            return;
        }

//        String inputFileName="tests/test6.cmm";
//        is=new FileInputStream(inputFileName);
        CharStream input=CharStreams.fromStream(is);

        //lexical part
        CmmLexer lexer=new CmmLexer(input);
        /*...*/

        //syntactical part
        CommonTokenStream tokenStream=new CommonTokenStream(lexer);
        CmmParser parser=new CmmParser(tokenStream);
        /*...*/

        //下面是实现报错自定义的一种方法
        parser.removeErrorListeners();
        parser.addErrorListener(new CustomErrListener());

        ParseTree tree=parser.program();
        //System.err.println(tree.toStringTree(parser));//？？？？？？err还是out输出？
        /*...*/


        //if语句判断是否有错误，没有错误就进行正常的打印
        if(Main.judgement){
            CmmVisitor cmmVisitor=new CmmVisitor();
            cmmVisitor.visit(tree);
        }
        Main.judgement=true;
    }
}

