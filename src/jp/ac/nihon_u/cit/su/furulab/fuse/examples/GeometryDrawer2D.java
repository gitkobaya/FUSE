package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Geometry;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Vector;

/** SimpleMeshGeometryを描画するためのクラスです */
public class GeometryDrawer2D extends ObjectDrawer2D{
    public static final int DEFAULT_PRIORITY=1000;
    public static final Color TRANSPARENT=new Color(0, 0, 0, 0);

    // エージェントサイズの基本
    public static final double SIZE_OF_AGENT=12;

    // 登場モデルの色
    public static final Color WALL_COLOR=new Color(128,32,32);
    public static final Color FIELD_COLOR=new Color(160,255,160);
    public static final Color WATER_COLOR_SHALLOW=new Color(96,96,224);
    public static final Color WATER_COLOR_DEEP=new Color(64,64,192);
    public static final Color AO_AGENT_COLOR=new Color(0,0,255);
    public static final Color AKA_AGENT_COLOR=new Color(255,0,0);
    public static final Color MIDORI_AGENT_COLOR=new Color(0,255,0);

    /** ジオメトリクラス */
    private SimpleMeshGeometry geometry;

    /** 地形情報表示用のキャッシュ */
    private BufferedImage geoImage;

    /** 実際に描画する地形メッシュ */
    private MeshCell[][] mesh;

    /** 光線の方向 */
    private Vector lightVector=new Vector(0, -0.7, -0.7);

    /** 環境光の強さ */
    private float[] ambient=new float[]{0.3f,0.3f,0.3f};

    /** 陰影表示フラグ */
    private boolean isShading=true;

    /** 色設定用クラス */
    private AltitudeColor colorManager=new AltitudeColor();

    /** 等高線設定用 */
    private double contour=0; // 0以下の時は描画しない

    public GeometryDrawer2D() {
        // TODO 自動生成されたコンストラクター・スタブ
    }

    public GeometryDrawer2D(SimpleMeshGeometry geo) {
        this.geometry=geo;
        this.mesh=geo.getMesh();
        this.setVirtualObject(geo);
    }

    /** 等高線を引く標高を設定します<br>
     * 0以下の値を指定すると等高線を描画しません */
    public void setContour(double contourValue){
        this.contour=contourValue;
    }

    /** 陰影設定フラグを取得します */
    public boolean isShading(){
        return this.isShading;
    }

    /** 陰影を設定します */
    public void setShadingFlag(boolean flag){
        this.isShading=flag;
    }

    /** 描画対象のメッシュを取得します<br>
     * 本体をもらってしまうことになるので、加工したらそのまま反映されます */
    public MeshCell[][] getMesh(){
        return this.mesh;
    }

    /** 描画対象のメッシュを設定します<br>
     * ジオメトリと独立に指定できてしまうため、不整合が起こるとハングアップする可能性があります */
    public void setMesh(MeshCell[][] mesh){
        this.mesh=mesh;
    }

    /** 描画対象のジオメトリを取得します */
    public SimpleMeshGeometry getGeometry(){
        return this.geometry;
    }

    /** 描画対象のジオメトリを登録します */
    public void setGeometry(SimpleMeshGeometry geo){
        this.setVirtualObject(geo);
        this.geometry=geo;
        this.mesh=this.geometry.getMesh();
    }

    /** 描画対象の環境を更新します<br>
     * ジオメトリが切り替わった場合などに呼び出してください */
    public void refreshEnvironment(SimpleMeshGeometry geo) {
        this.geometry=geo;
        this.mesh=geometry.getMesh();
    }

    /** セルの色設定用クラスを指定します */
    public void setColorManager(AltitudeColor colorManager){
        this.colorManager=colorManager;
    }


