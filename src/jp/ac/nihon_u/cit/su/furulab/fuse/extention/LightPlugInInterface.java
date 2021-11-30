package jp.ac.nihon_u.cit.su.furulab.fuse.extention;

import java.util.List;

public interface LightPlugInInterface{

  /** プラグインの名前を取得します */
  public String getName();

  /** プラグインのバージョンを取得します<br>
   * バージョンはint[3]で返り，メジャー，サブマイナー，マイナーバージョンを示します */
  public int[] getVersion();

  /** プラグインの識別クラスを取得します */
  public PlugInUnifier getUnifier();

  /** プラグインの依存情報を取得します */
  public List<PlugInUnifier> getDependence();

  /** 接続されたサーバクラスを取得します<br>
   * これを利用して機能を使うことになります */
  public LightPlugInServer getServer();

  /** このプラグインが登録されたサーバクラスを教えます */
  public void setServer(LightPlugInServer server);

  /** プラグインが初期化されるときのコールバック処理です */
  public void init();

  /** プラグインが更新された場合のコールバック処理です */
  public void update();

  /** プラグインが終了されるときのコールバック処理です */
  public void finish();


}
