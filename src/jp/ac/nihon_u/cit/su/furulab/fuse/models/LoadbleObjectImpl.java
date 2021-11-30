package jp.ac.nihon_u.cit.su.furulab.fuse.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.FuseArrayList;

/** 独自ファイルからロード可能なバーチャルオブジェクトの機能を提供するオブジェクトです<br>
 * 必要な機能をまとめているため，対象となるバーチャルオブジェクトはこのクラスを利用すると効率よく実装できます． */
public class LoadbleObjectImpl implements LoadableObject, Savable{
    public static String LOADABLE_OBJ_PROP="loadableObjectProperty";
    public static String FILEDATUM="fileDatum";
    private FuseArrayList<FileDataProperty> strageFiles;

    /** リストアで呼び出される場合の引数なしコンストラクタです */
    public LoadbleObjectImpl() {
    }

    /** コンストラクタで利用するファイルを指定します */
    public LoadbleObjectImpl(List<FileDataProperty> files) {
        this.strageFiles=new FuseArrayList<FileDataProperty>(files);
    }

    /** コンストラクタで利用するファイルを指定します */
    public LoadbleObjectImpl(FileDataProperty... files) {
        this.strageFiles=new FuseArrayList<FileDataProperty>(Arrays.asList(files));
    }

    /** 利用するファイルを設定します */
    public void addFile(String fileName, String describe){
        FileDataProperty fProp=new FileDataProperty(fileName, describe);
        this.strageFiles.add(fProp);
    }

    /** ファイルを全て取得します */
    public List<FileDataProperty> getFiles(){
        return this.strageFiles;
    }

    /** このオブジェクトの情報をセーブファイルに保存します．<br>
     * 保存されるのはファイル名とその説明文字列です */
    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=new SaveDataPackage(this);
        pack.setName(LOADABLE_OBJ_PROP);
        pack.addData(FILEDATUM, this.strageFiles);
        return pack;
    }

    /** このオブジェクトの情報をセーブファイルから復元します．<br>
     * 復元されるのは独自ファイル名とその説明文字列です．バーチャルオブジェクトは，このオブジェクトの情報を使って独自ファイルから情報を復元する必要があります． */
    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        this.strageFiles=(FuseArrayList<FileDataProperty>)saveData.getData(FILEDATUM);
        return this;
    }

    @Override
    public Savable restoreFromStrage(List<FileDataProperty> fileProperties) {
        throw new RuntimeException("restoreFromStrage method is not implemented");
    }

    @Override
    public Savable saveToStrage(List<FileDataProperty> fileProperties) {
        throw new RuntimeException("saveToStrage method is not implemented");
    }
}
