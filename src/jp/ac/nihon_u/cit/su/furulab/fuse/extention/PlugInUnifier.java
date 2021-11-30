package jp.ac.nihon_u.cit.su.furulab.fuse.extention;

/** プラグインの識別情報です */
public class PlugInUnifier {
    /** このプラグインの名前です */
    private String name="unnamed plugin";

    /** このプラグインのバージョンです */
    private int[] version=new int[]{1,0,0};

    /** このプラグインの名前を取得します<br>
     * この名前は依存関係に使われるので，ユニークなものとしてください */
    public String getName() {
        return name;
    }

    /** このプラグインの名前を設定します */
    public void setName(String name) {
        this.name = name;
    }

    /** バージョンを数列で取得します */
    public int[] getVersion() {
        return version;
    }

    /** バージョンを文字列で取得します */
    public String getVersionStr() {
        return version[0]+"."+version[1]+"."+version[2];
    }

    /** バージョンを配列で設定します */
    public void setVersion(int[] version) {
        this.version = version;
    }

    /** 引数で指定されたプラグインが自分と互換性があるかどうかを取得します<br>
     * 基本的には，自分と同じ名前であれば互換性があるとみなします． */
    public boolean isCompatible(PlugInUnifier unifier){
        boolean result=false;
        if (unifier.getName().equals(this.name)){
            result=true;
        }
        return result;
    }
}
