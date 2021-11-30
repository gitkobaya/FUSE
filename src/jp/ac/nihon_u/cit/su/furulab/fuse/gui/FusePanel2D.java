package jp.ac.nihon_u.cit.su.furulab.fuse.gui;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.nihon_u.cit.su.furulab.fuse.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.examples.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.Drawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.Effect;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.Effect2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer2D;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Geometry;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Vector;

/** SimpleMeshGeometoryに対応した表示パネルです<br>
 * これを継承することで、比較的簡単にGUIを設計できます*/
public class FusePanel2D extends FusePanel{

    // キーとマウスのイベントリスナ
    private KeyAndMouseListner2D kam;

    // スクリーン中心のスクリーン座標
    private int centerX,centerY;

    // 1mの大きさ(ピクセル数)
    private double dotsByMeter=1.0;

    // 地形描画に変更があったかどうかのフラグ
    private boolean panelChange=true;

    /** コンストラクタです */
    public FusePanel2D(SimulationEngine eng) {
        super(eng);

        // 世界の中心にフォーカスを合わせる
        this.setFocus(eng.getEnvironmentSizeX()/2, eng.getEnvironmentSizeY()/2);

        // 操作マネージャを登録
        this.setOpManager(new OperationManager(eng));

        // 背景は白
        this.setBackground(Color.white);
    }

    /** 描画対象の環境を更新します<br>
     * ジオメトリが切り替わった場合などに呼び出してください */
    @Override
    public void refreshEnvironment() {

    }

    /** 2D以外を登録した時にエラーを出すように */
    @Override
    public void addEffect(Effect effect) {
        super.addEffect((Effect2D)effect);
    }

    /** 2D以外を登録した時にエラーを出すように */
    @Override
    public void addObjectDrawerCreateRule(Class<? extends VirtualObject> vObjClass, Class<? extends ObjectDrawer> objDrawerClass) {
        super.addObjectDrawerCreateRule(vObjClass, (Class<? extends ObjectDrawer2D>) objDrawerClass);
    }

    /** 登録された操作用のリスナーを取得するメソッドです */
    public KeyAndMouseListner2D getKAMListner(){
        return this.kam;
    }

