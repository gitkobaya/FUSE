package jp.ac.nihon_u.cit.su.furulab.fuse.ai.script;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.script.Invocable;
import javax.script.ScriptEngine;

/** 外部からタスクを構築するためのファクトリクラスです<br>
 * CaSPAをスクリプト駆動する場合にはこれを使ってタスク生成を実施します．*/
public class TaskFactoryForScript{

    /** タスクインスタンスを生成します<br>
     * この時点で，オーナーとなるエージェントは既にスクリプトを評価済みである必要があります */
    public static TaskByScript create(AgentByScript owner, String taskName, Object... args){
        // まだオブジェクト化されていない場合，スクリプトエンジンを駆動してオブジェクトを登録する
        TaskByScript instance=(TaskByScript)owner.invokeMethodToRule("create_task", taskName, args); // Objectの配列であることに注意
        return instance;
    }
}
