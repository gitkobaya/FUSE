package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.Constants;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** 条件定義です<br>
 * コンストラクタでは初期条件を設定し、条件分岐先のタスク生成は特別な理由が無ければevaluate()の中で実施するようにしてください。
 * タスクをコンストラクタの中で生成した場合、生成したタスクのコンストラクタで条件が生成され、生成された条件のコンストラクタでタスクが生成され...という連鎖が発生し、多くの場合無限ループに陥ります。 */
public abstract class Condition extends FuseAIBase{
    private List<FuseTask> tasksToSolve=new ArrayList<FuseTask>();    // この条件を打開するためのタスクの集合
    private FuseTask parent=null; // この条件の親タスク
    private Truth status=Truth.UNCERTAIN; // この条件の結果
    private boolean impossible=false;
    private ReasonOfImpossible reason=ReasonOfImpossible.IAmNotImpossible; // デバッグ用

    private double conditionCost=Constants.HUGE; // この条件からアクションに至るコスト
    private List<FuseTask> historyToAction=new ArrayList<FuseTask>(); // この条件からアクションに至るための経路

    private static Logger logger=Logger.getGlobal();

    /** 引数を指定しないコンストラクタです<br>
     * 継承先のクラスのために設定されたものです。 */
    public Condition(){

    }

    /** 条件の評価結果を初期化します<br>
     * 命令タスクなどで条件インスタンスを再利用する場合などに利用します */
    public void refresh(){
        this.status=Truth.UNCERTAIN;
        this.conditionCost=Constants.HUGE;
        this.parent=null;
        this.tasksToSolve.clear();
        this.impossible=false;
        this.reason=ReasonOfImpossible.IAmNotImpossible;
    }

    /** この条件の条件コストを取得します */
    public double getConditionCost(){
        return this.conditionCost;
    }

    /** この条件からアクションに至る履歴を取得します */
    public List<FuseTask> getHistory(){
        return this.historyToAction;
    }

    /** この条件からアクションに至る履歴を設定します */
    public void setHistory(List<FuseTask> history, double cost){
        this.historyToAction=history;
        this.conditionCost=cost;

        // 条件の履歴が更新されたら親の履歴を更新
        if (this.parent!=null && !this.parent.isImpossible()){
            this.parent.renewTaskCost();
        }
    }

    /** この条件オブジェクトが所属する親タスクを取得します */
    public FuseTask getParentTask(){
        return this.parent;
    }

    /** この条件オブジェクトが所属する親タスクを設定します<br>
     * 基本的に呼び出し元のFuseTaskクラスから呼ばれることが多いです。 */
    public void setParentTask(FuseTask parent){
        this.parent=parent;
    }

    /** この条件オブジェクトが所属するタスクマネージャを取得します */
    public FuseTaskManager getTaskManager(){
        return this.parent.getTaskManager();
    }

    /** この条件オブジェクトがどのエージェントに所属するかを取得します */
    public Agent getAgent(){
        return this.parent.getAgent();
    }

    /** この条件の派生タスクを取得します */
    public List<FuseTask> getTasks(){
        return this.tasksToSolve;
    }

    /** この条件が真でない時，それを打開するために必要なタスクを追加します<br>
     * ここで追加するタスクは，引数で指定するタスクになります．<br>
     * 一方，実際に自分に登録されるタスクは，同一判定を通過したのちにマネージャから返されたタスクになります．<br>
     * つまり，「同じタスク」ではあっても同じインスタンスとは限りません．マネージャから返されたタスクには既に他の親が設定されている可能性があります．
     * この場合，他の親が優先され，自分を派生元と思わない可能性があります．
     * ただし，その場合でも自分は派生元条件の一つには含まれるため，自分よりコストの低い派生元が全滅すると，子タスクはまた自分を派生元と思うようになります．*/
    protected void addTask(FuseTask task){
        FuseTaskManager manager=this.parent.getTaskManager();
        task.setFromCondition(this);

        FuseTask returned=manager.addTask(task);
        if (returned!=null){
            this.tasksToSolve.add(returned);
        }
    }

