package jp.ac.nihon_u.cit.su.furulab.fuse.ai;


import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** アクションクラスです */
public abstract class FuseAction extends FuseAIBase{
    private FuseTask parent=null;
    private FuseTaskManager manager=null;
    private boolean impossible=false;
    private boolean isNotimeAction=false;

    /** コンストラクタで親タスクやタスクマネージャの機能が必要でない場合、こちらを利用できます */
    public FuseAction() {
    }

    /** タスクマネージャーを取得します */
    public FuseTaskManager getTaskManager(){
        if (this.manager==null){
            this.manager=this.parent.getTaskManager();
        }
        return manager;
    }

    /** このアクションのオーナーエージェントを取得します */
    public Agent getAgent(){
        return this.getParentTask().getAgent();
    }

    /** このアクションがノータイムアクションかどうかを取得します */
    public boolean isNotimeAction(){
        return this.isNotimeAction;
    }

    /** このアクションがノータイムアクションかどうかを設定します<br>
     * ノータイムアクションの場合，エージェントはアクション実行後に再度プランニングを実施します */
    public void setNotimeActionFlag(boolean flag){
        this.isNotimeAction=flag;
    }

    /** このアクションの呼び出し元のFuseTaskを指定します */
    public FuseTask getParentTask(){
        return this.parent;
    }

    /** このアクションの呼び出し元のFuseTaskを設定します<br>
     * FuseTaskにaddした場合には自動で設定されます。 */
    protected void setParentTask(FuseTask parent){
        this.parent=(parent);
    }

    /** このアクションが実施可能かを取得します */
    public boolean isImpossible(){
        return this.impossible;
    }

    /** 記憶領域に情報を書き込みます <br>
     * 引数は情報名，情報の内容，寿命です．寿命は論理時間で指定され，現在時刻から指定された時刻進行した時点でこのデータは消滅します<br>
     * 寿命にWhiteBoard.IMMORTALを設定すると無限の寿命となります */
    public void writeArticle(Object[] identifier, Object information, long lifetime){
        this.getTaskManager().getBoard().write(identifier, information, lifetime);
    }

    /** 記憶領域に情報を書き込みます <br>
     * 引数は情報名，情報の内容，寿命です．寿命は論理時間で指定され，現在時刻から指定された時刻進行した時点でこのデータは消滅します */
    public void writeArticle(Object[] identifier, Object information){
        this.getTaskManager().getBoard().write(identifier, information, Article.IMMORTAL);
    }

    /** 記憶領域から情報を削除します<br>
     * 指定した情報を削除した場合にtrueが返ります． */
    public boolean eraseArticle(Article art){
        return this.getTaskManager().getBoard().erase(art);
    }

    /** 記憶領域から名前で指定した情報を削除します<br>
     * 返り値は削除した情報の数です */
    public int eraseArticles(Object[] identifier){
        return this.getTaskManager().getBoard().erase(identifier);
    }

    /** 記憶を更新します<br>
     * 更新に成功した場合にtrueが返ります．falseであれば，単に追加しただけになります． */
    public boolean replaceArticle(Article newArticle, Article oldArticle){
        return this.getTaskManager().getBoard().replace(newArticle, oldArticle);
    }

    /** 定義されたアクションを実行します */
    public abstract void action();

    /** このアクションのデバッグ情報を取得します */
    public String debugInfo(){
        return this.getClass().getSimpleName()+" hashCode:"+this.hashCode();
    }

    /** ハッシュコードを返します */
    @Override
    public int hashCode(){
        int hash=this.getClass().hashCode()+this.getFieldsHash();
        return hash;
    }
}
