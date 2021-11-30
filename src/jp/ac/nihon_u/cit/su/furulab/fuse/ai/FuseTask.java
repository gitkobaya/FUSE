package jp.ac.nihon_u.cit.su.furulab.fuse.ai;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.Constants;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** FUSE AIで利用するためのタスクです。<br>
 * タスクにはアクションと条件が設定されます。 */
public abstract class FuseTask extends FuseAIBase{
    private List<Condition> conditions=new ArrayList<Condition>();
    private FuseTaskManager manager; // このタスクを制御するマネージャー

    private List<Condition> froms=new ArrayList<Condition>(); // 親集団

    private FuseAction action; // このタスクが実行可能な場合に実施される行動

    private double ownCost; // このタスク単体のコスト

    private double taskCost=Constants.HUGE; // このタスクからアクションの実行に至るまでのコスト
    private List<FuseTask> planToAction=new ArrayList<FuseTask>(); // このタスクからアクションの実行に至るまでの経路

    private boolean impossible; // このタスクが実行不可能であることの判明フラグ
    private boolean available; // このタスクが可能であることの判明フラグ
    private ReasonOfImpossible resaonOfImpossible=ReasonOfImpossible.IAmNotImpossible; // デバッグ用
    protected boolean removed=false; //デバッグ用
    private boolean driven=false; // 評価されたことがあるかどうか

    private long startTime,endTime;
    private long startTimeNano,endTimeNano;

    private static Logger logger = Logger.getGlobal();

    /** 条件とアクションを設定するメソッドです<br>
     * FUSEから呼ばれるコールバックメソッドであり、必ず実装しなければなりません。<br>
     * このメソッドが呼ばれる時点でParentとTaskManagerは決定していることが保証されます。<br>*/
    public abstract void define();

    /** 初期化メソッドです<br>
     * このメソッドを呼ぶことで，親と条件の子タスクを切り離します．*/
    public void init(){
        this.driven=false;
        this.froms.clear();
        this.conditions.clear();
    }

    /** 一度駆動されたタスクかどうかを取得します．<br>
     * 既に駆動されたタスクを使いまわしている場合，再度driveすると無限ループに陥るためです． */
    public boolean hasDriven(){
        return this.driven;
    }

    /** タスクマネージャを取得します */
    public FuseTaskManager getTaskManager(){
        return this.manager;
    }

    /** タスクマネージャを設定します<br>
     * 通常は呼び出さないメソッドです。できるだけTaskManager以外が呼び出さないように保護しておきます。 */
    protected void setTaskManager(FuseTaskManager man){
        this.manager=man;
    }

    /** 派生元を設定します */
    protected void addFrom(Condition pCon){
        // 同じ親がいないかチェック
        for (Condition p:this.froms){
            if (p!=null && p.equals(pCon) && p.getParentTask().equals(pCon.getParentTask())){
                // 同じ親がいたということは，枝刈に失敗している
                logger.severe("ERROR: This Task has same parents !! The parent Condition makes same tasks !!" +
                    "\n this task      :"+this.debugInfo()+
                    "\n already from Con    :"+p.debugInfo()+
                    "\n already parent task:"+p.getParentTask().debugInfo()+" removed:"+p.getParentTask().removed+
                    "\n rookie from Con    :"+pCon.debugInfo()+
                    "\n rookie parent task:"+pCon.getParentTask().debugInfo()+" removed:"+pCon.getParentTask().removed);
                throw new RuntimeException("Same Tasks are made by one Condition");
            }
        }

        froms.add(pCon);
    }

    /** このタスクの親Conditionへのリンクを切断 */
    protected boolean removeFromCondition(Condition cond){
        // 切断
        boolean result=this.froms.remove(cond);

        // 自分の親がいなくなった場合，どこからも参照されないということなので自分を削除
        if (this.froms.isEmpty()){
            this.manager.removeTask(this);
        }

        return result;
    }

