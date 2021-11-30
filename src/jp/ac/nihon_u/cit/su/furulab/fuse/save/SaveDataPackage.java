package jp.ac.nihon_u.cit.su.furulab.fuse.save;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;

/** ファイルに保存すべき情報を抽象化したクラスです */
public class SaveDataPackage {
    private static int NO_HASH=0;
    private Savable dataOwner; // このデータの対象オブジェクト
    private String dataName="noname"; // このデータの識別名
    private String ownerClassName;
    private int ownerHash; // このデータのハッシュ値
    private long ownerId=-1;
    private boolean dummy=false; // ダミーフラグ
    private List<SaveDataPackage> childPackages=new ArrayList<SaveDataPackage>();
    private List<DataContainer> containers=new ArrayList<DataContainer>();

    /** コンストラクタの時点でデータのオーナーを設定します<br>
     * たいていの場合はthisを指定することになるはずです */
    public SaveDataPackage(Savable owner) {
        this.dataOwner=owner;
        if (owner!=null){
            this.ownerHash=owner.hashCode();
        }

        if (owner!=null){
            this.setOwnerClassName(owner.getClass().getName());
        }

        if (owner instanceof VirtualObject){
            this.ownerId=((VirtualObject)owner).getId();
        }
    }

    /** オーナークラスを取得します */
    public Class<Savable> getOwnerClass(){
        Class<Savable> result=null;
        try{
            result=(Class<Savable>)Class.forName(this.ownerClassName);
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /** オーナークラス名の文字列を取得します */
    public String getOwnerClassName(){
        return ownerClassName;
    }

    /** オーナークラス名の文字列です<br>
     * シミュレーションを復元する際にシステムが使います<br>
     * このメソッドで入力する必要はほとんどありません */
    public void setOwnerClassName(String str){
        ownerClassName=str;
    }

    /** このデータのオーナー名を取得します */
    public String getName(){
       return this.dataName;
    }

    /** オーナーデータ名を設定します */
    public void setName(String name){
        this.dataName=name;
    }

    /** データのオーナーを取得します */
    public Savable getOwner(){
        return dataOwner;
    }

    /** オーナーのIDを取得します */
    public long getOwnerId(){
        return ownerId;
    }

    /** オーナーのIDを設定します */
    public void setOwnerId(long id){
        ownerId=id;
    }

    /** オーナーのハッシュ値を取得します */
    public int getOwnerHash(){
        return this.ownerHash;
    }

    /** オーナーのハッシュ値を設定します */
    public void setOwnerHash(int hash){
        this.ownerHash=hash;
    }

    /** このパッケージが中身のないダミーデータかどうかを取得します */
    public boolean isDummy(){
        return this.dummy;
    }

    /** このパッケージが中身のないダミーデータかどうかを設定します */
    public void setDummy(){
        this.dummy=true;
    }

    /** データをすべて取得します */
    public List<DataContainer> getAllData(){
        return containers;
    }

    /** データを添え字付きでひとつ取得します */
    public Object getData(int i){
        return containers.get(i);
    }

    /** データを名前で検索します */
    public Object getData(String name){
        Object result=null;
        for(DataContainer container:containers){
            if (container.getName().equals(name)){
                result=container.getData();
                break; // 見つけた時点でループを抜ける(万一同じ名前があったら最初のものにヒット)
            }
        }
        // コンテナの中に見つからなかったらchildPackageを探す
        if (result==null){
            for (SaveDataPackage pack:this.getAllChildren()){
                if (pack.getName().equals(name)){
                    result=pack.restore();
                    break;
                }
            }
        }
        return result;
    }

    /** データを追加します */
    public void addData(DataContainer cont){
        containers.add(cont);
    }

    /** データを追加します */
    public void addData(String name, Object value){
        if (value instanceof Savable){
            // Savableクラスだった場合，childPackageに追加する
            SaveDataPackage child=((Savable)value).saveStatus();
            child.setName(name);
            this.addChildPackage(child);
        }else{
            // Savableクラスでない場合，単に追加する
            DataContainer container=new DataContainer(name,value);
            this.addData(container);
        }
    }

    /** 子データを追加します */
    public void addChildPackage(SaveDataPackage pack){
        this.childPackages.add(pack);
        SaveDataPackage dummy=new SaveDataPackage(pack.getOwner()); // ダミーキャッシュを作っておく
        dummy.setDummy();
        SaveDataManager.putPakage(pack.getOwnerHash(), dummy);
    }

    /** 子データを追加します<br>
     * 複数のオブジェクトから参照されるオブジェクトが発生するようなケースでは、必ずこのメソッドを利用してください */
    public void addChildPackage(Savable savableObject){
        SaveDataPackage dummy=SaveDataManager.getPackage(savableObject.hashCode());
        if (dummy==null){
            // まだそのデータのパッケージが作られていない場合
            dummy=new SaveDataPackage(savableObject); // 一回ダミーを作りまして
            dummy.setDummy();
            SaveDataManager.putPakage(savableObject.hashCode(), dummy); // そのダミーをキャッシュに登録しまして
            SaveDataPackage real=savableObject.saveStatus(); // 自分の方には実体を作っておきます
            this.childPackages.add(real);
            //System.out.println("DEBUG: real pack: "+real.getOwnerClassName()+" :"+real.getOwnerHash() );
        }else{
            // 既にそのデータのダミーパッケージが存在する場合
            this.childPackages.add(dummy); // ダミーを引っ張ってきます
        }
    }

    /** 子データのリストを取得します */
    public List<SaveDataPackage> getAllChildren(){
        return childPackages;
    }

    /** このデータが入っているオブジェクトを復元します<br>
     * ただし、既に復元済みの内容であればそれを返します<br>
     * 複数から参照されるオブジェクトが発生するようなケースでは、必ずこのメソッドを利用してください */
    public Savable restore(){
        int hash=this.getOwnerHash();
        System.out.println("DEBUG: restore check :"+this.getOwnerClassName()+" hash:"+hash);

        Savable dataObject=SaveDataManager.getObject(hash); // 同じハッシュのオブジェクトがあれば生成せずにもらう

        // 同じハッシュのオブジェクトが存在しなかった場合
        try{
            if (dataObject==null){
                if (this.isDummy()){
                    System.err.println("ERROR: Can not restore.This is Dummy Package");
                    System.exit(-1);
                }

                // まだ展開していないものならば
                Class<Savable> ownerClass=(Class<Savable>)Class.forName(this.ownerClassName);
                dataObject=(Savable)ownerClass.newInstance();
                if (hash==NO_HASH){
                    hash=dataObject.hashCode();
                    this.setOwnerHash(hash);
                }
                SaveDataManager.putObject(this.getOwnerHash(), dataObject); // キャッシュに登録する
                System.out.println("DEBUG: restore :"+this.getOwnerClassName()+" hash:"+hash);
                dataObject.restoreStatus(this);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return dataObject;
    }
}
