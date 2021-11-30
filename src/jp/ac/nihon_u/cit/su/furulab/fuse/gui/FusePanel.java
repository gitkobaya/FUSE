package jp.ac.nihon_u.cit.su.furulab.fuse.gui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;

import jp.ac.nihon_u.cit.su.furulab.fuse.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.Effect;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects.ObjectDrawer;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Asset;
//import jp.ac.nihon_u.cit.su.furulab.fuse.models.Geometry;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** 可視化用クラスです。<br>
 *   シミュレーションエンジンとは可能な限り疎につながります。<br>
 *   シミュレーションエンジンと密に結合しているのではなく，情報を外から参照することで画面を書き換えます。*/
public abstract class FusePanel extends JPanel{
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	// シミュレーションエンジン
    private SimulationEngine engine;

    // シミュレーションID
    // 監視しているシミュレーションエンジンのシミュレーションIDが変化した場合，描画オブジェクトは初期化されます
//    private long simId;

    // シミュレーション操作用マネージャ
    private OperationManager<SimulationEngine> opManager;

    // 操作のID
    private long operationIdCounter=0;

    // 注視点
    private double[] focusPoint=new double[3];

    private boolean painting; //現在描画中

    // 描画すべきものです．ここに登録されているオブジェクトが実際に描画されます
    private Map<Long,ObjectDrawer> objectDrawers=new HashMap<Long, ObjectDrawer>();

    // バーチャルオブジェクトと描画オブジェクトの対応を設定します
    private Map<Class<? extends VirtualObject>,Class<? extends ObjectDrawer>> createRules=new HashMap<Class<? extends VirtualObject>, Class<? extends ObjectDrawer>>();

    /** エフェクトの一覧です<br> エフェクトリストは複数スレッドからアクセスされる可能性があります */
    private List<Effect> effects=new ArrayList<Effect>();

    // エフェクト用のロックオブジェクトです
    private Lock effectLock=new ReentrantLock();

    /** コンストラクタです */
    public FusePanel(SimulationEngine eng) {
        engine=eng;
        this.setPreferredSize(new Dimension(480,480));
    }

    /** シミュレーションエンジンを再設定します */
    public void setSimlationEngine(SimulationEngine eng){
        engine=eng;
    }

    /** シミュレーションエンジンを取得します */
    public SimulationEngine getSimulationEngine(){
        return engine;
    }

    /** 描画対象の環境を更新します<br>
     * ジオメトリが切り替わった場合などに呼び出してください */
    public abstract void refreshEnvironment();

    /** 1メートルが何ドットに当たるかを取得します<br>
     * より正確にいうと，画面中心点に表示された地形表面において，1メートルが何ドットに当たるかを取得します． */
    public abstract double getDotsByMeter();

    /** スクリーン座標からワールド座標を取得します<br>
     * スクリーン座標に対応するワールド座標は，厳密には点ではなく1次関数で表現されます．このメソッドでは．その直線が地形と交差する点を返します．<br>
     * 仮にその直線上に地形データが存在しなかった場合, 返り値は不定となります．*/
    public abstract Position getWorldPos(int sx,int sy);

    /** ワールド座標からスクリーン座標を取得します */
    public abstract int[] getScreenCoordinates(Position pos);

    /** フォーカス座標のXを取得します */
    public double getFocusX(){
        return this.focusPoint[0];
    }

    /** フォーカス座標のYを取得します */
    public double getFocusY(){
        return this.focusPoint[1];
    }

    /** フォーカス座標のZを取得します */
    public double getFocusZ(){
        return this.focusPoint[2];
    }

    /** フォーカス座標を配列で取得します */
    public double[] getFocus(){
        return this.focusPoint;
    }

    /** シミュレーション空間でのフォーカス座標を設定します */
    public void setFocus(double x, double y){
        this.focusPoint[0]=x;
        this.focusPoint[1]=y;
    }

    /** シミュレーション空間でのフォーカス座標を設定します */
    public void setFocus(Position pos){
        this.setFocus(pos.getX(), pos.getY(), pos.getZ());
    }

    /** シミュレーション空間でのフォーカス座標を設定します */
    public void setFocus(double x, double y, double z){
        this.focusPoint[0]=x;
        this.focusPoint[1]=y;
        this.focusPoint[2]=z;
    }

    /** シミュレーションエンジンが持つバーチャルオブジェクトのうち，また描画オブジェクトが登録されていないものについて描画オブジェクトを登録します<br>
     * また，新規に登録されたオブジェクトを返り値として返します． */
    public List<ObjectDrawer> checkAndRegisterDrawerObjects(){
        // 現時点での描画オブジェクトの集合をコピー
        Map<Long, ObjectDrawer> drawers=new HashMap<Long, ObjectDrawer>(this.objectDrawers);
        List<Agent> agents=this.getSimulationEngine().getAllAgents();
        List<Asset> assets=this.getSimulationEngine().getEnvironment().getAllAssets();

        List<ObjectDrawer> newDrawers=new ArrayList<ObjectDrawer>();

        // バーチャルオブジェクト全体のリストを生成
        List<VirtualObject> vObjects=new ArrayList<VirtualObject>(agents);
        vObjects.addAll(assets);

        for (VirtualObject vObj:vObjects){
            long id=vObj.getId();
            if (!drawers.containsKey(id)){
                ObjectDrawer drawer=this.createObjectDrawer(vObj);
                if (drawer!=null){
                    this.addObjectDrawer(id,drawer);
                    newDrawers.add(drawer);
                }
            }
        }
        return newDrawers;
    }

