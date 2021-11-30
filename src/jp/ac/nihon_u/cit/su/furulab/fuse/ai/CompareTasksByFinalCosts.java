package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

import java.util.Comparator;

/** TaskをコストでソートするためのComparatorです */
public class CompareTasksByFinalCosts implements Comparator<FuseTask>{

    @Override
    public int compare(FuseTask o1, FuseTask o2) {
        double cost1=o1.getCost();
        double cost2=o2.getCost();
        int result=0;

        if (cost1<cost2){
            result=-1;
        }else if(cost1>cost2){
            result=1;
        }

        return result;
    }
}
