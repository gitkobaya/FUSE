package jp.ac.nihon_u.cit.su.furulab.fuse.models;

/** オブジェクトとソート量を設定してソートするためのパッケージです */
public class ObjectSorter  implements Comparable<ObjectSorter>{
    private Object target;
    protected double value;

    public ObjectSorter(Object target, double compareValue) {
        this.value=compareValue;
        this.target=target;
    }

    public Object getTarget(){
        return this.target;
    }

    @Override
    public int compareTo(ObjectSorter o) {
        double check=this.value-o.value;
        int result=0;
        if (check<0){
            result=-1;
        }else if(check>0){
            result=1;
        }
        return result;
    }
}
