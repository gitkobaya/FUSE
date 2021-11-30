package jp.ac.nihon_u.cit.su.furulab.fuse.gui.aiview;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import jp.ac.nihon_u.cit.su.furulab.fuse.ai.Condition;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseAIBase;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.ConditionByScript;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.TaskByScript;

public class ConditionPart extends GUIPart{

    private Color condColor=new Color(255,128,255);

    public ConditionPart(FuseAIBase base) {
        super(base);
    }

    @Override
    public void paintComponent(Graphics g) {
        this.setSize(this.getEstimateWidth(), this.getEstimateHeight());
        g.setColor(this.condColor);
        g.fillRect(0, 0, this.getWidth()-1,this.getHeight()-1);
        g.setColor(g.getColor().darker());
        g.drawRect(0, 0, this.getWidth()-1,this.getHeight()-1);

        g.setColor(Color.black);
        int fSize=this.getFontSize();
        String condName="NO NAME";
        if (this.getAIBase() instanceof ConditionByScript){
            ConditionByScript thisCond=(ConditionByScript)this.getAIBase();
            condName=thisCond.getName();
        }else{
            condName=this.getAIBase().getClass().getSimpleName();
        }
        g.drawString(condName, fSize, fSize);


        this.drawFields(fSize, fSize*2, g);

        Font font=g.getFont();
        g.setFont(new Font("Gothic",Font.BOLD,this.getFontSize()));
        g.drawString(((Condition)this.getAIBase()).getTruth().toString(), fSize, fSize*(this.getFieldDatum().size()+2));
        g.setFont(font);

        //System.out.println("DEBUG: paint condition part");
    }

    @Override
    public int getEstimateWidth() {
        int width=DEFAULT_XSIZE;
        return width;
    }

    @Override
    public int getEstimateHeight() {
        int height=DEFAULT_YSIZE;
        height+=this.getFieldDatum().size()*this.getFontSize();
        height+=this.getFontSize();
        return height;
    }



}
