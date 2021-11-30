package jp.ac.nihon_u.cit.su.furulab.fuse.ai.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** ルートタスクを取得するためのローダーです<br>
 * ファクトリーパターンを利用すべきかも． */
public class RubyScriptLoader extends ScriptLoader{
    private String scriptPath=""; //スクリプト側インターフェースの格納場所
    private Map<Integer, String> loadedFiles=null;

    /** スクリプト側インターフェースの格納パスを指定します */
    @Override
    public void setSystemPath(String path){
        scriptPath=path;
    }

    /** ファイルオブジェクトを指定するとスクリプトの文字列を取得します<br>
     * 実装(特にrequire関係)が相当に汚いので後で直す予定 */
    @Override
    public String loadScript(File filepath){
        this.loadedFiles=new HashMap<Integer, String>();
        String str=this.loadScriptPrivate(filepath);
        return str;
    }

    private String loadScriptPrivate(File filepath){
        String str="";
        Pattern pReq = Pattern.compile("require '.*'");
        Pattern rbReq = Pattern.compile("\\.rb");
        List<String> requestedScripts=new LinkedList<String>();

        try{
            InputStreamReader reader=new InputStreamReader(
                    new FileInputStream(filepath),"UTF-8");
            BufferedReader buff = new BufferedReader(reader);
            String line;
            while((line = buff.readLine())!=null){
                Matcher m=pReq.matcher(line);
                // requireを発見した場合
                if (m.find()){
                    String path=filepath.getParent(); // ディレクトリ名取得
                    String rbName=m.group();
                    rbName=rbName.replaceAll("require", ""); // 削除
                    rbName=rbName.replaceAll("'", ""); // 削除
                    rbName=rbName.replaceAll(" ", ""); // 削除

                    m=rbReq.matcher(rbName);
                    if (m.find()){
                        // パスを再生成
                        path+="/"+rbName;
                        // 重複読み込みの回避
                        if (!loadedFiles.containsKey(path.hashCode())){
                            loadedFiles.put(path.hashCode(), path);

                            // カレントに存在すればそれを読み込む
                            File target=new File(path);
                            if (target.exists()){
                                requestedScripts.add(0,this.loadScriptPrivate(target));
                            }else{
                                // 標準ディレクトリから読み込む
                                path=scriptPath+"/"+rbName;
                                target=new File(path);
                                if (target.exists()){
                                    requestedScripts.add(0,this.loadScriptPrivate(target));
                                }
                            }
                        }
                    }
                }else{
                    str=str+line+"\n";
                }
            }
            buff.close();
            reader.close();
        }catch(Exception e){
            System.out.println("ファイル読み込み失敗...:"+filepath);
        }

        for (String reqed:requestedScripts){
            str=reqed+str;
        }

        return str;
    }
}
