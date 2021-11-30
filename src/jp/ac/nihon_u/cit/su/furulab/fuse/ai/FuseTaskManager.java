package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** タスク管理クラスです<br>
 * このクラスの最大の役割は、実行すべきタスクの重複を監視することです。<br>
 * 重複タスクの登録を許容した場合、確実に無限ループに陥ります。
 * また，アルゴリズムはシングルタスクの深さ優先探索に強く依存しています．マルチスレッド化する際は非常に注意深く実施する必要があります． */
public class FuseTaskManager {
    private Agent owner; // このタスク集合のオーナーです

    private WhiteBoard board=new WhiteBoard(); // このタスクマネージャが利用する記憶領域です

    private Map<Integer, FuseTask> taskCathe=new HashMap<Integer, FuseTask>(); // ユニークタスクの集合
    private List<FuseTask> availableTasks=new ArrayList<FuseTask>(); // 実施可能なタスクの集合
    private List<FuseAction> availableActions=new ArrayList<FuseAction>(); // 実施可能なタスクの集合
    private static Logger logger = Logger.getGlobal();
    private int numOfCheckTask=0; // タスクの登録(重複となったものも含む)を何回実施したか

    private FuseRule ruleClass=null; // このタスクマネージャが参照するルールクラス

    /** コンストラクタでオーナーエージェントを設定します */
    public FuseTaskManager(Agent agt) {
        this.owner=agt;
    }

    /** このタスクマネージャーのオーナーエージェントを取得します */
    public Agent getAgent(){
        return this.owner;
    }

    /** 記憶領域を取得します<br>
     * 記憶領域は全てのCaSPAエレメントが参照することができますが，アクションからしか書き込みをしてはいけません． */
    public WhiteBoard getBoard(){
        return this.board;
    }

    /** 記憶領域に情報を書き込みます <br>
     * 引数は情報名，情報の内容，寿命です．寿命は論理時間で指定され，現在時刻から指定された時刻進行した時点でこのデータは消滅します<br>
     * 寿命にWhiteBoard.IMMORTALを設定すると無限の寿命となります */
    public void writeArticle(Object[] identifier, Object information, long lifetime){
        this.board.write(identifier, information, lifetime);
    }

    /** 記憶領域に情報を書き込みます <br>
     * 引数は情報名，情報の内容，寿命です．寿命は論理時間で指定され，現在時刻から指定された時刻進行した時点でこのデータは消滅します */
    public void writeArticle(Object[] identifier, Object information){
        this.board.write(identifier, information, Article.IMMORTAL);
    }

    /** 記憶領域から情報を削除します<br>
     * 指定した情報を削除した場合にtrueが返ります． */
    public boolean eraseArticle(Article art){
        return this.board.erase(art);
    }

    /** 記憶領域から名前で指定した情報を削除します<br>
     * 返り値は削除した情報の数です */
    public int eraseArticles(Object[] identifier){
        return this.board.erase(identifier);
    }

    /** 記憶を更新します<br>
     * 更新に成功した場合にtrueが返ります．falseであれば，単に追加しただけになります． */
    public boolean replaceArticle(Article newArticle, Article oldArticle){
        return this.board.replace(newArticle, oldArticle);
    }

    /** 登録されているタスクを初期化します<br>
     * goalTaskを駆動する前には必ずclearしてください．<br>
     * なお，ここで初期化されるのはあくまでタスクグラフであり，記憶領域の内容は保持されます． */
    public void clear(){
        this.taskCathe.clear();
        this.availableTasks.clear();
        this.availableActions.clear();
        this.numOfCheckTask=0;

        SimulationEngine engine=this.owner.getEngine();
        if (engine!=null){
            this.board.update(engine.getLogicalTime());
        }
    }

