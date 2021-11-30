package jp.ac.nihon_u.cit.su.furulab.fuse.ai.script;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;

import jp.ac.nihon_u.cit.su.furulab.fuse.ai.Condition;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseAIBase;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.Truth;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** 外部から条件オブジェクトを生成するためのスケルトンクラスです<br>
 * CaSPAを外部から駆動する場合にはこれを使います */
public abstract class ConditionByScript extends Condition implements AIBaseByScript{
    private Object[] fields=null;
    private String conditionName="NoName";
    private int scriptFieldHash=0;

    /** コンストラクタで引数を設定します */
    public ConditionByScript(String name, Object... args) {
        this.conditionName=name;
        this.fields=args;
    }

    /** この条件の名前を取得します */
    public String getName(){
        return this.conditionName;
    }

    /** エージェントアクセスのためのインターフェースを取得します */
    public AgentInterface getInterface(){
        return ((TaskByScript)this.getParentTask()).getInterface();
    }

    /** タスク情報を渡す処理をオーバーライド */
    @Override
    public Agent getAgent(){
        AgentByScript agt=(AgentByScript)super.getAgent();
        // アクセス禁止だった場合はnullを返す
        if (agt!=null && agt.forbidsToAccess()){
            agt=null;
        }
        return agt;
    }

    /** 条件のフィールドを取得 */
    public Object[] getFields(){
        return this.fields;
    }

    /** ハッシュコードを取得します */
    @Override
    public int hashCode(){
        int nativeHash=this.getClass().hashCode();
        long hash=nativeHash+this.conditionName.hashCode()+this.getFieldsHash();
        return (int)(hash & 0xffffffff);
    }

    @Override
    public String debugInfo(){
        String fieldsInfo=new String();
        if (fields!=null){
            for (Object field:fields){
                if (field!=null){
                    fieldsInfo=fieldsInfo+" :"+field.toString();
                }
            }
        }
        String str=super.debugInfo();
        return str+" ConditionName:"+this.getName()+fieldsInfo;
    }

    /** フィールド配列のハッシュ値を取得します */
    @Override
    public int getFieldsHash(){
        int resultInt=0;
        //long result=0;
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
    }}
