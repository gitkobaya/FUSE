package samples;
import java.awt.BorderLayout;

import jp.ac.nihon_u.cit.su.furulab.fuse.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.examples.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.*;

/** 動作確認用のサンプルプログラムです．
 * 2D専用であるため，動作にK7は必要ありません */
public class FuseSample {

    /** メインクラス */
    public static void main(String[] args){
        SimpleMeshGeometry geo=new SimpleMeshGeometry(100, 100); // 地形の作成(100マスｘ100マス)
        geo.setMeshCellSize(10,10); // 地形のメッシュサイズの指定(10m x 10m)
        Environment env=new Environment(geo); // 環境の作成
        SimulationEngine engine=new SimulationEngine(env);    // シミュレーションエンジンの作成

        // 画面の設定関係
        FusePanelSimpleMesh panel=new FusePanelSimpleMesh(engine); // 表示パネルの作成
        KeyAndMouseListner2D kam=new KeyAndMouseListner2D(panel); // キー入力リスナーの設定
        panel.setKAMListner(kam); // キー入力リスナーの登録
        FuseWindow window=new FuseWindow(panel); // 表示パネルを乗せる枠組みを設定．コントロールパネルなどは標準で搭載済み

        SimpleAgent agent1=new SimpleAgent(); // 青エージェント作成
        agent1.setPosition(100,100,5);
        agent1.setSide(Side.BLUE_FORCE);
        engine.addAgent(agent1);

        SimpleAgent agent2=new SimpleAgent(); // 赤エージェント作成
        agent2.setPosition(900,900,5);
        agent2.setSide(Side.RED_FORCE);
        engine.addAgent(agent2);

        // エージェントクラスと描画クラスの関係をここで定義
        panel.addObjectDrawerCreateRule(SimpleAgent.class, SimpleAgentDrawer.class);

        FuseControler ctrl=new FuseControler(engine, window); // 操作パネル追加
        window.add(ctrl,BorderLayout.EAST);
        window.pack();
        window.setRefleshInterval(30); // 描画周期設定
        window.setVisible(true); // 画面表示

        window.setRefleshInterval(30); // 描画周期設定

        engine.start(1000);        // シミュレーション開始
    }
}