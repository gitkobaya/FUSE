package jp.ac.nihon_u.cit.su.furulab.fuse;

import java.util.Comparator;

public class SortNodes implements Comparator<FuseNode>{
    public int compare(FuseNode o1, FuseNode o2) {
        long result=o1.getId()-o2.getId();
        if (result<0){
            return -1;
        }else if (result>0){
            return 1;
        }
        return 0;
    }
}
