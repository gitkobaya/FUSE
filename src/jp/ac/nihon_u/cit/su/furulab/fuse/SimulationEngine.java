/**
 * since 2012.11
 * @author  K.Kuramoto
 * @version  0.95
 */
package jp.ac.nihon_u.cit.su.furulab.fuse;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jp.ac.nihon_u.cit.su.furulab.fuse.extention.LightPlugInServer;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.logging.AARManager;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.logging.SceneLogger;
import jp.ac.nihon_u.cit.su.furulab.fuse.thread.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

public class SimulationEngine extends LightPlugInServer implements Savable{

    private MultiThreadMode threadMode=MultiThreadMode.ESTIMATED; // マルチスレッドのデフォルトアルゴリズムを指定します
    public  static final int THREAD_PRIORITY_DFAULT=9; // 標準のスレッド優先度です
    private Environment environment;
    private Finisher finish=null;
    private Map<Long,Agent> agents=new HashMap<Long, Agent>();
    private long logicalTime=0;
    private long currentTimeStep=0; // 直近のシミュレーション進行のタイムステップ
    private boolean isPause=false;
    private int simWait=1; // ミリ秒

    // ロック関係
    private ReentrantLock computingLock=new ReentrantLock(); // シミュレーション実行中ロックです
    private ReadWriteLock agentLock=new ReentrantReadWriteLock(); // エージェントマップ操作中ロックです
    private ReadWriteLock sendingMessageLock=new ReentrantReadWriteLock(); // メッセージ送信中ロックです
 
    private ExecutorService exec;
    private int numOfThreads=6; // 想定スレッド数(実際のスレッド数とは一致しない場合がある)

    private ThreadAnalyzer analyzer=new ThreadAnalyzer();

    // 情報交換関係
    private List<Message> messageList=new ArrayList<Message>();
    private Queue<Message> sentMessages=new ArrayDeque<Message>(); // 送信済みメッセージ
    private AttributeManager attrManager=AttributeManager.getInstance();

    // ロギング関係
    private boolean aarLoggingMode=false; // AAR用ロギングを実施中かどうかのフラグです
    private boolean aarMode=false; // AARモードかどうかのフラグです
    private AARManager aarManager=null;

    // ネットワーク対応になった時のためのもの．未使用
    private boolean serverMode=false;

    // 時刻制御関係
    private long surplusTime=0; // そのシミュレーションサイクルでの余剰時間
    private long plusMinus=0; // そのシミュレーションサイクルでの余剰時間
    private boolean delayCancelFlag=false;

    private List<Agent> exitAgents=new ArrayList<Agent>(); // そのサイクルで離脱するエージェントのリスト

    // 現在実行中のシミュレーションの名前(ID)
    private long simId=-1;

    public SimulationEngine(Environment env) {
        this.firstInit();
        this.environment=env;
        ThreadFactory factory=ThreadManager.createThreadFactory(THREAD_PRIORITY_DFAULT);
        this.exec=Executors.newFixedThreadPool(Constants.MAXTHREADS,factory); // スレッドプールを作成する
    }

    /** 強力な初期化です。シナリオ初期化時に読み込まれます */
    public void firstInit(){
        System.setProperty("org.jruby.embed.localcontext.scope", "singlethread"); // スクリプトエンジンのおまじない

        // エージェントが登録されていたら離脱処理を行います
        List<Agent> agents=new ArrayList<Agent>(this.agents.values());
        for (Agent agt:agents){
            this.removeAgent(agt);
        }
        this.agents.clear();
    }

    /** シミュレーションを初期化します。
     * このメソッドを呼び出しても、シミュレーションは自動で進行しません。<br>
     * シミュレーションを進行させるためにはstart()またはupdate()を呼び出す必要があります。 */
    public void init(){
        this.logicalTime=0;
        this.environment.init(this);
        this.initAllPlugins(); // プラグインの初期化処理
        this.analyzer.init(); // スレッドアナライザの初期化
        this.simId=System.nanoTime();

        System.out.println("DEBUG: simulation is initialized");
    }

    /** 現在実行中のシミュレーションIDを取得します<br>
     * シミュレーションIDはシミュレーションエンジンを初期化するたびに変化します． <br>
     * シミュレーションIDはテンポラリな情報であり，セーブ・リストアでは保存・復元されません．*/
    public long getSimulationId(){
        return this.simId;
    }

