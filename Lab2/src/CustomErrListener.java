import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;

public class CustomErrListener implements ANTLRErrorListener {
    //主要是重写这个方法
    //主要是重写这个方法
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int line, int charPositionInLine, String msg, RecognitionException e) {
        Main.judgement=false;
//        if(line!=Main.lastLine){
//            System.err.println("Error type B at Line "+line+": "+msg);
//            Main.lastLine=line;
//        } else{
//            //do nothing
//        }
        if(Main.list.contains(line)){
            //do nothing
        }else{
            System.err.println("Error type B at Line "+line+": "+msg);
            Main.list.add(line);
        }
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

    }
}
