package jp.ac.nihon_u.cit.su.furulab.fuse.gui;

import java.awt.BorderLayout;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.plaf.FileChooserUI;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** ビュワーのウィンドウクラスです<br>
 * BordarLayoutを採用しています */
public class FuseWindow extends JFrame implements ActionListener{
    private FusePanel panel;
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenu menuEdit;
    private JMenu menuView;
    private JMenu menuSetting;
    private JMenuItem menuItemSave;
    private JMenuItem menuItemLoad;
    private JMenuItem menuItemCheck;

    private SimulationEngine engine;
    private Refresher reflesh; // 描画更新用スレッド

    private SaveDataPackage startStatus; // データ読み込み時のシミュレーション状況

    /** コンストラクタ */
    public FuseWindow(FusePanel pnl) {
        panel=pnl;
        this.setLayout(new BorderLayout());
        this.add("Center",panel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        engine=pnl.getSimulationEngine();
        this.setTitle("FUSE ver."+engine.getVersion());

        // メニューバーの生成
        menuBar=new JMenuBar();
        menuFile=new JMenu("ファイル");
        menuBar.add(menuFile);

        menuItemSave=new JMenuItem("シナリオ保存");
        menuItemSave.addActionListener(this);
        menuFile.add(menuItemSave);

        menuItemLoad=new JMenuItem("シナリオ読み込み");
        menuItemLoad.addActionListener(this);
        menuFile.add(menuItemLoad);

        menuItemCheck=new JMenuItem("セーブデータチェック");
        menuItemCheck.addActionListener(this);
        menuFile.add(menuItemCheck);

        menuEdit=new JMenu("編集");
        menuBar.add(menuEdit);

        menuView=new JMenu("表示");
        menuBar.add(menuView);

        menuSetting=new JMenu("設定");
        menuBar.add(menuSetting);
        this.setJMenuBar(menuBar);

        // 起動時の状態でいったんセーブ
        //startStatus=engine.saveStatus();

        // 初期状態では停止させておきます
        engine.pause();
        this.pack();
        this.setSize(640, 480);
        this.setRefleshInterval(50); // 標準では50msec
    }

    /** セルフリフレッシュ周期をミリ秒単位で設定します<br>
     * ゼロまたはそれ以下の数値を指定した場合、リフレッシュを実施しません。 */
    public void setRefleshInterval(int millisecond){
        if (reflesh!=null){
            reflesh.kill();
        }

        if (millisecond>0){
            reflesh=new Refresher(this,millisecond);
            reflesh.start();
        }
    }

    /** 表示用パネルを取得します */
    public FusePanel getFusePanel(){
        return panel;
    }

    /** メニューバーを取得します */
    public JMenuBar getFuseMenuBar(){
        return menuBar;
    }

    /** 表示メニューアイテムを取得します */
    public JMenu getMenuView(){
        return menuView;
    }

    /** シミュレーションエンジンを取得します */
    public SimulationEngine getSimulationEngine(){
        return panel.getSimulationEngine();
    }

    /** パネルが描画中かどうかを問い合わせます */
    public boolean isPainting(){
        return panel.isPainting();
    }

    /** シミュレーションを初期状態から再起動します */
    public void restart(){
        engine.pause(); // 順番を逆にするとエージェント0になって終了することがある
        engine.restoreStatus(startStatus);
    }

    /** ボタンが押された時の処理 */
    public void actionPerformed(ActionEvent e) {
        //System.out.println("DEBUG: pushed!");
        File file=null;

        // シミュレーションの保存
        if(e.getSource()==menuItemSave){
            JFileChooser chooser=new JFileChooser();
            chooser.setDialogTitle("シナリオファイルの保存");
            chooser.setApproveButtonText("保存");
            int selected = chooser.showSaveDialog(this);
            if (selected == JFileChooser.APPROVE_OPTION){
                file = chooser.getSelectedFile();
                engine.saveSimulationToFile(file);
            }
        }

        // シミュレーションの読み込み
        if(e.getSource()==menuItemLoad){
            JFileChooser chooser=new JFileChooser();
            chooser.setDialogTitle("シナリオファイルの読み込み");
            int selected = chooser.showOpenDialog(this);
            if (selected == JFileChooser.APPROVE_OPTION){
                file = chooser.getSelectedFile();
                engine.restoreSimulationFromFile(file);
            }
            startStatus=engine.saveStatus();
        }

        // セーブデータのチェック
        if (e.getSource()==menuItemCheck){
            engine.checkSaveData();
        }
    }
}

/** 描画ループのためのスレッド */
class Refresher extends Thread{
    private FuseWindow frame;
    private int waitTime;
    private boolean stopFlag=false;

    public Refresher(FuseWindow frm, int wait) {
        this.frame=frm;
        this.waitTime=wait;
    }

    public void run(){
        System.out.println("DEBUG: thread start");
        while(!stopFlag){
            try{
                Thread.sleep(this.waitTime);
            }catch(Exception e){
                e.printStackTrace();
                System.exit(-1);
            }

            // まだ描画中なら処理落ちさせる
            if (!frame.isPainting()){
                this.frame.repaint();
            }
        }
    }

    public void kill(){
        stopFlag=true;
    }
}
