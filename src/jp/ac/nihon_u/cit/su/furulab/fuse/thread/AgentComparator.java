package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import java.util.Comparator;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** エージェントを消費時間の逆順でソートします */
public class AgentComparator implements Comparator<Agent>{
    @Override
    public int compare(Agent o1, Agent o2) {
        return (int)(o2.getProcessingTime()-o1.getProcessingTime());
    }
}
