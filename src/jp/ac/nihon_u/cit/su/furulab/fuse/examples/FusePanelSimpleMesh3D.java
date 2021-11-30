package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.imageio.ImageIO;

import jp.ac.nihon_u.cit.su.furulab.fuse.Environment;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel3D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.TerrainFragment;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Geometry;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;
import k7system.GameCallBack;
import k7system.GraphicEngine;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.Material;
import k7system.gpuobjects.TextureK7;

/** 3D表示用パネルです<br>
 * 描画にK7Systemを利用しています．
 * ジオメトリクラスがSimpleMeshGeometryであることを前提とし，地形は平面上にあるものとして扱います．<br>
 * 球体を前提とする場合，別のクラスが必要となります．  */
public class FusePanelSimpleMesh3D extends FusePanel3D{
    // キーとマウスのイベントリスナ
    private KeyAndMouseListner3D kam;

    /** コンストラクタでパラメーターを指定します<br>
     * 第一引数は地形フラグメントの大きさで，描画単位をどのくらいの大きさで扱うかです<br>
     * 第二引数はカメラオフセットで，カメラと注視点の距離です．これを大きくするほど地面に寄った時の角度が水平に近くなります */
    public FusePanelSimpleMesh3D(SimulationEngine eng, double sizeOfFragment, double cameraOffset){
        super(eng, sizeOfFragment, cameraOffset);
        float[] target=this.getGraphicEngine().getCameraTarget();
        this.setFocus(target[0], target[1]);
    }

    public FusePanelSimpleMesh3D(SimulationEngine eng) {
        super(eng);
        float[] target=this.getGraphicEngine().getCameraTarget();
        this.setFocus(target[0], target[1]);
    }

    /** 登録された操作用のリスナーを取得するメソッドです */
    public KeyAndMouseListner3D getKAMListner(){
        return this.kam;
    }

    /** 操作用のリスナーを登録するメソッドです */
    public void setKAMListner(KeyAndMouseListner3D listner){
            kam=listner;
            this.addKeyListener(kam);
            this.addMouseWheelListener(kam);
            this.addMouseMotionListener(kam);
            this.addMouseListener(kam);
    }

    /** 画面中心における地形上での1メートルのドット数を取得します<br>
     * SimpleMeshGeometryは地球の丸みを考慮しない平面なので，計算は簡単です． */
    @Override
    public double getDotsByMeter() {
        double dbm=-1;
        int yPixelSize=this.getHeight()/2;

        float[] params=this.getGraphicEngine().getCameraParameters();

        double tanYZ=params[3]/params[4];
        double ySize=tanYZ*this.getGraphicEngine().getCameraPosition()[2];

        dbm=(double)yPixelSize/ySize;

        return dbm;
    }

    /** テクスチャを生成し，地形断片モデルに設定します */
    public void setTextures(AltitudeColor altColor){
        if (altColor!=null){
            for(TerrainFragment frag:this.getTerrainFragments()){
                int width=frag.getNumOfVerticesX();
                int height=frag.getNumOfVerticesY();
                BufferedImage image=new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
                List<Float> vertices=frag.getVertices();
                int x=0;
                int y=0;
                for(int counter=0;counter<vertices.size();counter+=3){
                    MeshCell cell=((SimpleMeshGeometry)frag.getGeometryObject()).getMeshCell(vertices.get(counter), vertices.get(counter+1));
                    if (cell!=null && (cell.getKind()==CellKind.ShallowWater|| cell.getKind()==CellKind.DeepWater)){
                        image.setRGB(x, y, Color.BLUE.getRGB());
                    }else{
                        int color=altColor.getColor(vertices.get(counter+2)).getRGB(); //+2はZ軸を得るため
                        image.setRGB(x, y, color);
                    }
                    x++;
                    if (x>=width){
                        x=0;
                        y++;
                    }
                }
                BasicMaterial mat=frag.getMaterial();
                mat.setDiffuseTexture(new TextureK7(image));
                mat.setColor(1.0f, 1.0f, 1.0f);
                mat.setSpecularColor(0.8f, 0.8f, 0.8f);
                mat.setEmissionColor(0.15f, 0.15f, 0.15f);
                mat.setShinness(1);
            }
        }
    }

    /** 画面サイズが変更された時の処理 */
    int prevWidth,prevHeihgt;
    @Override
    public void setBounds(int x, int y, int width, int height) {
        if (width!=prevWidth || height!=prevHeihgt){
            super.setBounds(x, y, width, height);
            prevWidth=width;
            prevHeihgt=height;
        }
    }

    /** フォーカスの設定(2次元)<br>
     * 2次元で指定された場合，そのx,yが示すジオメトリ座標にカメラのフォーカスを合わせます．*/
    @Override
    public void setFocus(double x, double y){
        double z=this.getSimulationEngine().getEnvironment().getAltitude(x, y); // フォーカス地点の標高を取得

        GraphicEngine gEngine=this.getGraphicEngine();

        float[] cPos=gEngine.getCameraPosition();

        double cameraOffset=this.getCameraOffset();
        double cameraRotation=this.getCameraRotation();

        cPos[0]=(float)(x-cameraOffset*Math.cos(cameraRotation));
        cPos[1]=(float)(y-cameraOffset*Math.sin(cameraRotation));
        gEngine.setCameraPosition(cPos);
        gEngine.setCameraTarget(new float[]{(float)x,(float)y,(float)z});

        //System.out.println("DEBUG: camera("+cPos[0]+", "+cPos[1]+", "+cPos[2]+")");
        //System.out.println("DEBUG: focus("+x+", "+y+", "+z+")");

        this.setFocus(x, y, z);
    }


    /** 描画処理 */
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
    }
}
