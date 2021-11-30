package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** 履歴を利用せず，純粋にエージェント数で割り付けを行います */
public class ThreadManagerGrained extends ThreadManager{
    protected int numOfThreads=1;
    protected long grain=0; // スレッドに渡す処理時間の基準
    protected long minimumSize=0;
    protected static float initialRate=0.50f; // 初期レート
    protected float reduce_rate=0.75f; // 2回目以降のレート

    /** 初期レートを設定します */
    public static void setInitialRate(float rate){
        initialRate=rate;
    }

    /** スレッド数を更新します */
    public void setNumOfThreads(int numOfThreads) {
        this.numOfThreads=numOfThreads;
        this.computeGrain();
    }

    /** エージェントを設定します<br>
     * スレッドの外から実行するのが前提なので，ロックがかかりません．注意してください． */
    public void setAgents(List<Agent> agentList){
        super.setAgents(agentList);
        this.marker=0;
        this.computeGrain();
    }

    /** 初期粒度を計算します */
    protected long computeGrain(){
        if (this.agents!=null){
            this.grain=(long)(this.agents.length/this.numOfThreads*initialRate); // トータル個数をスレッド数で割ったもの
        }
        this.minimumSize=1;
        this.reduce_rate=computeReduceRate(initialRate, this.numOfThreads);
        return this.grain;
    }

    /** 減少率を計算します */
    public static float computeReduceRate(float initial, int numOfThreads){
        return (numOfThreads-initial)/numOfThreads;
    }

    /** 残りのエージェント数を取得します */
    public int getNumOfRest(){
        int result;
        this.agentLock.readLock().lock();
        try{
            result=this.agents.length-this.marker;
        }finally{
            this.agentLock.readLock().unlock();
        }
        return result;
    }

    /** グレインの最小サイズを取得します */
    public long getMinimumGrainSize(){
        return this.minimumSize;
    }

    /** 処理すべきエージェントを取得します */
    public Marker popAgents(){
        this.addPopAgentCount();
        int start=-1;
        int size=0;
        this.agentLock.writeLock().lock();
        try{
            int length=this.agents.length;
            start=this.marker;
            size=(int)this.grain;
            if (size<=this.minimumSize){
                size=(int)this.minimumSize;
            }
            if (size+this.marker>length){
                size=length-this.marker;
            }
            this.marker+=size;
            this.grain*=this.reduce_rate; // 小さくする
        }finally{
            this.agentLock.writeLock().unlock();
        }
        return new Marker(start, size);
    }
}
