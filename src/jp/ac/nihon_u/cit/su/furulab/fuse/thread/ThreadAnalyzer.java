package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import java.util.ArrayList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** スレッドの初期値チューニングに利用するクラス */
public class ThreadAnalyzer {
    /** 過去の何サイクルの情報を参考にするかです */
    public static final int CHECK_SIZE=10;
    public static final float AMPLITUDE=0.1f; // 振れ幅
    public static final float SPEED=0.005f; // 移動幅

    private float currentAmplitude=AMPLITUDE;
    private int currentCheckSize=CHECK_SIZE;

    private List<Integer> numOfAgents=new ArrayList<Integer>(10000); // エージェント数
    private List<Long> cycleTimes=new ArrayList<Long>(10000);
    private List<Long> fetchingTimes=new ArrayList<Long>(10000);
    private List<Long> waitingTimes=new ArrayList<Long>(10000);
    private List<Long> wasteTimes=new ArrayList<Long>(10000);
    private List<Long> workingTimes=new ArrayList<Long>(10000);
    private List<Long> maxWorkingTimes=new ArrayList<Long>(10000);
    private List<Long> minWorkingTimes=new ArrayList<Long>(10000);
    private List<Float> aveWorkingTimes=new ArrayList<Float>(10000);
    private List<Float> sigmaWaitingTimes=new ArrayList<Float>(10000); // 待ち時間の分散
    private List<Float> sigmaFetchingTimes=new ArrayList<Float>(10000); // 待ち時間の分散
    private List<Float> sigmaWorkingTimes=new ArrayList<Float>(10000); // 待ち時間の分散
    private List<Float> initialRates=new ArrayList<Float>(10000);
    private List<Float> amplitudes=new ArrayList<Float>(10000);
    private List<Long> fullCycleTimes=new ArrayList<Long>(10000);
    private int counter=0; // サイクル数を数えるカウンター

    private long waitingTime=0;
    private long fetchingTime=0;

    private float initialRate=0.5f; // 初期レートの基準値です
    private float nextRate=0.5f;

    public void init(){
        this.numOfAgents.clear();
        this.cycleTimes.clear();
        this.fetchingTimes.clear();
        this.waitingTimes.clear();
        this.wasteTimes.clear();
        this.sigmaFetchingTimes.clear();
        this.sigmaWaitingTimes.clear();
        this.workingTimes.clear();
        this.initialRates.clear();
        this.currentAmplitude=AMPLITUDE;
        this.currentCheckSize=CHECK_SIZE;
        this.counter=0;
    }

    /** 現在のサイクルの状況を反映します<br>
     * 引数はスレッドクラスのリストと，そのサイクルの実行時間です */
    public void checkCurrentCycle(List<ExecThreadBase> threads, long cycleStartTime, long cycleEndTime, long fullCycleTime){
        long fTime=0;
        long wTime=0;
        long workTime=0;
        int numOfAgents=0;
        // スレッドごとの時間を合計
        for (ExecThreadBase th:threads){
            long threadTime=th.getThreadFinishTime()-th.getThreadStartTime(); // そのスレッドの実行時間
            fTime+=threadTime-th.getAgentsProcessTime(); // フェッチ時間加算
            wTime+=cycleEndTime-th.getThreadFinishTime(); // 待ち時間加算
            workTime+=th.getAgentsProcessTime();
            numOfAgents+=th.getNumOfAgents();
        }
        long fTimeAve=fTime/threads.size();
        long wTimeAve=wTime/threads.size();
        float fetchSigma=0;
        float waitSigma=0;
        float agtWorkSigma=0;

        // エージェント実行時間の平均の算出
        float aveWork=workTime/numOfAgents;
        long maxTime=Long.MIN_VALUE;
        long minTime=Long.MAX_VALUE;

        // 無駄時間とエージェント実行時間の標準偏差の算出
        for (ExecThreadBase th:threads){
            long threadTime=th.getThreadFinishTime()-th.getThreadStartTime(); // そのスレッドの実行時間
            fetchSigma+=Math.pow((threadTime-th.getAgentsProcessTime()-fTimeAve),2); // フェッチ時間から平均を引く
            waitSigma+=Math.pow((cycleEndTime-th.getThreadFinishTime()-wTimeAve),2); // 待ち時間から平均を引く
            for (Long pTime:th.getAgentProcessingTimes()){
                agtWorkSigma+=Math.pow((pTime-aveWork),2); // エージェント実行時間から平均を引く
                if (pTime>maxTime){
                    maxTime=pTime;
                }
                if (pTime<minTime){
                    minTime=pTime;
                }
            }
        }

        agtWorkSigma=(float)Math.sqrt(agtWorkSigma/numOfAgents);
        fetchSigma=(float)Math.sqrt(fetchSigma/threads.size());
        waitSigma=(float)Math.sqrt(waitSigma/threads.size());

        this.numOfAgents.add(numOfAgents);
        this.minWorkingTimes.add(minTime);
        this.maxWorkingTimes.add(maxTime);
        this.cycleTimes.add(cycleEndTime-cycleStartTime);
        this.fetchingTimes.add(fTime);
        this.waitingTimes.add(wTime);
        this.sigmaFetchingTimes.add(fetchSigma);
        this.sigmaWaitingTimes.add(waitSigma);
        this.fullCycleTimes.add(fullCycleTime);
        this.workingTimes.add(workTime);
        this.aveWorkingTimes.add(aveWork);
        this.sigmaWorkingTimes.add(agtWorkSigma);

        this.initialRates.add(this.initialRate);
        this.amplitudes.add(this.currentAmplitude);
        if (threads.get(0) instanceof ExecThreadGrain){
            this.nextRate=computeNextInitialRate();
        }

        this.waitingTime=wTime;
        this.fetchingTime=fTime;

        this.counter++;
    }

