package jp.ac.nihon_u.cit.su.furulab.fuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Asset;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Geometry;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.MessageReciever;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** エージェントが動き回るための空間です。
 * 内部に複数の地形と建物を有し、環境空間を構成します。 */
public class  Environment implements Savable,MessageReciever{

    private boolean isSphere=false; // 世界が球体かどうか

    private List<Geometry> geometries=new ArrayList<Geometry>(); // 地形
    private NodeManager manager; // マスターノードマネージャ

    //private List<Asset> assets=new ArrayList<Asset>();
    private Map<Long,Asset> assets=new HashMap<Long,Asset>();

    private double[] boundaries=new double[8];

    private Queue<Message> messageBox=new LinkedList<Message>();

    /** コンストラクタです */
    public Environment() {
    }
 
    /** コンストラクタです */
    public Environment(Geometry geo) {
        this.setGeometry(geo);
    }

    /** コンストラクタです */
    public Environment(List<Geometry> geometries) {
        this.geometries=geometries;
        this.createBoundaryBox();
    }

    /** 環境を完全に初期化します<br>
     * リストアの際に呼び出されます */
    protected void firstInit(){
        this.geometries.clear();
        this.assets.clear();
    }

    /** シミュレーション開始時に呼ばれる処理です<br>
     * シミュレーションエンジンを呼び出す必要がある処理は、コンストラクタではなくこのメソッドに記述しなければなりません。<br>
    * ただし、Environmentクラスの場合はこのメソッドは何もしません。必要に応じてこのクラスを継承し、このメソッドをオーバーライドしてください。 */
    public void init(SimulationEngine engine){

    }

    /** シミュレーションサイクルごとに呼ばれる処理です<br>
     * タイミングとしては、エージェントの処理が呼ばれる前にこの処理が呼ばれます。*/
    public void action(long timestep,SimulationEngine engine){

    }

    /** ノードセットを作成します<br>
     * 今は手抜き状態 */
    public void createNodeSet(int step){
        manager=geometries.get(0).createNodeSet(step);
    }

    /** ノードセットを取得します */
    public NodeManager getNodeManager(){
        return manager;
    }

    /** 地形リストを取得します */
    public List<Geometry> getGeometries(){
        return geometries;
    }

    /** 最初の地形を取得します */
    public Geometry getFirstGeometry(){
        return geometries.get(0);
    }

    /** 地形を設定します<br>
     * 最初の地形を設定することになります */
    public void setGeometry(Geometry geo){
        if (this.geometries.isEmpty()){
            this.geometries.add(geo);
        }else{
            this.geometries.set(0, geo);
        }
        this.createBoundaryBox();
    }

    /** 地形を追加します */
    public void addGeometry(Geometry geo){
        this.geometries.add(geo);
        this.createBoundaryBox();
    }

    /** この環境の重心を取得します<br>
     * 返り値はdouble型の配列となります． */
    public double[] getCenter(){
        double x=(this.boundaries[0]+this.boundaries[1])/2;
        double y=(this.boundaries[2]+this.boundaries[3])/2;
        double z=(this.boundaries[4]+this.boundaries[5])/2;
        return new double[]{x,y,z};
    }

    /**  環境のバウンダリボックスを取得します<br>
     * 返り値はdouble型の配列となり，{minX,maxX,minY,maxY,minZ,maxZ}が入ります<br>
     * 単位は地形オブジェクトに依存します．なお，通常はメートルを推奨します．*/
    public double[] getBoundaryBox(){
        return this.boundaries.clone();
    }

    /**  環境のバウンダリボックスを生成します<br>
     * 返り値はdouble型の配列となり，{minX,maxX,minY,maxY,minZ,maxZ}が入ります<br>
     * 単位は地形オブジェクトに依存します．なお，通常はメートルを推奨します．*/
    private double[] createBoundaryBox(){
        double minX=Constants.HUGE;
        double maxX=-Constants.HUGE;
        double minY=Constants.HUGE;
        double maxY=-Constants.HUGE;
        double minZ=Constants.HUGE;
        double maxZ=-Constants.HUGE;
        for(Geometry geo:this.geometries){
            double tempMinX=geo.getStartX();
            double tempMaxX=geo.getStartX()+geo.getSizeX();
            double tempMinY=geo.getStartY();
            double tempMaxY=geo.getStartY()+geo.getSizeY();
            double tempMinZ=geo.getStartZ();
            double tempMaxZ=geo.getStartZ()+geo.getSizeZ();

            if (tempMinX<minX){
                minX=tempMinX;
            }
            if (maxX<tempMaxX){
                maxX=tempMaxX;
            }
            if (tempMinY<minY){
                minY=tempMinY;
            }
            if (maxY<tempMaxY){
                maxY=tempMaxY;
            }
            if (tempMinZ<minZ){
                minZ=tempMinZ;
            }
            if (maxZ<tempMaxZ){
                maxZ=tempMaxZ;
            }
        }
        this.boundaries=new double[]{minX,maxX,minY,maxY,minZ,maxZ};
        return this.boundaries;
    }

