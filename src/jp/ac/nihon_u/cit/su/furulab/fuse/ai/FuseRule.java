package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** ルールクラスです<br>
 * このクラスだけは継承せずにそのまま使います */
public class FuseRule {
    public static final int TO_BE_TRUE=0;
    public static final int TO_BE_KNOWN=1;

    private Map<AbstractTask, Class<? extends FuseTask>> relation=new HashMap<AbstractTask, Class<? extends FuseTask>>(); // 抽象タスクと実タスクの関係

    /** タスククラスを取得します */
    public Class<? extends FuseTask> getTaskClass(Class<? extends ConditionFuzzy> condClass, String taskName, int toBeTrueOrKnown){
        AbstractTask key=new AbstractTask(condClass, taskName, toBeTrueOrKnown);
        Class<? extends FuseTask> taskClass=this.relation.get(key);
        if (taskClass==null){
            throw (new RuntimeException("Rule does not define taskClass <"+taskName+"> of "+condClass.getSimpleName()));
        }
        return taskClass;
    }

    /** タスクオブジェクトを取得します */
    public FuseTask getTask(Class<? extends ConditionFuzzy> condClass, String taskName, int toBeTrueOrKnown, Object... args){
        Class<? extends FuseTask> taskClass=this.getTaskClass(condClass, taskName, toBeTrueOrKnown);
        Class<?>[] types=new Class[args.length];
        FuseTask resultTask=null;
        for (int i=0;i<types.length;i++){
            types[i]=args[i].getClass();
            // プリミティブ型の処理
            if (types[i].equals(Integer.class)){
                types[i]=int.class;
            }else if(types[i].equals(Double.class)){
                types[i]=double.class;
            }else if(types[i].equals(Long.class)){
                types[i]=long.class;
            }else if(types[i].equals(Float.class)){
                types[i]=float.class;
            }else if(types[i].equals(Character.class)){
                types[i]=char.class;
            }else if(types[i].equals(Boolean.class)){
                types[i]=boolean.class;
            }else if(types[i].equals(Byte.class)){
                types[i]=byte.class;
            }
        }
        try{
            Constructor<? extends FuseTask> constructor = taskClass.getConstructor(types);
            resultTask=constructor.newInstance(args);
        }catch(Exception e){
            e.printStackTrace();
        }
        return resultTask;
    }

    /** 偽を真にするタスクの登録 */
    public void setTaskToBeTrue(Class<? extends ConditionFuzzy> condClass, String taskName, Class<? extends FuseTask> taskClass){
        this.registerTaskClass(condClass, taskName, taskClass, TO_BE_TRUE);
    }

    /** 未知を既知にするタスクの登録 */
    public void setTaskToBeKnown(Class<? extends ConditionFuzzy> condClass, String taskName, Class<? extends FuseTask> taskClass){
        this.registerTaskClass(condClass, taskName, taskClass, TO_BE_KNOWN);
    }

    /** タスククラスの登録 */
    private void registerTaskClass(Class<? extends ConditionFuzzy> condClass, String taskName,Class<? extends FuseTask> taskClass, int toBeTrueOrKnown){
        // 抽象タスクを生成
        AbstractTask key=new AbstractTask(condClass, taskName, toBeTrueOrKnown);
        this.relation.put(key, taskClass);
    }
}
