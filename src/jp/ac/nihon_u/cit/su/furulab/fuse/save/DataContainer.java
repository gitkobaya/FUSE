package jp.ac.nihon_u.cit.su.furulab.fuse.save;

/** データコンテナです<br>
 * XMLのエレメントと1対1で対応する基本データであり、プリミティブ型等が該当します<br>
 * シミュレーションデータのセーブ・ロード処理の中核になります。 */
public class DataContainer {
    private String dataName;
    private Object dataObject;

    /** コンストラクタです */
    public DataContainer(String name) {
        dataName=name;
    }

    /** 同時にデータもセットする場合のコンストラクタです */
    public DataContainer(String name, Object data) {
        this(name);
        this.setData(data);
    }

    /** データ内容です */
    public void setData(Object data){
        dataObject=data;
    }

    /** データの名前を取得します */
    public String getName(){
        return dataName;
    }

    /** データを取得します */
    public Object getData(){
        return dataObject;
    }
}
