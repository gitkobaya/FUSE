package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

public class ExecThreadIndividual extends ExecThreadBase{
    private ThreadManagerIndividual manager;

    public ExecThreadIndividual(SimulationEngine engine, ThreadManagerIndividual manager) {
        super(engine);
        this.manager=manager;
    }

    @Override
    public Long call() {
        this.startTime=System.nanoTime();
        Agent[] agents=this.manager.getAgents();
        while(this.manager.getNumOfRest()>0){
            int marker=this.manager.popAgent();
            this.addPopCount();
            if (marker<0){
                break;
            }
            Agent agt=agents[marker];
            this.callAgentAction(agt);
            //this.jobTime+=agt.getProcessingTime();
        }
        this.sendMessagesToEngine();
        this.endTime=System.nanoTime();
        return this.endTime-startTime;
    }
}
