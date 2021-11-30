package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldHashChecker {
    private int fieldHash=0;
    public final static int COUNT_UP=4099;

    /** ハッシュコードを取得します */
    @Override
    public int hashCode(){
        int nativeHash=this.getClass().hashCode();
        long hash=nativeHash+this.getFieldsHash();
        return (int)(hash & 0xffffffff);
    }

    /** 自分が宣言したフィールドのハッシュ値を取得します */
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
                double adder=0; // 加算用一時キャッシュ
                if (thisField instanceof Boolean){
                    if ((Boolean)thisField){
                        adder=1;
                    }
                }else if (thisField instanceof Character){
                    adder=(Character)thisField;
                }else if (thisField instanceof Short){
                    adder=(Short)thisField;
                }else if (thisField instanceof Integer){
                    adder=(Integer)thisField;
                }else if (thisField instanceof Long){
                    adder=(Long)thisField;
                }else if (thisField instanceof Float){
                    adder=(Float)thisField;
                }else if (thisField instanceof Double){
                    adder=(Double)thisField;
                }else if (thisField instanceof FuseAIBase){
                    adder=((FuseAIBase)thisField).getFieldsHash();
                }else if (thisField instanceof String){
                    adder=thisField.hashCode();
                }else if (thisField instanceof Object){ // よくわからないがオブジェクトならクラスハッシュをもらう
                    adder=thisField.getClass().hashCode();
                }else
                { // 解釈できない変数は飛ばす
                    continue;
                }
                result=result ^ (long)(counter*adder);
            }
            resultInt=(int)(result & 0xffffffff);
            this.fieldHash=resultInt;
        }
        return resultInt;
    }

}
