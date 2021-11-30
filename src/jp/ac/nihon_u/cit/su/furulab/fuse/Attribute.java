package jp.ac.nihon_u.cit.su.furulab.fuse;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** 属性オブジェクトです */
public class Attribute implements Savable{
    private String name;
    private long nameHash;
    private long ownerId=-1;
    private long ownerClassNameHash=-1;
    private long logicalTime=-1; // このアトリビュートが宣言された論理時刻
    private Object value;

    /** コンストラクタでアトリビュート名を設定します */
    public Attribute(String name) {
        this.name=name;
        this.nameHash=name.hashCode();
    }

    /** コンストラクタでアトリビュート名と値を設定します<br>
     * 値はプリミティブまたはSavableなオブジェクトとしてください */
    public Attribute(String name, Object value) {
        this(name);
        this.value=value;
    }

    /** このアトリビュートの持ち主のクラスハッシュを取得します */
    public long getOwnerClassNameHash(){
        return this.ownerClassNameHash;
    }

    /** このアトリビュートの持ち主のIDを取得します */
    public long getOwnerId(){
        return this.ownerId;
    }

    /** このアトリビュートの持ち主を設定します<br>
     * システムから呼び出されるのでユーザーが指定する必要はありません */
    public void setOwner(VirtualObject owner){
        this.ownerId=owner.getId();
        this.ownerClassNameHash=owner.getClass().getName().hashCode();
    }

    /** このアトリビュートのハッシュ値を取得します<br>
     * アトリビュート名とオーナーIDのXORにしています */
    public long getAttrHash(){
        return (this.nameHash^this.ownerId);
    }

    /** アトリビュートの値を取得します */
    public Object getAttributeValue(){
        return this.value;
    }

    /** アトリビュートの値を設定します <br>
    * 値はプリミティブまたはSavableなオブジェクトとしてください */
    public void setAtrributeValue(Object value){
        this.value=value;
    }

    /** アトリビュートを保存します */
    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=new SaveDataPackage(this);
        pack.addData("name", this.name);
        pack.addData("owner", this.ownerId);
        pack.addData("ownerClass", this.ownerClassNameHash);
        pack.addData("logicalTime", this.logicalTime);
        pack.addData("value", this.value);
        return pack;
    }

    /** アトリビュートを復元します */
    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        this.name=(String)saveData.getData("name");
        this.ownerId=(Long)saveData.getData("owner");
        this.ownerClassNameHash=(Long)saveData.getData("ownerClassHash");
        this.logicalTime=(Long)saveData.getData("logicalTime");
        this.value=saveData.getData("value");

        this.nameHash=this.name.hashCode();
        return this;
    }
}
