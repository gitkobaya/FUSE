package jp.ac.nihon_u.cit.su.furulab.fuse.util;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.DataContainer;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** float型で表す3次元座標です<br>
 * インターフェースはdouble型なのでPositionと置き換えて利用できますが，よりメモリの使用量が少ないのがメリットです<br>
 * そのかわり，メソッドのやり取りの際にキャストが発生するので処理速度では不利になります */
public class Positionf extends Position implements Savable{
    private float[] position=new float[3];

    /** デフォルトコンストラクタ */
    public Positionf() {
    }

    public Positionf(double x, double y, double z) {
        this.position[0]=(float)x;
        this.position[1]=(float)y;
        this.position[2]=(float)z;
    }

    public Positionf(double[] pos) {
        this.position[0]=(float)pos[0];
        this.position[1]=(float)pos[1];
        if (pos.length>2){
            this.position[2]=(float)pos[2];
        }else{
            this.position[2]=0;
        }
    }

    public Positionf(float[] pos) {
        this.position=pos.clone();
    }

    /** 座標を配列の形で取得します */
    @Override
    public double[] get(){
        if (this.position.length==2){
            return new double[]{this.position[0], this.position[1]};
        }else{
            return new double[]{this.position[0], this.position[1], this.position[2]};
        }
    }

    public float[] getf(){
        return this.position;
    }


    @Override
    public double getX(){
        return position[0];
    }

    @Override
    public double getY(){
        return position[1];
    }

    @Override
    public double getZ(){
        return position[2];
    }

    /** 座標を設定します */
    @Override
    public void setPosition(double x, double y, double z){
        this.position[0]=(float)x;
        this.position[1]=(float)y;
        this.position[2]=(float)z;
    }

    /** 座標をコピーします */
    public void setPosition(Positionf pos){
        this.position=pos.position.clone();
    }

    @Override
    public void setX(double x){
        this.position[0]=(float)x;
    }

    @Override
    public void setY(double y){
        this.position[1]=(float)y;
    }

    @Override
    public void setZ(double z){
        this.position[2]=(float)z;
    }

    /** 座標間の距離を取得します */
    public double getDistance(Positionf target){
        double distance;
        double[] p1=this.get();
        double[] p2=target.get();

        distance=Math.sqrt((p1[0]-p2[0])*(p1[0]-p2[0])+(p1[1]-p2[1])*(p1[1]-p2[1])+(p1[2]-p2[2])*(p1[2]-p2[2]));
        return distance;
    }

    /** このインスタンスの示す座標から引数で指定した座標までのベクトルを取得します */
    public Vector getVectorTo(Positionf end){
        double[] result=new double[3];
        double[] pos1=this.get();
        double[] pos2=end.get();

        result[0]=pos2[0]-pos1[0];
        result[1]=pos2[1]-pos1[1];
        result[2]=pos2[2]-pos1[2];

        return new Vector(result);
    }


    /** 文字列化のメソッド */
    @Override
    public String toString(){
        return (new Double(position[0]).toString()+", "+new Double(position[1]).toString()+", "+new Double(position[2]).toString());
    }

    /** コピーメソッド */
    @Override
    public Positionf clone() {
        Positionf clone=new Positionf(position);
        return clone;
    }

    /** 比較メソッドです。<br> */
    @Override
    public boolean equals(Object obj) {
        boolean result=false;

        if (obj!=null && obj.getClass()==Positionf.class){
            Positionf pos=(Positionf)obj;
            if (this.position[0]==pos.position[0] && this.position[1]==pos.position[1] && this.position[2]==pos.position[2]){
                result=true;
            }
        }
        return result;
    }

    /** 状態保存メソッド */
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=new SaveDataPackage(this);
        pack.addData("x", String.valueOf(getX()));
        pack.addData("y", String.valueOf(getY()));
        pack.addData("z", String.valueOf(getZ()));
        return pack;
    }

    /** 状態リストアメソッド */
    public Positionf restoreStatus(SaveDataPackage saveData) {

        for(DataContainer container:saveData.getAllData()){
            if (container.getName().equals("x")){
                this.position[0]=Float.valueOf((String)container.getData());
            }
            if (container.getName().equals("y")){
                this.position[1]=Float.valueOf((String)container.getData());
            }
            if (container.getName().equals("z")){
                this.position[2]=Float.valueOf((String)container.getData());
            }
        }

        return this;
    }

}
