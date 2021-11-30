package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

import java.util.Comparator;

/** TaskをコストでソートするためのComparatorです */
public class CompareConditionsByConditionCosts implements Comparator<Condition>{

    @Override
    public int compare(Condition o1, Condition o2) {
        double cost1=o1.getConditionCost();
        double cost2=o2.getConditionCost();
        int result=0;

        if (cost1<cost2){
            result=-1;
        }else if(cost1>cost2){
            result=1;
        }

        return result;
    }
}
