package jp.ac.nihon_u.cit.su.furulab.fuse.ai.script;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseTaskManager;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Asset;

/** スクリプトで駆動されるエージェントクラスです<br>
 * スクリプト関係の機能が追加されています */
public abstract class AgentByScript extends Agent{
    private static Lock lock=new ReentrantLock();

    private String script;
    private CompiledScript compiledScript;
    private Object ruleObject; // このエージェントが利用するルールオブジェクト
    private Bindings engineScope; // このエージェントが利用するエンジンスコープ

    // スクリプトからエージェントへのアクセスを禁止するかどうか
    private boolean forbidToAccess=false;


    /** このエージェントが利用するスクリプトエンジンを取得します */
    public ScriptEngine getScriptEngine(){
        return this.compiledScript.getEngine();
    }

    /** このエージェントを直接スクリプトに渡すことを禁止します<br>
     * その場合は，AgentInterfaceを経由してアクセスすることになります */
    public void setForbidToAccessFlag(boolean flag){
        this.forbidToAccess=flag;
    }

    /** このエージェントを渡すことが禁止されているかどうかを確認します */
    public boolean forbidsToAccess(){
        return this.forbidToAccess;
    }

    /** このエージェントと情報のやりとりをするためのインターフェースを取得します<br>
     * インターフェースを定義した時は，このメソッドをオーバーライドします. */
    public AgentInterface getInterface(){
        throw new InternalError("ERROR: Agent Interface is not Implements !");
    }

    /** このエージェントが利用するスクリプトエンジンを設定します<br>
     * 引数でスクリプトとスクリプトの種類を設定します．なお，スクリプトエンジンはcompilableでなければなりません． */
    public void setScriptEngine(String script, String scriptType){
        this.script=script;
        ScriptEngineManager man=new ScriptEngineManager();
        ScriptEngine scriptEngine=man.getEngineByName(scriptType);

        ScriptContext thisContext = new SimpleScriptContext();
        scriptEngine.setContext(thisContext);
        this.engineScope = thisContext.getBindings(ScriptContext.ENGINE_SCOPE);

        // エンジンが設定されたらルールを取得しておく
        try{
            this.compiledScript=((Compilable)scriptEngine).compile(this.script);
            this.compiledScript.eval(this.engineScope);
            this.ruleObject=compiledScript.getEngine().eval("Rule.new",this.engineScope);
        }catch(Exception e){
            try{
                Thread.sleep(200);
            }catch(Exception ex){
                ex.printStackTrace();
            }
            e.printStackTrace();
            System.out.println(this.script);
            System.exit(-1);
        }

        if (this.ruleObject==null){
            System.err.println("ERROR: Can not get Rule Object !");
            System.exit(-1);
        }
    }

    /** スクリプトに対してオブジェクトの取得を要求します */
    public Object getObject(String key){
        return this.compiledScript.getEngine().get(key);
    }

    /** メソッドを実行します */
    public Object invokeMethod(Object obj, String name, Object... args) {
        Object result=null;
        try{
            result=((Invocable)this.compiledScript.getEngine()).invokeMethod(obj, name, args);
        }catch(Exception e){
            e.printStackTrace();
            System.err.println(this.getScript());
            System.exit(-1);
        }
        return result;
    }

    /** ルールオブジェクトに含まれるメソッドを実行します */
    public Object invokeMethodToRule(String name, Object... args){
        Object result=this.invokeMethod(this.ruleObject, name, args);
        return result;
    }


    /** このエージェントのルートタスクを取得します */
    public TaskByScript getRootTask() {
        TaskByScript task=(TaskByScript)this.invokeMethod(ruleObject, "get_root_task_instance");
        return task;
    }

    /** このエージェントに対応するスクリプトオブジェクトを取得します */
    public String getScript(){
        return this.script;
    }

}