    /** このタスクの確定していない派生元一覧を取得します */
    public List<Condition> getFroms(){
        return this.froms;
    }

    /** 派生元の条件を設定します<br>
     * 追加ではないため，既に設定されている条件があればクリアされます<br>
     * マネージャーから呼ばれるメソッドなので，ルールの開発者が呼び出す必要はありません． */
    protected void setFromCondition(Condition fromCondition){
        this.froms.clear();
        this.addFrom(fromCondition);
    }

    /** タスクのオーナーエージェントを取得します */
    public Agent getAgent(){
        return this.manager.getAgent();
    }

    /** このタスク単体のコストを取得します */
    public double getOwnCost() {
        return this.ownCost;
    }

    /** このタスク単体のコストを設定します */
    public void setOwnCost(double cost){
        this.ownCost=cost;
    }

    /** このタスクのタスクコストを取得します */
    public double getCost(){
        return this.taskCost;
    }

    /** このタスクを実行するためのプランを取得します<br>
     * 内容を書き換えてはいけません． */
    public List<FuseTask> getPlan(){
        return this.planToAction;
    }

    /** このタスクがAvailableだった場合，タスクコストと履歴を初期化します */
    private void initTaskCost(){
        if (this.available){
            this.taskCost=this.ownCost;
        }
        this.planToAction.add(this);
    }

    /** このタスクのタスクコストを更新します．
     * それまでにタスクグラフが成立している必要があるため，グラフチェック段階で呼び出します． */
    protected void renewTaskCost(){
        this.planToAction.clear();
        this.taskCost=0;
        this.margeHistory();

        // そこまでの手順で自分を通過していなかったら
        if (!this.planToAction.contains(this)){
            this.planToAction.add(this); // 履歴の末尾に自分を追加
            this.taskCost+=this.ownCost;
        }

        for (Condition from:this.getFroms()){
            double fromConditionCost=from.getConditionCost();
            // タスクコストがfromの条件コストを下回っていたなら
            if (this.taskCost<Constants.HUGE && this.taskCost<fromConditionCost && !from.isImpossible()){
                from.setHistory(new ArrayList<FuseTask>(this.planToAction), this.taskCost); //コピーを渡す
            }
        }
    }

    /** 条件の履歴を統合することにより，このタスクの履歴を設定します */
    private void margeHistory(){
        if (!this.conditions.isEmpty()){
            Collections.sort(this.conditions,new CompareConditionsByConditionCosts());
            // その条件にコストが設定されているか
            if (this.conditions.get(this.conditions.size()-1).getConditionCost()<Constants.HUGE){
                for (Condition cond:this.conditions){
                    for (FuseTask task:cond.getHistory()){
                        if (!this.planToAction.contains(task)){
                            this.planToAction.add(task);
                            this.taskCost+=task.getOwnCost();
                        }
                    }
                }
            }else{
                this.taskCost=Constants.HUGE;
            }
        }
    }

    /** このタスクの実行可能フラグを取得します<br>
     * すべての条件が真の場合にタスクは実行可能になります。 */
    public boolean isAvailable(){
        return this.available;
    }

    /** 不可能フラグを取得します<br>
     * タスクが不可能である場合、このタスクからの派生クラスは無くなります。 */
    public boolean isImpossible(){
        return this.impossible;
    }

    /** 不可能フラグを設定します<br>
     * 不可能となった場合，コストがHUGEになります*/
    public void setImpossibleFlag(){
        this.impossible=true;
        this.setOwnCost(Constants.HUGE);

        // 派生元の不可能チェック
        for (Condition cond:this.getFroms()){
            if (!cond.isImpossible()){
                cond.checkImpossible();
            }
        }
    }

    /** 不可能に至った理由を設定します */
    protected void setReasonOfImpossible(ReasonOfImpossible reason){
        this.resaonOfImpossible=reason;
    }

