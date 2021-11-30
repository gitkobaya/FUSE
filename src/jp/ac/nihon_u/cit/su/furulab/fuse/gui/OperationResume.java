package jp.ac.nihon_u.cit.su.furulab.fuse.gui;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** シミュレーションを実行するオペレーターです */
public class OperationResume extends Operation {

    private SaveDataPackage current;

    @Override
    public void execute(Object targetObj) {
        SimulationEngine engine=(SimulationEngine)targetObj;

        // 現状を保存する
        this.current=engine.saveStatus();

        // 遅れ時間を初期化する
        engine.cancelDelayTime();
        // シミュレーションを再開する
        engine.resume();

    }

    @Override
    public void undo(Object targetObj) {
        SimulationEngine engine=(SimulationEngine)targetObj;
        // 保存された時点までシミュレーションを巻き戻す
        engine.restoreStatus(this.current);
    }

}
