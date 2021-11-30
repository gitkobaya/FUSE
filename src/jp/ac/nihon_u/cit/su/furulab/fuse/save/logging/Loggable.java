package jp.ac.nihon_u.cit.su.furulab.fuse.save.logging;


/** ロギングに対応したオブジェクトが実装するインターフェースです
 * このインターフェースを実装していないオブジェクトに関しては，座標と姿勢のみがロギングされます */
public interface Loggable {

    /** 状態が変化したかどうかのフラグです<br>
     * 状態が変化したときのみtrueにすることで，ログデータを圧縮することができます */
    public boolean isChanged();

    /** ログ再生モードで使用するためのステータスクラスを取得します */
    public ObjectStatus getObjectStatus();

    /** ログ再生モードで使用するためのデータを設定します */
    public void setObjectStatus(ObjectStatus status);
}
