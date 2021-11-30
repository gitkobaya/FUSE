package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

public class ThreadManager {
    protected Agent[] agents;
    protected int marker; // 割り当て対象のエージェント位置
    protected ReadWriteLock agentLock=new ReentrantReadWriteLock();
    private int numOfPops=0;

    /** エージェントを取得します<br>
     * コピーではなく管理リストの本体が返るので，絶対に加工しないでください */
    public Agent[] getAgents(){
        return this.agents;
    }

    /** エージェントを設定します<br>
     * スレッドの外から実行するのが前提なので，ロックがかかりません．注意してください． */
    public void setAgents(List<Agent> agentList){
        this.agents=agentList.toArray(new Agent[agentList.size()]);
        this.clearPopAgentCount();
    }

    /** エージェントをpopした回数を取得します */
    public int getPopAgentCount(){
        return this.numOfPops;
    }

    /** エージェントをpopした回数を取得します */
    public void clearPopAgentCount(){
        this.numOfPops=0;
    }

    /** エージェントをpopした回数を加算します */
    public void addPopAgentCount(){
        numOfPops++;
    }

    /** Executor Serviceでスレッドの優先度を指定するためのメソッド<br>
     * 指定した優先度のスレッドを作成して返します */
    public static  ThreadFactory createThreadFactory(final int priority) {
        return new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setPriority(priority);
                return thread;
            }
        };
    }

}
