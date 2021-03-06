package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** 履歴情報を利用してマルチスレッドを管理するためのクラスです */
public class ThreadManagerEstimated extends ThreadManagerGrained{
    private long totalTime=0;
    private AgentComparator comparator=new AgentComparator();
    private int grainCounter=0; // 現在のポップ対象のグレイン番号です
    private Marker[] grains; // グレイン分割情報です．最後にnullが入っています
    private static final long MINIMUM_TIME_UNIT=50; // ns

    /** エージェントを設定します<br>
     * このメソッドが実質的な初期化ルーチンになります．<br>
     * スレッド処理に入る前に実行するのが前提なので，ロックがかかりません．注意してください． */
    @Override
    public void setAgents(List<Agent> agentList){
        super.setAgents(agentList);

        //long testA=System.nanoTime();
        this.totalTime=0;
        this.grainCounter=0;

        for (Agent agt:this.agents){
            this.totalTime+=agt.getProcessingTime();
        }
        // エージェントを実行時間で降順にソート
        Arrays.sort(this.agents, this.comparator);
        //long testB=System.nanoTime();
        this.computeGrain();
        this.grains=this.divideGrains();
        //long testC=System.nanoTime();
        //long timeA=testB-testA;
        //long timeB=testC-testB;
        //long timeC=testC-testA;
    }

    /** グレイン分割を実施します<br>
     * 必ずエージェントを登録し，ソートしてから呼んでください*/
    protected Marker[] divideGrains(){
        Marker[] grs=new Marker[this.agents.length+1]; // 最後にnullが必要なので
        int agentMarker=0;
        int size=0;
        int counter=0;
        int length=this.agents.length;
        long diff=0;

        // グレインを分割する
        while (agentMarker<length){
            long time=0;
            int start=agentMarker;

            long currentGrainSize=this.grain-diff;
            long pTime=0;
            do{
                pTime=this.agents[agentMarker].getProcessingTime();
                if (pTime<MINIMUM_TIME_UNIT){ // あまりに小さい値は信用しない
                    pTime=MINIMUM_TIME_UNIT;
                }
                time+=pTime;
                agentMarker++;
            }while((time<=currentGrainSize || time<this.minimumSize) && agentMarker<length);
            diff=time-currentGrainSize; // オーバーしてしまった分
            size=agentMarker-start;
            grs[counter]=new Marker(start, size);

            this.grain*=this.reduce_rate;
            counter++;
        }

        return grs;
    }

    /** 初期粒度を計算します<br>
     * 必ずソートしてから呼び出すこと<br>
     * 万が一ソート前に呼ぶと最小グレインサイズの指定がひどいことになります  */
    @Override
    protected long computeGrain(){
        this.grain=(long)(this.totalTime/this.numOfThreads*initialRate); // トータル時間をスレッド数で割ったもの
        this.minimumSize=(long)(this.totalTime/this.numOfThreads*0.001); // グレインサイズ(スレッドあたりの大きさの0.1%)
        long minimumAgent=Long.MAX_VALUE;
        long minTime=0;
        if (this.agents!=null){
            minimumAgent=this.agents[this.agents.length-1].getProcessingTime()*10; // 最も小さいエージェント時間の10倍
            minTime=MINIMUM_TIME_UNIT*10; // 最小時間の10倍
            if (minimumAgent<minTime){
                minimumAgent=minTime;
            }
            minimumAgent*=this.numOfThreads; // スレッド数を掛けることでコリジョン回避
        }
        if (minimumAgent<this.minimumSize){ // 小さい方を選ぶ
            this.minimumSize=minimumAgent;
        }

        this.reduce_rate=computeReduceRate(initialRate, this.numOfThreads);
        return this.grain;
    }

    /** 処理すべきエージェントを取得します */
    @Override
    public Marker popAgents(){
        this.addPopAgentCount();
        Marker result=null;
        this.agentLock.writeLock().lock();
        try{
            result=this.grains[this.grainCounter];
            if (result!=null){
                this.grainCounter++;
                this.marker=result.start+result.size; // 残りを確認させるため
            }
        }finally{
            this.agentLock.writeLock().unlock();
        }
        return result;
    }
}