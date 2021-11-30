package jp.ac.nihon_u.cit.su.furulab.fuse.save.logging;

import java.util.ArrayList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.Message;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.FuseArrayList;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.FuseList;

/** ある論理時刻のバーチャルオブジェクトの状況を記録するためのクラスです */
public class SceneLogger implements Savable{
    private long logicalTime=0;
    private FuseList<ObjectStatus> statuses=new FuseArrayList<ObjectStatus>();
    private FuseList<Message> messages=new FuseArrayList<Message>();
    private FuseList<Agent> removedAgents=new FuseArrayList<Agent>(); // この時刻に取り除かれるエージェント

    public SceneLogger() {
    }

    /** バーチャルオブジェクトを設定してロガーを作成します */
    public SceneLogger(long logicalTime) {
        this.logicalTime=logicalTime;
    }

    /** 論理時刻を取得します */
    public long getLogicalTime(){
        return this.logicalTime;
    }

    /** 論理時刻を指定します */
    public void setLogicalTime(long logicalTime){
        this.logicalTime=logicalTime;
    }

    /** この時刻にやり取りされたメッセージを取得します */
    public List<Message> getMessages(){
        return this.messages;
    }

    /** この時刻にやり取りされたメッセージを保存します */
    public void setMessages(List<Message> messages){
        this.messages=new FuseArrayList<Message>(messages);
    }

    /** オブジェクトの状態を保存します */
    public void logging(VirtualObject vObj){
        ObjectStatus status=null;
        if (vObj instanceof Loggable){
            if (((Loggable) vObj).isChanged()){
                status=((Loggable) vObj).getObjectStatus();
            }
        }else{
            status=new ObjectStatus(vObj);
        }

        // オブジェクトがLoggableで変化があった場合，情報を保存．
        // オブジェクトがLoggableで変化が無かった場合，何もしない
        // Loggableでない場合は最低限の情報のみ保存
        if (status!=null){
            this.statuses.add(status);
        }
    }

    /** オブジェクト状況を取得します */
    public List<ObjectStatus> getStatuses(){
        return this.statuses;
    }

    /** このシーンで除去されるエージェントを取得します */
    public List<Agent> getRemovedAgents(){
        return this.removedAgents;
    }

    /** このシーンで除去されるエージェントを設定します */
    public void setRemovedAgents(List<Agent> agents){
        this.removedAgents=new FuseArrayList<Agent>(agents);
    }

    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=new SaveDataPackage(this);
        pack.addData("logicalTime", this.logicalTime);
        pack.addData("statuses", this.statuses);
        pack.addData("messages", this.messages);
        pack.addData("removedAgents", this.removedAgents);
        return pack;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        this.logicalTime=(Long)saveData.getData("logicalTime");
        this.statuses=(FuseList<ObjectStatus>)saveData.getData("statuses");
        this.messages=(FuseList<Message>)saveData.getData("messages");
        this.removedAgents=(FuseList<Agent>)saveData.getData("removedAgents");
        return this;
    }
}
