package jp.ac.nihon_u.cit.su.furulab.fuse.gui.aiview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.ai.Condition;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseAIBase;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseTask;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.TaskByScript;

/** タスクを可視化するためのGUI部品です */
public class TaskPart extends GUIPart {

    public static final int DEFAULT_CONDITION_OFFSET_X=16;
    public static final int DEFAULT_CONDITION_OFFSET_Y=16;

    public static final int LARGER_X=24;
    public static final int LARGER_Y=8;

    private List<ConditionPart> childConditionParts=new ArrayList<ConditionPart>();
    private ActionPart actionPart=null;

    private boolean mouseFocusFlag=false;

    public TaskPart(FuseAIBase base) {
        super(base);
        this.setSize(GUIPart.DEFAULT_XSIZE+DEFAULT_CONDITION_OFFSET_X*2, GUIPart.DEFAULT_YSIZE);
    }

    /** AIエレメントパーツが追加された時の処理です<br>
     * コンポーネントとしてAIエレメントパーツが追加されたときのみこのメソッドが呼ばれ，その他の場合には親クラスのaddメソッドが呼ばれます．<br>
     * 単純に登録順に表示されるため，アクションは最後に登録してください． */
    public Component add(GUIPart comp){
        Component result=super.add(comp);
        if (comp instanceof ConditionPart){
            this.childConditionParts.add((ConditionPart)comp);
        }else if(comp instanceof ActionPart){
            this.actionPart=(ActionPart)comp;
        }
        comp.addParent(this);
        this.setSize(this.getEstimateWidth(), this.getEstimateHeight());
        return result;
    }

    /** このタスクパーツに属する条件パーツ一覧を取得します */
    public List<ConditionPart> getConditionParts(){
        return this.childConditionParts;
    }

    /** 配置等を整理します */
    public void align(){
        int xSize=DEFAULT_XSIZE+LARGER_X;
        int ySize=DEFAULT_YSIZE+LARGER_Y/2;
        ySize+=DEFAULT_CONDITION_OFFSET_Y;
        ySize+=this.getFieldDatum().size()*this.getFontSize(); // 変数分拡大

        for (GUIPart cPart:this.childConditionParts){
            cPart.setLocation(DEFAULT_CONDITION_OFFSET_X, ySize);
            ySize+=cPart.getEstimateHeight();
        }
        if (this.actionPart!=null){
            this.actionPart.setLocation(DEFAULT_CONDITION_OFFSET_X, ySize);
            ySize+=this.actionPart.getEstimateHeight();
        }
        ySize+=this.getFontSize()*2.5;
        ySize+=LARGER_Y/2;
        this.setSize(xSize, ySize);
    }

    /** マウスフォーカスを取得します */
    public boolean isMouseFocused(){
        return this.mouseFocusFlag;
    }

    /** マウスフォーカスを設定します */
    public void setMouseFocus(boolean flag){
        this.mouseFocusFlag=flag;
    }

    /** タスク情報をダンプします */
    public void dumpInfo(){
        // 情報をダンプ
        System.out.println("DEBUG: *****");
        System.out.println("DEBUG: Name:"+this.getName());
        for (String field:this.getFieldDatum()){
            System.out.println("DEBUG: field:"+field);
        }
        for (ConditionPart con:this.getConditionParts()){
            System.out.println("DEBUG: Condition:"+con.getName()+" :"+((Condition)con.getAIBase()).getTruth());
            for (String field:con.getFieldDatum()){
                System.out.println("DEBUG:  field:"+field);
            }
        }
        if (this.actionPart!=null){
            System.out.println("DEBUG: Action:"+this.actionPart.getName());
            for (String field:this.actionPart.getFieldDatum()){
                System.out.println("DEBUG:  field:"+field);
            }
        }
        System.out.println("DEBUG: ownCost:"+((FuseTask)this.getAIBase()).getOwnCost());
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.ORANGE);

        FuseTask origin=(FuseTask)this.getAIBase();
        if (origin.isImpossible()){
            g.setColor(Color.GRAY);
        }else if (origin.isAvailable()){
            g.setColor(Color.YELLOW);
        }

        g.fillRect(0, 0, this.getWidth()-1,this.getHeight()-1);
        g.setColor(g.getColor().darker());

        if (origin.isAvailable()){
            g.setColor(Color.RED);
            if (this.actionPart !=null && this.actionPart.getAIBase()==origin.getTaskManager().getLowestCostAction()){
                Graphics2D g2 = (Graphics2D)g;
                g2.setStroke(new BasicStroke(3));
            }
        }

        if (this.isMouseFocused()){
            g.setColor(Color.BLACK);
            Graphics2D g2 = (Graphics2D)g;
            g2.setStroke(new BasicStroke(4));
        }

        g.drawRect(0, 0, this.getWidth()-1,this.getHeight()-1);
        g.setColor(Color.black);
        int fSize=this.getFontSize();
        String taskName="NO NAME";
        if (this.getAIBase() instanceof TaskByScript){
            TaskByScript thisTask=(TaskByScript)this.getAIBase();
            taskName=thisTask.getName();
        }else{
            taskName=this.getAIBase().getClass().getSimpleName();
        }
        g.drawString(taskName, fSize, fSize);
        this.drawFields(fSize, fSize*2, g);

        g.drawString("Total COST:"+((FuseTask)this.getAIBase()).getCost(), fSize*2, this.getHeight()-(int)(fSize*1.5));
        g.drawString("Own COST:"+((FuseTask)this.getAIBase()).getOwnCost(), fSize*2, this.getHeight()-(int)(fSize*0.5));
    }

    @Override
    public int getEstimateWidth() {
        this.align();
        return this.getWidth();
    }

    @Override
    public int getEstimateHeight() {
        this.align();
        return this.getHeight();
    }
}
