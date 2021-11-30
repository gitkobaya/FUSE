package jp.ac.nihon_u.cit.su.furulab.fuse.save;

import java.io.Serializable;


/** セーブ可能であることを示すインターフェースです<br>
 * このインターフェースを実装するクラスは、できるだけデフォルトコンストラクタを定義してください<br>
 * 特に、FuseListの要素はデフォルトコンストラクタを持っていないとセーブデータからの展開に失敗します */
public interface Savable extends Serializable{

    /** セーブ情報を作成します */
    public abstract SaveDataPackage saveStatus();

    /** セーブ情報からインスタンスを復元し、復元したオブジェクトを返します<br>
     * 本メソッドが呼ばれた場合、自分自身を書き換え、自分を返り値として返します。<br>
     * つまり、return this;で終わることになります。 */
    public abstract Savable restoreStatus(SaveDataPackage saveData);

}
