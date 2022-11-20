import java.util.ArrayList;
import java.util.List;

public class Array extends Type{
    public Kind kind=Kind.ARRAY;
    public Type elementType;
    public List<Integer> arrayNum_list = new ArrayList();//用于传数组里的值
    public int dimention;
    public Array(){
        super.kind=Kind.ARRAY;
    }
}
