package jp.ac.nihon_u.cit.su.furulab.fuse;

import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.DataContainer;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** エージェント間の情報交換に利用するクラスです<br>
 * 宛先や差出人はIDで指定することを推奨します．参照で指定することもできますが，推奨しません． */
public class Message implements Savable{
    private Agent fromAgent=null;
    private long fromAgentId;
    private Agent toAgent=null;
    private long toAgentId;
    private Savable data;
    private long timeStump;
    private MessageType type=MessageType.Other; // メッセージの種類

    /** 空のメッセージを作成 */
    public Message(){

    }
 
    /** 差出人と送信先を設定 */
    public Message(Agent from, Agent to){
        this(from.getId(),to.getId());
        fromAgent=from;
        toAgent=to;
    }

    /** 差出人と送信先をIDで設定 */
    public Message(long from, long to){
        fromAgentId=from;
        toAgentId=to;
    }

    /** 送信先を取得します */
    public Agent getToAgent(){
        return toAgent;
    }

    /** 送信先IDを取得します */
    public long getToAgentId(){
        return toAgentId;
    }

    /** 送信先を設定します */
    public void setToAgent(Agent toAgent){
        this.toAgent=toAgent;
        this.setToAgentId(toAgent.getId());
    }

    /** 送信先を設定します */
    public void setToAgent(long toId){
        this.setToAgentId(toId);
    }

    /** 送信先を明示的にIDで設定します */
    public void setToAgentId(long t){
        this.toAgentId=t;
    }

    /** 送信元を取得します */
    public Agent getFromAgent() {
        return fromAgent;
    }

    /** 送信元IDを取得します */
    public long getFromAgentId() {
        return fromAgentId;
    }

    /** 送信元を設定します */
    public void setFromAgent(Agent fromAgent){
        //this.fromAgent=fromAgent;
        this.setFromAgentId(fromAgent.getId());
    }

    /** 送信元を設定します */
    public void setFromAgent(long fromId){
        this.setFromAgentId(fromId);
    }

    /** 送信元をIDで明示的に設定します */
    protected void setFromAgentId(long fromId){
        this.fromAgentId=fromId;
    }

    /** メッセージの種類を取得します */
    public MessageType getMessagetType(){
        return type;
    }

    /** メッセージの種類を設定します */
    public void setMessagetType(MessageType type){
        this.type=type;
    }

    /** メッセージのタイムスタンプを設定します。<br>
     *   シミュレーションエンジンが呼び出すメソッドです。 */
    protected void setTimeStump(long time){
        timeStump=time;
    }

    /** メッセージのタイムスタンプを取得します */
    public long getTimeStump(){
        return timeStump;
    }

    /** 通信内容を設定します */
    public void setData(Savable d){
        data=d;
    }

    /** 通信内容を取得します */
    public Savable getData(){
        return data;
    }

    /** メッセージデータの保存 */
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=new SaveDataPackage(this);
        pack.addData("from", fromAgent.getId());
        pack.addData("to", toAgent.getId());
        pack.addData("type", type);
        pack.addChildPackage(data.saveStatus());
        return pack;
    }

    /** メッセージデータの復元<br>
     * <b>まだ未完成</b> */
    public Message restoreStatus(SaveDataPackage saveData) {
        // 差出人と宛先を復元
        List<DataContainer> containers=saveData.getAllData();
        for(DataContainer cont:containers){
            if (cont.getName().equals("from")){
                this.setFromAgentId(Long.parseLong(cont.getData().toString()));
            }
            if (cont.getName().equals("to")){
                this.setToAgentId(Long.parseLong(cont.getData().toString()));
            }
            if (cont.getName().equals("type")){
                this.setMessagetType(MessageType.valueOf((String)cont.getData()));
            }
        }

        // データ本体を復元
        try {
            SaveDataPackage contentSaveData=(SaveDataPackage)saveData.getAllChildren().get(0);
            Savable content=(Savable)Class.forName(contentSaveData.getOwnerClassName()).newInstance();
            content.restoreStatus(contentSaveData);
            this.setData(content);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }
}
