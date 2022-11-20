import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
//import org.antlr.v4.runtime.Token;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
public class Main
{
    public static boolean judgement=true;
    public static void main(String[] args){
        String fileName="tests/test3.cmm";
        CharStream input;
        String text="";
        try {
            File file=new File(fileName);
            FileReader fileReader=new FileReader(file);
            BufferedReader br=new BufferedReader(fileReader);
            StringBuilder sb=new StringBuilder();
            String temp="";
            while ((temp = br.readLine()) != null) {
                sb.append(temp).append("\n");
            }
            br.close();
            text=sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        input = CharStreams.fromString(text);


        CmmLexer lexer = new CmmLexer(input){
            @Override
            public void notifyListeners (LexerNoViableAltException e) {
                String output="";
                String text = _input.getText(Interval.of(_tokenStartCharIndex, _input.index()));
                output="Error type A at Line "+String.valueOf(getLine())+": undefined symbols "+text.trim()+" ";
                System.err.println(output);
                Main.judgement=false;
            }
        };

        List<? extends Token> tokenList = null;
        tokenList = lexer.getAllTokens();
        if(Main.judgement){
            for(int i=0;i<tokenList.size();i++){
                String output="";
                String tokenText= tokenList.get(i).getText();
                String ruleName=lexer.getVocabulary().getSymbolicName(tokenList.get(i).getType());
                if(ruleName.equals("FLOAT")){
                    double re=Double.parseDouble(tokenText);
                    DecimalFormat df = new DecimalFormat("#.000000");
                    df.setRoundingMode(RoundingMode.HALF_UP);//是四舍五入的
                    tokenText=df.format(re);

                    if(tokenText.charAt(0) == '.'){
                        tokenText="0"+tokenText;
                    }
                }
                if(ruleName.equals("Octal")){
                    ruleName="INT";
                    tokenText = tokenList.get(i).getText().substring(1);
                    tokenText = String.valueOf(Long.parseLong(tokenText,8));
                    //tokenText = String.valueOf(OctalToDecimal(Integer.parseInt(tokenText)));
                }
                if(ruleName.equals("Hexadecimal")){
                    ruleName="INT";
                    tokenText = tokenList.get(i).getText().substring(2);
                    tokenText = String.valueOf(Long.parseLong(tokenText,16));
                }

                String lineNum=String.valueOf(tokenList.get(i).getLine());
                output=ruleName+" "+tokenText+" "+"at Line"+" "+lineNum+".";
                if(i==tokenList.size()){
                    System.err.print(output);
                }else{
                    System.err.println(output);
                }

            }
        }
        Main.judgement=true;
    }
    public static int OctalToDecimal(int octal)
    {
        int decimalNumber = 0, i = 0;
        while(octal != 0)
        {
            decimalNumber += (octal % 10) * Math.pow(8, i);
            ++i;
            octal/=10;
        }
        return decimalNumber;
    }
}

