package jp.ac.nihon_u.cit.su.furulab.fuse.models;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** LoadableObjectが利用するファイルの情報をまとめたクラスです<br>
 * ファイル名と，そのファイルの意味を管理します */
public class FileDataProperty implements Savable{
    public static final String DATAFILENAME="dataFileName";
    public static final String DATADESCRIPTION="desctiption";

    /** 情報をまとめたファイル名です.ファイル名とはいうものの，URL等かもしれません */
    private String fileName;
    private String describe;

    public FileDataProperty() {
    }

    public FileDataProperty(String file, String describe) {
        this.fileName=file;
        this.describe=describe;
    }

    /** ファイル名を取得します */
    public String getFileName(){
        return this.fileName;
    }

    /** ファイルの説明を取得します */
    public String getFileDescription(){
        return this.describe;
    }

    /** このファイル情報オブジェクトを保存します */
    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=new SaveDataPackage(this);
        pack.addData(DATAFILENAME, this.fileName);
        pack.addData(DATADESCRIPTION, this.describe);
        return pack;
    }

    /** このファイル情報オブジェクトを復元します */
    @Override
    public FileDataProperty restoreStatus(SaveDataPackage saveData) {
        this.fileName=(String)saveData.getData(DATAFILENAME);
        if (fileName==null){
            throw new RuntimeException("Can not find Filename of fileDataProperty");
        }
        this.describe=(String)saveData.getData(DATADESCRIPTION);
        return this;
    }

}
