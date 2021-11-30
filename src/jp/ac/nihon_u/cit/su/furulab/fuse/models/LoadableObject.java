package jp.ac.nihon_u.cit.su.furulab.fuse.models;

import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;

/** 情報がファイル等から復元できることを示すインターフェースです<br>
 * このインターフェースを実装したバーチャルオブジェクトは，情報を独自形式のファイル等から復元できるため，
 * セーブ・リストア時に全ての情報をセーブファイルに書き込む必要がなくなります．xmlと相性の悪い情報を扱うオブジェクトは，このインターフェースを利用することで独自形式のファイルを利用することができます．<br>
 * このインターフェースが最も有効に働く例は地形データです． 巨大な地形データをセーブファイルに書き込むことは現実的ではなく，独自フォーマットのファイルによって管理すべきです．
 * このインターフェースを実装したバーチャルオブジェクトを復元する場合，いったんファイルから復元して初期化したのち，シミュレーションによって加えられた変更をセーブデータから読み込むという手順が考えられます． */
public interface LoadableObject {

    /** 独自形式のファイル等からのロードによってオブジェクトを復元します */
    public Savable restoreFromStrage(List<FileDataProperty> fileProperties);

    /** オブジェクトの状態を独自形式のファイル等へセーブします */
    public Savable saveToStrage(List<FileDataProperty> fileProperties);

}
