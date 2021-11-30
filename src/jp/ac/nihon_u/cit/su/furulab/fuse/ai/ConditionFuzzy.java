package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

/** 派生タスクが確定していない条件クラスです<br>
 * タスク名と実タスクをリンクする必要があります */
public abstract class ConditionFuzzy extends Condition{
    FuseRule rule=null;


    /** 評価前の処理でルールの登録を行います */
    @Override
    public Truth getTruth() {
        this.setRule(this.getTaskManager().getRule());
        return super.getTruth();
    }

    /** ルールクラスを登録します<br>
     * タスクマネージャが実施するのでプログラマが呼び出す必要はありません */
    protected void setRule(FuseRule rule){
        this.rule=rule;
    }

    @Override
    protected void addTask(FuseTask task){
        throw(new RuntimeException("You can't call addTask at ConditionFuzzy. Please call addTaskToBeTrue or addTaskToBeKnown."));
    }

    /** 偽だった時にそれを真にするタスク名と引数を登録 */
    protected void addTaskToBeTrue(String name, Object...args){
        this.registerAbstractTask(FuseRule.TO_BE_TRUE,name,args);
    }

    /** 不明だった時にそれを真偽を判定するタスク名と引数を登録 */
    protected void addTaskToBeKnown(String name, Object...args){
        this.registerAbstractTask(FuseRule.TO_BE_KNOWN,name,args);
    }

    /** 抽象タスクを生成して登録する */
    private void registerAbstractTask(int toBeTrueOrKnown, String name, Object...args){
        FuseTask task=this.rule.getTask(this.getClass(), name, toBeTrueOrKnown, args);
        if (task==null){
            throw (new RuntimeException("Rule does not define task <"+name+"> of "+this.getClass().getSimpleName()));
        }
        super.addTask(task);
    }
}