    /** 高度を取得します<br>
     * ここで取得した高度はアセット込みのDEMになります */
    public double getAltitude(double horizontal, double virtical){
        double altitude=0; // 初期値

        Geometry mostDetailed=null;
        double detail=Constants.HUGE;
        // 指定した地点が含まれる地形のうち、最も詳細なものを選ぶ
        for(Geometry geo:geometries){
            if (geo.getStartX()<=horizontal && horizontal<geo.getStartX()+geo.getSizeX()
                    && geo.getStartY()<=virtical && virtical<geo.getStartY()+geo.getSizeY()){
                if (geo.getResolution()<detail){
                    mostDetailed=geo;
                    detail=geo.getResolution();
                }
            }
        }

        if (mostDetailed!=null){
            altitude=mostDetailed.getAltitude(horizontal, virtical);
        }

        return altitude;
    }

    /** 環境に登録されているアセットを取得します */
    public List<Asset> getAllAssets(){
        return new ArrayList<Asset>(assets.values());
    }

    /** 環境に登録されているアセットをIDで取得します */
    public Asset getAssetById(long id){
        return this.assets.get(id);
    }

    /** アセットを削除します<br>
     * めったに呼ばれることはなさそう． */
    public void removeAsset(Asset asset){
        this.assets.remove(asset.getId());
    }

    /** 指定した座標の近隣アセットを取得します */
    public List<Asset> getNeighborAssets(Position pos, double dist){
        List<Asset> neighbors=new ArrayList<Asset>(assets.size()>>2); // 初期値を適切に決めておかないとパフォーマンス落ちます
        double distance;
        for(Asset ast:this.assets.values()){
            distance=pos.getDistance(ast.getPosition());
            if(distance<dist ){
                neighbors.add(ast);
            }
        }
        return neighbors;
    }

    /** 指定した座標に最も近いアセットを取得します */
    public Asset getNearestAsset(Position pos){
        Asset nearest=null;
        double distance=Constants.HUGE;
        for(Asset ast:this.assets.values()){
            double dist=pos.getDistance(ast.getPosition());
            if(dist<distance){
                nearest=ast;
                distance=dist;
            }
        }
        return nearest;
    }

    /** 絶対座標に対してアセットを設定します。 */
    public void addAsset(double x, double y, double z, Asset asset){
        asset.setPosition(new double[]{x,y,z});
        this.addAsset(asset);
    }

    /** アセットを設定します。 */
    public void addAsset(Asset asset){
        assets.put(asset.getId(),asset);
    }

    /** 地面に対してアセットを設定します */
    public void addAssetOnGround(Asset asset){
        Position pos=asset.getPosition();
        double altitude=this.getAltitude(pos.getX(), pos.getY());
        // 高度の設定
        asset.setZ(altitude);

        this.addAsset(asset);
    }

    /** 地面に対してアセットを設定します */
    public void addAssetOnGround(double horizontal, double vertical, Asset as){
        as.setX(horizontal);
        as.setY(vertical);
        this.addAssetOnGround(as);
    }


    /** 2点間の見通しを判定します */
   public boolean getLos(Position start, Position end){
       boolean result=true;
       for (Geometry geo:geometries){
           if (geo.getLos(start, end)==false){
               result=false;
               break;
           }
       }

       return result;
   }

   /** シミュレーションエンジンからメッセージを送り込むメソッドです。<br>
    * シミュレーションエンジンから呼び出されるため、シミュレーション開発者が操作する必要はありません。 */
   @Override
   public void recieveMessage(Message mess){
       messageBox.add(mess);
   }

   /** 自分に届いたメッセージを一通確認します */
   @Override
   public Message getOneMessage(){
       Message mess=messageBox.poll();
       return mess;
   }

   /** 自分に届いたメッセージをすべて取得します<br>
    * この処理によってエージェントのメッセージボックスは空になります．<br>
    * Get all messages that the agent recieved. <br>
    * By executing the method, the message box of the agent becomes empty.*/
   @Override
   public List<Message> getAllMessages(){
       LinkedList<Message>result=new LinkedList<Message>();
       int loop=messageBox.size();
       for(int i=0;i<loop;i++){
           result.add(messageBox.poll());
       }
       return result;
   }


   /** 環境データの保存メソッドです */
   public SaveDataPackage saveStatus() {
       SaveDataPackage sdp=new SaveDataPackage(this);
       // アセットの保存
       for (Asset asset:this.getAllAssets()){
           sdp.addChildPackage(asset);
       }

       // ジオメトリの保存
       for (Geometry geo:geometries){
           sdp.addChildPackage(geo);
       }
       return sdp;
   }

   /** 環境データの復元メソッドです */
    public Savable restoreStatus(SaveDataPackage saveData) {
        this.firstInit();

        for (SaveDataPackage child:saveData.getAllChildren()){
            try{
                // アセットの復元
                if (Asset.class.isAssignableFrom(child.getOwnerClass())){
                    Asset as=(Asset)child.restore();
                    this.assets.put(as.getId(), as);
                }
                // ジオメトリの復元
                if (Geometry.class.isAssignableFrom(child.getOwnerClass())){
                    Geometry geo=(Geometry)child.restore();
                    this.geometries.add(geo);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return this;
    }
}
