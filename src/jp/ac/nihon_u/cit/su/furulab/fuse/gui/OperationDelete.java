package jp.ac.nihon_u.cit.su.furulab.fuse.gui;

import java.util.concurrent.locks.Lock;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** エージェントの削除を担当するオペレーターです */
public class OperationDelete extends Operation{
    private Agent agent;

    /** コンストラクタです */
    public OperationDelete(Agent unit) {
        agent=unit;
    }

    /** エージェントを削除するメソッドです */
    @Override
    public void execute(Object target) {
        SimulationEngine engine=(SimulationEngine)target;
        Lock lock=engine.getSimulationLock();
        lock.lock();
        try{
            agent.exitSimulation();
            engine.removeAgent(agent);
        }finally{
            lock.unlock();
        }
    }

    /** エージェントの削除を取り消すメソッドです */
    @Override
    public void undo(Object target) {
        SimulationEngine engine=(SimulationEngine)target;
        Lock lock=engine.getSimulationLock();
        lock.lock();
        try{
            agent.cancellToExit();
            engine.addAgent(agent);
        }finally{
            lock.unlock();
        }
    }
}