    /** 現在のタスクグラフを評価します<br>
     * このメソッドを呼ぶことにより，引数のタスクをゴールタスクとして，各タスクのコストが確定します．
     * 返り値として，アクションが返されます．*/
    protected List<FuseAction> checkTaskGraph(FuseTask task){

        for (FuseTask available:this.availableTasks){
            available.renewTaskCost(); // タスクコストを更新します
        }

        // 実行可能をクリアし，改めてセット
        this.availableTasks.clear();
        for (FuseTask taskInHistory:task.getPlan()){
            if (taskInHistory.isAvailable()){
                this.availableTasks.add(taskInHistory);
            }
        }
        for(FuseTask aTask:this.availableTasks){
            this.availableActions.add(aTask.getAction());
        }

        return this.availableActions;
    }

    /** 指定したアクションを実行します */
    public void exec(FuseAction action){
        action.action();
    }

    /** このマネージャに登録された最安コストのアクションを取得します */
    public FuseAction getLowestCostAction(){
        FuseAction result=null;
        if (!this.availableActions.isEmpty()){
            // 葉ノードが抽象タスクになっている可能性を考慮する
            for (FuseAction act:this.availableActions) {
                if (act!=null){
                    result=act;
                    break;
                }
            }
        }

        return result;
    }

    /** このマネージャに登録されたすべての実行可能なアクションを取得します */
    public List<FuseAction> getActions(){
        return this.availableActions;
    }

    /** このマネージャに実行可能なタスクを追加します */
    protected boolean addAvailableTask(FuseTask task){
        boolean result=false;
        if (task.isAvailable()){
            this.availableTasks.add(task);
            result=true;
        }
        return result;
    }


    /** このマネージャに登録されたすべてのタスクを取得します */
    public Collection<FuseTask> getTasks(){
        return taskCathe.values();
    }

    /** ゴールタスクを取得します */
    public FuseTask getGoalTask(){
        FuseTask goal=null;
        for (FuseTask task:new ArrayList<FuseTask>(this.taskCathe.values())){
            for (Condition cond:task.getFroms()){
                if (cond instanceof GoalCondition){
                    goal=task;
                    break;
                }
            }
        }
        return goal;
    }

    /** ゴールタスクを登録します*/
    public void setGoalTask(FuseTask task){
        task.init();
        Condition goal=new GoalCondition(); // ゴール条件を作る
        task.addFrom(goal);
        this.addTask(task);
    }

    /** ルールクラスを取得します */
    public FuseRule getRule(){
        return this.ruleClass;
    }

    /** ルールクラスを登録します */
    public void setRule(FuseRule rule){
        this.ruleClass=rule;
    }

    /** このタスクマネージャのゴールタスクを駆動します<br>
     * 返り値としてコスト最小アクションを返します．
     * ゴールタスクが登録されていない場合にはエラーになります． */
    public FuseAction drive(){
        FuseTask root=this.getGoalTask();
        root.buildPlans();
        return this.getLowestCostAction();
    }

    /** タスクを追加します<br>
     * ユーザーが登録したタスクがルートタスクとなり、そのタスクをドライブすることで行動判断がなされます。<br>
     * ユーザーは普通呼び出さないメソッドですが、Condition内部からはタスク生成の度に呼び出されます。<br>*/
    public FuseTask addTask(FuseTask task){
        FuseTask resultTask=null;

        if (!this.taskCathe.containsKey(task.hashCode())){
            task.setTaskManager(this);

            // 同じタスクがまだ登録されていないなら、タスクを登録
            logger.info("DEBUG: add task. "+task.debugInfo());

            this.taskCathe.put(task.hashCode(), task);
            resultTask=task;
        }else{
            // 既に登録されていたなら，そのタスクに重複タスクの親を追加(重複タスクは捨てる)
            FuseTask already=this.taskCathe.get(task.hashCode());

            // 新規タスクが持つfromは必ず一つ
            Condition from=task.getFroms().get(0);
            try{
                already.addFrom(from);
            }catch(Exception e){
                System.err.println("ERROR: Agent name <"+this.getAgent().getName()+">\n  newTask: "+task.debugInfo()+"\n  alreadyTask:"+already.debugInfo());
                e.printStackTrace();
            }

            resultTask=already;
        }

        this.numOfCheckTask++;

        return resultTask;
    }