    /** この条件の状態を取得します<br>
     * 状態は、真、偽、不明、未確定の4つです。<br>
     * このメソッドを最初に呼び出した場合、条件が評価され、状態が設定されます。<br>
     * 一度評価されると状態は確定され、その後は評価メソッドは呼び出されず、キャッシュされた状態を返すことになります。 */
    public Truth getTruth(){
        Truth result=Truth.UNCERTAIN;
        if (this.status==Truth.UNCERTAIN){
            result=this.evaluate();
            this.status=result;
            // 条件が真だった時の処理
            if (this.status==Truth.TRUE){
                this.historyToAction.clear();
                this.conditionCost=0;
            }
        }else{
            result=this.status;
        }
        return result;
    }

    /** この条件が不可能かどうかを取得します */
    public boolean isImpossible(){
        return this.impossible;
    }

    /** 派生タスクのImpossible情報を分析します.<br>
     * 自分がFalseまたはUnknownで，かつ自分から派生したすべてのタスクがImpossibleになった場合，この条件もImpossibleになります．<br>
     * また，自分がFalseまたはUnknownで，かつ自分から派生したすべてのタスクが自分の先祖だった場合，この条件もImpossibleになります．<br>
     * システムから利用されるメソッドであるため，ユーザーが意識する必要はありません．
     * 当然ですが，evaluateメソッド実行後でなければ正しく機能しません． */
    public void checkImpossible(){
        boolean canDoAnything=false;

        if (this.getTruth()==Truth.TRUE){
            // TRUEなら不可能フラグは立てない
            canDoAnything=true;
        }else{
            for (FuseTask task:tasksToSolve){
                // 何か一つでも不可能ではないタスクがあるなら，不可能フラグは立てない
                if (!task.isImpossible()){
                    canDoAnything=true;
                    break;
                }
            }

            if (!canDoAnything){
                // 全タスクが不可能と言われてしまったら，この条件は不可能
                this.reason=ReasonOfImpossible.BecauseAllOfMyChildrenBecomeImpossible;
                this.setImpossibleFlag();
            }
        }
    }

    /** 派生タスクを削除します */
    protected FuseTask removeChildTask(FuseTask task){
        FuseTask removedTask=null;

        for(FuseTask target:this.tasksToSolve){
            if (task.equals(target)){
                removedTask=target;
                break;
            }
        }

        if (removedTask!=null){
            if (!this.tasksToSolve.remove(removedTask)){
                logger.severe("ERROR: Can not remove child task !!  parent:"+task.debugInfo());
            }
        }

        // 派生タスクが削除されたので，不可能判定をかけます
        this.checkImpossible();
        return removedTask;
    }

    /** 全ての派生タスクを削除します */
    protected void removeAllChildren(){
        this.tasksToSolve.clear();
    }

    /** この条件が不可能であることを設定します<br>
     * 条件が不可能になった場合，親タスクが不可能になります． */
    public void setImpossibleFlag(){
        logger.info("DEBUG: This condition is IMPOSSIBLE !! "+this.reason+" :"+this.debugInfo());
        this.impossible=true;

        FuseTask parentTask=this.getParentTask();

        parentTask.setReasonOfImpossible(ReasonOfImpossible.BecauseOneOfMyConditionBecomesImpossible);
        parentTask.setImpossibleFlag();
    }

    /** この条件の真偽または不明を取得します<br>
     * 具体的なメソッドはこの抽象クラスの継承先で実装することになります。<br>
     * 派生タスクはこのメソッドで登録します */
    public abstract Truth evaluate();

    public String debugInfo(){
        String parentInfo;
        FuseTask parent=this.getParentTask();
        if (parent!=null){
            parentInfo=""+parent.hashCode();
        }else{
            parentInfo="No Parent";
        }
        return this.getClass().getSimpleName()+" result:"+this.status+" hash:"+this.hashCode()+" parentTask:"+parentInfo;
    }

    /** 条件オブジェクトが同一かどうかの判定です */
    @Override
    public boolean equals(Object req){
        boolean result=false;
        if (this.hashCode()==req.hashCode()){
            result=true;
        }
        return result;
    }

    /** ハッシュを取得します<br>
     * Conditonのハッシュはその親であるFuseTaskを含めて計算します．
     * そのため，newした直後にハッシュを利用しようとするとNull例外を吐きます． */
    @Override
    public int hashCode(){
        int parentHash=0;
        FuseTask parent=this.getParentTask();
        if (parent!=null){
            parentHash=this.getParentTask().hashCode();
        }
        int hash=this.getClass().hashCode()+this.getFieldsHash()+parentHash;
        return hash;
    }
}
