package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

/** Task, Condition, Actionの親クラスです */
public abstract class FuseAIBase extends FieldHashChecker{
    private int fieldHash=0;
    public final static int COUNT_UP=4099;

    /** 記憶領域を参照します<br>
     * 情報の名前を文字列で指定します */
    public Article[] readArticles(Object[] identifier){
        FuseTaskManager man=this.getTaskManager();
        WhiteBoard board=man.getBoard();
        List<Article> articles=board.readArticles(identifier);
        Article[] result=(Article[])articles.toArray(new Article[0]);
        return result;
    }

    /** タスクマネージャーを取得用メソッドのひな形です<br>
     * 必要に応じてオーバーライドして利用してください */
    public abstract FuseTaskManager getTaskManager();

    /** ハッシュコードを取得します */
    @Override
    public int hashCode(){
        int nativeHash=this.getClass().hashCode();
        long hash=nativeHash+this.getFieldsHash();
        return (int)(hash & 0xffffffff);
    }

    /** 自分が宣言したフィールドのハッシュ値を取得します */
    @Override
    public int getFieldsHash(){
        int resultInt=0;
        long result=0;

        if (this.fieldHash!=0){
            resultInt=this.fieldHash;
        }else{
            Field[] fields=this.getClass().getDeclaredFields();

            long counter=1021; // 素数がよさげ
            for (Field fd:fields){
                if ( !Modifier.isPublic(fd.getModifiers())) {
                    fd.setAccessible(true);
                }

                Object thisField=null;
                try{
                    thisField=fd.get(this);
                }catch(Exception e){
                    e.printStackTrace();
                }

                // フィールドがプリミティブなら単純に値を取得する
                counter+=COUNT_UP; // 素数がよさげ
                double adder=this.getValue(thisField); // 加算用一時ハッシュ
                result=result ^ (long)(counter*adder);
            }
            resultInt=(int)(result & 0xffffffff);
            this.fieldHash=resultInt;
        }
        return resultInt;
    }

    /** オブジェクトの数値を取得します */
    public double getValue(Object thisField){
        double value=0;
        if (thisField instanceof Boolean){
            if ((Boolean)thisField){
                value=1;
            }
        }else if (thisField instanceof Character){
            value=(Character)thisField;
        }else if (thisField instanceof Short){
            value=(Short)thisField;
        }else if (thisField instanceof Integer){
            value=(Integer)thisField;
        }else if (thisField instanceof Long){
            value=(Long)thisField;
        }else if (thisField instanceof Float){
            value=(Float)thisField;
        }else if (thisField instanceof Double){
            value=(Double)thisField;
        }else if (thisField instanceof Collection){
            value=this.getArrayHash(((Collection)thisField).toArray());
        }else if (thisField==null){
            value=0;
        }else if (thisField.getClass().isArray()){
            value=this.getArrayHash(this.createArray(thisField));
        }else if (thisField instanceof FuseAIBase){ // AIベースを継承していればハッシュをもらう
            value=((FuseAIBase)thisField).hashCode();
        }else if (thisField instanceof Object){ // よくわからないがオブジェクトならインスタンスハッシュをもらう
            value=thisField.hashCode();
        }else { // 解釈できない変数は飛ばす
            value=0;
        }
        return value;
    }

    /** オブジェクトが適切な型に設定された配列を無理やり作成します */
    public Object[] createArray(Object target){
        Object[] result=null;
        if (target.getClass().isArray()){
            int length=Array.getLength(target);
            result=new Object[length];
            for(int i=0;i<length;i++){
                result[i]=Array.get(target, i);
            }
        }
        return result;
    }

    /** 配列のハッシュを再帰的に取得します */
    public double getArrayHash(Object[] target){
        long counter=1021;
        double totalValue=0;
        for (Object o:target){
            double value=this.getValue(o);
            totalValue+=value*counter;
            counter+=COUNT_UP;
        }
        return totalValue;
    }
}
