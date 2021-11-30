package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** 情報交換用クラスです<br>
 * 主語と述語で構成され、通常はMessageの内容として送信されます。 */
public class Information implements Savable{

    /** この情報の名前 */
    private String name=null;
    /** 述語:対象に対するアクション */
    private Object subject=null;
    /** 目的語:対象となるオブジェクト、座標、その他 */
    private Object object=null;
    /** この情報が発行された論理時刻 */
    private long timeStump=0;

    public Information() {
    }

    /** 実質的な初期化です<br>
     * 引数としてこの情報の名前と、情報に込める主語と述語、情報を発行した論理時刻を設定します。<br>
     * 例 「ある座標に向かえ」: name="MoveToPoint", predicate=Position, subject=String("Move") */
    public void init(String name, Object predicate, Object subject, long logicalTime){
        this.name=name;
        this.object=predicate;
        this.subject=subject;
        this.setTimeStump(logicalTime);
    }


    /** 実質的な初期化です<br>
     * 引数としてこの情報の名前と、情報に込める主語と述語を設定します。<br>
     * 例 「ある座標に向かえ」: name="MoveToPoint", predicate=Position, subject=String("Move") */
    public void init(String name, Object predicate, Object subject){
        this.name=name;
        this.object=predicate;
        this.subject=subject;
    }

    /** 実質的な初期化です<br>
     * 引数として目的語と述語を設定します。<br>
     * 例 「ある座標に向かえ」: object=Position, subject=String("Move") */
    public void init(Object object, Object subject){
        this.init("NoName",object, subject);
    }

    /** この情報の名前を取得します<br>
     * この情報を解釈する際に、名前は主語述語のフォーマットを知るための重要な手掛かりになります。 */
    public String getName(){
        return this.name;
    }

    /** この情報に名前を設定します */
    public void setName(String name){
        this.name=name;
    }

    /** この情報のタイムスタンプを取得します */
    public long getTimeStump() {
        return timeStump;
    }

    /** この情報のタイムスタンプを設定します */
    public void setTimeStump(long timeStump) {
        this.timeStump = timeStump;
    }

    /** この情報の目的語を取得します */
    public Object getObject(){
        return object;
    }

    /** この情報に目的語を設定します */
    public void setObject(Object object){
        this.object=object;
    }

    /** この情報の述語を取得します */
    public Object getSubject(){
        return subject;
    }

    /** この情報に述語を設定します */
    public void setSubject(Object subject){
        this.subject=subject;
    }

    public SaveDataPackage saveStatus() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    public Savable restoreStatus(SaveDataPackage saveData) {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

}
