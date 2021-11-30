package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jp.ac.nihon_u.cit.su.furulab.fuse.Environment;
import jp.ac.nihon_u.cit.su.furulab.fuse.FuseNode;
import jp.ac.nihon_u.cit.su.furulab.fuse.NodeManager;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Building;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** 道路情報の読み込みを行います */
public class VectorDataLoader {
    public static final String NODE="node";
    public static final String WAY="way";
    public static final String ND="nd";
    public static final String REF="ref";
    public static final String TAG="tag";
    public static final String ID="id";
    public static final String LAT="lat";
    public static final String LON="lon";

    public static final String BUILDING="building";
    public static final String LEVELS="building:levels";
    public static final String HIGHWAY="highway";
    public static final String FOOTWAY="footway";
    public static final String ONEWAY="oneway";
    public static final String LANES="lanes";
    public static final String NAME="name";
    public static final String YES="yes";

    public static final double EARTH_RADUIS=6356752.314; // 単位はメートル
    public static final double WIDTH_OF_A_LANE=2.75;


    private double standardLatidule=35.666666667; // 初期値は船橋市近辺
    private double standardLongitude=140.0;
    private double cosin=Math.cos(this.standardLatidule/180*Math.PI);

    private Map<Long, Position> nodeMap=new HashMap<Long, Position>();

    // ノードマネージャー
    private NodeManager manager=null;

    // 建物リスト
    private List<Building> buildings=null;

    /** 標準緯度経度設定 */
    public void setStandardPoint(double lati, double longi){
        this.standardLatidule=lati;
        this.standardLongitude=longi;
        this.cosin=Math.cos(this.standardLatidule/180*Math.PI);
    }

    /** 標準緯度取得 */
    public double getStandardLatitude(){
        return this.standardLatidule;
    }

    /** 標準経度取得 */
    public double getStandardLongitude(){
        return this.standardLongitude;
    }

    /** ノードマネージャーを取得します<br>
     * このメソッドを呼ぶ前にloadVectorDataメソッドを呼んでいる必要があります */
    public NodeManager getNodeManager(){
        return this.manager;
    }

    /** 建造物リストを取得します<br>
     * このメソッドを呼ぶ前にloadVectorDataメソッドを呼んでいる必要があります */
    public List<Building> getBuildings(){
        return this.buildings;
    }

    /** ベクトル情報を読み込みます<br>
     * 引数として，ファイル名と環境を指定します<br>
     * これは，道路を地表に設定するためです */
    public NodeManager loadVectorData(File fileName, Environment env){
        this.nodeMap.clear();
        this.manager=new NodeManager();
        this.buildings=new ArrayList<Building>();

        DocumentBuilderFactory dbFactory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder=null;
        Document doc=null;

        // Dataを全部抽出
        try{
            builder=dbFactory.newDocumentBuilder();
            doc=builder.parse(fileName);
        }catch(Exception e){
            e.printStackTrace();
        }

        // ノード読み込み
        NodeList nodeList=doc.getElementsByTagName(NODE);
        int length=nodeList.getLength();
        for(int i=0;i<length;i++){
            Node node=nodeList.item(i);
            NamedNodeMap nMap=node.getAttributes();
            String idStr=nMap.getNamedItem(ID).getNodeValue();
            String latStr=nMap.getNamedItem(LAT).getNodeValue();
            String lonStr=nMap.getNamedItem(LON).getNodeValue();
            double lat=Double.valueOf(latStr);
            double lon=Double.valueOf(lonStr);

            double y=EARTH_RADUIS*Math.PI*(lat/180);
            double x=EARTH_RADUIS*Math.PI*(lon/180)*this.cosin;
            long id=Long.valueOf(idStr);
            double altitude=0;
            if (env!=null){
                altitude=env.getAltitude(x, y);
            }
            Position fNode=new Position(x, y, altitude);
            this.nodeMap.put(id, fNode);
        }

        // ノードからベクトルデータの生成
        NodeList wayList=doc.getElementsByTagName(WAY);
        for (int i=0;i<wayList.getLength();i++){
            Node way=wayList.item(i);
            FuseLinkRoad road=this.isRoadElement(way);
            if (road!=null){
                // 道路だったら既に追加されている
            }else if (this.isBuildingElement(way)){
                this.addBuilding(way);
            }
        }
        return manager;
    }


