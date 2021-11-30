package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import jp.ac.nihon_u.cit.su.furulab.fuse.Message;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** 各エージェントの処理を担当するスレッド<br>
 * このクラスそのものはスレッドセーフではないので、複数のスレッドからアクセスしないでください。 */
public abstract class ExecThreadBase implements Callable<Long> {
    private List<Agent> agents=null; // このスレッドで処理予定のエージェントです
    private long timeStep=1; // 初期値
    private SimulationEngine engine;
    protected long estimatedJobTime=0; // エージェントの処理時間の予想値
    protected long jobTime=0; // 実際にエージェントの処理に使った時間
    protected long startTime=0; // スレッドの処理を開始した時間
    protected long endTime=0; // スレッドの処理を終了した時間
    protected int popCount=0; // スレッドがジョブを取得しにいった回数

    private List<Long> jobTimes=new ArrayList<Long>(5000);
    private int numOfAgents=0; // このスレッドが実際に処理したエージェント数です

    private List<Message> sendMessageBuffer=new ArrayList<Message>(); // 送信用メッセージの一時バッファ(スレッド処理終了時に一括転送)

    /** コンストラクタでシミュレーションエンジンを設定します */
    public ExecThreadBase(SimulationEngine engine) {
        this.engine=engine;
    }

    /** シミュレーションエンジンを取得します */
    public SimulationEngine getEngine(){
        return this.engine;
    }

    /** 時刻進行のタイムステップを取得します */
    public long getTimeStep(){
        return this.timeStep;
    }

    /** 時刻進行のタイムステップを指定します */
    public void setTimeStep(long step){
        this.timeStep=step;
    }

    /** このスレッドが担当するエージェントを取得します */
    public List<Agent> getAgents(){
        return this.agents;
    }

    /** このスレッドが担当するエージェントを登録します */
    public void setAgents(List<Agent> agts){
        this.agents=agts;
        this.jobTime=0; // ジョブ実行時間初期化
        this.estimatedJobTime=0; // 予想実行時間初期化
    }

    /** このスレッドが担当するエージェントを追加します */
    public void addAgent(Agent agt){
        this.agents.add(agt);
        this.estimatedJobTime+=agt.getProcessingTime();
    }

    /** このスレッドがジョブを取得に行った回数を取得します */
    public int getPopCount(){
        return this.popCount;
    }

    /** このスレッドがジョブを取得に行った回数を加算します */
    public void addPopCount(){
        this.popCount++;
    }

    /** このスレッドのエージェント処理予測時間(nsec)を取得します */
    public long getAgentsProcessTimeEstimated(){
        return this.estimatedJobTime;
    }

    /** このスレッドのエージェント処理時間(nsec)を取得します */
    public long getAgentsProcessTime(){
        return this.jobTime;
    }

    /** このスレッドの開始時刻(nsec)を取得します<br>
     * スレッド終了後に呼び出してください */
    public long getThreadStartTime(){
        return this.startTime;
    }

    /** このスレッドの終了時刻(nsec)を取得します<br>
     * スレッド終了後に呼び出してください */
    public long getThreadFinishTime(){
        return this.endTime;
    }


    /** シミュレーションエンジンにメッセージを送るメソッドです<br>
     * メッセージに既に宛先が登録されている場合に使えます */
    public void sendMessage(Message mess){
        this.sendMessageBuffer.add(mess);
    }

    /** シミュレーションエンジンにメッセージを送るメソッドです<br>
     * 既に宛先がメッセージに登録されている場合に使えます */
    public void sendMessages(List<Message> messages){
        this.sendMessageBuffer.addAll(messages);
    }

    /** SendMessageされたメッセージを実際にエンジンへ送信します */
    public void sendMessagesToEngine(){
        if (this.engine!=null){
            this.engine.sendMessages(this.sendMessageBuffer);
            this.sendMessageBuffer.clear();
        }
    }

    /** エージェントのアクションを呼び出します */
    public void callAgentAction(Agent agt){
        agt.setMyThread(this);
        try{
            agt.callAction(this.getTimeStep());
        }catch(Exception e){
            e.printStackTrace();
        }

        // 移動がエンジンにゆだねられているなら、移動処理
        if(agt.isSetMoveByEngine()){
            //System.out.println("DEBUG: time:"+logicalTime+" agt:"+agt);
            double[] pos=agt.getPosition().get();
            double[] vec=agt.getVelocity();

            pos[0]+=vec[0];
            pos[1]+=vec[1];
            pos[2]+=vec[2];
            agt.setPosition(pos);
            agt.setMoveByEngine(false);
        }

        // もし離脱フラグが立っていたなら
        if (agt.isExitAgent()){
            this.getEngine().addExitAgent(agt);
        }
        long pTime=agt.getProcessingTime();
        this.jobTime+=pTime;
        this.jobTimes.add(pTime);
        this.numOfAgents++;
    }

    /** このスレッドが実際に担当したエージェントの数を取得します */
    public int getNumOfAgents(){
        return this.numOfAgents;
    }

    /** このスレッドで実行したエージェント実行時間のリストを取得します */
    public List<Long> getAgentProcessingTimes(){
        return this.jobTimes;
    }

    /** 登録されたエージェントの処理を実施<br>
     * 返り値はエージェントの実行時間(nsec)です */
    public abstract Long call();
}