    /** セルの色を取得します<br>
     * セルの色を変えたい場合はこのメソッドをオーバーライドします。 */
    public Color getCellColor(MeshCell cell){
        Color cellColor=Color.BLACK;
        if (cell.getKind()==CellKind.Field){
            cellColor=this.colorManager.getColor(cell.getAltitude());
        }else if (cell.getKind()==CellKind.Wall){
            cellColor=WALL_COLOR;
        }else if (cell.getKind()==CellKind.ShallowWater){
            cellColor=WATER_COLOR_SHALLOW;
        }else if (cell.getKind()==CellKind.DeepWater){
            cellColor=WATER_COLOR_DEEP;
        }else{
            System.out.println("Unknown Color");
        }
        return cellColor;
    }

    /** 地形の描画<br>
     * 引数は描画領域のピクセル数を設定する */
    public void drawGeoView(Graphics g, FusePanel2D panel){
        double sizeOfCellX=this.geometry.getMeshCellSizeX();
        double sizeOfCellY=this.geometry.getMeshCellSizeY();
        int numOfMeshX=this.geometry.getMesh()[0].length;
        int numOfMeshY=this.geometry.getMesh().length;
        double dotsByMeter=panel.getDotsByMeter();

        // スクリーン左下と右上の座標をワールド座標に変換
        Position scLeftBottom=panel.getWorldPos(0, panel.getHeight()-1);
        Position scRightUpper=panel.getWorldPos(panel.getWidth()-1, 0);
        // ジオメトリ左下の座標をキャッシュ
        Position southWest=new Position(this.geometry.getStartX(),this.geometry.getStartY(),0);

        // 描画開始領域左下
        Position leftBottom=new Position();

        // 描画開始場所のワールド座標
        if (scLeftBottom.getX()<southWest.getX()){
            leftBottom.setX(southWest.getX());
        }else{
            leftBottom.setX(scLeftBottom.getX());
        }

        if (scLeftBottom.getY()<southWest.getY()){
            leftBottom.setY(southWest.getY());
        }else{
            leftBottom.setY(scLeftBottom.getY());
        }

        // 描画開始場所をセル座標にするとどこにあたるのか
        int startScanX=(int)((leftBottom.getX()-southWest.getX())/sizeOfCellX);
        int startScanY=(int)((leftBottom.getY()-southWest.getY())/sizeOfCellY);
        if (startScanX<0){
            startScanX=0;
        }
        if (startScanY<0){
            startScanY=0;
        }

        int paintCellsOfX=(int)((scRightUpper.getX()-scLeftBottom.getX())/sizeOfCellX)+startScanX+3; //左下に埋まる分と右上に埋まる分を+しておく
        int paintCellsOfY=(int)((scRightUpper.getY()-scLeftBottom.getY())/sizeOfCellY)+startScanY+3;

        if (numOfMeshX<=paintCellsOfX){
            paintCellsOfX=numOfMeshX-1;
        }

        if (numOfMeshY<=paintCellsOfY){
            paintCellsOfY=numOfMeshY-1;
        }

        int stepX=(int)(1.0/(sizeOfCellX*dotsByMeter));
        if (stepX==0){
            stepX=1;
        }
        int stepY=(int)(1.0/(sizeOfCellY*dotsByMeter));
        if (stepY==0){
            stepY=1;
        }

        int tempX=-999;
        int tempY=-999;
        for(int y=startScanY;y<paintCellsOfY;y+=stepY){
            for(int x=startScanX;x<paintCellsOfX;x+=stepX){
                int startX,startY,sizeX,sizeY;
                if (this.getGeometry().getMeshFormat()==SimpleMeshGeometry.OTHELLO_TYPE){
                    startX=(int)(panel.getScreenX( ((double)x-0.5)*sizeOfCellX+southWest.getX()) );
                    startY=(int)(panel.getScreenY( ((double)y-0.5)*sizeOfCellY+southWest.getY()) );
                }else{
                    startX=(int)(panel.getScreenX(x*sizeOfCellX+southWest.getX()));
                    startY=(int)(panel.getScreenY(y*sizeOfCellY+southWest.getY()));
                }
                sizeX=(int)(dotsByMeter*sizeOfCellX+1);
                sizeY=(int)(dotsByMeter*sizeOfCellY+1);
                if (tempX!=startX || tempY!=startY){    // 同じピクセルは描画しない
                    if (this.isShading){
                        // 法線を取得
                        Vector normal=null;
                        if (x<numOfMeshX-1 && y<numOfMeshY-1){
                            Position pos=new Position((x+1)*sizeOfCellX, (y+1)*sizeOfCellY, mesh[y+1][x+1].getAltitude());
                            Position pos1=new Position(x*sizeOfCellX, (y+1)*sizeOfCellY, mesh[y+1][x].getAltitude());
                            Position pos2=new Position((x+1)*sizeOfCellX, y*sizeOfCellY, mesh[y][x+1].getAltitude());
                            Vector vec1=pos.getVectorTo(pos1);
                            Vector vec2=pos1.getVectorTo(pos2);
                            normal=vec1.getCross3(vec2);
                            normal.normalize();
                        }

                        // 明るさを取得
                        float bright=1.0f;
                        if (normal!=null){
                            bright=-(float)normal.getDot(lightVector);
                            if (bright<0){
                                bright=0;
                            }
                        }
                        Color col=this.getCellColor(mesh[y][x]);

                        float red=(float)col.getRed()/255*(bright+this.ambient[0]);
                        float green=(float)col.getGreen()/255*(bright+this.ambient[1]);
                        float blue=(float)col.getBlue()/255*(bright+this.ambient[2]);
                        if (red>1.0f){
                            red=1.0f;
                        }
                        if (green>1.0f){
                            green=1.0f;
                        }
                        if (blue>1.0f){
                            blue=1.0f;
                        }
                        g.setColor(new Color(red,green,blue));
                    }else{
                        g.setColor(this.getCellColor(mesh[y][x]));
                    }
                    g.fillRect(startX, startY-sizeY, sizeX, sizeY);

                    // 等高線色を描画
                    if (this.contour>0){
                        boolean contourFlag=false;
                        if (x>0){
                            if ((int)(mesh[y][x-1].getAltitude()/this.contour)-(int)(mesh[y][x].getAltitude()/this.contour)!=0){
                                contourFlag=true;
                            };
                        }
                        if (y>0){
                            if ((int)(mesh[y-1][x].getAltitude()/this.contour)-(int)(mesh[y][x].getAltitude()/this.contour)!=0){
                                contourFlag=true;
                            };
                        }
                        if (contourFlag){
                            g.setColor(new Color(96,24,24,160));
                            g.fillRect(startX, startY-sizeY, sizeX, sizeY);
                        }
                    }
                }
                tempX=startX;
                tempY=startY;
            }
        }

        //System.out.println("DEBUG: draw!! sizeX:"+numOfMeshX+" sizeY:"+numOfMeshY+" scCenterX:"+this.getFocusX()+" scCenterY:"+this.getFocusY());
    }

    @Override
    public void draw(Graphics g, FusePanel2D panel) {
        int panelWidth=panel.getWidth();
        int panelHeight=panel.getHeight();

        // 地形を描画
        if (panel.isChanged()){ // 地形表示に変更があった場合
            this.geoImage = null; // 明示的にGCを教える
            this.geoImage = new BufferedImage( panelWidth, panelHeight, BufferedImage.TYPE_INT_ARGB );
            Graphics2D g2 = this.geoImage.createGraphics();
            g2.setColor(TRANSPARENT);// 背景を透明色でクリア
            g2.fillRect(0, 0, panelWidth, panelHeight);
            this.drawGeoView(g2, panel);
            g2.dispose();

            //System.out.println("DEBUG: width:"+panelWidth+" height:"+panelHeight);
        }
        g.drawImage(geoImage, 0, 0, panelWidth, panelHeight, 0, 0, panelWidth, panelHeight, panel);
    }
}
