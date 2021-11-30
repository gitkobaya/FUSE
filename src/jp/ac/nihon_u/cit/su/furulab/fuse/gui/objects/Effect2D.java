package jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects;

import java.awt.Graphics;

import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;

public abstract class Effect2D extends Effect implements Drawer2D{
    private int priority=0; // 通常，エフェクトは最前面に来るため

    @Override
    public int getLayer() {
        return priority;
    }

    public void setPriority(int priority){
        this.priority=priority;
    }

    public abstract void draw(Graphics g, FusePanel2D panel);

}
