package jp.ac.nihon_u.cit.su.furulab.fuse.examples.traffic;

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
                int minX=startOnScreen[0];
                int minY=startOnScreen[1];
                int maxX=destOnScreen[0];
                int maxY=destOnScreen[1];
                if (destOnScreen[0]<startOnScreen[0]){
                    minX=destOnScreen[0];
                    maxX=startOnScreen[0];
                }
                if (destOnScreen[1]<startOnScreen[0]){
                    minY=destOnScreen[1];
                    maxY=startOnScreen[1];
                }

                if (!(maxX<0 || maxY<0 || panel.getWidth()<minX || panel.getHeight()<minY)){
                    if (link instanceof FuseLinkRoad){
                        FuseLinkRoad road=(FuseLinkRoad)link;
                        float widthOnScreen=(float)(road.getWidth()*panel.getDotsByMeter());
                        g2.setStroke(new BasicStroke(widthOnScreen));
                    }
                    g.drawLine(startOnScreen[0], startOnScreen[1], destOnScreen[0], destOnScreen[1]);
                    g2.setStroke(new BasicStroke(1));
                }
            }
            // デバッグ
            //g.fillOval(startOnScreen[0]-3, startOnScreen[1]-3, 6, 6);
        }
    }
}
