package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;


/** 個々のメッシュセルのクラスです<br>
 *  実質構造体です。機能を拡張したい場合はこのクラスを継承してください。<br>
 *  ただし、必ず引数なしのコンストラクタで生成できるようにしてください。 */
public class MeshCell implements Savable{
    private CellKind kind=CellKind.Field;
    private double altitude=0;
    private int meshX,meshY; // メッシュ上での座標
    private boolean isChanged=false; // シミュレーション開始時点から変化したかどうか

    public MeshCell() {
    }

    /** シミュレーション開始時から変化したかどうかを取得します */
    public boolean isChanged(){
        return this.isChanged;
    }

    /** シミュレーション開始時から状態が変化した場合に指定します */
    public void changed(){
        this.isChanged=true;
    }

    public void setChangedFlag(boolean flag){
        this.isChanged=flag;
    }

    public CellKind getKind() {
        return kind;
    }

    public void setKind(CellKind kind) {
        this.kind = kind;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    /** X方向の何番目かを取得します */
    public int getMeshX(){
        return this.meshX;
    }

    /** Y方向の何番目かを取得します */
    public int getMeshY(){
        return this.meshY;
    }

    /** X方向の何番目かを設定します */
    public void setMeshX(int x){
        this.meshX=x;
    }

    /** Y方向の何番目かを設定します */
    public void setMeshY(int y){
        this.meshY=y;
    }

    /** クローンメソッドです */
    @Override
    public MeshCell clone(){
        MeshCell result=null;
        try {
            result = (MeshCell)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage sdp=new SaveDataPackage(this);
        sdp.addData("kind", this.kind.toString());
        sdp.addData("altitude", this.altitude);
        sdp.addData("location_x",this.meshX);
        sdp.addData("location_y",this.meshY);
        return sdp;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {

        System.out.println("DEBUG: cell hash:"+saveData.getOwnerHash());
        String k=(String)saveData.getData("kind");
        System.out.println("DEBUG: cell kind:"+k);
        this.kind=CellKind.valueOf(k);
        this.altitude=(Double)saveData.getData("altitude:value");
        this.meshX=(Integer)saveData.getData("location_x:value");
        this.meshY=(Integer)saveData.getData("location_y:value");
        return this;
    }


}
