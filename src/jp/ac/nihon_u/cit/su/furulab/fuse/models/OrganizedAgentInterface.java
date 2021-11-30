package jp.ac.nihon_u.cit.su.furulab.fuse.models;

import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.standard.MediaSize.Engineering;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.DataContainer;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** 組織モデルの一員となるエージェントのインターフェース<br>
 * 実装はOrganizedAgentImplementedに記述されています．
 * なお，リーダーや部下の登録処理はIDでの処理を前提としているため，シミュレーションエンジンに登録された後でないと正常に実行できません． */
public interface OrganizedAgentInterface {
    public static final long NOLEADER=-1;

    /** このエージェントのIDを取得．<br>
     * Agentクラスを継承していれば自動的に実装されます． */
    public long getId();

    /** このエージェントの階級を取得<br>
     * 整数値であり、大きいほど高い階級を示します。 */
    public int getRank();

    /** このエージェントの階級を設定 */
    public void setRank(int rank);

    /** このエージェントのリーダーを設定 */
    public void setMyLeader(OrganizedAgentInterface leader);

    /** このエージェントのリーダーをIDで設定 */
    public void setMyLeader(long leaderId);

    /** このエージェントのリーダーを削除 */
    public void deleteMyLeader();

    /** このエージェントのリーダーを取得<br>
     * 負の値が返ってきたらリーダー不在。 */
    public long getMyLeaderId();

    /** このエージェントの部下を追加します*/
    public void addSubordinate(Long subId);

    /** このエージェントの部下をIDで指定して削除 */
    public void deleteSubordinate(Long subId);

    /** このエージェントの部下の一覧を参照で取得 */
    public List<Long> getSubordinatesList();

}
