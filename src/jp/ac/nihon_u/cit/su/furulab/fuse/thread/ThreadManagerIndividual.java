package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** 動的分散でのマルチスレッドを管理するためのクラスです */
public class ThreadManagerIndividual extends ThreadManager{

    @Override
    public void setAgents(List<Agent> agentList) {
        super.setAgents(agentList);
        this.marker=0;
    }

    /** 残りのエージェント数を取得します */
    public int getNumOfRest(){
        int result;
        this.agentLock.readLock().lock();
        try{
            result=this.agents.length;
        }finally{
            this.agentLock.readLock().unlock();
        }
        return result;
    }

    /** 処理すべきエージェントを取得します */
    public int popAgent(){
        this.addPopAgentCount();
        int result=-1;
        this.agentLock.writeLock().lock();
        try{
            result=marker;
            if (result>=this.agents.length){
                result=-1;
            }
            marker++;
        }finally{
            this.agentLock.writeLock().unlock();
        }
        return result;
    }
}
