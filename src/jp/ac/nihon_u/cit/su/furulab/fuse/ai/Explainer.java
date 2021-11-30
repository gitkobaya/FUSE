package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** 説明生成クラス<br>
 * タスクグラフが生成され、コスト計算が終わってから呼び出します */
public class Explainer {

    public static int NUM_OF_EXPLAINS=5; // 説明タスクをいくつ遡るか

    /** 説明を生成し、単一の文字列にまとめて返します */
    public static String createExplanation(FuseTaskManager manager){
        List<String> expList=createExplanations(manager);
        String result="";

        for (String str:expList){
            result+=str+"\n";
        }

        return result;
    }

    /** 説明を生成し、リストで返します */
    public static List<String> createExplanations(FuseTaskManager manager){
        List<String> expList=new LinkedList<String>();
        if (manager!=null){
            FuseTask goal=manager.getGoalTask();
            FuseAction action=manager.getLowestCostAction();
            FuseTask currentTask;
            if (action!=null){
                currentTask=action.getParentTask();
                expList=getExplains(currentTask, goal, dietPlan(goal.getPlan()), new LinkedList<FuseTask>());
            }
        }
        return expList;
    }

    /** 遡っていきます<br>
     * planはタスクマネージャでプランニングされたタスク列，alreadiesは既に説明されたタスク */
    private static List<String> getExplains(FuseTask task,FuseTask goal, List<FuseTask> plan, List<FuseTask> alreadies){
        int order=plan.indexOf(task);
        List<String> result=new LinkedList<String>();
        if (!alreadies.contains(task)){ // まだ説明していないタスク
            result.add(0,getExplain(task, plan, alreadies));
            alreadies.add(task);
            if (task!=goal){
                FuseTask nextTask=plan.get(order+1);
                result.addAll(0,getExplains(nextTask, goal, plan, alreadies));
            }
        }
        //System.out.println("DEBUG: "+result.size()+" ["+result+"]");
        return result;
    }

    /** 対象となるタスクから説明を取得します */
    private static String getExplain(FuseTask task, List<FuseTask> plan, List<FuseTask> already){
        String taskExp="説明不能";
        String conditionExp="";
        boolean cutFlag=false;
        int order=plan.indexOf(task);

        if (task instanceof Explainable){
            // 前後切断判定，文脈の切り替え
            taskExp=((Explainable)task).getScript();
            if (order<plan.size()-1){
                FuseTask from=plan.get(order+1);
                if (!isConnected(from,task)){
                    taskExp="また，"+taskExp;
                }
            }
            // 次で文脈が切り替わるときは条件の話をしない
            if (order>0){
                FuseTask to=plan.get(order-1);
                if (!isConnected(task,to)){
                    cutFlag=true;
                }
            }
        }

        if (task.getChildTasks()!=null && !cutFlag){ // 文脈切り替えの時は条件の話をしない
            List<Condition> targetConditions=getNextConditions(task, plan, already);
            if (targetConditions.size()!=0){
                conditionExp="\nそのためには";
                int numOfNotTrue=0;
                for (Condition con:task.getConditons()){
                    if (con.getTruth()!=Truth.TRUE){
                        numOfNotTrue++;
                    }
                }
                if (numOfNotTrue>1){
                    conditionExp+="少なくとも";
                }
                int numOfConditions=targetConditions.size();
                for (int i=0;i<numOfConditions;i++){
                    Condition cond=targetConditions.get(i);
                    if (cond instanceof Explainable){
                        conditionExp+=((Explainable)cond).getScript();
                    }else{
                        conditionExp+="何かをしなければならない";
                    }

                    if (cond.getTruth()==Truth.UNKNOWN){
                        if (i<numOfConditions-1){
                            conditionExp+="が、それが不明であり、";
                        }else{
                            conditionExp+="が、それが不明であるため、";
                        }
                    }else if(cond.getTruth()==Truth.FALSE){
                        if (i<numOfConditions-1){
                            conditionExp+="が、そうではなく、";
                        }else{
                            conditionExp+="が、そうではないため、";
                        }
                    }
                }
            }
            taskExp=taskExp+conditionExp;
        }
        return taskExp;
    }

    /** タスクが接続されているかどうかを確認します */
    private static boolean isConnected(FuseTask fromTask, FuseTask toTask){
        boolean result=false;
        for(Condition from:toTask.getFroms()){
            if (fromTask==from.getParentTask()){
                result=true;
                break;
            }
        }
        return result;
    }

    /** このタスクから次のタスクが派生した理由となる条件を取得します<br>
     * 場合によっては複数存在するかもしれません */
    private static List<Condition> getNextConditions(FuseTask task, List<FuseTask> plan, List<FuseTask> already){
        List<Condition> results=new ArrayList<Condition>();
        for (Condition cond:task.getConditons()){
            for (FuseTask t:cond.getTasks()){
                if (plan.contains(t) && already.contains(t)){ // 直接のプランに関係ないものは無視する
                    results.add(cond);
                }
            }
        }
        return results;
    }

    /** プランをダイエットします<br>
     * 最低コスト以外の実行可能タスクを弾きます */
    private static List<FuseTask> dietPlan(List<FuseTask> plan){
        FuseTask lowest=plan.get(0);
        List<FuseTask> result=new ArrayList<FuseTask>(plan);
        for (FuseTask t:plan){
            if (t.isAvailable() && t!=lowest){
                result.remove(t);
            }
        }
        return result;
    }
}
