package jp.ac.nihon_u.cit.su.furulab.fuse.ai.script;

/** スクリプトから生成されるFUSEエレメントが備えるべきメソッドを定義します<br>
 * どっちかというと，XXbyScriptのクラスがスクリプトAIエレメントであることを保証するためのものです． */
public interface AIBaseByScript {

    /** このエレメントのフィールドを取得 */
    public Object[] getFields();

}
