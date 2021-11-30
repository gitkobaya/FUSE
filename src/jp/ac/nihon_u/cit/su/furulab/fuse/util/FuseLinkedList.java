package jp.ac.nihon_u.cit.su.furulab.fuse.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** 状態セーブに対応したアレイリストです */
public class FuseLinkedList<T extends Savable> extends LinkedList<T> implements FuseList<T>{

    public FuseLinkedList() {
        super();
    }

    /** 引数付きのコレクション */
    public FuseLinkedList(Collection<T> savableCollection){
        super(savableCollection);
    }

    /** セーブデータを生成します */
    public SaveDataPackage saveStatus() {
        SaveDataPackage saveData=new SaveDataPackage(this);

        /** 子データを保存します */
        for(Savable element:this){
            SaveDataPackage child=element.saveStatus();
            saveData.addChildPackage(child);
        }
        return saveData;
    }

    /** セーブデータから自分を復元します */
    public FuseLinkedList<T> restoreStatus(SaveDataPackage saveData) {
        List<SaveDataPackage> children=saveData.getAllChildren();

        // 要素を復元していく
        for(SaveDataPackage pack:children){
            try{
                T element=(T)pack.restore();
                ((List<T>)this).add(element);
            }catch(Exception e){
                e.printStackTrace();
                System.exit(-1);
            }
        }
        return this;
    }
}
