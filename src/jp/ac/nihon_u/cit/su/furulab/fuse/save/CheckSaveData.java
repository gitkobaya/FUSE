package jp.ac.nihon_u.cit.su.furulab.fuse.save;


/** セーブデータの確認用クラス */
public class CheckSaveData {

    public static void analyzeData(SaveDataPackage saveData,int indent){

        System.out.println("Owner:"+saveData.getOwnerClassName()+" ID:"+saveData.getOwnerId());
        for(DataContainer data:saveData.getAllData()){
            CheckSaveData.insertIndent(indent);
            System.out.println(data.getName()+":"+data.getData());
        }

        for(SaveDataPackage pack:saveData.getAllChildren()){
            CheckSaveData.analyzeData(pack,indent+4);
        }
    }

    public static void insertIndent(int indent){
        for(int i=0;i<indent;i++){
            System.out.print(" ");
        }
    }
}
