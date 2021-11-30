package jp.ac.nihon_u.cit.su.furulab.fuse.examples.traffic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.ac.nihon_u.cit.su.furulab.fuse.FuseLink;
import jp.ac.nihon_u.cit.su.furulab.fuse.FuseNode;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** 道路としての情報を有するリンクです */
public class FuseLinkRoad extends FuseLink{
    private double width=3; // 道路幅
    private int lanes=1; // レーン数(歩道の場合は0)
    private double laneWidth=2.75; // 1レーンの幅です
    private boolean oneway=false; // 一方通行属性
    private Set<VirtualObject> objectsOnRoad=new HashSet<VirtualObject>();

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

    /** 道路幅を更新します */
    public void updateWidth(){
        this.width=this.laneWidth*this.lanes;
        if (!this.oneway){
            this.lanes*=2;
        }
    }

    /** この道路に連結している次の道路を取得します．<br>
     * 行き止まり，または交差点にぶつかった場合にはnullが返ります */
    public FuseLinkRoad getNextRoad(){
        FuseLinkRoad nextRoad=null;
        FuseNode dest=this.getDestination();
        List<FuseLink> links=new ArrayList<FuseLink>(dest.getLinks());
        links.remove(this.getStart()); // 出発地点へ伸びるリンクを削除する
        if (links.size()==1){
            nextRoad=(FuseLinkRoad)links.get(0);
        }
        return nextRoad;
    }

    /** この道路系列に位置するエージェントを開始地点からソートして返します */
    public List<VirtualObject> getAgentsWithSort(){
        Set<VirtualObject> objs=this.getVirtualObjects();

        List<VirtualObject> sorted=new ArrayList<VirtualObject>(objs);
        Comparator<VirtualObject> comp=new VehicleComparator(this.getStart().getPosition());
        Collections.sort(sorted, comp);
        return sorted;
    }

    /** あるエージェントの前にいるエージェントを取得します<br>
     * 2番目の引数は何メートル先まで参照するかを設定します */
    public List<VirtualObject> getPrecedingAgent(VirtualObject vo, double distance){
        double targetRange=0;
        List<VirtualObject> objs=this.getAgentsWithSort(); // この道路に属するエージェントを取得
        int index=objs.indexOf(vo);
        // 自分より手前のエージェントを捨てる
        for (int i=0;i<=index;i++){
            objs.remove(0);
        }

        targetRange+=vo.getPosition().getDistance(this.getDestination().getPosition());

        // 先の道路の情報を追加していく
        FuseLinkRoad targetRoad=this.getNextRoad();
        while(targetRoad!=null && targetRange<distance){
            objs.addAll(targetRoad.getAgentsWithSort());
            targetRange+=targetRoad.getStart().getPosition().getDistance(targetRoad.getDestination().getPosition());
            targetRoad=targetRoad.getNextRoad();
        }
        return objs;
    }

    /** 1レーンの幅を取得します<br>
     * 単位はメートルです */
    public double getLaneWidth() {
        return laneWidth;
    }

    /** 1レーンの幅を設定します<br>
     * 単位はメートルです */
    public void setLaneWidth(double laneWidth) {
        this.laneWidth = laneWidth;
        this.updateWidth();
    }

    /** 一方通行属性を取得します */
    public boolean isOneway(){
        return this.oneway;
    }

    /** 一方通行属性を設定します */
    public void setOnewayFlag(boolean flag){
        this.oneway=flag;
        this.updateWidth();
    }

    /** 道路に登録されたオブジェクトを取得します */
    public Set<VirtualObject> getVirtualObjects(){
        return this.objectsOnRoad;
    }

    /** 道路にオブジェクトを登録します */
    public void addVirtualObject(VirtualObject vo){
        this.objectsOnRoad.add(vo);
    }

    /** 道路からオブジェクトを削除します */
    public boolean removeVirtualObject(VirtualObject vo){
        return this.objectsOnRoad.remove(vo);
    }

    /** 指定した座標が何番レーンに当たるかを取得します<br>
     * 0~0.99..ならば1番左のレーンということになります．<br>
     * 明らかに範囲外の場合には-1が返ります */
    /*
    public double getLaneNo(Position pos){

    }
    */

    /** レーン数を取得します */
    public int getLanes() {
        return lanes;
    }

    /** レーン数を設定します */
    public void setLanes(int lanes) {
        this.lanes = lanes;
        this.updateWidth();
    }

}