    /** パネルが持つ描画オブジェクトのうち，シミュレーションエンジンに存在しないものを削除します．
     * 削除された描画オブジェクトを返り値として返します */
    public List<ObjectDrawer> checkAndRemoveDrawerObjects(){
        SimulationEngine engine=this.getSimulationEngine();

        Collection<ObjectDrawer> drawers=this.objectDrawers.values();
        List<ObjectDrawer> removeTargets=new ArrayList<ObjectDrawer>();

        for (ObjectDrawer drw:drawers){
            if (drw.getVirtualObject() instanceof Agent){
                Agent result=engine.getAgentById(drw.getObjectId());
                if (result==null){
                    removeTargets.add(drw);
                }
            }else if (drw.getVirtualObject() instanceof Asset){
                Asset result=engine.getEnvironment().getAssetById(drw.getObjectId());
                if (result==null){
                    removeTargets.add(drw);
                }
            }
        }

        for(ObjectDrawer remove:removeTargets){
            this.objectDrawers.remove(remove.getObjectId());
        }
        return removeTargets;
    }

    /** 描画オブジェクトの生成ルールを登録します */
    public void addObjectDrawerCreateRule(Class<? extends VirtualObject> vObjClass, Class<? extends ObjectDrawer> objDrawerClass){
        this.createRules.put(vObjClass,objDrawerClass);
    }

    /** バーチャルオブジェクトクラスに対応する描画オブジェクトを生成します<br>
     * 引数で指定したクラスに対応する描画オブジェクトクラスが指定されていない場合，nullが返ります．*/
    public ObjectDrawer createObjectDrawer(VirtualObject vObj){
        Class<? extends ObjectDrawer> creator=this.createRules.get(vObj.getClass());
        ObjectDrawer result=null;
        if (creator!=null){
            try{
                result=creator.newInstance();
                result.setVirtualObject(vObj);
            }catch(Exception e){
                e.printStackTrace();
                System.exit(-1);
            }
        }
        return result;
    }

    /** 描画オブジェクトを取得します */
    public Collection<ObjectDrawer> getObjectDrawers(){
        return this.objectDrawers.values();
    }

    /** 描画オブジェクトを追加します */
    public void addObjectDrawer(long id,ObjectDrawer drawer){
        this.objectDrawers.put(id,drawer);
    }

    /** 描画オブジェクトを削除します */
    public void removeObjectDrawer(ObjectDrawer drawer){
        this.objectDrawers.remove(drawer);
    }

    /** エフェクトを取得します */
    public List<Effect> getEffects(){
        List<Effect> result;
        this.effectLock.lock();
        try{
            result=new ArrayList<Effect>(this.effects);
        }finally{
            this.effectLock.unlock();
        }
        return result;
    }

    /** エフェクトを追加します<br>
     * パネルが2DならEffect2Dを，3DならEffect3Dを登録します．
     * エフェクトは指定された時間が経過すると自動的に消滅させなければいけません． */
    public void addEffect(Effect effect){
        this.effectLock.lock();
        try{
            this.effects.add(effect);
        }finally{
            this.effectLock.unlock();
        }
    }

    /** エフェクトを削除します<br>
     * エフェクトは自動的に消滅するため，内部からしか呼ばれません． */
    protected void removeEffect(Effect effect){
        this.effectLock.lock();
        try{
            this.effects.remove(effect);
        }finally{
            this.effectLock.unlock();
        }
    }

    /** IDに対応する描画オブジェクトを取得します */
    public ObjectDrawer getObjectDrawer(long id){
        return this.objectDrawers.get(id);
    }

    /** 操作制御オブジェクトを取得します */
    public OperationManager getOpManager(){
        return this.opManager;
    }

    /** 操作制御オブジェクトを設定します */
    public void setOpManager(OperationManager manager){
        this.opManager=manager;
    }

    /** オペレーションIDを発行します */
    public long getOpId(){
        return this.operationIdCounter;
    }

    /** オペレーション依頼を処理します */
    public void exec(Operation ope){
        ope.setId(operationIdCounter);
        opManager.exec(ope);
        operationIdCounter++;
    }

    /** オペレーション依頼を処理します */
    public void exec(List<Operation> operations){
        for(Operation ope:operations){
            ope.setId(operationIdCounter);
            opManager.exec(ope);
        }
        operationIdCounter++;
    }

    /** オペレーション依頼を処理します */
    public void undo(){
        opManager.undo();
    }

    /** オペレーション依頼を処理します */
    public void redo(){
        opManager.redo();
    }

    /** 描画中フラグをセットします */
    protected void setPaintingFlag(boolean flag){
        painting=flag;
    }

    /** 描画中フラグを読み込みます */
    public boolean isPainting(){
        return painting;
    }

}