    /** 過去のサイクル状況を見て次の初期レートを決定します<br>
     * カウンターが奇数の時は基準値より小さい方へ，偶数のときは基準値より大きい方へ振れます  */
    protected float computeNextInitialRate(){
        float next=this.initialRate;
        int winSmaller=0; // 小さい初期値が勝った数
        int winBigger=0; // 大きい初期値が勝った数
        float rateMin=Float.MAX_VALUE;
        float rateMax=0;
        if (this.counter>CHECK_SIZE){
            for (int i=counter;i>counter-this.currentCheckSize;i--){
                long time1=this.fetchingTimes.get(i)+this.waitingTimes.get(i);
                long time2=this.fetchingTimes.get(i-1)+this.waitingTimes.get(i-1);
                if (i % 2==1){ // iが奇数の時
                    if (time1<time2){
                        winSmaller++;
                    }else if (time1>time2){
                        winBigger++;
                    }
                }else if (i % 2==0){ // iが偶数の時
                    if (time2<time1){
                        winSmaller++;
                    }else if (time2>time1){
                        winBigger++;
                    }
                }
                // 範囲確認
                float ri=this.initialRates.get(i);
                if (i==counter){
                    rateMin=ri;
                    rateMax=ri;
                }else if (ri<rateMin){
                    rateMin=ri;
                }if (rateMax<ri){
                    rateMax=ri;
                }
            }

            // 初期レート変動
            if (winBigger<winSmaller){
                this.initialRate-=SPEED;
                if (this.initialRate<=SPEED){
                    this.initialRate=SPEED;
                }
            }else if (winBigger>winSmaller){
                this.initialRate+=SPEED;
                if (this.initialRate>=1.0f-SPEED){
                    this.initialRate=1.0f-SPEED;
                }
            }

            // 振幅操作(変動が少ないようなら減少させる)
            if (this.initialRate-this.currentAmplitude < rateMin && rateMax < this.initialRate+this.currentAmplitude){
                this.currentAmplitude*=0.95;
                if (this.currentAmplitude<SPEED){
                    this.currentAmplitude=SPEED;
                }
            }else{
                /*
                this.currentAmplitude=(rateMax-rateMin)*2;
                if (this.currentAmplitude>AMPLITUDE){
                    this.currentAmplitude=AMPLITUDE;
                }
                */
                this.currentAmplitude=AMPLITUDE;
                this.currentCheckSize=0;
            }

            this.currentCheckSize++;
            if (this.currentCheckSize>CHECK_SIZE){
                this.currentCheckSize=CHECK_SIZE;
            }
        }
        if (this.counter % 2==0){
            next=this.initialRate+this.currentAmplitude;
        }else{
            next=this.initialRate-this.currentAmplitude;
        }
        if (next<0){
            next=0;
        }else if (next>1){
            next=1.0f;
        }

        return next;
    }

    /** サイクルごとののエージェントの数をリストで取得します */
    public List<Integer> getNumOfAgents(){
        return this.numOfAgents;
    }

    /** サイクルごとののエージェント実行時間の最大値をリストで取得します */
    public List<Long> getMaxWorkingTimes(){
        return this.maxWorkingTimes;
    }

    /** サイクルごとののエージェント実行時間の最小値をリストで取得します */
    public List<Long> getMinWorkingTimes(){
        return this.minWorkingTimes;
    }

    /** フェッチ時間のリストをリストで取得します */
    public List<Long> getFetchingTimes(){
        return this.fetchingTimes;
    }

    /** サイクル時間のリストを取得します */
    public List<Long> getCycleTimes(){
        return this.cycleTimes;
    }

    /** 直近の待ち時間を取得します */
    public long getCurrentWaitingTime(){
        return this.waitingTime;
    }

    /** 待ち時間のリストを取得します */
    public List<Long> getWaitingTimes(){
        return this.waitingTimes;
    }

    /** フェッチ時間の標準偏差のリストを取得します */
    public List<Float> getFetchingTimeSigmas(){
        return this.sigmaFetchingTimes;
    }

    /** 待ち時間の標準偏差のリストを取得します */
    public List<Float> getWaitingTimeSigmas(){
        return this.sigmaWaitingTimes;
    }

    /** 1サイクルのフルタイムを取得します */
    public List<Long> getFullCycleTimes(){
        return this.fullCycleTimes;
    }

    /** 1サイクルのエージェント実行時間のリストを取得します */
    public List<Long> getAgentWorkingTimes(){
        return this.workingTimes;
    }

    /** 1サイクルのエージェント実行時間の標準偏差のリストを取得します */
    public List<Float> getAgentWorkingTimeSigmas(){
        return this.sigmaWorkingTimes;
    }

    /** 振幅値のリストを取得します */
    public List<Float> getAmplitudes(){
        return this.amplitudes;
    }

    /** 現在の振幅値を取得します */
    public float getAmplitude(){
        return this.currentAmplitude;
    }


    /** 次に初期値に指定すべき数値を取得します */
    public float getInitialRate(){
        return this.nextRate;
    }

    /** 初期レートのリストを取得します */
    public List<Float> getInitialRates(){
        return this.initialRates;
    }
}
