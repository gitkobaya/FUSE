package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import java.util.Arrays;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** 履歴情報を利用してマルチスレッドを管理するためのクラスです */
public class ThreadManagerIndividualEstimated extends ThreadManagerIndividual{
    private AgentComparator comparator=new AgentComparator();

    /** エージェントを設定します<br>
     * スレッドの外から実行するのが前提なので，ロックがかかりません．注意してください． */
    @Override
    public void setAgents(List<Agent> agentList){
        super.setAgents(agentList);
        Arrays.sort(this.agents, this.comparator);
    }
}

