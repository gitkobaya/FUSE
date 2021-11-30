package jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import k7system.Model3D;
import k7system.VectorManager;

/** 3D表示対応バーチャルオブジェクトが備えるべきインターフェースです<br>
 * K7SystemのModel3Dクラスを継承しています．このクラスを継承したオブジェクトを生成し，FusePanel3Dへ登録することで描画が始まります．*/
public abstract class ObjectDrawer3D extends Model3D implements ObjectDrawer{
    private VirtualObject targetObject;

    private float[] initialMatrix=VectorManager.createIdentityMatrix(4);

    /** モデルの初期マトリクスを取得します<br>
     * FUSE標準ではX軸正方向が前，Y軸正方向が左，Z軸正方向が上になります<br>
     * モデルの座標がそれと違っている場合，初期マトリクスを設定することで補正します */
    public float[] getInitialMatrix(){
        return this.initialMatrix;
    }

    /** モデルの初期マトリクスを設定します<br>
     * FUSE標準ではX軸正方向が前，Y軸正方向が左，Z軸正方向が上になります<br>
     * モデルの座標がそれと違っている場合，初期マトリクスを設定することで補正します */
    public void setInitialMatrix(float[] initialMatrix){
        this.initialMatrix=initialMatrix;
    }

    @Override
    public long getObjectId() {
        return this.targetObject.getId();
    }

    @Override
    public void setVirtualObject(VirtualObject obj) {
        this.targetObject=obj;
    }

    @Override
    public VirtualObject getVirtualObject() {
        return this.targetObject;
    }

    /** 描画処理 */
    @Override
    public void draw(GL3 gl){
        // オブジェクトのマトリクスを登録
        this.setMatrix(VectorManager.multMatrix4(targetObject.getMatrixf(),initialMatrix));
        super.draw(gl);
    }
}
