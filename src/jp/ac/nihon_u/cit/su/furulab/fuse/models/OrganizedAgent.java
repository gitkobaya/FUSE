package jp.ac.nihon_u.cit.su.furulab.fuse.models;

import java.util.ArrayList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.DataContainer;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** 組織モデルの一員となるエージェント */
public abstract class OrganizedAgent extends Agent implements OrganizedAgentInterface{
    public static final long NOLEADER=-1;

    private long myLeaderId=-1; // 自分の上司のID
    private List<Long> subordinates=new ArrayList<Long>(); // 部下たちのID

    /** このエージェントの階級です。階級は整数値であり、大きいほど階級が上です。自分の上司は自分と同等かより高い階級でなければなりません。 */
    private int myRank=0;

    /** このエージェントの階級を取得<br>
     * 整数値であり、大きいほど高い階級を示します。 */
    public int getRank(){
        return myRank;
    }

    /** このエージェントの階級を設定<br>
     *  整数値であり、大きいほど高い階級を示します。 */
    public void setRank(int rank){
        myRank=rank;
    }

    /** このエージェントのリーダーを設定 */
    public void setMyLeader(OrganizedAgentInterface leader){
        this.setMyLeader(leader.getId());
    }

    /** このエージェントのリーダーをIDで設定 */
    public void setMyLeader(long leaderId){
        if (this.getEngine()!=null){
            Agent leader=this.getEngine().getAgentById(leaderId);

            // もし以前にリーダーがいたなら、前のリーダーに絶縁
            if (leader instanceof OrganizedAgent && myLeaderId!=NOLEADER){
                Agent oldLeader=this.getEngine().getAgentById(myLeaderId);
                ((OrganizedAgent) oldLeader).deleteSubordinate(this.getId());
            }

            // リーダーも組織モデルなら、自分を部下として登録
            if (leader instanceof OrganizedAgent){
                ((OrganizedAgent) leader).addSubordinate(this.getId());
            }
        }
        myLeaderId=leaderId;
    }

    /** このエージェントのリーダーをIDで設定<br>
     * ただし自分の情報のみを設定し，IDで設定したエージェントはこのエージェントを部下として認識していません */
    public void setMyLeaderJustForMe(long leaderId){
        this.myLeaderId=leaderId;
    }

    /** このエージェントのリーダーを削除 */
    public void deleteMyLeader(){
        myLeaderId=NOLEADER;
    }

    /** このエージェントのリーダーを取得<br>
     * 負の値が返ってきたらリーダー不在。 */
    public long getMyLeaderId(){
        return myLeaderId;
    }

    /** このエージェントの部下を追加 */
    public void addSubordinate(Long subId){
        boolean isExist=false;
        for (Long buka:subordinates){
            if (subId==buka){
                isExist=true;
                break;
            }
        }

        if (!isExist){
            subordinates.add(subId);
        }
    }

    /** このエージェントの部下をIDで指定して削除 */
    public void deleteSubordinate(Long subId){
        subordinates.remove(subId);
    }

    /** このエージェントの部下の一覧をIDで取得<br>
     * 与えられるリストは複製なので，加工または破壊しても問題ありません．
     * ただし，要素のオブジェクトそのものを書き換えてはいけません． */
    public List<Long> getSubordinatesList(){
        return new ArrayList<Long>(subordinates);
    }

    /** 上司部下情報の保存 */
    @Override
    public SaveDataPackage saveStatus(){
        SaveDataPackage pack=super.saveStatus();

        // 自分の階級
        pack.addData("rank",myRank);

        // 自分のリーダーが存在する場合
        if (myLeaderId>=0){
            pack.addData("leaderId", myLeaderId);
        }

        // 部下が存在する場合
        for (long id:subordinates){
            pack.addData("subordinateId", id);
        }
        return pack;
    }

    /** 上司部下情報の復元 */
    @Override
    public OrganizedAgent restoreStatus(SaveDataPackage saveData){
        super.restoreStatus(saveData);

        // このクラス独自の変数を復元
        List<DataContainer> containers=saveData.getAllData();

        for(DataContainer cont:containers){
            if (cont.getName().equals("rank")){
                this.setRank(Integer.valueOf(cont.getData().toString()));
            }
            if (cont.getName().equals("leaderId")){
                this.setMyLeader(Long.valueOf(cont.getData().toString()));
            }
            if (cont.getName().equals("subordinateId")){
                this.addSubordinate(Long.valueOf(cont.getData().toString()));
            }
        }

        return this;
    }

}
