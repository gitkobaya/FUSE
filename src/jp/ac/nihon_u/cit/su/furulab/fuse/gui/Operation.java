package jp.ac.nihon_u.cit.su.furulab.fuse.gui;

/** シミュレーションに対する操作クラスです */
public abstract class Operation<T> {
    private long operationId=this.hashCode(); // 標準ではハッシュ値を利用

    /** 定義された操作を実行するメソッドです */
    public abstract void execute(T operationTarget);

    /** 定義された操作を取り消すメソッドです */
    public abstract void undo(T operationTarget);

    /** 一連動作のIDを取得するメソッドです */
    public long getId(){
        return operationId;
    }

    /** 一連動作のIDを設定するメソッドです */
    public void setId(long id){
        operationId=id;
    }

}