    /** このタスクの子タスクを取得します<br>
     * defineの実行後でないと正しい結果を返しません．<br>
     * この場合，子タスクは自分が子と思っているタスクであり，子に親を聞いても自分ではない可能性があります */
    public List<FuseTask>getChildTasks(){
        ArrayList<FuseTask> children=new ArrayList<FuseTask>();

        for (Condition cond:conditions){
            children.addAll(cond.getTasks());
        }

        return children;
    }

    /** このタスクのアクションを取得します */
    public FuseAction getAction(){
        return this.action;
    }

    /** このタスクのアクションを設定します<br>
     * アクションには呼び出し元のタスクが登録されます。 */
    protected void setAction(FuseAction act){
        act.setParentTask(this);  // コンストラクタで設定されない場合，ここで設定される
        this.action=act;
    }

    /** タスクに含まれる条件をハッシュ値で取得します */
    public Condition getConditionByHash(int hash){
        Condition result=null;
        for (Condition con:this.conditions){
            if (con.hashCode()==hash){
                result=con;
                break;
            }
        }
        return result;
    }

    /** タスクに含まれる条件を取得します */
    public List<Condition> getConditons(){
        return this.conditions;
    }

    /** 条件を追加します */
    protected void addCondition(Condition cond){
        cond.setParentTask(this); // コンストラクタで設定されない場合，ここで設定される
        this.conditions.add(cond);
    }

    /** タスクを駆動し，このタスクから始まるタスクグラフを構築します<br>
     * タスクの全条件が真ならアクションをタスクマネージャに登録し、
     * そうでないなら真でない条件から派生タスクを展開させます。
     * タスクが実行不能だった場合(とれる選択肢がないorとるべき選択肢がない）、falseが返ります。 */
    public boolean buildPlans(){
        // 駆動開始

        // 構築フェーズ
        boolean result=this.drive();

        if (result){
            // コスト計算フェーズ
            this.manager.checkTaskGraph(this);
        }

        // 再度デバッグ
        // this.dumpTaskInfo();

        return result;
    }

    /** タスクを駆動し，タスクグラフを構築します<br>
     * CaSPA内部の処理です．備忘録として重要な点は，タスクツリーの構築中にはコスト計算をする必要がないということです．
     * コスト計算はツリーを構築した後に実施します． */
    protected boolean drive(){
        this.startTime=System.currentTimeMillis();
        this.startTimeNano=System. nanoTime();
        boolean canDoAnything=false; // 何かできることはあるか
        boolean trueFalse=true;           // このタスクのアクションは実行可能か

        // 保険
        if (this.driven){
            logger.severe("ERROR: Illegal call of [drive] "+this.debugInfo());
            System.err.println("DEBUG: Check which you've clear your task manager !");
            System.exit(-1);
        }

        if (!this.isImpossible()) { // このタスクが不可能であると判明していない
            this.driven=true; //駆動済みフラグを立てておく

            if (!this.conditions.isEmpty()){
                System.out.println("DEBUG: Reuse !");
            }

            // タスクを設定
            this.define();

            // 条件ごとに処理を実施
            for (Condition cond:this.conditions){
                Truth truth=cond.getTruth();
                //logger.info("DEBUG: "+cond.debugInfo()+" <"+truth+">"); //どの条件から派生したか

                Collection<FuseTask> tasks=null;
                switch(truth){
                    // 真だった場合
                    case TRUE:
                       break;

                    // 未知だった場合
                    case UNKNOWN:
                        tasks=cond.getTasks();
                        trueFalse=false;
                        break;

                    //偽だった場合
                    case FALSE:
                        tasks=cond.getTasks();
                        trueFalse=false;
                        break;

                    default:
                        logger.severe("ERROR: UNCERTAINED Condition !! "+cond.debugInfo());
                        break;
                }

               if (tasks!=null && tasks.size()!=0){
                    // 得られたタスクを展開
                   Iterator<FuseTask> i = tasks.iterator();
                   while(i.hasNext()) {
                        FuseTask task=i.next();
                        // 実行済みかどうか
                        if (!task.hasDriven()){
                            task.drive();
                        }
                        // 不可能でないなら何かできた
                        if (!task.isImpossible()){
                            canDoAnything=true;
                        }
                    }
                }

               // 子タスクの評価が終わったところで改めて条件の不可能判定
               // 子タスクが全部不可能だった場合，条件も不可能になる
               cond.checkImpossible();

               // 条件が一つでも不可能だった場合，それ以上他の条件を評価する必要はない
               if (cond.isImpossible()){
                   this.resaonOfImpossible=ReasonOfImpossible.BecauseOneOfMyConditionBecomesImpossible;
                   this.setImpossibleFlag(); // 自分も不可能になる
                   break;
               }
            }

            if (trueFalse) {
                this.available=true; // フラグセット
                this.initTaskCost();  // タスクコスト初期化
                this.manager.addAvailableTask(this);
                if (action!=null){
                    canDoAnything=true;
                }
            }
        }else{
            trueFalse=false;
        }

        this.endTime=System.currentTimeMillis();
        this.endTimeNano=System.nanoTime();
        return canDoAnything;
    }

