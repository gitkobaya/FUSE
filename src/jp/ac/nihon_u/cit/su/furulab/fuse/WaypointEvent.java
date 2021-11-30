package jp.ac.nihon_u.cit.su.furulab.fuse;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;

/** ウェイポイント上で発生するイベントです */
public abstract class WaypointEvent implements Savable{

    /** 設定されているイベントを実行します<br>
     * Agentから呼び出されることを想定しています。 */
    public abstract void executeEvent(Agent agent);


}
