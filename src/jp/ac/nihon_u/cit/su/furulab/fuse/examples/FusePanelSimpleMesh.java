package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.examples.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Geometry;

/** SimpleMeshGeometoryに対応した表示パネルです<br>
 * これを継承することで、比較的簡単にGUIを設計できます*/
public class FusePanelSimpleMesh extends FusePanel2D{

    private Class<? extends GeometryDrawer2D> geoDrawClass=GeometryDrawer2D.class;
    private List<GeometryDrawer2D> drawers=new ArrayList<GeometryDrawer2D>();

    /** コンストラクタです */
    public FusePanelSimpleMesh(SimulationEngine eng) {
        super(eng);
        this.refreshGeometry();
    }

    /** コンストラクタです */
    public FusePanelSimpleMesh(SimulationEngine eng, Class<? extends GeometryDrawer2D> drawerClass) {
        super(eng);
        this.geoDrawClass=drawerClass;
        this.refreshGeometry();
    }

    /** 現在設定されているジオメトリ描画オブジェクトを取得します */
    public List<GeometryDrawer2D> getGeometryDrawers(){
        return this.drawers;
    }

    /** 現在設定されているジオメトリ描画クラスを取得します */
    public Class<? extends GeometryDrawer2D> getGeometryDrawerClass(){
        return this.geoDrawClass;
    }

    /** ジオメトリ描画クラスを設定します<br>
     * 標準のGeometryDrawer2D以外のものを利用する際に呼び出します */
    public void setGeometryDrawerClass(Class<? extends GeometryDrawer2D> drawerClass){
        this.geoDrawClass=drawerClass;
    }

    /** 色設定マネージャを設定します */
    public void setColorManager(AltitudeColor colorManager){
        for (GeometryDrawer2D drawer:this.drawers){
            drawer.setColorManager(colorManager);
        }
    }

    /** 等高線を引く高度を指定します<br>
     * 0(またはそれ以下の数字)を指定すると等高線を引きません */
    public void setContourAltitude(double alti){
        for (GeometryDrawer2D drawer:this.drawers){
            drawer.setContour(alti);
        }
    }

    /** シェーディングを設定します */
    public void setShadingFlag(boolean flag){
        for (GeometryDrawer2D drawer:this.drawers){
            drawer.setShadingFlag(flag);
        }
    }

    /** シミュレーションエンジンを参照し，ジオメトリ情報を再設定します */
    public void refreshGeometry(){
        SimulationEngine engine=this.getSimulationEngine();
        Environment env=engine.getEnvironment();

        // 削除する
        for (GeometryDrawer2D drawer:this.drawers){
            this.removeObjectDrawer(drawer);
        }
        this.drawers.clear();

        // 描画オブジェクトを追加する
        List<Geometry> geos=new ArrayList<Geometry>(env.getGeometries());
        Collections.sort(geos, new GeometryComparator());
        int priority=0;
        for (Geometry geo:geos){ // レゾリューションの値が小さい(詳細)なものほどプライオリティの値が小さくなる
            if (geo instanceof SimpleMeshGeometry){
                try {
                    GeometryDrawer2D drawer=this.geoDrawClass.newInstance();
                    drawer.setGeometry((SimpleMeshGeometry)geo);
                    drawer.setLayer(GeometryDrawer2D.DEFAULT_PRIORITY+priority);
                    this.drawers.add(drawer);
                    this.addObjectDrawer(geo.hashCode(), drawer);
                    priority++;
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return;
    }
}
/** 地形オブジェクトを降順に並び替えます */
class GeometryComparator implements Comparator<Geometry>{

    @Override
    public int compare(Geometry o1, Geometry o2) {
        double check=o1.getResolution()-o2.getResolution();
        int result=0;
        if (check<0){
            result=-1;
        }else if (check>0){
            result=1;
        }
        return result;
    }

}