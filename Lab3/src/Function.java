import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Function extends Type{
    public Kind kind=Kind.FUNCTION;
    public List<Type> paramList=new ArrayList();
    public Type returnType;
    public Function(){
        super.kind=Kind.FUNCTION;
    }
}
