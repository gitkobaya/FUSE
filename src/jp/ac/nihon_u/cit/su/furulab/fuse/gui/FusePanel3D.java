package jp.ac.nihon_u.cit.su.furulab.fuse.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jogamp.common.util.locks.RecursiveLock;
import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLRunnable;
import com.jogamp.opengl.awt.AWTGLAutoDrawable;
import com.jogamp.opengl.awt.GLJPanel;

import jp.ac.nihon_u.cit.su.furulab.fuse.Environment;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer3D;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.TerrainFragment;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Geometry;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;
import k7system.GameCallBack;
import k7system.GraphicEngine;
import k7system.LightObject;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.Material;
import k7system.gpuobjects.Shader;

/** FusePanelのJOGL対応バージョンです<br>
 * 描画機能等を追加する場合には，このパネルを継承してGameCallBackクラスのコールバックメソッドを実装するようにしてください． */
public class FusePanel3D extends FusePanel implements AWTGLAutoDrawable,GameCallBack{
    public static final double DEFAULT_SIZE_OF_GEOFRAGMENT=1000; //地形パーツの大きさ（１辺の長さ）

    private double sizeOfGeofragment=DEFAULT_SIZE_OF_GEOFRAGMENT;
    private GLJPanel panel=new GLJPanel();

    private List<TerrainFragment>geoFrangments=new ArrayList<TerrainFragment>();

    private GraphicEngine gEngine;

    private float[] cameraPos=new float[]{100,100,800};
    private float[] cameraTarget=new float[]{100,100,0};
    private float[] cameraUpper=new float[]{0,1,0};

    private float[] bgColor=new float[]{0,0,0.5f};

    private double cameraRotation=Math.PI/2; // カメラの上．基本はY軸正方向
    private double cameraOffset=500; // カメラと注視点のオフセット距離

    private LightObject sunLight; // 基本ライト

    /** コンストラクタでパラメーターを指定します */
    public FusePanel3D(SimulationEngine eng, double sizeOfFragment, double cameraOffset){
        super(eng);
        this.sizeOfGeofragment=sizeOfFragment;
        this.cameraOffset=cameraOffset;
        this.init();
    }

    /** コンストラクタです */
    public FusePanel3D(SimulationEngine eng) {
        super(eng);
        this.init();
    }

    /** シミュレーション用の3Dパネルとして初期化します */
    public void init(){
        this.setLayout(new BorderLayout());
        this.add(panel,BorderLayout.CENTER);
        this.gEngine=new GraphicEngine(this);
        SimulationEngine engine=this.getSimulationEngine();
        Environment env=null;
        // 自分をコールバックとして登録
        this.gEngine.setGameCallBack(this);

        if (engine!=null){
            env=this.getSimulationEngine().getEnvironment();
            this.buildGeoFragments(env);
        }
        // 地形の登録
        for (TerrainFragment frag:this.geoFrangments){
            this.gEngine.addNode(frag);
            frag.setVisible(true);
        }

        // 視点情報の登録
        this.gEngine.setProjectionMode(GraphicEngine.PERSPECTIVE);
        this.gEngine.setCameraParameters(-3f, 3f, -3f, 3f, 4.5f, 10000f);
        this.gEngine.setAutoAspect(true);

        // カメラ位置設定
        double[] boundary=env.getBoundaryBox();
        this.cameraPos[0]=(float)(boundary[0]+boundary[1])/2;
        this.cameraPos[1]=(float)(boundary[2]+boundary[3])/2;
        this.cameraPos[2]=(float)(boundary[1]-boundary[0]);
        this.cameraTarget[0]=cameraPos[0];
        this.cameraTarget[1]=cameraPos[1];
        this.cameraTarget[2]=0;
        this.gEngine.setCameraPosition(this.cameraPos,this.cameraTarget,this.cameraUpper);

        // 基本ライト設定
        this.sunLight=new LightObject();
        this.sunLight.setLightPosition(new float[]{0.0f,0.7f,-0.7f,0.0f});
        this.sunLight.setPower(new float[]{0.5f,0.5f,0.5f,1f});
        this.sunLight.setAmbient(new float[]{0.1f,0.1f,0.1f,1f});
        this.sunLight.enable();
        this.gEngine.addLightObject(this.sunLight);

        // 背景色設定
        this.gEngine.setBgColor(this.bgColor);
    }

    @Override
    public void refreshEnvironment() {
        // TODO TBD

    }

    /** 地形の3Dモデルを取得します */
    public List<TerrainFragment> getTerrainFragments(){
        return this.geoFrangments;
    }

    /** 現在設定されている地形の3Dモデルの大きさを取得します */
    public double getSizeOfGeofragment(){
        return this.sizeOfGeofragment;
    }

