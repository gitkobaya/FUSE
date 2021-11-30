package jp.ac.nihon_u.cit.su.furulab.fuse.extention;

import java.util.ArrayList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;

/** 軽量プラグインはこのクラスを継承します<br>
 * 軽量プラグインは単一のクラスファイルによって構成されます． */
public abstract class LightPlugIn implements LightPlugInInterface{
    /** このプラグインの識別クラスです */
    private PlugInUnifier unifier=new PlugInUnifier();

    /** このプラグインが依存するプラグインの情報です */
    private List<PlugInUnifier> depends=new ArrayList<PlugInUnifier>();

    /** このプラグインが登録されたサーバーです */
    private LightPlugInServer myServer;

    /** プラグインの名前を取得します */
    @Override
    public String getName(){
        return this.unifier.getName();
    }

    /** プラグインの名前を設定します */
    protected void setName(String name){
        unifier.setName(name);
    }

    /** プラグインのバージョンを取得します */
    @Override
    public int[] getVersion(){
        return this.unifier.getVersion();
    }

    /** プラグインの識別クラスを取得します */
    @Override
    public PlugInUnifier getUnifier(){
        return this.unifier;
    }

    /** プラグインの依存情報を取得します */
    @Override
    public List<PlugInUnifier> getDependence(){
        return this.depends;
    }

    /** このプラグインが登録されたサーバーを取得します */
    @Override
    public LightPlugInServer getServer(){
        return this.myServer;
    }

    @Override
    public void setServer(LightPlugInServer server){
        this.myServer=server;
    }

}
