package jp.ac.nihon_u.cit.su.furulab.fuse.save.logging;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** VirtualObjectが必要とする基本情報です<br>
 * つまり座標と方位 */
public class BasicLogData extends LogData{
    public static final String EXIT_FLAG="exitFlag";
    public static final String MATRIX="matrix";
    private boolean exitFlag;
    private double[] matrix;

    /** リストア用の空コンストラクタ */
    protected BasicLogData() {

    }

    /** 同次行列と離脱フラグを設定 */
    public BasicLogData(double[] matrix){
        if (matrix.length!=16){
            throw new RuntimeException("Allowed matrix is only 4x4");
        }
        this.matrix=matrix;
    }

    /** マトリクスを取得 */
    public double[] getMatrix(){
        return this.matrix;
    }

    /** 離脱フラグを取得 */
    public boolean isExit(){
        return this.exitFlag;
    }

    /** 離脱フラグを設定 */
    public void setExitFlag(boolean flag){
        this.exitFlag=flag;
    }

    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=new SaveDataPackage(this);
        pack.addData("EXIT_FLAG", this.exitFlag);
        for(int i=0;i<this.matrix.length;i++){
            pack.addData(MATRIX+i, this.matrix[i]);
        }
        return pack;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        this.exitFlag=(Boolean)saveData.getData("EXIT_FLAG");
        for(int i=0;i<this.matrix.length;i++){
            this.matrix[i]=(Double)saveData.getData(MATRIX+i);
        }
        return this;
    }
}
