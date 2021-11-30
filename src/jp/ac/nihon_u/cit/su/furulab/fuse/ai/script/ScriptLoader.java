package jp.ac.nihon_u.cit.su.furulab.fuse.ai.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** ルートタスクを取得するためのローダーです */
public abstract class ScriptLoader {

    /** スクリプト側インターフェースの格納パスを指定します */
    public  abstract  void setSystemPath(String path);

    /** ファイルパスを指定するとスクリプトの文字列を取得します */
    public String loadScript(String filepath){
        File file=new File(filepath);
        return this.loadScript(file);
    }

    /** ファイルオブジェクトを指定するとスクリプトの文字列を取得します */
    public abstract String loadScript(File filepath);
}
