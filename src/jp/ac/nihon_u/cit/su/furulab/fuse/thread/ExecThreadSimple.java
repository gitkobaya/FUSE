package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** シングルスレッド用です */
public class ExecThreadSimple extends ExecThreadBase{

    public ExecThreadSimple(SimulationEngine engine, List<Agent> agents) {
        super(engine);
        this.setAgents(agents);
    }

    /** 全部のエージェントを順番に実行 */
    @Override
    public Long call() {
        this.startTime=System.nanoTime();
        List<Agent> agents=this.getAgents();
        for (Agent agt:agents){
            this.callAgentAction(agt);
            //this.jobTime+=agt.getProcessingTime();
        }
        this.sendMessagesToEngine();
        this.endTime=System.nanoTime();
        return this.endTime-this.startTime;
    }

}