    /** 操作用のリスナーを登録するメソッドです */
    public void setKAMListner(KeyAndMouseListner2D listner){
        kam=listner;
        this.addKeyListener(kam);
        this.addMouseWheelListener(kam);
        this.addMouseMotionListener(kam);
        this.addMouseListener(kam);
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

    /** ドットの大きさを取得します */
    public double getDotsByMeter(){
        return dotsByMeter;
    }

    /** ドットの大きさを設定します */
    public void setDotsByMeter(double dotSize){
        panelChange=true;
        dotsByMeter=dotSize;
        //System.out.println ("Debug: change dotsByMeter:"+dotsByMeter);
    }

    /** パネルに変更があったかを取得します */
    public boolean isChanged(){
        return panelChange;
    }

    /** パネル変更フラグを設定します */
    public void setChangeFlag(boolean flag){
        panelChange=flag;
    }

    /** マウスのX座標を取得します */
    public int getMouseX(){
        return kam.getMouseX();
    }

    /** マウスのY座標を取得します */
    public int getMouseY(){
        return kam.getMouseY();
    }

    /** スクリーン2点間の距離を取得します */
    public double getDistance(int x1, int y1, int x2, int y2){
        double distance;
        distance=Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
        return distance;
    }

    /** ワールド座標からスクリーン座標を取得します */
    public int[] getScreenCoordinates(Position pos){
        int[] result=this.getScreenCoordinates(pos.getX(),pos.getY());
        return result;
    }

    /** ワールド座標からスクリーン座標を取得します */
    public int[] getScreenCoordinates(double x,double y){
        int[] result=new int[2];
        result[0]=this.getScreenX(x);
        result[1]=this.getScreenY(y);
        return result;
    }

    /** ワールド座標からスクリーンのY座標を取得します */
    public int getScreenY(double y){
        int scY=0;
        scY=this.getHeight()-(int)((double)(y-this.getFocusY())*dotsByMeter)-centerY;
        return scY;
    }

    /** ワールド座標からスクリーンのX座標を取得します */
    public int getScreenX(double x){
        int scX=0;
        scX=(int)((double)(x-this.getFocusX())*dotsByMeter)+centerX;
        return scX;
    }

    /** ワールド座標がスクリーン領域に含まれるかを確認します */
    public boolean isInScreen(Position pos){
        boolean result=this.isInScreenArea(pos, 0, 0, this.getWidth(), this.getHeight());
        return result;
    }

    /** ワールド座標が指定されたスクリーン領域に含まれるかを確認します */
    public boolean isInScreenArea(Position pos,int startX, int startY, int sizeX, int sizeY){
        boolean result=false;
        int[] sc=this.getScreenCoordinates(pos);

        if (startX<=sc[0] && sc[0]<startX+sizeX && startY<=sc[1] && sc[1]<startY+sizeY) {
            result=true;
        }
        return result;
    }

    /** スクリーン座標からワールド座標を取得します */
    public Position getWorldPos(int sx,int sy){
        double simX=(double)(sx-centerX)/dotsByMeter+this.getFocusX();
        double simY=(this.getHeight()-centerY-sy)/dotsByMeter+this.getFocusY();
        double simZ=this.getSimulationEngine().getEnvironment().getAltitude(simX, simY); // あとで
        Position pos=new Position(simX, simY, simZ);

        return pos;
    }

    /** スクリーン上の点に最も近いエージェントを返します */
    public Agent getNearestAgent(int sx,int sy){
        Position pos=this.getWorldPos(sx, sy);
        Agent nearest=this.getSimulationEngine().getNearestAgent(pos);
        return nearest;
    }

    /** スクリーン上の点に最も近いエージェントまでの距離を返します */
    public double getScDistanceToNearestAgent(int sx,int sy){
        Agent unit=this.getNearestAgent(sx, sy);
        int[] sc=this.getScreenCoordinates(unit.getPosition());
        double dist=this.getDistance(sx, sy, sc[0], sc[1]);
        return dist;
    }

    /** 描画ルーチン */
    int panelWidth;
    int panelHeight;
    long prevTime; // 最後に描画した時刻
    @Override
    protected void paintComponent(Graphics g) {
        this.setPaintingFlag(true); // 描画中フラグをセット
        long currentTime=System.currentTimeMillis();
        int interval=(int)(currentTime-this.prevTime);
        this.prevTime=currentTime;

        if (panelWidth!=this.getWidth() || panelHeight!=this.getHeight()){
            this.panelChange=true;
            this.panelWidth=this.getWidth();
            this.panelHeight=this.getHeight();
        }

        // 描画の中心を設定
        this.centerX=panelWidth/2;
        this.centerY=panelHeight/2;

        //System.out.println("DEBUG: dotsByMeter"+this.getDotsByMeter()+" centerX:"+this.centerX+" centerY:"+this.centerY);

        // 描画準備
        List<Drawer2D> drawers2d=new ArrayList<Drawer2D>(this.getObjectDrawers().size()+this.getEffects().size());
        this.checkAndRegisterDrawerObjects();
        for (ObjectDrawer d:this.getObjectDrawers()){
            drawers2d.add((ObjectDrawer2D)d);
        }
        for (Effect e:this.getEffects()){
            drawers2d.add((Effect2D)e);
        }

        // 描画オブジェクトの整列
        Collections.sort(drawers2d, new CompareDrawerObject());

        // エージェントを描画
        for (Drawer2D drawer:drawers2d){
            if (drawer instanceof ObjectDrawer2D){
                if (((ObjectDrawer2D) drawer).getVisible()){
                    ((ObjectDrawer2D)drawer).draw(g,this);
                }
            }else if (drawer instanceof Effect2D){
                Effect2D effect=(Effect2D)drawer;
                ((Effect2D)effect).draw(g,this);
                effect.reduceLife(interval);
                if (!effect.isLive()){
                    this.removeEffect(effect);
                }
            }
        }

        this.checkAndRemoveDrawerObjects();
        this.panelChange=false;
        this.setPaintingFlag(false); // 描画中フラグをリセット
    }
}

/** priorityの値を使って降順ソートを行います */
class CompareDrawerObject implements Comparator<Drawer2D>{

    @Override
    public int compare(Drawer2D o1, Drawer2D o2) {
        return o2.getLayer()-o1.getLayer();
    }

}