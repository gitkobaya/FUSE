package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import java.util.Comparator;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** スレッドを推定処理時間でソートします */
public class ThreadComparator implements Comparator<ExecThreadBase>{
    @Override
    public int compare(ExecThreadBase o1, ExecThreadBase o2) {
        return (int)(o1.getAgentsProcessTimeEstimated()-o2.getAgentsProcessTimeEstimated());
    }
}
