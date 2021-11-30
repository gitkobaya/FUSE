package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** 各エージェントの処理を担当するスレッド<br>
 * このクラスそのものはスレッドセーフではないので、複数のスレッドからアクセスしないでください。 */
public class ExecThreadGrain extends ExecThreadBase {
    private ThreadManagerGrained manager;

    /** コンストラクタでシミュレーションエンジンを設定します */
    public ExecThreadGrain(SimulationEngine engine,ThreadManagerGrained manager) {
        super(engine);
        this.manager=manager;
    }

    /** 登録されたエージェントの処理を実施 */
    @Override
    public Long call()  {
        this.startTime=System.nanoTime();
        Agent[] agents=this.manager.getAgents();
        while(this.manager.getNumOfRest()>0){
            Marker m=this.manager.popAgents(); // ここでエージェントを取得
            if (m!=null){
                this.addPopCount();
                for (int i=m.start;i<m.start+m.size;i++){
                    this.callAgentAction(agents[i]);
                    //this.jobTime+=agents[i].getProcessingTime();
                }
            }
        }
        this.sendMessagesToEngine();
        this.endTime=System.nanoTime();
        return this.endTime-startTime;
    }
}
