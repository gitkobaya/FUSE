package jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects;

import java.awt.Graphics;

import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;

/** 2D表示対応バーチャルオブジェクトが備えるべきインターフェースです */
public abstract class ObjectDrawer2D implements ObjectDrawer, Drawer2D{
    private VirtualObject vObject;
    private int layer=100; // 標準はこのくらい
    private boolean visible=true;

    /** 表示するかどうかのフラグを取得します */
    public boolean getVisible(){
        return this.visible;
    }

    /** 表示するかどうかを設定します */
    public void setVisible(boolean flag){
        this.visible=flag;
    }

    /** 描画の優先順位を取得します */
    @Override
    public int getLayer() {
        return layer;
    }

    /** 描画の優先順位を設定します */
    public void setLayer(int layer){
        this.layer=layer;
    }

    @Override
    public long getObjectId() {
        return vObject.getId();
    }

    @Override
    public void setVirtualObject(VirtualObject obj) {
        this.vObject=obj;
    }

    @Override
    public VirtualObject getVirtualObject() {
        return vObject;
    }

    /** エージェントを2D描画するためのメソッドです<br>
     * システムはこのメソッドを呼ぶとき，クラスがパネル内に適切な描画を行うことを期待します． */
    public abstract void draw(Graphics g, FusePanel2D panel);

}