    /** タスク実行にかかった時間(msec)を取得します<br>
     * 当然ながら、タスク実行後でないと0が返ります。 */
    public long getTaskTime(){
        return endTime-startTime;
    }

    /** タスク実行にかかった時間(nsec)を取得します<br>
     * 当然ながら、タスク実行後でないと0が返ります。 */
    public long getTaskTimeNano(){
        return endTimeNano-startTimeNano;
    }


    /** デバッグ情報を取得します */
    public String debugInfo(){
        String possibleness;
        if (impossible){
            possibleness="Impossible :"+this.resaonOfImpossible;
        }else if(available){
            possibleness="Available";
        }else{
            possibleness="Possible";
        }
        String output=this.getClass().getSimpleName()+" ["+possibleness+"]  hash:"+this.hashCode()+" fieldsHash:"+this.getFieldsHash()+" ownCost:"+this.ownCost+" totalCost:"+this.getCost()+" spentTime:"+this.getTaskTime()+"msec"+" removed:"+this.removed;
        return output;
    }

    /** 同一のタスクかどうかを判定します */
    @Override
    public boolean equals(Object task){
        boolean result=false;
        FuseTask target=(FuseTask)task;
        if (this.hashCode()==target.hashCode()){
            result=true;
        }

        return result;
    }


    public void dumpTaskInfo(){
        List<Integer> doneList=new ArrayList<Integer>();
        this.dumpTaskInfo(doneList);
    }

    /** デバッグ用にタスクツリー情報を吐き出します */
    public void dumpTaskInfo(List<Integer> doneList){
        if (doneList.contains(this.hashCode())){
            return; // 行儀悪いけど
        }else{
            doneList.add(this.hashCode());
        }

        System.out.println("TASK info <"+this.debugInfo()+">");
        String finalFrom="Null";
        System.out.println("  Final From Condition info <"+finalFrom+">");
        for (Condition cond:this.getFroms()){
            String condInfo;
            if (cond!=null){
                condInfo=cond.debugInfo();
            }else{
                condInfo="Root Condition";
            }
            System.out.println("  From Condition info <"+condInfo+"> impossible:"+cond.isImpossible());
        }

        for (Condition cond:this.getConditons()){
            System.out.println("  Condition info <"+cond.debugInfo()+"> impossible:"+cond.isImpossible());
            for (FuseTask child:cond.getTasks()){
                System.out.println("    ChildTask info <"+child.debugInfo()+">");
            }
        }
        System.out.println("");

        for (Condition cond:this.getConditons()){
            for (FuseTask child:cond.getTasks()){
                child.dumpTaskInfo(doneList);
            }
        }
    }
}
