package jp.ac.nihon_u.cit.su.furulab.fuse.save.logging;

import java.util.*;

import jp.ac.nihon_u.cit.su.furulab.fuse.Environment;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Asset;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Geometry;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.FuseArrayList;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.FuseList;

/** ログを取り，またログを再生するためのクラスです<br>
 * Class for logging simulation or replaying with log. */
public class AARManager implements Savable{
    private static final long DEFAULT=-1;
    private FuseList<SceneLogger> sceneLoggers=new FuseArrayList<SceneLogger>();
    private long beginTime=-1;
    private long finishTime=-1;
    private SceneLogger latestScene=null; // 直近に再生したシーン


    public AARManager() {
        this.sceneLoggers.clear();
    }

    /** ログ終了時刻を取得します */
    public long getFinishTime(){
        return this.finishTime;
    }

    /** ログ終了時刻を設定します */
    public void setFinishTime(long finishTime){
        this.finishTime=finishTime;
    }

    /** ある論理時刻のログを取ります */
    public void logging(SimulationEngine engine){
        long logicalTime=engine.getLogicalTime();
        SceneLogger scene=new SceneLogger(logicalTime);

        if (beginTime==DEFAULT){
            beginTime=logicalTime;
        }

        // メッセージを保存
        scene.setMessages(engine.getSentMessages());

        Environment env=engine.getEnvironment();
        // アセットを保存
        for (Asset as:env.getAllAssets()){
            scene.logging(as);
        }
        // ジオメトリを保存
        for (Geometry geo:env.getGeometries()){
            if (geo instanceof Loggable){
                if (((Loggable) geo).isChanged()){
                    scene.logging(geo);
                }
            }
        }

        // エージェントを保存
        List<Agent> agents=engine.getAllAgents();
        for(Agent agt:agents){
            scene.logging(agt);
        }

        // その時刻の変化がゼロでないなら追加
        if (scene.getStatuses().size()!=0){
            this.sceneLoggers.add(scene);
        }
    }

    /** ある論理時刻の状況を復元します */
    public void replay(long logicalTime, SimulationEngine engine){
        // その論理時刻のシーン情報を取得
        SceneLogger scene=this.getScene(logicalTime);

        if (this.latestScene!=scene){
            // オブジェクトの状態を復元
            for (ObjectStatus status:scene.getStatuses()){
                long id=status.getObjectId();
                VirtualObject obj=engine.getObjectById(id);
                // 存在しないオブジェクトだった場合にはエンジンに登録
                if (obj==null){
                    obj=status.getObject();
                    if (obj instanceof Agent){
                        engine.addAgent((Agent)obj);
                    }else if (obj instanceof Asset){
                        engine.getEnvironment().addAsset((Asset)obj);
                    }
                }

                Object data=status.getLogDatum().get(0);
                if (data instanceof BasicLogData){
                    BasicLogData basic=(BasicLogData)data;
                    double[] mat=basic.getMatrix();
                    obj.setMatrix(mat);
                    if (basic.isExit()){
                        engine.addExitAgent((Agent)obj);
                    }
                }
                if (obj instanceof Loggable){
                    Loggable objLogger=(Loggable)obj;
                    objLogger.setObjectStatus(status);
                }
            }
            this.latestScene=scene;
        }
    }

    /** シーン一覧を取得します */
    public List<SceneLogger> getScenes(){
        return this.sceneLoggers;
    }

    /** 指定したシーンにもっとも近いシーンを取得します */
    public SceneLogger getScene(long logicalTime){
        SceneLogger result=null;
        if (this.sceneLoggers!=null && this.sceneLoggers.size()!=0 && this.sceneLoggers.get(0).getLogicalTime()<=logicalTime){
            if (logicalTime<=this.sceneLoggers.get(this.sceneLoggers.size()-1).getLogicalTime()){
                result=this.sceneLoggers.get(binarySearch(logicalTime,0, this.sceneLoggers.size()-1));
            }else{
                result=this.sceneLoggers.get(this.sceneLoggers.size()-1);
            }
        }
        return result;
    }

    /** デバッグ用 */
    public void setSceneLoggersDebug(List<SceneLogger> scenes){
        this.sceneLoggers=new FuseArrayList<SceneLogger>(scenes);
    }

    /** 自作バイナリサーチ<br>
     * logicalTimeと全く同じ数値が見つからなかった場合，logicalTimeの直前のSceneLoggerを選びます */
    public int binarySearch(long logicalTime, int start, int end){
        int result=-1;
        if(this.sceneLoggers.get(end).getLogicalTime()<=logicalTime){
            // 最後の要素がヒットした場合
            result=end;
        }else if (end-start==0){
            // 要素が1つしかなかった場合
            result=start;
        }else if (end-start==1){
            // 要素が2つの場合
            if (this.sceneLoggers.get(start).getLogicalTime()<=logicalTime && logicalTime<this.sceneLoggers.get(end).getLogicalTime()){
                result=start;
            }
        }else{
            int half=(start+end+1)/2;
            long logTimeHalf=this.sceneLoggers.get(half).getLogicalTime();
            if (logTimeHalf==logicalTime){
                result=half;
            }else{
                if (logTimeHalf<logicalTime){// ターゲットが真ん中より大きいなら
                    result=binarySearch(logicalTime, half, end); // 後半を探索
                }else{
                    result=binarySearch(logicalTime, start, half); // 真ん中より小さいなら前半を探索
                }
            }
        }
        return result;
    }


    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=new SaveDataPackage(this);
        pack.addData("scenes",this.sceneLoggers);
        pack.addData("beginTime",this.beginTime);
        pack.addData("finishTime",this.finishTime);
        pack.addData("latestScene",this.latestScene);
        return pack;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        this.sceneLoggers=(FuseList<SceneLogger>)saveData.getData("scenes");
        this.beginTime=(Long)saveData.getData("beginTime");
        this.finishTime=(Long)saveData.getData("finishTime");
        this.latestScene=(SceneLogger)saveData.getData("latestScene");
        return this;
    }

}
