package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** WhiteBoardに登録する情報 */
public class Article implements Savable{

    public static Object WILD=WhiteBoard.WILD;
    public static long IMMORTAL=WhiteBoard.IMMORTAL; // 寿命にIMMORTALが指定されていた場合，寿命無限となります

    /**  識別情報<br>ただし，最初の要素はStringであることを期待 */
    private Object[] identifier;
    /** この情報の生成時刻 */
    private long createdTime=0;
    /** 情報の中身 */
    private Object Information;
    /** この情報の寿命です．IMMORTALを代入すると寿命無限として扱われます */
    private long lifetime=IMMORTAL;

    /** デフォルトコンストラクタ */
    public Article() {
        int dummy=0;
    }

    /** この情報の生成時刻を取得します */
    public long getCreatedTime(){
        return this.createdTime;
    }

    /** この情報の生成時刻を設定します */
    public void setCreatedTime(long t){
        this.createdTime=t;
    }

    /** この情報の識別情報リストを取得します */
    public Object[] getIdentifiers(){
        return this.identifier;
    }

    /** この情報の識別情報リストを登録します */
    public void setIdentifiers(Object[] identifiers){
        if (! (identifiers[0] instanceof String)){
            throw new RuntimeException("ERROR: The first identifier must be String !");
        }
        this.identifier=identifiers;
    }

    /** 情報オブジェクトを取得します */
    public Object getInformation(){
        return this.Information;
    }

    /** 情報オブジェクトを登録します */
    public void setInformation(Object info){
        this.Information=info;
    }

    /** この情報の寿命を返します */
    public long getLifetime(){
        return this.lifetime;
    }

    /** この情報の寿命を設定します */
    public void setLifetime(long lifetime){
        this.lifetime=lifetime;
    }

    /** この情報の識別コードを返します */
    public long getLongHash(){
        long hash=0;
        for (Object id:this.identifier){
            int thisHash=0;
            if (id!=null){
                thisHash=id.hashCode();
            }
            hash+=thisHash;
            hash=hash^thisHash;
        }
        return hash;
    }

    @Override
    public SaveDataPackage saveStatus() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

}
