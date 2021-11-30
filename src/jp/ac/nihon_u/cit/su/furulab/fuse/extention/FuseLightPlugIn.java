package jp.ac.nihon_u.cit.su.furulab.fuse.extention;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;

/** Fuse用の軽量プラグインクラスです */
public abstract class FuseLightPlugIn extends LightPlugIn{

    /** シミュレーションエンジンを取得します<br>
     * FUSEでは常にシミュレーションエンジンがプラグインサーバーとなります */
    public SimulationEngine getEngine(){
        SimulationEngine engine=(SimulationEngine)this.getServer();
        return engine;
    }

    /** 初期化処理では特に何もしない */
    @Override
    public void init(){

    }

    /** 終了処理では特に何もしない */
    @Override
    public void finish(){

    }

}
