package jp.ac.nihon_u.cit.su.furulab.fuse.gui;

import javax.swing.JButton;

/** FuseToolbarに登録するためのボタンです。<br>
 * 押された場合の処理を記述します */
public abstract class FuseButton extends JButton {
    private FusePanel simPanel;

    /** ボタンのコンストラクタです */
    public FuseButton(FusePanel panel) {
        simPanel=panel;
    }

    /** FusePanelを取得します */
    public FusePanel getPanel(){
        return simPanel;
    }

    /** FusePanelを設定します */
    public void setPanel(FusePanel panel){
        simPanel=panel;
    }

    /** ボタンが押された場合の処理を記述します */
    public abstract void action();

}