    /** シミュレーションのマルチスレッドモードを指定します */
    public void setMultiThreadMode(MultiThreadMode mode){
        this.threadMode=mode;
    }

    /** シミュレーションウェイトの取得 */
    public int getSimwait(){
        return simWait;
    }

    /** シミュレーションウェイトの設定 */
    public void setSimwait(int millisec){
        simWait=millisec;
        if (simWait<1){
            simWait=1;
        }
        //System.out.println("DEBUG: simWait:"+simWait);
    }

    /** シミュレーションの遅れをキャンセルします */
    public void cancelDelayTime(){
        this.delayCancelFlag=true;
    }

    /** シミュレーションに使うスレッド数の設定<br>
     * スクリプトを利用する場合はここでスクリプトエンジンが設定されます */
    public void setNumOfThreads(int num){
        numOfThreads=num;
    }

    /** アトリビュートマネージャを取得します */
    public AttributeManager getAttrManager(){
        return this.attrManager;
    }

    /** ログ取得モードかどうかを取得します<br>
     * Check whether it is logging mode or not. */
    public boolean isLoggingMode(){
        return this.aarLoggingMode;
    }

    /** ログ取得モードを設定します<br>
     * このモードではログが取られ，あとでAARを実施できるようになります */
    public void setLoggingMode(boolean flag){
        this.aarLoggingMode=flag;
        if (flag==true && this.aarManager==null){
            this.aarManager=new AARManager();
        }
    }

    /** 現在がログ再生モードかどうかを取得します */
    public boolean isReplayMode(){
        return this.aarMode;
    }

    /** ログ再生モードを設定します */
    public void setReplayMode(AARManager manager){
        this.aarManager=manager;
        this.setReplayMode(true);
    }

    /** ログ再生モードを設定します */
    public void setReplayMode(boolean flag){
        if (flag==true){
            if (this.aarManager!=null){
                this.aarMode=true;
                this.init(); // リプレイモードを設定した時点でシミュレーション初期化
                this.clearAgents(); // エージェント削除
            }
        }else{
            this.aarMode=false;
        }
    }

    /** シミュレーションが停止しているかの確認 */
    public boolean isPaused(){
        return isPause;
    }

    /** シミュレーションの一時停止 */
    public void pause(){
        isPause=true;
    }

    /** シミュレーションの再開 */
    public void resume(){
        isPause=false;
    }

    /** 論理時刻を取得します */
    public long getLogicalTime(){
        return logicalTime;
    }

    /** 論理時刻を設定します<br>
     * 実質的にリストア時専用です．シミュレーション実行中には設定しないでください． */
    public void setLogicalTime(long time){
        this.logicalTime=time;
    }

    /** シミュレーションの前サイクルでの余剰時間を取得します<br>
     * ここに正の値が入った場合，計算能力にはまだ余裕があります． */
    public long getSurplusTime(){
        return this.surplusTime;
    }

    /** シミュレーションの遅れ時間を取得します */
    public long getDelayTime(){
        return this.plusMinus;
    }

    /** シミュレーション処理のロックオブジェクトを取得します<br>
     * このlockしている間、シミュレーション実行がブロックされます */
    public ReentrantLock getSimulationLock(){
        return computingLock;
    }

    /** エージェントマップ読み込みロックを取得します */
    public Lock getAgentReadLock(){
        return this.agentLock.readLock();
    }

    /** エージェントマップ操作ロックを取得します */
    public Lock getAgentWriteLock(){
        //System.out.println("DEBUG: get Write Lock");
        return this.agentLock.writeLock();
    }

    /** メッセージリスト読み込みロックを取得します */
    protected Lock getMessageReadLock(){
        return this.sendingMessageLock.readLock();
    }

    /** メッセージリスト編集ロックを取得します */
    protected Lock getMessageWriteLock(){
        return this.sendingMessageLock.writeLock();
    }


    /** リプレイモードの場合，ある論理時刻へ移動します */
    public void moveToTime(long logicalTime){
        this.logicalTime=logicalTime;
        this.aarManager.replay(logicalTime, this);
    }

    /** スレッド分析クラスを取得します */
    public ThreadAnalyzer getThreadAnalyzer(){
        return this.analyzer;
    }

