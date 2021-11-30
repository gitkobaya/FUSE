package jp.ac.nihon_u.cit.su.furulab.fuse.ai.script;

import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseAIBase;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseAction;

public class ActionByScript extends FuseAction implements AIBaseByScript{
    private Object[] fields=null;
    private String actionName="NoName";
    private int scriptFieldHash=0;

    public ActionByScript(String name, Object... args) {

        actionName=name;
        this.fields=args;
    }

    /** このアクションの名前を取得します */
    public String getName(){
        return this.actionName;
    }

    /** フィールドを取得します */
    public Object[] getFields(){
        return this.fields;
    }

    /** このアクションのエージェントインターフェースを取得します */
    public AgentInterface getInterface(){
        return ((AgentByScript)this.getTaskManager().getAgent()).getInterface(); // タスク経由だと禁止対象になるのでマネージャからもらう
    }

    @Override
    public void action() {
        // TODO 自動生成されたメソッド・スタブ

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
        return super.debugInfo()+" :"+this.actionName+" "+fieldsInfo;
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
    }
}
