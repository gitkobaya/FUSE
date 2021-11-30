package jp.ac.nihon_u.cit.su.furulab.fuse.extention;

import java.util.HashMap;
import java.util.Map;

/** 軽量プラグインの受け手となるクラスです<br>
 * プラグインの依存関係を調査し，登録します */
public class LightPlugInServer {
    private Map<String, LightPlugInInterface> plugInMap=new HashMap<String, LightPlugInInterface>();

    /** 名前でプラグインを指定して取得します */
    public LightPlugInInterface getPlugIn(String name){
        return this.plugInMap.get(name);
    }

    /** プラグインの依存性を確認します<br>
     * 既に登録されているプラグイン以外のものに依存していた場合，falseが返ります */
    public boolean checkDependence(LightPlugInInterface plugin){
        boolean result=true;
        for(PlugInUnifier depend:plugin.getDependence()){
            // 引数で指定されたプラグインの依存関係をチェック
            boolean check=false;
            for (LightPlugInInterface p:this.plugInMap.values()){
                // 既に登録されているプラグインを確認
                if (depend.isCompatible(p.getUnifier())){
                    check=true;
                    break;
                }
            }
            if (check==false){ // 登録されているプラグインでは依存関係を解決できなかった
                result=false;
                break;
            }
        }
        return result;
    }

    /** プラグインを登録します<br>
     * プラグインの登録に成功すればtrueが，失敗すればfalseが返ります */
    public boolean addPlugIn(LightPlugInInterface plugIn){
        boolean result=false;

        if (!this.plugInMap.containsKey(plugIn.getName())){ // 重複登録しようとしたら無条件で失敗
            if (this.checkDependence(plugIn)){  // 依存関係が解決できたらマップに追加
                this.plugInMap.put(plugIn.getName(), plugIn);
                plugIn.setServer(this);
                result=true;
            }
        }
        return result;
    }

    /** 登録されているプラグインの初期化処理を実施します */
    public void initAllPlugins(){
        for (LightPlugInInterface plugin:this.plugInMap.values()){
            plugin.init();
        }
    }

    /** 登録されているプラグインを更新します */
    public void updateAllPlugins(){
        for (LightPlugInInterface plugin:this.plugInMap.values()){
            plugin.update();
        }
    }

    /** 登録されているプラグインの終了処理を実施します */
    public void finishAllPlugins(){
        for (LightPlugInInterface plugin:this.plugInMap.values()){
            plugin.finish();
        }
    }
}
