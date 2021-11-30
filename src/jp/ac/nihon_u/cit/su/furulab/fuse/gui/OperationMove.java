package jp.ac.nihon_u.cit.su.furulab.fuse.gui;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** 移動を実施するオペレーターです */
public class OperationMove extends Operation {
    private long targetId; // 元エージェントが削除される可能性があるのでIDで管理
    private Position moveTo;
    private Position moveFrom;

    /** コンストラクタで指定 */
    public OperationMove(Agent unit, Position to, long id) {
        targetId=unit.getId();
        moveTo=to;
        moveFrom=unit.getPosition();
        this.setId(id);
    }

    /** 移動を実施するためのメソッド */
    @Override
    public void execute(Object  targetObj) {
        SimulationEngine engine=(SimulationEngine)targetObj;
        Agent target=engine.getAgentById(targetId);
        if (target!=null){
            target.setPosition(moveTo);
        }
    }

    /** 移動を取り消すためのメソッド */
    @Override
    public void undo(Object  targetObj) {
        SimulationEngine engine=(SimulationEngine)targetObj;
        Agent target=engine.getAgentById(targetId);
        if (target!=null){
            target.setPosition(moveFrom);
        }
    }
}
