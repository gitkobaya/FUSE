package jp.ac.nihon_u.cit.su.furulab.fuse.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 引数分析を行うクラスです<br>
 * -がついた語句をオプション名，つかない語句を値とします<br>
 * オプション名から容易に値を取得できるため，このクラスを利用すれば何度もパラメーター解析ルーチンを作る必要はありません */
public class ArgumentsAnalyzer {
    private Map<String, String[]> arguments=new HashMap<String, String[]>();

    /** 引数分析を行います */
    public ArgumentsAnalyzer(String[] args) {
        for (int i=0;i<args.length;i++){
            String word=args[i];
            if (word.charAt(0)=='-'){
                List<String> values=new ArrayList<String>();
                i++;
                for (int j=i;j<args.length;j++){
                    String val=args[j];
                    if (val.charAt(0)!='-'){
                        values.add(val);
                        i=j+1;
                    }else{
                        i--; // 戻す
                        break;
                    }
                }
                String[] valueArray=values.toArray(new String[values.size()]);
                this.arguments.put(word, valueArray);
            }
        }
    }

    /** 指定した名前のデータが存在するかを問い合わせます */
    public boolean exists(String name){
        return this.arguments.containsKey(name);
    }

    /** 指定した名前に対応する値の配列を取得します */
    public String[] getValues(String name){
        String[] result=this.arguments.get(name);
        return result;
    }

    /** 指定した名前に対応する値のうち最初のものを取得します */
    public String getFirstValue(String name){
        String result=null;
        String[] values=this.getValues(name);
        if (values!=null){
            result=values[0];
        }
        return result;
    }
}
