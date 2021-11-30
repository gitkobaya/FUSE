package jp.ac.nihon_u.cit.su.furulab.fuse.ai.script;

/** クラスと引数のパッケージです */
public class CaspaPackage {
    private Object caspaElement; // CaSPAクラス
    private Object[] args; // それに渡す引数

    /** コンストラクタで値を設定 */
    public CaspaPackage(Object factoryObject, Object... args) {
        this.caspaElement=factoryObject;
        this.args=args;
    }

    /** パッケージ内のファクトリーを取得します */
    public Object getFactory(){
        return this.caspaElement;
    }

    /** パッケージ内の引数を取得します */
    public Object[] getArgs(){
        return this.args;
    }
}
