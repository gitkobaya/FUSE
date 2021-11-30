package jp.ac.nihon_u.cit.su.furulab.fuse.gui.aiview;

import java.awt.Color;
import java.awt.Graphics;

import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseAIBase;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.ActionByScript;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.ConditionByScript;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.TaskByScript;

public class ActionPart extends GUIPart{

    private Color actColor=new Color(255,128,128);

    public ActionPart(FuseAIBase base) {
        super(base);
        // TODO 自動生成されたコンストラクター・スタブ
    }

    @Override
    public void paintComponent(Graphics g) {
        this.setSize(this.getEstimateWidth(), this.getEstimateHeight());
        g.setColor(this.actColor);
        g.fillRect(0, 0, this.getWidth()-1,this.getHeight()-1);
        g.setColor(g.getColor().darker());
        g.drawRect(0, 0, this.getWidth()-1,this.getHeight()-1);

        g.setColor(Color.black);
        int fSize=this.getFontSize();
        String actionName="NO NAME";
        if (this.getAIBase() instanceof ActionByScript){
            ActionByScript thisAct=(ActionByScript)this.getAIBase();
            actionName=thisAct.getName();
        }else{
            actionName=this.getAIBase().getClass().getSimpleName();
        }
        g.drawString(actionName, fSize, fSize);

        this.drawFields(fSize, fSize*2, g);

        //System.out.println("DEBUG: paint action part");
    }

    @Override
    public int getEstimateWidth() {
        int width=DEFAULT_XSIZE;
        return width;
    }

    @Override
    public int getEstimateHeight() {
        int height=DEFAULT_YSIZE+this.getFieldDatum().size()*this.getFontSize();
        return height;
    }


}
