package jp.ac.nihon_u.cit.su.furulab.fuse.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JToolBar;

/** GUI操作用のツールバーです */
public class FuseToolBar extends JToolBar implements ActionListener{
    private FusePanel simPanel;

    /** コンストラクタです */
    public FuseToolBar(FusePanel panel) {
        simPanel=panel;
        System.out.println("DEBUG: Add toolbar");
    }

    /** ボタンを追加します */
    public void addButton(FuseButton button){
        System.out.println("DEBUG: Add Button to toolbar");
        // ボタンのパネルが設定されていなかったら設定しておく
        if (button.getPanel()==null){
            button.setPanel(simPanel);
        }
        button.addActionListener(this);
        this.add(button);
    }

    /** ボタンを押された際の処理です */
    public void actionPerformed(ActionEvent e) {
        // ボタンのactionメソッドを利用します
        System.out.println("DEBUG: toolbar pushed "+e.getSource());
        if (e.getSource() instanceof FuseButton){
            ((FuseButton)e.getSource()).action();
        }
    }
}
