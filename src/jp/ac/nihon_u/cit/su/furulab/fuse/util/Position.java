package jp.ac.nihon_u.cit.su.furulab.fuse.util;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.DataContainer;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** 3次元座標です */
public class Position implements Savable{
    private double[] position=new double[3];

    /** デフォルトコンストラクタ */
    public Position() {

    }

    public Position(double x, double y, double z) {
        this.position[0]=x;
        this.position[1]=y;
        this.position[2]=z;
    }

    public Position(double[] pos) {
        this.position[0]=pos[0];
        this.position[1]=pos[1];
        if (pos.length>2){
            this.position[2]=pos[2];
        }else{
            this.position[2]=0;
        }
    }


    /** 座標を配列の形で取得します */
    public double[] get(){
        return position;
    }

    public double getX(){
        return position[0];
    }

    public double getY(){
        return position[1];
    }

    public double getZ(){
        return position[2];
    }

    /** 座標を設定します */
    public void setPosition(double x, double y, double z){
        this.position[0]=x;
        this.position[1]=y;
        this.position[2]=z;
    }

    /** 座標をコピーします */
    public void setPosition(Position pos){
        this.position=pos.get().clone();
    }

    public void setX(double x){
        this.position[0]=x;
    }

    public void setY(double y){
        this.position[1]=y;
    }

    public void setZ(double z){
        this.position[2]=z;
    }

    /** 座標間の距離を取得します */
    public double getDistance(Position target){
        double distance;
        double[] p1=this.get();
        double[] p2=target.get();

        distance=Math.sqrt((p1[0]-p2[0])*(p1[0]-p2[0])+(p1[1]-p2[1])*(p1[1]-p2[1])+(p1[2]-p2[2])*(p1[2]-p2[2]));
        return distance;
    }

    /** 2次元の座標間の距離を取得します */
    public static double getDistance2D(double x1,double y1,double x2, double y2){
        double distance=Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));

        return distance;
    }

    /** このインスタンスの示す座標から引数で指定した座標までのベクトルを取得します */
    public Vector getVectorTo(Position end){
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
    public Position clone() {
        Position clone=new Position(position);
        return clone;
    }

    /** 比較メソッドです。<br> */
    @Override
    public boolean equals(Object obj) {
        boolean result=false;

        if (obj!=null && obj.getClass()==Position.class){
            Position pos=(Position)obj;
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
    public Position restoreStatus(SaveDataPackage saveData) {

        for(DataContainer container:saveData.getAllData()){
            double value=0;
            if (container.getData() instanceof String){
                value=Double.valueOf((String)container.getData());
            }else if(container.getData() instanceof Double){
                value=(Double)container.getData();
            }else{
                throw (new RuntimeException("Illegal Type of Position element:"+container.getData().getClass()));
            }
            if (container.getName().equals("x")){
                this.position[0]=value;
            }
            if (container.getName().equals("y")){
                this.position[1]=value;
            }
            if (container.getName().equals("z")){
                this.position[2]=value;
            }
        }

        return this;
    }

}
