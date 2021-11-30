package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

/** 抽象タスクです<br>
 * 実際にどの実タスクに相当するかは，ルールによって定められます */
public  class AbstractTask{
    public String taskClassName;
    public Class<? extends ConditionFuzzy> conditionClass;
    public int toBeTrueOrKnown;

    /** その条件において，その名前で， */
    public AbstractTask(Class<? extends ConditionFuzzy> class1, String taskClassName, int toBeTrueOrKnown) {
        this.conditionClass=class1;
        this.taskClassName=taskClassName;
        this.toBeTrueOrKnown=toBeTrueOrKnown;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result=false;
        if (obj.hashCode()==this.hashCode()){
            result=true;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return this.conditionClass.hashCode()+this.taskClassName.hashCode()+this.toBeTrueOrKnown;
    }

}