package jp.ac.nihon_u.cit.su.furulab.fuse;

/** シミュレーションエンジンに終了条件を教えるためのクラスです。<br> */
public abstract class Finisher {
    private SimulationEngine engine;

    /** シミュレーションエンジンを設定します。<br>
     * シミュレーションエンジンに登録した時点で自動で呼ばれるため、
     * 通常はプログラマが呼び出す必要はありません。 */
    public void setEngine(SimulationEngine eng){
        engine=eng;
    }

    /** シミュレーションエンジンを取得します */
    protected SimulationEngine getEngine(){
        return engine;
    }
 
    /** 終了条件をここに記述します */
    public abstract boolean isFinish();

    /** 終了処理をここに記述します<br>
     * シミュレーションエンジンがループを抜けてからこのメソッドが呼ばれます。*/
    public void finish(){

    }

}