    /** タスクを削除します．<br>
     * リンクの切断ではなく，登録されたタスクが完全に消滅します．
     * 正常に削除された場合、trueが返ります。 */
    public boolean removeTask(FuseTask task){
        boolean result=false;
        FuseTask removed=this.taskCathe.remove(task.hashCode());

        if (removed==task){
            result=true;

            // そのタスクに子がいた場合，自分へのリンクをカットする
            for(Condition cond:task.getConditons()){
                List<FuseTask> children=new ArrayList<FuseTask>(cond.getTasks());
                for(FuseTask child:children){
                    child.removeFromCondition(cond);
                }
            }

            // そのタスクの親からのリンクをカットする
            for (Condition cond:task.getFroms()){
                cond.removeChildTask(task);
            }

        }else{
            // 登録されていないタスクを削除しようとした
            logger.warning("WARNING: Target task did not regist normaly ! "+task.debugInfo());
        }
        task.removed=true;
        return result;
    }

    /** 指定されたタスクがマネージャに登録されているか確認します */
    public boolean checkExitst(FuseTask task){
        boolean result=false;
        FuseTask t=this.taskCathe.get(task.hashCode());
        // 参照が一致したなら
        if (t==task){
            result=true;
        }

        return result;
    }

    /** 指定されたタスク群がマネージャに登録されているか確認します */
    public boolean checkExitst(Collection<FuseTask> tasks){
        boolean result=true;
        for (FuseTask task:tasks){
            if(!this.checkExitst(task)){
                System.out.println("DEBUG: Can't find the task in TaskManager "+task.debugInfo());
                result=false;
            }
        }
        return result;
    }

    /** タスクの追加回数を取得します<br>
     *  デバッグ用です。*/
    public int getNumOfCheckTask(){
        return this.numOfCheckTask;
    }

    Map<Integer,FuseTask> checked=new HashMap<Integer,FuseTask>();
    /** 現在のタスクグラフを吐き出します<br>
     * タスクグラフをチェックしてから実行してください<br>
     * デバッグ用です */
    public void dumpTaskGraph(){
        FuseTask task=this.getGoalTask();
        this.checked.clear();
        System.out.println("DEBUG: ***BEGIN DEBUG***");
        System.out.println("DEBUG: Agent Name:"+task.getAgent().getName()+" ID:"+task.getAgent().getId());
        this.outputTaskData(task);

        System.out.println("DEBUG: --Available Actions--");
        for (FuseAction avai:this.getActions()){
            if (avai!=null){
                System.out.println("DEBUG: "+avai.debugInfo());
            }else{
                System.out.println("DEBUG: Leaf Task has no Action !");
            }
        }
        System.out.println("DEBUG: -------------------");

        System.out.println("DEBUG: ***FINISH DEBUG***");
    }

    /** タスクの情報を出力します */
    protected void outputTaskData(FuseTask task){
        if (!checked.containsKey(task.hashCode())){
            checked.put(task.hashCode(), task);
            System.out.println("DEBUG: ----Task Info----");
            System.out.println("DEBUG: Task Data:"+task.debugInfo());
            List<Condition> condList=task.getConditons();
            for(Condition con:condList){
                System.out.println("DEBUG: Condition Data:"+con.debugInfo());
                for (FuseTask t:con.getTasks()){
                    System.out.println("DEBUG:   Child Task:"+t.debugInfo());
                }
            }
            FuseAction act=task.getAction();
            if (act!=null){
                System.out.println("DEBUG: Action Data:"+act.debugInfo());
            }
            System.out.println("DEBUG: -----------------");

            for(Condition con:condList){
                for (FuseTask t:con.getTasks()){
                    this.outputTaskData(t);
                }
            }
        }
    }

    /** タスクの重複判定です */
    public boolean contains(FuseTask task){
        boolean result=false;
        if (taskCathe.get(task.hashCode())!=null){
            result=true;
        }
        return result;
    }
}
