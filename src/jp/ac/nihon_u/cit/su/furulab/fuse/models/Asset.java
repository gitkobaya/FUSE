package jp.ac.nihon_u.cit.su.furulab.fuse.models;

import java.util.ArrayList;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.DataContainer;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** 地形上に配置される建造物等の基本形です */
public class Asset extends VirtualObject {

    /** デフォルトコンストラクタ */
    public Asset() {
        super();
        double[] matrix=this.getMatrix();
        // 単位行列を設定
        for (int i=0;i<16;i++){
            matrix[i]=0;
        }
        matrix[0]=1.0;
        matrix[5]=1.0;
        matrix[10]=1.0;
        matrix[15]=1.0;
        this.setMatrix(matrix);
    }

    // マトリクスを文字列に変換します
    private String encodeMatrix(double[] mat){
        String result=new String();
        for(int i=0;i<mat.length;i++){
            result=result+String.valueOf(mat[i])+",";
        }
        return result;
    }

    /** 状態セーブ */
    @Override
    public SaveDataPackage saveStatus(){
        SaveDataPackage pack=super.saveStatus();
        // マトリクスを保存
        String matStr=this.encodeMatrix(this.getMatrix());
        pack.addData("matrix", matStr);
        return pack;
    }

    /** 状態復元 */
    @Override
    public Asset restoreStatus(SaveDataPackage pack){
        for (DataContainer cont:pack.getAllData()){
            if (cont.getName().equals("matrix")){
                this.setMatrix(this.decodeMatrix(cont.getData().toString()));
            }
        }
        return this;
    }

}
