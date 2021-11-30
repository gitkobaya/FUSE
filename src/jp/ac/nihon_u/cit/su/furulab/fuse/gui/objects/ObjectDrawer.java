package jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;

/** エージェントやアセットの表示のためのインターフェースです<br>
 * このインターフェースを実装したバーチャルオブジェクト描画クラスであれば，FUSEが標準で描画できます．<br>
 * コンストラクタで初期化を実施してください．ただし，対応するバーチャルオブジェクトのインスタンスはsetVirtualObjectを呼ばれるまで確定しません． */
public interface ObjectDrawer{

    /** 対応するバーチャルオブジェクトのIDを取得します */
    public long getObjectId();

    /** 対応するバーチャルオブジェクトを設定します */
    public void setVirtualObject(VirtualObject obj);

    /** エージェントを取得します */
    public VirtualObject getVirtualObject();

}
