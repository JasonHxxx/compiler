import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main
{
    public static Map<String, Type> symbolTable=new HashMap<String, Type>();
    public static List<Integer> list = new ArrayList();
    public static void main(String[] args) throws IOException {
        String inputFileName="tests/test1.cmm";
        CharStream input=CharStreams.fromFileName(inputFileName);

        //lexical part
        CmmLexer lexer=new CmmLexer(input);

        //syntactical part
        CommonTokenStream tokenStream=new CommonTokenStream(lexer);
        CmmParser parser=new CmmParser(tokenStream);
        ParseTree tree=parser.program();


        //怎么遍历，必须有值的传递
//        CmmVisitor cmmVisitor=new CmmVisitor();
//        cmmVisitor.visit(tree);

        CmmSemanticVisitor cmmSemanticVisitor=new CmmSemanticVisitor();
        cmmSemanticVisitor.visit(tree);

        int debug=0;



//样例10算术运算没有解决



        // 每次遇到一个type
        // 如果是定义，读符号表，看符号表中是否已经有了这个type   如果已经有了就需要在这这一行报错，否则将这个新的type加入符号表
        // 如果是使用，则到符号表中查找这个符号，如果没有这个符号就报错
        // 将相关内容填入符号表————符号表用map做一个简单的映射


    }
}
