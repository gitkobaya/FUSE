package jp.ac.nihon_u.cit.su.furulab.fuse.models;

import jp.ac.nihon_u.cit.su.furulab.fuse.NodeManager;
import jp.ac.nihon_u.cit.su.furulab.fuse.examples.MeshCell;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** 地形クラスのアブストラクトクラスです<br>
 * 地形クラスは，多くの場合メッシュとして実装されます． */
public abstract class Geometry extends VirtualObject{
    private boolean immutable=true; // 不変かどうか
    private boolean isChanged=false; // 変化したか

    public static double DEFAULT_ALTITUDE=-9999.9;
    //public static double DEFAULT_ALTITUDE=0;

    /** この地形が固定かどうかを取得します<br>
     * 固定でない場合、地形は変化する可能性があります */
    public boolean isImmutable(){
        return this.immutable;
    }

    /** この地形の固定フラグを設定します */
    public void setImmutableFlag(boolean flag){
        this.immutable=flag;
    }

    /** この地形に変化があったかを取得します */
    public boolean isChanged(){
        return isChanged;
    }

    /** 地形の変化フラグを設定します*/
    public void setChangedFlag(boolean flag){
        this.isChanged=flag;
    }

    /** 2点間のLoSを取得します<br>
     * 2点間に地形があり見通しが取れない場合，falseが返ります．<br>
     * 一般に，地形の範囲外を参照した場合には障害物がないのでtrueを返すようにしてください． */
    public abstract boolean getLos(Position start, Position end);

    /**  ある座標における高度を取得します。<br>
     *    引数は、横方向と縦方向です。<br>
     *    基本はX,Yですが、経度、緯度かもしれません。 */
    public abstract double getAltitude(double horizontal, double virtical);

    /** ノードセットを設定します */
    public NodeManager createNodeSet(int step){
        return null;
    }

    /** このジオメトリのX軸方向のサイズを取得します<br>
     * サイズの単位は各クラスが定めます．基本的にはメートルを推奨します */
    public abstract double getSizeX();

    /** このジオメトリのY軸方向のサイズを取得します */
    public abstract double getSizeY();

    /** このジオメトリのZ軸方向のサイズを取得します */
    public abstract double getSizeZ();

    /** 解像度を取得します．小さいほど細かいとされます<br>
     * 何を基準にするかは実装依存ですが、一般にはメートルとします。<br>
     * たとえばメッシュジオメトリの場合、一般に1メッシュの大きさが解像度となります。<br>
     * 領域が重なり合った複数のジオメトリのうち、Environmentがどのジオメトリを採用するかは、<br>
     * 解像度の細かさが優先されます．そのため，Environmentに登録するジオメトリは解像度に対して共通の基準を有する必要があります*/
    public abstract double getResolution();

    /** 水平方向の解像度を取得します．小さいほど細かいとされます<br>
     * 何を基準にするかは実装依存ですが、一般にはメートルとします。<br>
     * たとえばメッシュジオメトリの場合、一般に1メッシュの大きさが解像度となります。<br>
     * 領域が重なり合った複数のジオメトリのうち、Environmentがどのジオメトリを採用するかは、<br>
     * 解像度の細かさが優先されます．そのため，Environmentに登録するジオメトリは解像度に対して共通の基準を有する必要があります*/
    public abstract double getResolutionHorizontal();

    /** 垂直方向の解像度を取得します．小さいほど細かいとされます<br>
     * 何を基準にするかは実装依存ですが、一般にはメートルとします。<br>
     * たとえばメッシュジオメトリの場合、一般に1メッシュの大きさが解像度となります。<br>
     * 領域が重なり合った複数のジオメトリのうち、Environmentがどのジオメトリを採用するかは、<br>
     * 解像度の細かさが優先されます．そのため，Environmentに登録するジオメトリは解像度に対して共通の基準を有する必要があります*/
    public abstract double getResolutionVertical();


    /** 解像度を設定します．小さいほど細かいとされます<br>
     * 何を基準にするかは実装依存ですが、一般にはメートルとします。<br>
     * たとえばメッシュジオメトリの場合、一般に1メッシュの大きさが解像度となります。<br>
     * 領域が重なり合った複数のジオメトリのうち、Environmentがどのジオメトリを採用するかは、<br>
     * 解像度の細かさが優先されます．そのため，Environmentに登録するジオメトリは解像度に対して共通の基準を有する必要があります*/
    public abstract void setResolution(double res);

    /** 指定した点が地形に含まれるかを返します */
    public boolean isInbound(double x, double y){
        boolean result=false;
        double startX=this.getX();
        double startY=this.getY();
        if (startX<=x && x<startX+this.getSizeX() && startY<=y && y<startY+this.getSizeY()){
            result=true;
        }
        return result;
    }

    /** ジオメトリ開始地点のX座標を取得します */
    public double getStartX() {
        return this.getX();
    }

    /** ジオメトリ開始地点のY座標を取得します */
    public double getStartY() {
        return this.getY();
    }

    /** ジオメトリ開始地点のZ座標を取得します */
    public double getStartZ() {
        return this.getZ();
    }

    /** 地形の開始地点(左下座標)を設定します */
    public void setStartPosition(double startX, double startY, double startZ){
        this.setX(startX);
        this.setY(startY);
        this.setZ(startZ);
    }

    /** 地形の開始地点(左下座標)のX座標を設定します */
    public void setStartX(double startX) {
        this.setX(startX);
    }

    /** 地形の開始地点(左下座標)のY座標を設定します */
    public void setStartY(double startY) {
        this.setY(startY);
    }

    /** 地形の開始地点(左下座標)のZ座標を設定します */
    public void setStartZ(double startZ) {
        this.setZ(startZ);
    }

    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage sdp=super.saveStatus();
        sdp.addData("immutable", this.immutable);
        return sdp;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        super.restoreStatus(saveData);
        this.immutable=(Boolean)saveData.getData("immutable");
        return this;
    }
}
