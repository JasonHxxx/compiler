import java.util.HashMap;
import java.util.Map;

public class Structure extends Type{
    public Map<String, Type> memberlist=new HashMap<String, Type>();
    public Kind kind=Kind.STRUCTURE;
    public Structure(){
        super.kind=Kind.STRUCTURE;
    }
}
