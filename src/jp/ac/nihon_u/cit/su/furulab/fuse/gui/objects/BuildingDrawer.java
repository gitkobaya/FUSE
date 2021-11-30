package jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import org.jruby.ast.DotNode;

import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Building;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** Buildingオブジェクトの描画用クラスです */
public class BuildingDrawer extends ObjectDrawer2D{
    public static final Color BUILDING_COLOR=new Color(192, 192, 192);
    public static final Color BUILDING_OUTLINE_COLOR=new Color(64, 64, 64);
    public static final int BUILDING_LAYER=500;
    private int[] xPoints=null;
    private int[] yPoints=null;
    private int nPoint=0;

    public BuildingDrawer() {
        this.setLayer(BUILDING_LAYER);
    }

    @Override
    public void draw(Graphics g, FusePanel2D panel) {
        List<Position> shape=((Building)this.getVirtualObject()).getShape();

        if (xPoints==null){ // 最初の呼び出し時に変換を実行
            this.nPoint=shape.size();
            this.xPoints=new int[this.nPoint];
            this.yPoints=new int[this.nPoint];
        }

        for (int i=0;i<this.nPoint;i++){
            Position pos=shape.get(i);
            int[] points=panel.getScreenCoordinates(pos);
            this.xPoints[i]=points[0];
            this.yPoints[i]=points[1];
        }
        // 太さの設定
        Graphics2D g2=(Graphics2D)g;
        int hutosa=(int)panel.getDotsByMeter();
        g2.setStroke(new BasicStroke(hutosa));

        g.setColor(BUILDING_COLOR);
        g.fillPolygon(xPoints, yPoints, nPoint);
        g.setColor(BUILDING_OUTLINE_COLOR);
        g.drawPolygon(xPoints, yPoints, nPoint);
    }
}