    /** シミュレーションのリプレイを実施します<br>
     * かなり汚い実装なのであとで直す */
    public void replay(long startTime){
        List<SceneLogger> scenes=this.aarManager.getScenes();
        for(SceneLogger scn:scenes){
            long beginTime=0; // そのサイクルの処理が始まった時刻
            long endTime=0; // そのサイクルの処理が終わった時刻

            while(isPause){
                try{
                    Thread.sleep(50);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            // シミュレーションの実時間遅れの調査
            beginTime=System.currentTimeMillis();
            if (this.delayCancelFlag){
                this.plusMinus=0; //累計の時刻進行遅れをキャンセル
                this.delayCancelFlag=false;
            }

            // リプレイを実施
            this.aarManager.replay(scn.getLogicalTime(), this);

            // シミュレーションウェイト
            endTime=System.currentTimeMillis();
            long currentWait=this.plusMinus+this.simWait-(endTime-beginTime); // このサイクルでの余剰時間
            this.plusMinus=currentWait;

            // 余剰時間があった場合には次に引き継がない
            if (this.plusMinus>0){
                this.plusMinus=0;
            }

            this.surplusTime=this.simWait-(endTime-beginTime);

            if (currentWait<1) {
                currentWait=1;
            }
            try{
                Thread.sleep(currentWait);
                //System.out.println("DEBUG:simwait "+simWait+" current wait:"+currentWait+" endTime:"+endTime+" beginTime"+beginTime+" endTime-beginTime:"+(endTime-beginTime));
            }catch(Exception e){
                e.printStackTrace();
            }

            // 終了フラグの立っているエージェントをまとめて削除
            try{
                this.getAgentWriteLock().lock();
                agents.values().removeAll(exitAgents);
            }finally{
                this.getAgentWriteLock().unlock();
            }
            this.logicalTime=scn.getLogicalTime();
        }
    }

    /** シミュレーションを開始します。<br>
     * このメソッドを呼び出した場合、シミュレーションは自動で進行します。 */
    public void start(long timeStep){
        this.init();
        boolean isFinish=false;
        long beginTime=0; // そのサイクルの処理が始まった時刻
        long endTime=0; // そのサイクルの処理が終わった時刻

        while(!isFinish){
            // シミュレーション実行中か確認
            while(isPause){
                try{
                    Thread.sleep(50);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            // シミュレーションの実時間遅れの調査
            beginTime=System.currentTimeMillis();
            if (this.delayCancelFlag){
                this.plusMinus=0; //累計の時刻進行遅れをキャンセル
                this.delayCancelFlag=false;
            }

            this.update(timeStep); // 時刻更新とシミュレーション処理

            // フィニッシャーに確認する
            if (finish!=null){
                isFinish=finish.isFinish();
            }

            // シミュレーションウェイト
            endTime=System.currentTimeMillis();
            long currentWait=this.plusMinus+this.simWait-(endTime-beginTime); // このサイクルでの余剰時間
            this.plusMinus=currentWait;

            // 余剰時間があった場合には次に引き継がない
            if (this.plusMinus>0){
                this.plusMinus=0;
            }

            this.surplusTime=this.simWait-(endTime-beginTime);

            if (currentWait<1) {
                currentWait=1;
            }
            try{
                Thread.sleep(currentWait);
                //System.out.println("DEBUG:simwait "+simWait+" current wait:"+currentWait+" endTime:"+endTime+" beginTime"+beginTime+" endTime-beginTime:"+(endTime-beginTime));
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        // ログモードだったときに終了時刻を通知
        if (this.aarManager!=null && this.aarLoggingMode){
            this.aarManager.setFinishTime(this.logicalTime);
        }

        if (finish!=null){
            this.finishAllPlugins(); // プラグインの終了処理
            finish.finish();
        }
    }

    private long beforeEnd=0;
    /** 次のタイムステップへ進行します。 */
    public void update(long step){
        this.exitAgents.clear();
        this.computingLock.lock();

        try{
            // 環境モデルの処理を実施
            this.environment.action(step, this);

            // エージェントへのメッセージを配布する
            Lock readLock=this.getMessageReadLock();
            try{
                readLock.lock();
                for(Message mess:this.messageList){
                    // ブロードキャストしようとしていた場合
                    if(mess.getToAgentId()==Agent.BROADCAST_AGENT_ID){
                        // 全てのエージェントに送り込む
                        for(Agent agt:this.getAllAgents()){
                            agt.recieveMessage(mess);
                        }
                    }
                    // 宛先が決まっている場合
                    Agent toAgent=this.getAgentById(mess.getToAgentId());
                    if (toAgent!=null){
                        toAgent.recieveMessage(mess);
                    }
                    // 送信済みメッセージをキューに送る
                    this.sentMessages.add(mess);
                }
            }finally{
                readLock.unlock();
            }

            // 配布したらメッセージリストを空にする
            Lock writeLock=this.getMessageWriteLock();
            try{
                writeLock.lock();
                this.messageList.clear();
            }finally{
                writeLock.unlock();
            }

            // スレッドの数を調整
            int currentThreads=numOfThreads;
            int diff=currentThreads-agents.size(); // スレッド数とエージェント数の差。通常はマイナス
            if (diff>0){ // エージェント数がスレッド数以下になった場合
                currentThreads=currentThreads-diff;
            }
            if (currentThreads<=0){
                currentThreads=1;
            }

            List<Agent> aList=this.getAllAgents();
            List<ExecThreadBase> threads=null;
            ThreadManager manager=null;

            if (currentThreads>1) { // スレッドが2以上あった場合
                switch(this.threadMode){
                // エージェント数によるグレイン実行
                case SIMPLE_GRAIN:
                    manager=this.grainManager;
                    threads=this.execAgentsBySimpleGrain(currentThreads, step, aList);
                    break;

                    // 履歴参照によるグレインでの実行
                case ESTIMATED:
                    manager=this.estimatedManager;
                    threads=this.execAgentsByEstimation(currentThreads, step, aList);
                    break;

                    // 履歴参照による個別実行
                case INDIVIDUAL_ESTIMATED:
                    manager=this.ieManager;
                    threads=this.execAgentByIndividualEstimate(currentThreads, step, aList);
                    break;

                case INDIVIDUAL:
                    manager=this.individualManager;
                    threads=this.execAgentByIndividual(currentThreads, step, aList);
                    break;

                case CYCLIC:
                    threads=this.execAgentByCyclic(currentThreads, step, aList);
                    break;

                case CYCLIC_ESTIMATED:
                    threads=this.execAgentByEstimatedCyclic(currentThreads, step, aList);
                    break;

                }
            }else{ // 1スレッドだった場合
                threads=new ArrayList<ExecThreadBase>();
                ExecThreadSimple simpleThread=new ExecThreadSimple(this, aList);
                threads.add(simpleThread);
            }

            // スレッドの実行
            long startTime=System.nanoTime(); // スレッド処理開始
            try{
                this.getAgentReadLock().lock(); // エージェント処理中は書き込み禁止
                List<Future<Long>> finishTimes=this.exec.invokeAll(threads); // 各スレッドの実行時間を取得
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                this.getAgentReadLock().unlock(); // エージェントロックを外す
            }
            long endTime=System.nanoTime(); // スレッド処理終了

            // プラグイン処理の実行
            this.updateAllPlugins();


            // スレッド分析
            if (this.agents.size()>0){
                this.analyzer.checkCurrentCycle(threads, startTime, endTime, endTime-this.beforeEnd);
                if (manager instanceof ThreadManagerGrained){
                    ((ThreadManagerGrained)manager).setInitialRate(this.analyzer.getInitialRate());
                }
            }
            this.beforeEnd=endTime;

            // ロギングモードならログの取得
            if (this.aarLoggingMode){
                this.aarManager.logging(this);
            }

            // アトリビュートの更新
            this.attrManager.updateAttributes();
            List<Long> deadAgentIds=new ArrayList<Long>(this.exitAgents.size());
            for (Agent agt:this.exitAgents){
                deadAgentIds.add(agt.getId());
            }
            this.attrManager.deleteDeadAgentAttributes(deadAgentIds);

            // 終了フラグの立っているエージェントをまとめて削除
            try{
                this.getAgentWriteLock().lock();
                agents.values().removeAll(exitAgents);
            }finally{
                this.getAgentWriteLock().unlock();
            }
            logicalTime+=step;
        }finally{
            // ロックの解放
            computingLock.unlock();
            currentTimeStep=step;
        }
    }

    /** 静的実行によるスレッド実行 */
    public List<ExecThreadBase> execAgentByCyclic(int currentThreads, long step, List<Agent> aList){
        ArrayList<ExecThreadBase> threads=new ArrayList<ExecThreadBase>(currentThreads);
        for (int i=0;i<currentThreads;i++){
            ExecThreadCyclic th=new ExecThreadCyclic(this);
            th.setTimeStep(step);
            threads.add(th);
        }

        // 0,1,2,,,n,0,1,2,,,nと順番に割り振っていく
        for (int i=0;i<aList.size();i++){
            int target=(int)(i % currentThreads);
            ExecThreadCyclic th=(ExecThreadCyclic)threads.get(target);
            th.addAgent(aList.get(i));
        }
        return threads;
    }

    /** 推測付き静的実行によるスレッド実行 */
    private AgentComparator comparator=new AgentComparator();
    public List<ExecThreadBase> execAgentByEstimatedCyclic(int currentThreads, long step, List<Agent> aList){
        ExecThreadBase[] threadArray=new ExecThreadBase[currentThreads];
        for (int i=0;i<currentThreads;i++){
            ExecThreadCyclic th=new ExecThreadCyclic(this);
            th.setTimeStep(step);
            threadArray[i]=th;
        }

        // ソート処理
        Agent[] agentArray=aList.toArray(new Agent[aList.size()]);
        Arrays.sort(agentArray, this.comparator);

        // 負荷均等になるように割り振っていく
        ThreadComparator tComp=new ThreadComparator();
        for (int i=0;i<agentArray.length;i++){
            Arrays.sort(threadArray, tComp);
            threadArray[0].addAgent(agentArray[i]);
        }
        return Arrays.asList(threadArray);
    }


    private ThreadManagerIndividual individualManager=new ThreadManagerIndividual(); // 毎回作るのももったいないので
    /** 個別動的分散によるスレッド実行 */
    public List<ExecThreadBase> execAgentByIndividual(int currentThreads, long step, List<Agent> aList){
        LinkedList<ExecThreadBase> threads=new LinkedList<ExecThreadBase>();
        this.individualManager.setAgents(aList);
        for (int i=0;i<currentThreads;i++){
            ExecThreadIndividual th=new ExecThreadIndividual(this, this.individualManager);
            th.setTimeStep(step);
            threads.add(th);
        }
        return threads;
    }

    private ThreadManagerGrained grainManager=new ThreadManagerGrained();
    /** 前回実行時間を参照したスレッド実行 */
    public List<ExecThreadBase> execAgentsBySimpleGrain(int currentThreads, long step, List<Agent> aList){
        LinkedList<ExecThreadBase> threads=new LinkedList<ExecThreadBase>();
        this.grainManager.setNumOfThreads(currentThreads);
        this.grainManager.setAgents(aList);
        for (int i=0;i<currentThreads;i++){
            ExecThreadGrain th=new ExecThreadGrain(this, this.grainManager);
            th.setTimeStep(step);
            threads.add(th);
        }
        return threads;
    }

    private ThreadManagerIndividualEstimated ieManager=new ThreadManagerIndividualEstimated(); // 毎回作るのももったいないので
    /** 個別動的分散によるスレッド実行 */
    public List<ExecThreadBase> execAgentByIndividualEstimate(int currentThreads, long step, List<Agent> aList){
        LinkedList<ExecThreadBase> threads=new LinkedList<ExecThreadBase>();
        this.ieManager.setAgents(aList);
        for (int i=0;i<currentThreads;i++){
            ExecThreadIndividual th=new ExecThreadIndividual(this, this.ieManager);
            th.setTimeStep(step);
            threads.add(th);
        }
        return threads;
    }


    private ThreadManagerEstimated estimatedManager=new ThreadManagerEstimated();
    /** 前回実行時間を参照したスレッド実行 */
    public List<ExecThreadBase> execAgentsByEstimation(int currentThreads, long step, List<Agent> aList){
        LinkedList<ExecThreadBase> threads=new LinkedList<ExecThreadBase>();
        this.estimatedManager.setNumOfThreads(currentThreads);
        this.estimatedManager.setAgents(aList);
        for (int i=0;i<currentThreads;i++){
            ExecThreadGrain th=new ExecThreadGrain(this, this.estimatedManager);
            th.setTimeStep(step);
            threads.add(th);
        }
        return threads;
    }

    /** 削除予定エージェントを追加します */
    public synchronized void addExitAgent(Agent agt){
        this.exitAgents.add(agt);
    }

    /** 直近のタイムステップを取得します<br>
     * Get latest timestep. */
    public long getLatestTimeStep(){
        return currentTimeStep;
    }

    /** フィニッシャーを設定します<br>
     *  Define finisher object.*/
    public void setFinisher(Finisher fin){
        finish=fin;
        finish.setEngine(this);
    }

    /** シミュレーションに属するバーチャルオブジェクトをIDで取得します<br>
     * バーチャルオブジェクトとは，エージェント，アセット，ジオメトリを意味します<br>
     * もしバーチャルオブジェクトの種類が分かっているなら，別のメソッドを利用すべきです． */
    public VirtualObject getObjectById(long id){
        VirtualObject result=null;
        for (Geometry geo:this.environment.getGeometries()){
            if (geo.getId()==id){
                result=geo;
                break;
            }
        }

        if (result==null){
            result=this.environment.getAssetById(id);
        }
        if (result==null){
            result=this.getAgentById(id);
        }
        return result;
    }

    /** エージェントを消滅させます
     * 外部から呼び出す場合，シミュレーション一時停止中等に利用してください．シミュレーション実行中(特にエージェントの処理中)に呼び出すとハングアップの可能性があります */
    public void clearAgents(){
        this.getAgentWriteLock().lock();
        try{
            this.agents.clear();
        }finally{
            this.getAgentWriteLock().unlock();
        }
    }

    /** エージェントを追加します<be>
     * 外部から呼び出す場合，シミュレーション一時停止中等に利用してください．シミュレーション実行中(特にエージェントの処理中)に呼び出すとハングアップの可能性があります */
    public void addAgent(Agent agt){
        this.getAgentWriteLock().lock();
        try{
            agt.setEngine(this);
            agents.put(agt.getId(),agt);
        }finally{
            this.getAgentWriteLock().unlock();
        }
    }

    /** エージェントを削除します<br>
     * 外部から呼び出す場合，シミュレーション一時停止中等に利用してください．シミュレーション実行中(特にエージェントの処理中)に呼び出すとハングアップの可能性があります */
    public void removeAgent(Agent agt){
        agt.setEngine(null);
        this.getAgentWriteLock().lock();
        try{
            agents.remove(agt.getId());
        }finally{
            this.getAgentWriteLock().unlock();
        }
    }

    /** 全てのエージェントを取得します<br>
     * 返り値はエージェントのリストとして渡されます．また，リストそのものはインスタンスなので，リストを操作してもエンジン内部の情報には影響はありません．
     * 別の言い方をすると，リストにエージェントオブジェクトを追加したとしても，エンジン内部で管理するエージェントとして登録されるわけではありません．
     * ただし，リストの中身のエージェントオブジェクトは参照であるため，そのエージェントに対する操作はエンジン内部のエージェントに反映されます．*/
    public List<Agent> getAllAgents(){
        ArrayList<Agent> allAgent=null;
        try{
            this.getAgentReadLock().lock();
            allAgent=new ArrayList<Agent>(agents.values());
        }finally{
            this.getAgentReadLock().unlock();
        }
        return allAgent;
    }

    /** 引数で指定したクラスのエージェントを取得します */
    public List<Agent> getAgentsByType(Class<? extends Agent> agentType){
        List<Agent> allAgent=new ArrayList<Agent>();
        List<Agent> target=new ArrayList<Agent>();
        allAgent=this.getAllAgents();
        for (Agent agt:allAgent){
            if (agt.getClass().equals(agentType)){
                target.add(agt);
            }
        }
        return target;
    }

    /** 該当するIDのエージェントを取得します */
    public Agent getAgentById(long id){
        Agent result=null;
        result=agents.get(id);
        return result;
    }

    /** 該当する名前のエージェントを取得します<br>
     * 同じ名前が複数存在した場合、そのうちのどれかが返り値になりますが、指定することはできません */
    public Agent getAgentByName(String name){
        Agent result=null;
        for (Agent agt:this.getAllAgents()){
            if (agt.getName().equals(name)){
                result=agt;
                break;
            }
        }
        return result;
    }

    /** 指定したエージェントの近隣エージェントを取得します */
    public List<Agent> getNeighborAgents(Agent target, double dist){
        List<Agent> neighbors=new ArrayList<Agent>(agents.size()>>2); // 初期値を適切に決めておかないとパフォーマンス落ちます
        double distance;
        for(Agent agt:this.getAllAgents()){
            distance=this.getDistance(target, agt);
            if(distance<dist && agt!=target){ // 自分を含めない処理が必要
                neighbors.add(agt);
            }
        }
        return neighbors;
    }

    /** 指定した座標の近隣エージェントを取得します */
    public List<Agent> getNeighborAgents(Position pos, double dist){
        List<Agent> neighbors=new ArrayList<Agent>(agents.size()>>2); // 初期値を適切に決めておかないとパフォーマンス落ちます
        double distance;
        for(Agent agt:this.getAllAgents()){
            distance=pos.getDistance(agt.getPosition());
            if(distance<dist ){
                neighbors.add(agt);
            }
        }
        return neighbors;
    }

    /** 指定した座標の近隣エージェントを取得します<br>
     * ただし、XY平面での距離での近隣エージェントとします。 */
    public List<Agent> getNeighborAgentsXY(double x, double y, double dist){
        List<Agent> neighbors=new ArrayList<Agent>(agents.size()>>2); // 初期値を適切に決めておかないとパフォーマンス落ちます
        double distance;
        for(Agent agt:this.getAllAgents()){
            double agtX=agt.getX();
            double agtY=agt.getY();
            distance=Math.sqrt(Math.pow(x-agtX,2)+Math.pow(y-agtY,2));
            if(distance<dist ){
                neighbors.add(agt);
            }
        }
        return neighbors;
    }

    /** 指定した座標に最も近いエージェントを取得します */
    public Agent getNearestAgent(Position pos){
        Agent nearest=null;
        double distance=Constants.HUGE;
        for(Agent agt:this.getAllAgents()){
            double dist=pos.getDistance(agt.getPosition());
            if(dist<distance){
                nearest=agt;
                distance=dist;
            }
        }
        return nearest;
    }

    /** 指定した座標にXY平面上で最も近いエージェントを取得します */
    public Agent getNearestAgentXY(double x, double y){
        Agent nearest=null;
        double distance=Constants.HUGE;
        for(Agent agt:this.getAllAgents()){
            double[] pos=agt.getPosition().get();
            double dist=Position.getDistance2D(x, y, pos[0], pos[1]);
            if(dist<distance){
                nearest=agt;
                distance=dist;
            }
        }
        return nearest;
    }

    /** アセットを取得します<br>
     * Environmentにも同名メソッドがあり，このメソッドはそちらを呼び出しています． */
    public List<Asset> getAllAssets(){
        return environment.getAllAssets();
    }

    /** オブジェクト間の距離を取得します */
    public double getDistance(VirtualObject agt1, VirtualObject agt2){
        return agt1.getPosition().getDistance(agt2.getPosition());
    }

    /** メッセージをエンジンに送信するメソッドです<br>
     * このメッセージを直接呼ぶことは推奨しません．できるだけAgentのsendMessageを利用してください． */
    public void sendMessages(List<Message> messages){
        for (Message mess:messages){
            mess.setTimeStump(logicalTime);
        }
        this.getMessageWriteLock().lock();
        try{
            this.messageList.addAll(messages);
        }finally{
            this.getMessageWriteLock().unlock();
        }
    }

    /** メッセージ送信リストを直接設定します．リプレイモード用なのでエージェントは呼び出してはいけません */
    protected void setMessages(List<Message> messages){
        this.messageList=messages;
    }

    /** 前回このメソッドが呼ばれてから送信されたメッセージを取得します。<br>
     * 主にロギング等で使用します．*/
    public List<Message> getSentMessages(){
        this.getMessageWriteLock().lock();
        List<Message> result=new ArrayList<Message>();
        try{
            if (this.sentMessages.size()!=0){
                result.addAll(this.sentMessages);
                this.sentMessages.clear();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            this.getMessageWriteLock().unlock();
        }
        return result;
    }

    /** シミュレーション環境を取得します */
    public Environment getEnvironment(){
        return environment;
    }

    /** シミュレーション環境を設定します */
    public void setEnvironment(Environment env){
        this.environment=env;
    }


    /** シミュレーション世界のX方向の広さを取得します */
    public double getEnvironmentSizeX(){
        return environment.getFirstGeometry().getSizeX();
    }

    /** シミュレーション世界のY方向の広さを取得します */
    public double getEnvironmentSizeY(){
        return environment.getFirstGeometry().getSizeY();
    }

    /** シミュレーション世界のZ方向の広さを取得します */
    public double getEnvironmentSizeZ(){
        return environment.getFirstGeometry().getSizeZ();
    }

    /** AARマネージャを用いてログを保存<br>
     * 現時点でデバッグ用なので気を付けること */
    public void saveLogToFile(File saveFile){
        SaveDataManager.clearPackageCache(); // パッケージキャッシュの初期化
        SaveDataPackage rootPackage=this.aarManager.saveStatus();
        SaveDataManager.saveSimulation(saveFile, rootPackage);
    }

    /** 現在のシミュレーション状態を保存します */
    public void saveSimulationToFile(File savefile){
        SaveDataManager.clearPackageCache(); // パッケージキャッシュの初期化
        SaveDataPackage rootPackage=this.saveStatus();
        SaveDataManager.saveSimulation(savefile, rootPackage);
    }

    /** シミュレーションをリストアします */
    public SaveDataPackage restoreSimulationFromFile(File loadfile){
        // ファイルから状態オブジェクトを作ります
        SaveDataPackage restore=SaveDataManager.restoreSimulation(loadfile);
        this.restoreStatus(restore);
        return restore;
    }

    /** セーブデータ作成メソッドです */
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=new SaveDataPackage(this);
        DataContainer contTime=new DataContainer("logicalTime");
        contTime.setData(logicalTime);
        pack.addData(contTime);

        // 環境の状態を保存します
        pack.addChildPackage(environment);

        // エージェントの状態を保存します
        for(Agent agt:this.getAllAgents()){
            pack.addChildPackage(agt);
        }

        return pack;
    }

    /** シミュレーション復元メソッドです */
    public Savable restoreStatus(SaveDataPackage saveData) {
        System.out.println("DEBUG: restore simulation start");

        // いったんシミュレーションを初期化
        this.firstInit();

        System.out.println("DEBUG: restore logicalTime");
        List<DataContainer> dataList=saveData.getAllData();
        for(DataContainer container:dataList){
            String name=container.getName();
            if (name.equals("logicalTime")){
                logicalTime=Long.parseLong(container.getData().toString());
            }
        }

        // バーチャルオブジェクトの復元を開始
        List<SaveDataPackage> childPackages=saveData.getAllChildren();
        for(SaveDataPackage pack:childPackages){
            // オーナーを調査
            try {
                Object owner=(Class.forName(pack.getOwnerClassName())).newInstance();

                // そのデータの持ち主が環境だった場合
                if (owner instanceof Environment){
                    Environment env=(Environment)pack.restore();
                    this.setEnvironment(env);
                }

                // そのデータの持ち主がエージェントだった場合
                if (owner instanceof Agent){
                    Agent agt=(Agent)pack.restore();
                    this.addAgent(agt);
                }
                pack=null; // 少しでもメモリを回収
            } catch (Exception e) {
                e.printStackTrace();
            }
            // リストアの追加処理を実施
            for (Agent agt:this.getAllAgents()){
                agt.additionalRestore(this);
            }
        }

        // 各エージェントの持っているメッセージに実態を反映
        System.out.println("DEBUG: restore simulation finish");

        return null; // 返す相手がいないので
    }

    /** バージョン取得用メソッドです */
    public String getVersion(){
        return "0.9.261129";
    }

    /** デバッグ用<br>
     * セーブデータ確認メソッドです */
    public void checkSaveData(){
        SaveDataPackage saveData=this.saveStatus();
        this.checkSaveData(saveData);
    }

    public void checkSaveData(SaveDataPackage saveData){
        System.out.println("Checking Save Data is begun");
        CheckSaveData.analyzeData(saveData,0);

        System.out.println("Checking Save Data is finish");
    }
}
