package jp.ac.nihon_u.cit.su.furulab.fuse.ai.script;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseAIBase;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseTask;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** スクリプト処理用のタスククラスです */
public abstract class TaskByScript extends FuseTask implements AIBaseByScript{
    private Object[] fields=null;
    private String taskName="NoName";
    private int scriptFieldHash=0;

    /** コンストラクタは可変長引数で設定しておきます */
    public TaskByScript(String name,Object... args){
        this.taskName=name;
        this.fields=args;
    }

    /** タスクのフィールドを取得 */
    public Object[] getFields(){
        return this.fields;
    }

    /** このタスクの名前を取得 */
    public String getName(){
        return taskName;
    }

    /** このタスクの名前を設定 */
    public void setName(String name){
        this.taskName=name;
    }

    /** エージェントアクセスのためのインターフェースを取得します */
    public AgentInterface getInterface(){
        return ((AgentByScript)super.getAgent()).getInterface();
    }

    /** タスク情報を渡す処理をオーバーライドしています<br>
     * もしエージェントがアクセス禁止だった場合，nullが返ります． */
    @Override
    public Agent getAgent(){
        AgentByScript agt=(AgentByScript)super.getAgent();
        // アクセス禁止だった場合はnullを返す
        if (agt.forbidsToAccess()){
            agt=null;
        }
        return agt;
    }

    /** デバッグ情報 */
    @Override
    public String debugInfo(){
        String fieldsInfo=new String();
        Object[] fields=this.getFields();
        if (fields!=null){
            for (Object field:fields){
                if (field!=null){
                    fieldsInfo=fieldsInfo+" :"+field.toString();
                }
            }
        }
        return super.debugInfo()+" TaskName:"+this.getName()+" "+fieldsInfo;
    }

    /** ハッシュコードを取得します */
    @Override
    public int hashCode(){
        int nativeHash=this.getClass().hashCode();
        long hash=nativeHash+this.taskName.hashCode()+this.getFieldsHash();
        return (int)(hash & 0xffffffff);
    }

    /** フィールド配列のハッシュ値を取得します */
    @Override
    public int getFieldsHash(){
        int resultInt=0;
        String res="";

        if (this.scriptFieldHash!=0){
            resultInt=this.scriptFieldHash;
        }else{
            long counter=511; // 素数がよさげ
            for (Object thisField:fields){
                double adder=this.getValue(thisField);// 加算用一時キャッシュ
                res+=adder;
            }
            resultInt=res.hashCode();
            this.scriptFieldHash=resultInt;
        }
        return resultInt;
    }
}
