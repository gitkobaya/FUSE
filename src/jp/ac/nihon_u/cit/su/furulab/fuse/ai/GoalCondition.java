package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

/** ゴールタスクの派生元として設定されるダミークラスです */
public class GoalCondition extends Condition{

    @Override
    public Truth evaluate() {
        return Truth.TRUE;
    }

    /**ゴール条件のハッシュ値は常にゼロ */
    @Override
    public int hashCode() {
        return 0;
    }
}
