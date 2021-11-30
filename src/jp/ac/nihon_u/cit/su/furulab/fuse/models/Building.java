package jp.ac.nihon_u.cit.su.furulab.fuse.models;

import java.util.ArrayList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.FuseArrayList;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** 単純な建物モデルです<br>
 * 屋内の概念はなく，多角形の柱として表現されます */
public class Building extends Asset{
    public static final double HEIGHT_PER_LEVEL=3.5;
    private List<Position> shape=new ArrayList<Position>(); // 建物の外形です．最初と最後に同じ要素が入った閉じた形状です
    private String name="no name";
    private int levels=1; // 階数
    private double height=10; // 高さ

    public Building() {
        // TODO 自動生成されたコンストラクター・スタブ
    }

    /** この建物の名前を取得します */
    public String getName(){
        return this.name;
    }

    /** この建物に名前を設定します */
    public void setName(String name){
        this.name=name;
    }

    /** コンストラクタで形状を決定します */
    public Building (List<Position> shape){
        this.shape=shape;
    }

    /** この建物の階数を取得します */
    public int getLevels(){
        return this.levels;
    }

    /** この建物の階数を設定します<br>
     * 設定すると建物の高さが自動的に設定されます */
    public void setLevels(int lv){
        this.levels=lv;
        this.height=this.levels*HEIGHT_PER_LEVEL;
    }

    /** この建物の高さを取得します */
    public double getHeight() {
        return height;
    }

    /** この建物の高さを設定します */
    public void setHeight(double height) {
        this.height = height;
    }

    /** 外形を取得します */
    public List<Position> getShape(){
        return shape;
    }

    /** 外形を設定します */
    public void setShape(List<Position> position){
        this.shape=position;
    }

    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=super.saveStatus();
        FuseArrayList<Position> shapeCopy=new FuseArrayList<Position>(this.shape);
        pack.addData("shape",shapeCopy);
        return pack;
    }

    @Override
    public Building restoreStatus(SaveDataPackage pack) {
        super.restoreStatus(pack);
        this.shape=(FuseArrayList<Position>)pack.getData("shape:value");
        return this;
    }

}
