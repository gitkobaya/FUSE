package jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects;

import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** 画面表示のエフェクトの親クラスです。<br>
 * エフェクトは完全に表示のみのオブジェクトであり、シミュレーションに影響を及ぼしません。 */
public abstract class Effect {
    private Position mainPosition; // 主要な座標です
    private int life; // ミリ秒

    /** エフェクトの寿命を取得します<br>
     * 単位はミリ秒になります */
    public int getLife(){
        return life;
    }

    /** エフェクトの寿命を設定します<br>
     * 単位はミリ秒になります */
    public void setLife(int life){
        this.life=life;
    }

    /** エフェクトの寿命が尽きているかの判定です */
    public boolean isLive(){
        boolean result=false;
        if (life>0) {
            result=true;
        }
        return result;
    }

    /** エフェクトの寿命を減少させます */
    public void reduceLife(int reduce){
        life-=reduce;
    }

    /** エフェクトの座標を取得します */
    public Position getPosition(){
        return mainPosition;
    }

    /** エフェクトの座標を設定します */
    public void setPosition(Position pos){
        mainPosition=pos;
    }
}
