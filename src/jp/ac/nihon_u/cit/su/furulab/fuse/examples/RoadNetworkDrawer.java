package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.FuseLink;
import jp.ac.nihon_u.cit.su.furulab.fuse.FuseNode;
import jp.ac.nihon_u.cit.su.furulab.fuse.NodeManager;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

public class RoadNetworkDrawer extends ObjectDrawer2D{
    private int priority=500;
    private NodeManager manager;

    public RoadNetworkDrawer(NodeManager man) {
        this.manager=man;
    }

    @Override
    public int getLayer() {
        return this.priority;
    }

    /** ノードとリンクからなるネットワークを描画します */
    @Override
    public void draw(Graphics g, FusePanel2D panel) {
        List<FuseNode> nodes=manager.getAll();
        g.setColor(Color.GRAY);

        // 太さの設定
        Graphics2D g2=(Graphics2D)g;

        for (FuseNode node:nodes){
            List<FuseLink> links=node.getLinks();
            Position start=node.getPosition();
            int[] startOnScreen=panel.getScreenCoordinates(start);
            for (FuseLink link:links){
                Position destination=link.getDestination().getPosition();
                int[] destOnScreen=panel.getScreenCoordinates(destination);

                if (link instanceof FuseLinkRoad){
                    FuseLinkRoad road=(FuseLinkRoad)link;
                    float widthOnScreen=(float)(road.getWidth()*panel.getDotsByMeter());
                    g2.setStroke(new BasicStroke(widthOnScreen));
                    // デバッグ
                    /*
                    if (road.isOneway()){
                        g.setColor(Color.RED);
                    }
                    */
                }
                g.drawLine(startOnScreen[0], startOnScreen[1], destOnScreen[0], destOnScreen[1]);
                //g.setColor(Color.GRAY);
            }
        }
    }
}
