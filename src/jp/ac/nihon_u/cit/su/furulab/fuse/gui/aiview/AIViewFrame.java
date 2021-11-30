package jp.ac.nihon_u.cit.su.furulab.fuse.gui.aiview;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputListener;

import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseTaskManager;

/** CaSPAのデバッグウィンドウと関連機能をひとまとめにしたフレームです */
public class AIViewFrame extends JFrame {

    private FuseAIView view;

    public AIViewFrame(FuseTaskManager manager) {
        this.setTitle("CaSPA Viewer :"+manager.getAgent().getName()+" ID:"+manager.getAgent().getId());
        this.view=new FuseAIView(manager);
        JScrollPane scroll=new JScrollPane(this.view,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.getContentPane().add(scroll, BorderLayout.CENTER);
        this.view.setScrollPane(scroll);
        this.setSize(640, 480);
    }

    /**AIの状況を最新版に更新します．<br>
     * このメソッドを呼び出した瞬間のタスクマネージャの状態を取得します．
     * 現時点でこのメソッドはスレッドセーフではないため，タスクマネージャの駆動中に呼び出した場合はエラーを発生する可能性があります．
     * かならずタスクマネージャの評価が終了してから呼び出してください．また，このメソッドの実行中にタスクマネージャの駆動を開始しないように注意してください．*/
    public void refreshStatus(){
        this.view.refreshStatus();
    }
}
