package jp.ac.nihon_u.cit.su.furulab.fuse.save.logging;

import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.FuseArrayList;

/** 時刻ごとのバーチャルオブジェクトの状態です */
public class ObjectStatus implements Savable{
    private long objectId=-1;
    private VirtualObject target=null;
    private FuseArrayList<LogData> datum=new FuseArrayList<LogData>();

    public static final String OBJECT_ID="objectId";
    public static final String LOG_DATA="logData";

    /** 対象オブジェクトを指定してコンストラクタを呼びます<br>
     * 生成された時点で座標と削除フラグという基本情報は設定されます */
    public ObjectStatus(VirtualObject obj) {
        this.objectId=obj.getId();
        this.target=obj;
        this.datum=new FuseArrayList<LogData>();
        BasicLogData basicLog=new BasicLogData(obj.getMatrix());
        if (obj instanceof Agent){
            basicLog.setExitFlag(((Agent) obj).isExitAgent());
        }
        this.datum.add(basicLog);
    }

    /** リストア時用 */
    public ObjectStatus(){
    }

    /** オブジェクトを取得します */
    public VirtualObject getObject(){
        return this.target;
    }

    /** オブジェクトを設定します */
    public void setObject(VirtualObject obj){
        this.target=obj;
    }

    /** オブジェクトIDを取得します */
    public long getObjectId(){
        return this.objectId;
    }

    /** オブジェクトIDを設定します */
    public void setObjectId(int objectId){
        this.objectId=objectId;
    }

    /** ログデータを設定します */
    public void setLogData(List<LogData> datum){
        this.datum=new FuseArrayList<LogData>(datum);
    }

    /** ログデータを追加します */
    public void addLogData(LogData data){
        this.datum.add(data);
    }

    /** データを直接追加します */
    public void addData(String name, Object data){
        LogData logData=new LogData(name, data);
        this.addLogData(logData);
    }

    /** ログデータを取得します */
    public List<LogData> getLogDatum(){
        return datum;
    }

    /** 名前を指定してデータを取得します */
    public LogData getLogData(String name){
        LogData result=null;
        for (LogData data:this.datum){
            if (data.getDataName().equals(name)){
                result=data;
                break;
            }
        }
        return result;
    }

    /** 名前を指定してデータを取得します */
    public Object getData(String name){
        Object result=null;
        for (LogData data:this.datum){
            if (data.getDataName().equals(name)){
                result=data.getData();
                break;
            }
        }
        return result;
    }


    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=new SaveDataPackage(this);
        pack.addData(OBJECT_ID, this.objectId);
        pack.addData(LOG_DATA,datum);
        return pack;
    }

    @Override
    public ObjectStatus restoreStatus(SaveDataPackage saveData) {
        this.objectId=(Long)saveData.getData(OBJECT_ID);
        this.datum=(FuseArrayList<LogData>)saveData.getData(LOG_DATA);
        return this;
    }

}
