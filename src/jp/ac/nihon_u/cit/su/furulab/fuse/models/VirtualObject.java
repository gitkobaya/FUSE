package jp.ac.nihon_u.cit.su.furulab.fuse.models;

import java.util.ArrayList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.DataContainer;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** シミュレーション内で利用されるオブジェクトクラスです<br>
 * エージェント，アセット等のクラスはこのクラスを継承しています．<br>
 * Object class used in simulation. Agent, Asset, Geometry and more some classes are child classes of this class.*/
public abstract class VirtualObject implements Savable{
    private double[] matrix=new double[]{1,0,0,0,  0,1,0,0,  0,0,1,0,  0,0,0,1};// 初期値は単位行列

    /** このオブジェクトのIDです．オブジェクト生成時に決定し，消滅するまで変化しません */
    private long id;
    private String name; //オブジェクトの名前
    private boolean isSubstance=true; // 実体フラグ
    private Position[] aabb=new Position[2]; // このオブジェクトのバウンディング座標


    /** コンストラクタです<br>
     * Constructor */
    public VirtualObject() {
        id=this.hashCode();
        name="Object"+id;
    }

    /** このオブジェクトが実体かどうかを取得します<br>
     * 分散環境等ではシミュレーションエンジンに対してgetAgent等を実施した場合にコピーが渡される可能性があり，その場合はオブジェクトを操作したとしても実態には影響しません<br>
     * Get information which this object is substance or not.<br>
     * In the case of running on distributed environment, Simulation Engine may return clone object and you can't make any effect to real object by operating clone.*/
    public boolean isSubstance(){
        return this.isSubstance;
    }

    /** このオブジェクトが実体かどうかを設定します<br>
     * 基本的にシミュレーションエンジンから呼ばれる処理であり，他から操作する必要はありません */
    protected void setSubstance(boolean flag){
        this.isSubstance=flag;
    }

    /** オブジェクトIDを取得します */
    public long getId(){
        return id;
    }

    /** オブジェクトIDを設定します */
    public void setId(long i){
        id=i;
    }

    /** オブジェクト名を取得します */
    public String getName(){
        return name;
    }

    /** オブジェクト名を設定します */
    public void setName(String n){
        name=n;
    }

    /** 座標を設定します */
    public void setPosition(Position pos){
        this.matrix[12]=pos.getX();
        this.matrix[13]=pos.getY();
        this.matrix[14]=pos.getZ();
    }

    /** 座標を設定します */
    public void setPosition(double[] pos){
        this.matrix[12]=pos[0];
        this.matrix[13]=pos[1];
        this.matrix[14]=pos[2];
    }

    /** 座標を設定します */
    public void setPosition(double x, double y, double z){
        this.matrix[12]=x;
        this.matrix[13]=y;
        this.matrix[14]=z;
    }

    /** 座標を取得します。<br>
     * 安全のため、座標オブジェクトのコピーが返されます。 */
    public Position getPosition(){
        return new Position(this.matrix[12], this.matrix[13], this.matrix[14]);
    }

    public double getX(){
        return this.matrix[12];
    }

    public double getY(){
        return this.matrix[13];
    }

    public double getZ(){
        return this.matrix[14];
    }

    public void setX(double x){
        this.matrix[12]=x;
    }

    public void setY(double y){
        this.matrix[13]=y;
    }

    public void setZ(double z){
        this.matrix[14]=z;
    }

    /** このオブジェクトのバウンディングボックスの対角座標を取得します<br>
     * 適切な値を設定するのは開発者の責任です */
    public Position[] getAABB(){
        return this.aabb;
    }

    /** このオブジェクトのバウンディングボックスの対角座標を設定します */
    public void setAABB(Position min, Position max){
        this.aabb=new Position[]{min, max};
    }

    /** このバーチャルオブジェクトの回転角を指定します．
     *  引数はGL.glLotateと同じです */
    public void rotate(double angle ,double x,double y,double z){
        double sinAngle=Math.sin(angle/180*Math.PI);
        double cosAngle=Math.cos(angle/180*Math.PI);

        this.matrix=new double[]{
            x*x*(1-cosAngle)+cosAngle,x*y*(1-cosAngle)+z*sinAngle,x*z*(1-cosAngle)-y*sinAngle,0,
            x*y*(1-cosAngle)-z*sinAngle,y*y*(1-cosAngle)+cosAngle,y*z*(1-cosAngle)+x*sinAngle,0,
            x*z*(1-cosAngle)+y*sinAngle,y*z*(1-cosAngle)-x*sinAngle,z*z*(1-cosAngle)+cosAngle,0,
            this.matrix[12],this.matrix[13],this.matrix[14],1
        };
    }

    /** 同次行列を取得します */
    public double[] getMatrix(){
        return this.matrix;
    }

    /** 同次行列をfloat型の配列で取得します */
    public float[] getMatrixf(){
        float[] mat=new float[16];
        for(int i=0;i<this.matrix.length;i++){
            mat[i]=(float)this.matrix[i];
        }
        return mat;
    }

    /** 同次行列を設定します */
    public void setMatrix(double[] mat){
        this.matrix=mat.clone();
    }

    /** 同次行列をfloat型で設定します */
    public void setMatrix(float[] mat){
        for(int i=0;i<this.matrix.length;i++){
            this.matrix[i]=mat[i];
        }
    }

    // コンマ区切りの文字列からマトリクスを復元します
    protected double[] decodeMatrix(String mat){
        String[] elems=mat.split(",");
        ArrayList<Double> matrix=new ArrayList<Double>(16);
        for (int i=0;i<elems.length;i++){
            if (elems[i]!=""){
                double val=Double.parseDouble(elems[i]);
                matrix.add(val);
            }
        }
        double[] result=new double[matrix.size()];
        for (int i=0;i<result.length;i++){
            result[i]=matrix.get(i);
        }
        return result;
    }


    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage saveData=new SaveDataPackage(this);
        saveData.addData("name",name);
        saveData.addData("x",this.matrix[12]);
        saveData.addData("y",this.matrix[13]);
        saveData.addData("z",this.matrix[14]);
        return saveData;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        // IDを復元
        this.setId(saveData.getOwnerId());

        // このクラス独自の変数を復元
        List<DataContainer> containers=saveData.getAllData();

        for(DataContainer cont:containers){
            if (cont.getName().equals("name")){
                this.setName(cont.getData().toString());
            }
            if (cont.getName().equals("x")){
                this.matrix[12]=(Double)cont.getData();
            }
            if (cont.getName().equals("y")){
                this.matrix[13]=(Double)cont.getData();
            }
            if (cont.getName().equals("z")){
                this.matrix[14]=(Double)cont.getData();
            }
        }
        return this;
    }
}
