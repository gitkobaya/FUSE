package jp.ac.nihon_u.cit.su.furulab.fuse.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FuseWindow;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.*;

/** 操作用パネルです<br>*/
public class FuseControler extends JPanel implements ActionListener,ChangeListener{
    private int width=160;
    private JLabel counter=new JLabel("",JLabel.RIGHT);
    private JLabel timeViewer=new JLabel("",JLabel.RIGHT);
    private ButtonPanel buttons=new ButtonPanel(this);
    private JLabel rateView=new JLabel("",JLabel.RIGHT);
    private SliderPanel slider=new SliderPanel(this);
    private SimulationEngine engine;
    private FuseWindow window;

    private long prevCountTime=0; // 前回ゲーム率を計算した時刻
    private long prevLogicalTime=0; // 前回ゲーム率を計算した論理時刻

    private int logicalSecond=1000; // 1秒に対応する論理時間です

    /** コンストラクタです */
    public FuseControler(SimulationEngine eng, FuseWindow win) {
        this.engine=eng;
        this.window=win;

        //this.setLayout(new FlowLayout());
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(0.0f);

        // 間隔調整用不可視オブジェクト
        Component adjust=Box.createRigidArea(new Dimension(width,64));

        // カウンタの設定
        TitledBorder borderCounter = new TitledBorder(
            new EtchedBorder() , "Logical Time" , TitledBorder.LEFT , TitledBorder.TOP
        );
        counter.setPreferredSize(new Dimension(width,48));
        counter.setBackground(Color.white);
        counter.setBorder(borderCounter);
        this.add(counter);

        // 時刻表示の設定
        TitledBorder borderTimer = new TitledBorder(
            new EtchedBorder() , "Time" , TitledBorder.LEFT , TitledBorder.TOP
        );
        this.timeViewer.setPreferredSize(new Dimension(width,48));
        this.timeViewer.setBackground(Color.white);
        this.timeViewer.setBorder(borderTimer);
        this.add(timeViewer);

        //this.add(adjust);

        // ボタンパネルの設定
        TitledBorder borderButtons = new TitledBorder(
            new EtchedBorder() , "Play" , TitledBorder.LEFT , TitledBorder.TOP
        );
        this.buttons.setPreferredSize(new Dimension(width,64));
        this.buttons.setBorder(borderButtons);
        this.add(buttons);

        // ゲーム率表示の設定
        TitledBorder gameRate=new TitledBorder(
            new EtchedBorder() , "Current Game Rate" , TitledBorder.LEFT , TitledBorder.TOP
        );
        this.rateView.setPreferredSize(new Dimension(width,48));
        this.rateView.setBorder(gameRate);
        this.add(rateView);

        // スライダーパネルの設定
        TitledBorder borderSlider = new TitledBorder(
            new EtchedBorder() , "Game Rate" , TitledBorder.LEFT , TitledBorder.TOP
        );
        this.slider.setPreferredSize(new Dimension(width,64));
        this.slider.setBorder(borderSlider);
        this.add(slider);

        //this.engine.setSimwait((int)(1000/slider.getValue()));

        this.validate();
        this.setPreferredSize(new Dimension(width,480));
    }

    @Override
    protected void paintComponent(Graphics g) {

        // JLabelは自動でうまく調整されない様子
        this.counter.setText(String.valueOf(engine.getLogicalTime()));
        this.counter.setLocation(8, 0);
        this.counter.setSize(new Dimension(width-16,48));

        this.timeViewer.setText(TimeConverter.getTimeAsString(engine.getLogicalTime()));
        this.timeViewer.setLocation(8, 50);
        this.timeViewer.setSize(new Dimension(width-16,48));

        // 現在のゲーム率をスライダに反映
        int wait=this.engine.getSimwait();
        //this.slider.setGameRate(1000/wait);

        // ゲーム率計算
        long current=System.currentTimeMillis();
        long currentLogical=this.engine.getLogicalTime();
        if (this.prevCountTime+1000<current){
            double rate=((double)((double)currentLogical-this.prevLogicalTime))/logicalSecond;
            this.rateView.setText(new Double(rate).toString());
            this.prevCountTime=current;
            this.prevLogicalTime=currentLogical;
        }
        this.rateView.setLocation(8, 152);
        this.rateView.setSize(new Dimension(width-16,48));

        super.paintComponent(g);
    }

