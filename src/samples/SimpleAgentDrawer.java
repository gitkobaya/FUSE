package samples;
import java.awt.Color;
import java.awt.Graphics;

import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;

/** エージェント描画用クラス */
public class SimpleAgentDrawer extends ObjectDrawer2D{
    public static final int SIZE=32;

    /** 描画の時にグラフィックエンジンから呼び出されるメソッドです */
    @Override
    public void draw(Graphics g, FusePanel2D panel) {
        SimpleAgent agt=(SimpleAgent)this.getVirtualObject();
        g.setColor(Color.BLUE);
        int x=panel.getScreenX(agt.getX());
        int y=panel.getScreenY(agt.getY());
        g.fillRect(x-SIZE/2, y-SIZE/2, SIZE, SIZE);
    }
}