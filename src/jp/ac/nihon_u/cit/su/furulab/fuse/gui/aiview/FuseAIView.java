package jp.ac.nihon_u.cit.su.furulab.fuse.gui.aiview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.MouseInputListener;

import jp.ac.nihon_u.cit.su.furulab.fuse.ai.Article;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.Condition;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseAIBase;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseAction;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseTask;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseTaskManager;

/** AIの状況をビジュアルに取得するためのクラスです<br>
 * プログラマはウィンドウとなる適当なSwingコンポーネントを用意して，このパネルを貼り付けます． */
public class FuseAIView extends JLayeredPane implements MouseInputListener, KeyListener{

    // パーツを並べる場合にずらす幅(PowerPointを思い浮かべてください)
    private static final int INITIAL_OFFSET_X=16;
    private static final int INITIAL_OFFSET_Y=16;

    private Map<FuseTask,TaskPart> taskParts=new HashMap<FuseTask,TaskPart>(); // CaSPAパーツ
    private FuseTaskManager manager=null;

    private ReentrantLock lock=new ReentrantLock();

    private MouseInputListener listner;

    /** タスクグラフを完全表示するかどうかの指定です */
    private boolean fullDisplayMode=true;

    // 左右にどれだけの大きさになったか
    private int xMax=0;
    private int yMax=0;

