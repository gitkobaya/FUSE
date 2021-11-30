package jp.ac.nihon_u.cit.su.furulab.fuse.gui;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** エージェント生成 */
public class OperationCreateAgent extends Operation {
    private Agent agent;

    /** コンストラクタでパラメータ設定 */
    public OperationCreateAgent(Agent unit) {
        agent=unit;
    }

    @Override
    public void execute(Object target) {
        SimulationEngine engine=(SimulationEngine)target;
        engine.addAgent(agent);
    }

    @Override
    public void undo(Object target) {
        SimulationEngine engine=(SimulationEngine)target;
        engine.removeAgent(agent);
    }

}
