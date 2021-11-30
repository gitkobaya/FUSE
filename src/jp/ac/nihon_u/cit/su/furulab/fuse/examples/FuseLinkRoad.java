package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import jp.ac.nihon_u.cit.su.furulab.fuse.FuseLink;
import jp.ac.nihon_u.cit.su.furulab.fuse.FuseNode;

/** 道路としての情報を有するリンクです */
public class FuseLinkRoad extends FuseLink{
    private double width=3; // 道路幅
    private int lanes=1; // レーン数(歩道の場合は0)
    private boolean oneway=false; // 一方通行属性

    public FuseLinkRoad(FuseNode start, FuseNode end, double distance) {
        super(start, end, distance);
    }

    /** 道路幅を取得します<br>
     * 単位はメートルです */
    public double getWidth(){
        return width;
    }

    /** 道路幅を設定します<br>
     * 単位はメートルです */
    public void setWidth(double width){
        this.width=width;
    }

    /** 一方通行属性を取得します */
    public boolean isOneway(){
        return this.oneway;
    }

    /** 一方通行属性を設定します */
    public void setOnewayFlag(boolean flag){
        this.oneway=flag;
    }


    /** レーン数を取得します */
    public int getLanes() {
        return lanes;
    }

    /** レーン数を設定します */
    public void setLanes(int lanes) {
        this.lanes = lanes;
    }
}