    /** ボタンが押された際に呼び出されます */
    public void actionPerformed(ActionEvent e) {
        // 操作用にシミュレーションパネルを取得しておく
        FusePanel panel=window.getFusePanel();

        //一時停止ボタンを押された場合
        if(e.getSource()==buttons.getPauseButton()){
            System.out.println("DEBUG: pause!");
            engine.pause();
        }

        //再開ボタンを押された場合
        if(e.getSource()==buttons.getPlayButton()){
            System.out.println("DEBUG: resume!");

            OperationResume ope=new OperationResume();
            //panel.markersClear();

            panel.getOpManager().exec(ope);
            //panel.getOpManager().clear();
            //engine.resume();
        }

        //停止ボタンを押された場合
        if(e.getSource()==buttons.getInitButton()){
            System.out.println("DEBUG: stop and init!");

            // ポーズする
            engine.pause();

            // 現在シミュレーション実行中なら、undoする
            if (panel.getOpManager().showLatestExec() instanceof OperationResume){
                panel.getOpManager().undo();
            }

            //window.restart();
        }
    }

    /** 値が変わった場合に呼び出されます */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource()==slider.getSlider()){
            double value=slider.getSlider().getValue();
            slider.setGameRate(value);
        }
        if (e.getSource()==slider.getSpinner()){
            double value=(Double) slider.getSpinner().getValue();
            slider.setGameRate(value*100);
        }
        engine.setSimwait((int)(1000/slider.getValue()));

        // スタックオーバーフロー防止の微小ウェイト
        try{
            Thread.sleep(5);
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }
}

/** ボタンを配置されたパネルです */
class ButtonPanel extends JPanel{

    private JButton buttonPlay=new JButton(" > ");
    private JButton buttonPause=new JButton("| | ");
    private JButton buttonInit=new JButton("□ ");

    public ButtonPanel(ActionListener parent) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // ボタンの設定
        buttonPlay.setPreferredSize(new Dimension(32,24));
        buttonPlay.addActionListener(parent);
        buttonPause.setPreferredSize(new Dimension(32,24));
        buttonPause.addActionListener(parent);
        buttonInit.setPreferredSize(new Dimension(32,24));
        buttonInit.addActionListener(parent);
        this.add(buttonPlay);
        this.add(buttonPause);
        this.add(buttonInit);
    }

    public JButton getPlayButton(){
        return buttonPlay;
    }

    public JButton getPauseButton(){
        return buttonPause;
    }

    public JButton getInitButton(){
        return buttonInit;
    }
}

/** ゲーム率指定用クラス */
class SliderPanel extends JPanel{
    private JSlider slider=new JSlider(10,100000,10000); // 100倍された値
    private SpinnerNumberModel spModel=new SpinnerNumberModel(100,0.1,1000,0.1);
    private JSpinner spinner=new JSpinner(spModel);

    public SliderPanel(FuseControler cont) {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        slider.addChangeListener(cont);
        spinner.addChangeListener(cont);

        this.add(slider);
        this.add(spinner);
    }

    /** スライダーオブジェクトを取得します */
    public JSlider getSlider(){
        return slider;
    }

    /** スピナーオブジェクトを取得します */
    public JSpinner getSpinner(){
        return spinner;
    }

    /** 値を取得します */
    public double getValue(){
        return (Double) spinner.getValue();
    }

    /** 値をセットします */
    public void setGameRate(double gameRate){
        slider.setValue((int)gameRate);
        spinner.setValue(gameRate/100);
    }

    @Override
    public void setPreferredSize(Dimension preferredSize){
       super.setPreferredSize(preferredSize) ;
       int width=preferredSize.width;
       slider.setPreferredSize(new Dimension(width-16,48));
       spinner.setPreferredSize(new Dimension(width-16,32));
    }
}