package jp.ac.nihon_u.cit.su.furulab.fuse.save.logging;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** 基本的なデータ型を対象としたログデータです<br>
 * プリミティブ，文字列，Savableなデータに対応しています */
public class LogData implements Savable{
    private String dataName="";
    Object data=null;

    public LogData(){}

    public LogData(String name, Object data) {
        this.setDataName(name);
        this.data=data;
    }


    /** このデータの名前を設定します */
    public String getDataName() {
        return dataName;
    }

    /** このデータの名前を取得します */
    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    /** データを取得します */
    public Object getData(){
        return this.data;
    }

    /** データを設定します */
    public void setData(Object data){
        this.data=data;
    }

    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=new SaveDataPackage(this);
        pack.addData("thisData", this.data);
        return pack;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        this.data=saveData.getData("thisData");
        return this;
    }

}
