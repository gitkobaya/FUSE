package jp.ac.nihon_u.cit.su.furulab.fuse.models;

/** 組織モデルの一員となるエージェントのダミークラス<br>
 * OrganizedAgentを作りたいが，そのエージェントが既に他のクラスを継承していた場合にこのダミークラスを用いて対応します．
 * ID関係の処理はダミーではなく実体エージェントのIDを使うように再実装されます */
public class OrganizedAgentImpl extends OrganizedAgent implements OrganizedAgentInterface{
    public static final long NOLEADER=-1;

    private Agent realAgent;

    /** コンストラクタでこのクラスの親になるエージェントを指定します */
    public OrganizedAgentImpl(Agent agent) {
        this.realAgent=agent;
    }

    /** 親エージェントのIDを返します */
    @Override
    public long getId() {
        return this.realAgent.getId();
    }

    /** このエージェントのリーダーをIDで設定 */
    @Override
    public void setMyLeader(long leaderId){
        if (this.realAgent.getEngine()!=null){
            Agent leader=this.realAgent.getEngine().getAgentById(leaderId);

            // もし以前にリーダーがいたなら、前のリーダーに絶縁
            if (leader instanceof OrganizedAgentInterface && this.getMyLeaderId()!=NOLEADER){
                Agent oldLeader=this.realAgent.getEngine().getAgentById(this.getMyLeaderId());
                ((OrganizedAgentInterface) oldLeader).deleteSubordinate(this.realAgent.getId());
            }

            // リーダーも組織モデルなら、自分を部下として登録
            if (leader instanceof OrganizedAgentInterface){
                ((OrganizedAgentInterface) leader).addSubordinate(this.realAgent.getId());
            }
        }
        super.setMyLeaderJustForMe(leaderId);
    }

    @Override
    public void action(long timeStep) {
        System.err.println("ERROR: This method can not be called !");
        RuntimeException ex=new RuntimeException("This method can not be called");
        throw ex;
    }
}