    /** 地形の3Dモデルの大きさを指定します */
    public void setSizeOfGeofragment(int size){
        this.sizeOfGeofragment=size;
    }

    /** 地形の3Dモデルを生成します */
    public void buildGeoFragments(Environment env){
        List<TerrainFragment> geos=this.getTerrainFragments();
        geos.clear();

        List<Geometry> geoList=env.getGeometries();

        for (Geometry geo:geoList){
            // 規格化された大きさでループを回す
            double startX=geo.getStartX();
            double startY=geo.getStartY();
            double geoSizeX=geo.getSizeX();
            double geoSizeY=geo.getSizeY();
            BasicMaterial baseMat=new BasicMaterial();
            Shader baseShader=baseMat.getShader();
            for(double y=startY;y<startY+geoSizeY;y+=this.sizeOfGeofragment){
                double sizeY=this.sizeOfGeofragment;
                if (startY+geo.getSizeY()<y+sizeY){
                    sizeY=startY+geo.getSizeY()-y;
                }
                for(double x=startX;x<startX+geoSizeX;x+=this.sizeOfGeofragment){
                    BasicMaterial mat=new BasicMaterial();
                    mat.setShader(baseShader);
                    mat.setColor(0f, 1f, 0f, 1f); // まみどり
                    mat.setName("Geo material");

                    double sizeX=this.sizeOfGeofragment;
                    if (startX+geo.getSizeX()<x+sizeX){
                        sizeX=startX+geo.getSizeX()-x;
                    }
                    TerrainFragment frag=new TerrainFragment(x, y, sizeX, sizeY, geo, mat);
                    geos.add(frag);
                }
            }
        }
    }

    /** グラフィックエンジンの取得 */
    public GraphicEngine getGraphicEngine(){
        return this.gEngine;
    }

    /** コールバックオブジェクトの登録 */
    public void setGameCallBack(GameCallBack callBack){
        this.getGraphicEngine().setGameCallBack(callBack);
    }


    /** カメラオフセットを取得します */
    public double getCameraOffset(){
        return this.cameraOffset;
    }

    /** カメラオフセットを設定します */
    public void setCameraOffset(double offset){
        this.cameraOffset=offset;
    }

    /** カメラ回転角を取得します */
    public double getCameraRotation(){
        return this.cameraRotation;
    }

    /** カメラ回転角をラジアンで設定します */
    public void setCameraRotation(double rotation){
        this.cameraRotation=rotation;
    }

    /** 画面中心における地形上での1メートルのドット数を取得します */
    @Override
    public double getDotsByMeter() {
        // TODO 自動生成されたメソッド・スタブ
        return -1;
    }

