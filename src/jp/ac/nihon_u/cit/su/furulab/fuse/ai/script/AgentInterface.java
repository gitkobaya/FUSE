package jp.ac.nihon_u.cit.su.furulab.fuse.ai.script;

import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** エージェントとスクリプトの間のやり取りをするためのインターフェースです<br>
 * スクリプト設計者とシミュレーション開発者が別々の場合，エージェントオブジェクトをスクリプトで直接利用してしまうと潜在的なバグの原因となり，また"チート"が可能となる場合があります．<br>
 * そのため，スクリプトが直接にエージェント情報を取得することを禁止し，このようなインターフェースオブジェクトを利用して情報をやりとりするようにします． <br>
 * 最低でも，エージェントが何者なのか，またエージェントの座標に関しては取得できなければなりません．*/
public interface AgentInterface {

    /** このエージェントのIDを取得します */
    public long getId();

    /** 現在の論理時刻を取得します */
    public long getLogicalTime();

    /** エージェントの位置情報を取得します */
    public Position getPosition();

    /** エージェントのX座標を取得します */
    public double getX();

    /** エージェントのY座標を取得します */
    public double getY();

    /** エージェントのZ座標を取得します */
    public double getZ();
}
