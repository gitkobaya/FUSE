package jp.ac.nihon_u.cit.su.furulab.fuse.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** CaSPAルールから参照するエージェントの記憶領域です<br>
 * エージェントが記憶を持たなければならない時は，このオブジェクトを利用します<br>
 * 記事情報には識別情報が設定されます．識別情報が一致した場合，あとから登録されたもので上書きされ，重複しての保持はしません */
public class WhiteBoard implements Savable{
    /** ワイルドカードを意味する定数です */
    public static Object WILD=null;

    /** 寿命無限を表す定数です */
    public static long IMMORTAL=-1;

    /** 瞬間記憶を表す定数です<br>
     * 一度アップデートをかけた時点で消滅します． */
    public static long IMMEDIATE=0;

    private long currentTime=0; // 現在時刻です
    private List<Article> articles=new ArrayList<Article>();

    /** 板の記事情報を取得します<br>
    板に登録された全ての記事情報を取得します．*/
    public Collection<Article> getAllArticles(){
        return this.articles;
    }

    /** 板の記事情報を取得します<br>
     * 指定した識別情報にマッチする記事情報をすべて取得します */
    public List<Article> readArticles(Object[] identifiers){
        List<Article> selected=new ArrayList<Article>();
        for(Article target:this.articles){
            if (this.isMatch(target.getIdentifiers(),identifiers)){
                selected.add(target);
            }
        }
        return selected;
    }

    /** 板に記事情報を書き込みます<br>
     * 原則として，Actionからしか書き込みをしてはいけません． */
    protected void write(Article article){
        List<Article> sameTypeArticles=this.readArticles(article.getIdentifiers());

        if (sameTypeArticles.isEmpty()){ // 一致しない場合は単に追加
            this.articles.add(article);
        }else{ // 一致した場合は上書き
            for (Article target:sameTypeArticles){
                target.setInformation(article.getInformation());
                target.setCreatedTime(article.getCreatedTime());
            }
        }
    }

    /** 板に記事情報を書き込みます<br>
     * 原則として，CaSPAからはAction経由でしか書き込みをしてはいけません．
     * 引数は識別情報，記事情報の内容，寿命です．寿命は論理時間で指定され，現在時刻から指定された時刻進行した時点でこのデータは消滅します<br>
     * ただし，寿命にIMMORTALを代入した場合，寿命は無限として扱われます<br>
     * 既存の記事情報と識別情報が一致した場合，内容が上書きされます．複数の記事情報と一致してしまった場合，一致したすべての記事情報の本体情報が上書きされます．
     * 一致しなかった場合，新たに記事情報が追加されます */
    protected void write(Object[] identifier, Object information, long lifetime){
        Article arti=new Article();
        arti.setIdentifiers(identifier);
        arti.setInformation(information);
        arti.setCreatedTime(this.currentTime);
        arti.setLifetime(lifetime);
        List<Article> sameTypeArticles=this.readArticles(identifier);
        if (sameTypeArticles.isEmpty()){
            this.write(arti);
        }else{
            // このシチュエーション存在するかなあ...
            for (Article target:sameTypeArticles){
                target.setInformation(information);
                target.setCreatedTime(arti.getCreatedTime());
            }
        }
    }

    /** 板の記事情報を削除します */
    protected boolean erase(Article article){
        boolean result=this.articles.remove(article);
        return result;
    }

    /** 板の記事情報を識別情報で一括して削除します */
    protected int erase(Object[] identifiers){
        int num=0;
        for(Article target:new ArrayList<Article>(this.articles)){
            if (this.isMatch(target.getIdentifiers(), identifiers)){
                this.articles.remove(target);
                num++;
            }
        }
        return num;
    }

    /** 引数で与えた2つの識別情報が一致するかを調査します */
    private boolean isMatch(Object[] id1, Object[] id2){
        boolean result=true;
        for (int i=0;i<id1.length;i++){
            if (id2.length<=i){
                break;
            }
            // nullは無条件で一致とみなす
            if (id1[i]==null || id2[i]==null){
                continue;
            }
            // 一致しない要素があったら同じではない
            if (!id1[i].equals(id2[i])){
                result=false;
                break;
            }
        }
        return result;
    }

    /** 板の記事情報を新しい記事情報で上書きします<br>
     * 上書きすべき記事情報が見つからなかった場合，単に追加します． */
    protected boolean replace(Article newInfo, Article oldInfo){
        boolean result=this.articles.remove(oldInfo);
        this.articles.add(newInfo);
        return result;
    }

    /** 板の記事情報を更新します<br>
     * 引数として論理時刻を指定すると，その時刻までに消滅する記事情報が削除されます．<br>
     * 現在時刻を更新するため，インスタンスを生成したら最初に必ず呼んでください．*/
    protected void update(long logicalTime){
        this.currentTime=logicalTime;
        for(Article arti:new ArrayList<Article>(this.articles)){
            long limit=arti.getLifetime()+arti.getCreatedTime();
            // 寿命が切れた情報を削除
            if (arti.getLifetime()!=IMMORTAL && limit<logicalTime){
                this.articles.remove(arti);
            }
        }
    }

    @Override
    public SaveDataPackage saveStatus() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }


}