    @Override
    public Position getWorldPos(int sx, int sy) {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public int[] getScreenCoordinates(Position pos) {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    /** 描画オブジェクトを追加します */
    @Override
    public void addObjectDrawer(long id,ObjectDrawer drawer){
        super.addObjectDrawer(id, drawer);

        this.getGraphicEngine().addNode((ObjectDrawer3D)drawer);
    }

    /** 描画オブジェクトを削除します */
    @Override
    public void removeObjectDrawer(ObjectDrawer drawer){
        super.removeObjectDrawer(drawer);
        this.getGraphicEngine().removeNode((ObjectDrawer3D)drawer);
    }

    /* ここから下は描画関係のコールバック */

    /** 描画処理 */
    @Override
    public void displayCall(GLAutoDrawable gla) {
        Collection<ObjectDrawer> removed=this.checkAndRemoveDrawerObjects();
        for (ObjectDrawer drawer:removed){
            this.gEngine.removeNode((ObjectDrawer3D)drawer);
        }

        Collection<ObjectDrawer> newDrawers=this.checkAndRegisterDrawerObjects();
        for (ObjectDrawer drawer:newDrawers){
            ((ObjectDrawer3D)drawer).setVisible(true);
            this.gEngine.addNode((ObjectDrawer3D)drawer);
            ((ObjectDrawer3D)drawer).setName(drawer.getVirtualObject().getName());
            System.out.println("DEBUG: Add object drawer of <"+drawer.getVirtualObject().getName()+">");
        }
    }

    @Override
    public void displayFinish(GLAutoDrawable gla) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void reshapeCall(GLAutoDrawable gla) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void reshapeFinish(GLAutoDrawable gla) {
        // TODO 自動生成されたメソッド・スタブ

    }

    /* ここから下はAWTGLAutoDrawableのインターフェース */
    @Override
    public void display() {
        this.panel.display();
    }

    @Override
    public void addGLEventListener(GLEventListener arg0) {
        this.panel.addGLEventListener(arg0);
    }

    @Override
    public void addGLEventListener(int arg0, GLEventListener arg1)
            throws IndexOutOfBoundsException {
        this.panel.addGLEventListener(arg0, arg1);
    }

    @Override
    public GLContext createContext(GLContext arg0) {
        return this.panel.createContext(arg0);
    }

    @Override
    public void destroy() {
        this.panel.destroy();
    }

    @Override
    public GLAnimatorControl getAnimator() {
        return this.panel.getAnimator();
    }

    @Override
    public boolean getAutoSwapBufferMode() {
        return this.panel.getAutoSwapBufferMode();
    }

    @Override
    public GLContext getContext() {
        return this.panel.getContext();
    }

    @Override
    public int getContextCreationFlags() {
        return this.panel.getContextCreationFlags();
    }

    @Override
    public GLDrawable getDelegatedDrawable() {
        return this.panel.getDelegatedDrawable();
    }

    @Override
    public GL getGL() {
        return this.panel.getGL();
    }

    @Override
    public Object getUpstreamWidget() {
        return this.panel.getUpstreamWidget();
    }

    @Override
    public boolean invoke(boolean arg0, GLRunnable arg1) {
        return this.panel.invoke(arg0, arg1);
    }

    @Override
    public GLEventListener removeGLEventListener(GLEventListener arg0) {
        return this.panel.removeGLEventListener(arg0);
    }

    @Override
    public void setAnimator(GLAnimatorControl arg0) throws GLException {
        this.panel.setAnimator(arg0);
    }

    @Override
    public void setAutoSwapBufferMode(boolean arg0) {
        this.panel.setAutoSwapBufferMode(arg0);
    }

    @Override
    public void setContextCreationFlags(int arg0) {
        this.panel.setContextCreationFlags(arg0);
    }

    @Override
    public GL setGL(GL arg0) {
        return this.panel.setGL(arg0);
    }

    @Override
    public GLCapabilitiesImmutable getChosenGLCapabilities() {
        return this.panel.getChosenGLCapabilities();
    }

    @Override
    public GLDrawableFactory getFactory() {
        return this.panel.getFactory();
    }

    @Override
    public GLProfile getGLProfile() {
        return this.panel.getGLProfile();
    }

    @Override
    public long getHandle() {
        return this.panel.getHandle();
    }

    @Override
    public NativeSurface getNativeSurface() {
        return this.panel.getNativeSurface();
    }

    @Override
    public boolean isRealized() {
        return this.panel.isRealized();
    }

    @Override
    public void setRealized(boolean arg0) {
        this.panel.setRealized(arg0);
    }

    @Override
    public void swapBuffers() throws GLException {
        this.panel.swapBuffers();
    }


    @Override
    public GLContext setContext(GLContext paramGLContext, boolean paramBoolean) {
        return this.panel.setContext(paramGLContext, paramBoolean);
    }

    @Override
    public int getGLEventListenerCount() {
        return this.panel.getGLEventListenerCount();
    }

    @Override
    public GLEventListener getGLEventListener(int paramInt)
            throws IndexOutOfBoundsException {
        return this.panel.getGLEventListener(paramInt);
    }

    @Override
    public boolean getGLEventListenerInitState(
            GLEventListener paramGLEventListener) {
        return this.panel.getGLEventListenerInitState(paramGLEventListener);
    }

    @Override
    public void setGLEventListenerInitState(
            GLEventListener paramGLEventListener, boolean paramBoolean) {
            this.panel.setGLEventListenerInitState(paramGLEventListener, paramBoolean);
    }

    @Override
    public GLEventListener disposeGLEventListener(
            GLEventListener paramGLEventListener, boolean paramBoolean) {
        return this.panel.disposeGLEventListener(paramGLEventListener, paramBoolean);
    }

    @Override
    public Thread setExclusiveContextThread(Thread paramThread)
            throws GLException {
        return this.panel.setExclusiveContextThread(paramThread);
    }

    @Override
    public Thread getExclusiveContextThread() {
        return this.panel.getExclusiveContextThread();
    }

    @Override
    public boolean invoke(boolean paramBoolean, List<GLRunnable> paramList) {
        return this.panel.invoke(paramBoolean, paramList);
    }

    @Override
    public boolean isGLOriented() {
        return this.panel.isGLOriented();
    }



    //// ここから下がGameCallBack関連

    @Override
    public void graphicEngineIsSet(GraphicEngine engine) {
        this.gEngine=engine;
    }

    @Override
    public void initCall(GLAutoDrawable gla) {


    }

    @Override
    public void initFinish(GLAutoDrawable gla) {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public boolean areAllGLEventListenerInitialized() {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public void flushGLRunnables() {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public RecursiveLock getUpstreamLock() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean isThreadGLCapable() {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    @Override
    public GLCapabilitiesImmutable getRequestedGLCapabilities() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public int getSurfaceHeight() {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

    @Override
    public int getSurfaceWidth() {
        // TODO 自動生成されたメソッド・スタブ
        return 0;
    }

}
