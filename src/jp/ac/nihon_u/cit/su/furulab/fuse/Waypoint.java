package jp.ac.nihon_u.cit.su.furulab.fuse;


import java.util.LinkedList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.DataContainer;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.FuseLinkedList;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** ユニットの経路を指定するためのウェイポイントです */
public class Waypoint extends Position {
    private long ownerId;
    private Agent ownerUnit=null;
    private List<WaypointEvent> events=new FuseLinkedList<WaypointEvent>();
    private long until=0;


    /** デフォルトコンストラクタ */
    public Waypoint() {
        // TODO 自動生成されたコンストラクター・スタブ
    }

    /** 座標を設定してインスタンスを作成します */
    public Waypoint(Position pos, Agent unit) {
        this.setPosition(pos.clone());
        ownerUnit=unit;
        ownerId=unit.getId();
    }

    /** このウェイポイントの持ち主を取得します */
    public Agent getOwner(){
        return ownerUnit;
    }

    /** このウェイポイントの持ち主を設定します */
    public void setOwner(Agent unit){
        ownerUnit=unit;
    }

    /**(暫定)このウェイポイントの待機時刻を取得します */
    public long getUntilTime(){
        return until;
    }

    /** このウェイポイントに設定されているイベントを取得します */
    public List<WaypointEvent> getEventList(){
        return events;
    }

    /** このウェイポイントにイベントリストを設定します */
    public void setEventList(FuseLinkedList<WaypointEvent> events){
        this.events=events;
    }

    /** このウェイポイントにイベントを設定します */
    public void addEvent(WaypointEvent event){
        events.add(event);
    }

    /** このウェイポイントの座標をPosition型で取得します */
    public Position getPosition(){
        return new Position(this.getX(),this.getY(),this.getZ());
    }

    /** 比較メソッドです */
    @Override
    public boolean equals(Object wp){
        boolean result=false;

        if (wp instanceof Waypoint){
            Waypoint temp=(Waypoint)wp;
            if (temp.getOwner().getId()==this.ownerUnit.getId()){
                double pos[]=temp.get();
                double thisPos[]=this.get();
                if (pos[0]==thisPos[0] && pos[1]==thisPos[1] && pos[2]==thisPos[2]){
                    result=true;
                }
            }
        }

        return result;
    }

    /** 状態保存メソッドです */
    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=super.saveStatus();
        pack.addData("waitUntil", String.valueOf(until));
        return pack;
    }

    /** 状態リストアメソッドです */
    @Override
    public Waypoint restoreStatus(SaveDataPackage saveData) {
        super.restoreStatus(saveData);

        for(DataContainer container:saveData.getAllData()){
            if (container.getName().equals("waitUntil")){
                this.until=Long.valueOf((String)container.getData());
            }
        }

        return this;
    }

}
