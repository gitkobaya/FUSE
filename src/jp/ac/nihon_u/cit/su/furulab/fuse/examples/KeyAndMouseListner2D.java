package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.MouseInputListener;



import jp.ac.nihon_u.cit.su.furulab.fuse.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.ObjectSorter;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** 主に2D系画面を対象としたパネル操作用リスナーのスケルトンです<br>
 * このままでもFusePanelSimpleMeshと組み合わせることで、拡大、縮小、スクロールが使えます。 */
public class KeyAndMouseListner2D extends KeyAndMouseListner{

    public KeyAndMouseListner2D(FusePanel panel) {
        super(panel);
    }

    /** 現在のマウス座標にもっとも近いエージェントで，かつ指定した範囲のエージェントを指定します．
     * 範囲指定の単位はピクセルです */
    @Override
    public Agent pickAgent(MouseEvent e,int range){
        Agent agt=this.pickAgent(e);
        if (agt!=null){
            int[] screenPos=this.getSimulationPanel().getScreenCoordinates(agt.getPosition());
            int mx=e.getX();
            int my=e.getY();
            double dist=Position.getDistance2D(mx, my, screenPos[0], screenPos[1]);
            // 範囲外だった場合
            if (range<dist){
                agt=null;
            }
        }
        return agt;
    }

    /** 現在のマウス座標から指定した範囲のエージェントを返します．
     * 範囲指定の単位はピクセルです */
    public List<Agent> pickAgents(MouseEvent e,int range){
        List<Agent> resultAgents=new ArrayList<Agent>();
        List<ObjectSorter> sortList=new ArrayList<ObjectSorter>();
        List<Agent> agents=this.getSimulationPanel().getSimulationEngine().getAllAgents();

        for(Agent agt:agents){
            int[] screenPos=this.getSimulationPanel().getScreenCoordinates(agt.getPosition());
            int mx=e.getX();
            int my=e.getY();
            double dist=Position.getDistance2D(mx, my, screenPos[0], screenPos[1]);
            // 範囲内だった場合
            if (dist<range){
                sortList.add(new ObjectSorter(agt, dist));
            }
        }
        Collections.sort(sortList);
        for (ObjectSorter sorter:sortList){
            resultAgents.add((Agent)sorter.getTarget());
        }

        return resultAgents;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        FusePanel2D panel=(FusePanel2D)this.getSimulationPanel();
        panel.setChangeFlag(true);
    }

    /** マウスホイール操作の監視 */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        FusePanel panel=this.getSimulationPanel();
        double dotsByMeter=panel.getDotsByMeter();
        if (e.getWheelRotation()<0){
            dotsByMeter*=1.05;
        }else{
            dotsByMeter/=1.05;
        }
        ((FusePanel2D)panel).setDotsByMeter(dotsByMeter);
        //System.out.println("DEBUG: wheel");
    }
}