    /** コンストラクタを呼び出す際にタスクマネージャを取得します */
    public FuseAIView(FuseTaskManager manager) {
        this.manager=manager;
        this.setBackground(Color.WHITE);
        this.setLayout(null); //配置を自分で処理するのでnullを指定する
        if (manager!=null){
            this.refreshStatus();
        }
        this.listner=this;
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);
        this.setFocusable(true);
    }

    /** 完全表示モード化プランニング対象のみを表示するのかを取得します */
    public boolean isFullDisplayMode(){
        return this.fullDisplayMode;
    }

    /** 完全表示モードかプランニング対象のみを表示するのかを切り替えを実施します */
    public void setFullDisplayMode(boolean flag){
        if (this.fullDisplayMode==true && flag==false){
            FuseTask root=this.manager.getGoalTask();
            for (TaskPart tp:this.taskParts.values()){
                FuseTask task=(FuseTask)tp.getAIBase();
                if (task!=root && !root.getPlan().contains(task)){
                    tp.setVisible(false);
                }
            }
        }else if(this.fullDisplayMode==false && flag==true){
            for (TaskPart tp:this.taskParts.values()){
                tp.setVisible(true);
            }
        }

        this.fullDisplayMode=flag;
    }

    /** AIの状況を最新版に更新します．<br>
     * このメソッドを呼び出した瞬間のタスクマネージャの状態を取得します．
     * 現時点でこのメソッドはスレッドセーフではないため，タスクマネージャの駆動中に呼び出した場合はエラーを発生する可能性があります．
     * かならずタスクマネージャの評価が終了してから呼び出してください．<br>
     * また，このメソッドの実行中にタスクマネージャの駆動を開始しないように注意してください． */
    public void refreshStatus(){
        this.lock.lock();
        try{
            this.clearParts();
            FuseTask root=this.manager.getGoalTask(); // かならずCaSPAの評価が終わってから呼び出すこと
            this.removeAll(); // いったん表示用コンポーネントを全て外す

            if (root!=null){
                // ルートを起点にしてにタスク解析
                TaskPart rootTaskPart=this.analyzeTask(root);

                // GUIパーツが揃ったのでパネルに登録していく
                rootTaskPart.setLocation(INITIAL_OFFSET_X, INITIAL_OFFSET_Y);
                this.add(rootTaskPart, JLayeredPane.DEFAULT_LAYER);
                this.addTaskPart(rootTaskPart);
            }

            this.setPreferredSize(new Dimension(this.xMax, this.yMax) );
            this.revalidate();
        }finally{
            this.lock.unlock();
        }
        System.out.println("DEBUG: Size x:"+this.getWidth()+" y:"+this.getHeight());
        this.repaint();
    }

    /** ルートからたどってタスク表示オブジェクトを登録していきます */
    protected void addTaskPart(TaskPart tPart){
        int x=tPart.getX();
        int y=tPart.getY();

        for(ConditionPart cPart:tPart.getConditionParts()){
            int yOffset=0;
            List<FuseTask> children=((Condition)cPart.getAIBase()).getTasks();
            List<TaskPart> addingTaskParts=new ArrayList<TaskPart>();
            int counter=0;
            for (FuseTask childTask:children){
                TaskPart t=this.taskParts.get(childTask);
                Component p=t.getParent();
                if (p!=this){
                    int scX=x+tPart.getEstimateWidth()+INITIAL_OFFSET_X+counter*4;
                    int scY=tPart.getLocation().y+cPart.getLocation().y+yOffset;
                    System.out.println("DEBUG: "+childTask.debugInfo()+" x:"+scX+" y:"+scY);
                    t.setLocation(scX, scY);
                    this.add(t,JLayeredPane.DEFAULT_LAYER);
                    addingTaskParts.add(t);

                    // リスナー登録
                    t.addMouseListener(this);
                    t.addMouseMotionListener(this);

                    if (this.xMax<scX+t.getWidth()){
                        this.xMax=scX+t.getWidth();
                    }
                    if (this.yMax<scY+t.getHeight()){
                        this.yMax=scY+t.getHeight();
                    }
                    counter++;
                }
                yOffset+=INITIAL_OFFSET_Y;
            }
            for (TaskPart tp:addingTaskParts){
                this.addTaskPart(tp);
            }
        }
    }

    /** タスクの分析を実施し，GUI部品を生成します */
    public TaskPart analyzeTask(FuseTask task){
        TaskPart partTask=(TaskPart)this.taskParts.get(task);

        // 既に登録されているタスクか確認
        if (partTask==null){

            partTask=new TaskPart(task);
            partTask.setMouseInputListner(this.listner);
            // タスクをGUIパーツとして登録
            this.addGuiTaskPart(partTask);

            // 条件パーツを作ってタスクパーツに追加
            for (Condition cond:task.getConditons()){
                ConditionPart partCond=new ConditionPart(cond);
                for (FuseTask childTask:cond.getTasks()){
                    TaskPart childTaskPart=this.analyzeTask(childTask);
                    childTaskPart.addParent(partCond);
                }
                partTask.add(partCond); // タスクに条件パーツの追加
            }

            // アクションパーツを作ってタスクパーツに追加
            FuseAction act=task.getAction();
            if (act!=null){
                ActionPart partAct=new ActionPart(act);
                partTask.add(partAct);
            }

        }

        // 登録済みのタスクだったらそのパーツを返す
        return partTask;
    }

    /** 指定されたAIエレメントに対応するGUIパーツを返します */
    public GUIPart getGuiPart(FuseAIBase aiElement){
        GUIPart result=this.taskParts.get(aiElement);
        return result;
    }

    /** GUIパーツを管理領域に追加します */
    public void addGuiTaskPart(TaskPart part){
        this.taskParts.put((FuseTask)part.getAIBase(), part);
    }

    /** GUIパーツの管理領域を初期化します */
    public void clearParts(){
        this.taskParts.clear();
    }


    /** ここでリンクの線を描画します<br>
     * paintメソッドのオーバーライドはあんまり推奨されないのですが，問答無用で上書きする必要があるためにこうなります． */
    @Override
    public void paint(Graphics g){
        if(!this.lock.isLocked()){

            super.paint(g);
            g.setColor(Color.BLACK);
            if (this.manager!=null && this.manager.getGoalTask()!=null ){
            List<FuseTask> history=this.manager.getGoalTask().getPlan();

            Collection<TaskPart> drawTasks=this.taskParts.values();
            for (TaskPart tPart:drawTasks){
                if (tPart!=this.taskParts.get(this.manager.getGoalTask()) && tPart.isVisible()){
                    int startX=tPart.getLocation().x;
                    int startY=tPart.getLocation().y+tPart.getHeight()/2;
                    for(GUIPart parentPart:tPart.getParentParts()){
                        int endX=parentPart.getParent().getLocation().x+parentPart.getLocation().x+parentPart.getWidth();
                        int endY=parentPart.getParent().getLocation().y+parentPart.getLocation().y+parentPart.getHeight()/2;

                        TaskPart parentTask=(TaskPart)parentPart.getParent();

                        if (parentTask.isVisible()){

                            // マウスクリックの時の処理
                            if (tPart.isMouseFocused()){
                                Graphics2D g2=(Graphics2D)g;
                                g2.setColor(Color.RED);
                                g2.setStroke(new BasicStroke(3));
                                g2.drawRect(parentTask.getX()+parentPart.getX(), parentTask.getY()+parentPart.getY(), parentPart.getWidth(), parentPart.getHeight());
                            }else if (parentTask.isMouseFocused()){
                                Graphics2D g2=(Graphics2D)g;
                                g2.setColor(Color.BLUE);
                                g2.setStroke(new BasicStroke(3));
                                g2.drawRect(tPart.getX(), tPart.getY(), tPart.getWidth(), tPart.getHeight());
                            }else{
                                Graphics2D g2=(Graphics2D)g;
                                g2.setColor(Color.BLACK);
                                g2.setStroke(new BasicStroke(1));
                            }

                            // ヒストリ調査
                            if (history.contains(tPart.getAIBase()) && history.contains(parentTask.getAIBase())
                                    && history.indexOf(tPart.getAIBase())<history.indexOf(parentTask.getAIBase()))
                            {
                                Graphics2D g2=(Graphics2D)g;
                                g2.setStroke(new BasicStroke(3));
                            }
                            g.drawLine(startX, startY, endX, endY);
                        }
                    }
                }
            }
        }
        }
        //System.out.println("DEBUG:paint!");
    }

    /** 現在の状況を表示します */
    @Override
    public void paintComponent(Graphics g){
        if (!this.lock.isLocked()){
            super.paintComponent(g);
        }
    }

    ////// ここから下は入力関係 //////

    private JScrollPane scroll=null;
    private boolean[] currentPushButton=new boolean[4];

    /** このパネルに関連付けられたスクロールペインを設定します<br>
     * これを設定していないとドラッグでのスクロールができません． */
    public void setScrollPane(JScrollPane scroll){
        this.scroll=scroll;
    }

    /** 該当するマウスボタンが押されているかどうかを判定します */
    public boolean isPush(int button){
        return this.currentPushButton[button];
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int currentX=e.getXOnScreen();
        int currentY=e.getYOnScreen();

        this.diffX=currentX-this.mouseX;
        this.diffY=currentY-this.mouseY;

        this.mouseX=currentX;
        this.mouseY=currentY;


        //System.out.println("DEBUG "+e.getSource());
        if (e.getSource() instanceof TaskPart){
            TaskPart tp=(TaskPart)e.getSource();
            int nowX=tp.getX();
            int nowY=tp.getY();

            tp.setLocation(nowX+this.diffX,nowY+this.diffY);
            this.repaint();
        }else if (e.getSource()==this && this.isPush(MouseEvent.BUTTON2)){
            if (this.scroll!=null){
                JViewport vp=this.scroll.getViewport();
                Point currentPoint=vp.getViewPosition();
                currentPoint.x-=this.diffX;
                currentPoint.y-=this.diffY;

                if (currentPoint.x<0){
                    currentPoint.x=0;
                }
                if (currentPoint.y<0){
                    currentPoint.y=0;
                }
                vp.setViewPosition(currentPoint);
            }
        }

    }

    int mouseX=0;
    int mouseY=0;
    int diffX=0;
    int diffY=0;
    @Override
    public void mouseMoved(MouseEvent e) {
        int currentX=e.getXOnScreen();
        int currentY=e.getYOnScreen();

        this.diffX=currentX-this.mouseX;
        this.diffY=currentY-this.mouseY;

        this.mouseX=currentX;
        this.mouseY=currentY;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.currentPushButton[e.getButton()]=true;
        if (e.getSource() instanceof TaskPart){
            TaskPart tp=(TaskPart)e.getSource();
            tp.setMouseFocus(true);
            tp.dumpInfo();
            this.moveToFront(tp);
        }else if (e.getSource()==this && e.getButton()==MouseEvent.BUTTON3){
            this.setFullDisplayMode(!this.isFullDisplayMode());
        }

        this.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.currentPushButton[e.getButton()]=false;
        if (e.getSource() instanceof TaskPart){
            TaskPart tp=(TaskPart)e.getSource();
            tp.setMouseFocus(false);
            this.repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getSource() instanceof TaskPart){
            TaskPart tp=(TaskPart)e.getSource();
            tp.setMouseFocus(false);
            this.repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO 自動生成されたメソッド・スタブ

    }

    /** キータイプの際処理 */
    @Override
    public void keyPressed(KeyEvent e) {
        // "A"のタイプで記憶ダンプ
        if (e.getKeyCode()==KeyEvent.VK_A){
            Collection<Article> articles=this.manager.getBoard().getAllArticles();
            for(Article art:new ArrayList<Article>(articles)){
                System.out.print("ARTICLE: Keys:[");
                for (Object key:art.getIdentifiers()){
                    System.out.print(key+", ");
                }
                System.out.print("]  Information:");
                Object info=art.getInformation();
                if (info.getClass().isArray()){
                    Object[] infoArray=(Object[])info;
                    System.out.print("[");
                    for(Object i:infoArray){
                        System.out.print(i+",");
                    }
                    System.out.print("]");
                }else{
                    System.out.print(info);
                }
                System.out.print(" Life:"+art.getLifetime()+" Create Time:"+art.getCreatedTime());
                System.out.print("\n");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO 自動生成されたメソッド・スタブ

    }
}

