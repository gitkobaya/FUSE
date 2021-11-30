package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import jp.ac.nihon_u.cit.su.furulab.fuse.Environment;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel3D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import k7system.GraphicEngine;

/** 主に3D系画面を対象としたパネル操作用リスナーのスケルトンです<br>
 * FusePanel3Dまたはそれを継承したパネルと組み合わせることを前提に設計されています． */
public class KeyAndMouseListner3D extends KeyAndMouseListner{

    /** コンストラクタです */
    public KeyAndMouseListner3D(FusePanel3D panel) {
        super(panel);
    }

    /** マウス座標にもっとも近いエージェントの取得 */
    @Override
    public Agent pickAgent(MouseEvent e, int range) {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    /** マウスホイール操作の監視 */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        FusePanel3D panel=(FusePanel3D)this.getSimulationPanel();
        GraphicEngine gEngine=panel.getGraphicEngine();
        Environment env=panel.getSimulationEngine().getEnvironment();
        float[] camPos=gEngine.getCameraPosition();
        float z=camPos[2];
        int wRot=e.getWheelRotation();

        z=z*(float)Math.pow(1.05,wRot);

        float[] cameraParam=gEngine.getCameraParameters();

        double minZ=env.getAltitude(camPos[0], camPos[1])+cameraParam[4]+panel.getCameraOffset();

        if (z<minZ){
            z=(float)minZ;
        }

        camPos[2]=z;
        gEngine.setCameraPosition(camPos);

        cameraParam[5]=z+(float)(Math.sqrt(Math.pow(-env.getBoundaryBox()[0]+env.getBoundaryBox()[1],2)
                +Math.pow(-env.getBoundaryBox()[2]+env.getBoundaryBox()[3],2)
                +Math.pow(-env.getBoundaryBox()[4]+env.getBoundaryBox()[5],2))); // farを更新
        gEngine.setCameraParameters(cameraParam[0],cameraParam[1],cameraParam[2],cameraParam[3],cameraParam[4],cameraParam[5]);

        /*
        float[] cameraParams=panel.getGraphicEngine().getCameraParameters();
        float near=cameraParams[4];
        if (e.getWheelRotation()<0){
            near*=1.05;
        }else{
            near/=1.05;
        }
        cameraParams[4]=near;
        panel.getGraphicEngine().setCameraParameters(cameraParams[0], cameraParams[1], cameraParams[2], cameraParams[3], cameraParams[4], cameraParams[5]);
        */
    }


}