    /** 道路を作成します */
    private void addRoadToManager(Node way, double lanes, boolean oneway){
        // 道だった場合
        NodeList childNodes=way.getChildNodes();
        FuseNode prevNode=null;
        for (int j=0;j<childNodes.getLength();j++){
            Node child=childNodes.item(j);
            if (child.getNodeName().equals(ND)){
                NamedNodeMap nMap=child.getAttributes();
                String nodeId=nMap.getNamedItem(REF).getNodeValue();
                long id=Long.valueOf(nodeId);
                FuseNode fuseNode=manager.getNodeById(id);
                if (fuseNode==null){ // 未登録ノードだった場合
                    Position pos=nodeMap.get(id);
                    fuseNode=new FuseNode(id, pos.get());
                    manager.addNode(fuseNode);
                }
                if (prevNode!=null){ // 接続用のリンクを作成
                    double dist=prevNode.getPosition().getDistance(fuseNode.getPosition());
                    double width=lanes*WIDTH_OF_A_LANE;
                    if (width<1.0) {
                        width=1.0;
                    }
                    FuseLinkRoad road=new FuseLinkRoad(prevNode, fuseNode, dist);
                    road.setLanes((int)lanes);
                    road.setWidth(width);
                    prevNode.addLink(road);
                    if (!oneway){
                        FuseLinkRoad road2=new FuseLinkRoad(fuseNode, prevNode, dist);
                        road2.setLanes((int)lanes);
                        road2.setWidth(width);
                        fuseNode.addLink(road2);
                    }else{
                        road.setOnewayFlag(true);
                    }
                }
                prevNode=fuseNode;
            }
        }
    }

    /** 建造物を作成します */
    public void addBuilding(Node node){
        NodeList childNodes=node.getChildNodes();
        List<Position> shape=new ArrayList<Position>();
        String name=null;
        int levels=1;
        for (int j=0;j<childNodes.getLength();j++){
            Node child=childNodes.item(j);
            if (child.getNodeName().equals(ND)){
                NamedNodeMap nMap=child.getAttributes();
                String nodeId=nMap.getNamedItem(REF).getNodeValue();
                long id=Long.valueOf(nodeId);
                Position pos=nodeMap.get(id);
                shape.add(pos);
            }else if (child.getNodeName().equals(TAG)){
                NamedNodeMap map=child.getAttributes();
                if (map.getNamedItem("k").getNodeValue().equals(NAME)){
                    name=map.getNamedItem("v").getNodeValue();
                }
                if (map.getNamedItem("k").getNodeValue().equals(LEVELS)){
                    levels=Integer.parseInt(map.getNamedItem("v").getNodeValue());
                }
            }
        }
        Building building=new Building(shape);
        building.setLevels(levels);
        if (name!=null){
            building.setName(name);
        }
        this.buildings.add(building);
    }

    /** 対象のノードが建造物かどうかを調査します */
    private boolean isBuildingElement(Node target){
        boolean result=false;
        Element elem=(Element)target;
        NodeList nodeList=elem.getElementsByTagName(TAG);
        for (int i=0;i<nodeList.getLength();i++){
            Node tag=nodeList.item(i);
            NamedNodeMap map=tag.getAttributes();
            if (map.getNamedItem("k").getNodeValue().equals(BUILDING)){
                result=true;
                break;
            }
        }
        return result;
    }

    /** 対象のノードが道路かどうかを調査します
     * 道路であれば道路オブジェクトが，違うならNullが返ります */
    private FuseLinkRoad isRoadElement(Node target){
        FuseLinkRoad result=null;
        Element elem=(Element)target;
        NodeList nodeList=elem.getElementsByTagName(TAG);
        double lanes=-1;
        boolean oneway=false;
        for (int i=0;i<nodeList.getLength();i++){
            Node tag=nodeList.item(i);
            NamedNodeMap map=tag.getAttributes();
            if (map.getNamedItem("k").getNodeValue().equals(HIGHWAY)){
                lanes=1;
                if (map.getNamedItem("v").getNodeValue().equals(FOOTWAY)){
                    lanes=0;
                }
            }
            if (map.getNamedItem("k").getNodeValue().equals(LANES)){
                lanes=Double.valueOf(map.getNamedItem("v").getNodeValue());
            }
            if (map.getNamedItem("k").getNodeValue().equals(ONEWAY)){
                if (map.getNamedItem("v").getNodeValue().equals(YES)){
                    oneway=true;
                }
            }
        }

        // 道路だった場合
        if (lanes>=0){
            this.addRoadToManager(target, lanes, oneway);
        }
        return result;
    }
}
