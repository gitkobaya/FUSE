package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** ファイルからSimpleMeshGeometryを取得します */
public class SimpleMeshGeometryLoader implements GeometryLoader<SimpleMeshGeometry>{

    public static final String LOWER_CORNER="gml:lowerCorner";
    public static final String UPPER_CORNER="gml:upperCorner";
    public static final String NUM_OF_ELEMTNS="gml:high";
    public static final String TUPPLE_LIST="gml:tupleList";
    public static final double EARTH_RADUIS=6356752.314; // 単位はメートル

    private double standardLatidule=35.666666667; // 初期値は船橋市近辺
    private double standardLongitude=140.0;

    /** 標準緯度経度設定 */
    public void setStandardPoint(double lati, double longi){
        this.standardLatidule=lati;
        this.standardLongitude=longi;
    }

    /** 標準緯度取得 */
    public double getStandardLatitude(){
        return this.standardLatidule;
    }

    /** 標準経度取得 */
    public double getStandardLongitude(){
        return this.standardLongitude;
    }

    /** 複数のファイルからSimpleMeshGeometryを読み込みます */
    @Override
    public SimpleMeshGeometry loadGeometry(List<File> files) {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    /** ファイルからSimpleMeshGeometryを読み込みます */
    @Override
    public SimpleMeshGeometry loadGeometry(File file) {
        DocumentBuilderFactory dbFactory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder=null;
        Document doc=null;

        // Dataを全部抽出
        try{
            builder=dbFactory.newDocumentBuilder();
            doc=builder.parse(file);
        }catch(Exception e){
            e.printStackTrace();
        }

        // 境界を取得
        double lx=0; // 北緯ゼロ，経度ゼロの点を基準とした位置座標
        double ly=0;
        NodeList nodes=doc.getElementsByTagName(LOWER_CORNER);
        Node lowerNode=nodes.item(0);
        String lowerStr=lowerNode.getTextContent();
        String[] dataSet=lowerStr.split(" ");
        double lowerLatitude=Double.valueOf(dataSet[0]);
        ly=EARTH_RADUIS*Math.PI*(lowerLatitude/180);
        lx=EARTH_RADUIS*Math.PI*(Double.valueOf(dataSet[1])/180)*Math.cos(this.standardLatidule/180*Math.PI);

        double ux=0;
        double uy=0;
        nodes=doc.getElementsByTagName(UPPER_CORNER);
        Node upperNode=nodes.item(0);
        String upperStr=upperNode.getTextContent();
        dataSet=upperStr.split(" ");
        double upperLatitude=Double.valueOf(dataSet[0]);
        uy=EARTH_RADUIS*Math.PI*(upperLatitude/180);
        ux=EARTH_RADUIS*Math.PI*(Double.valueOf(dataSet[1])/180)*Math.cos(this.standardLatidule/180*Math.PI);
        double diffX=ux-lx;
        double diffY=uy-ly;

        // 要素の個数を取得
        int numOfX;
        int numOfY;
        nodes=doc.getElementsByTagName(NUM_OF_ELEMTNS);
        Node numNode=nodes.item(0);
        String numStr=numNode.getTextContent();
        dataSet=numStr.split(" ");
        numOfX=Integer.valueOf(dataSet[0]);
        numOfY=Integer.valueOf(dataSet[1]);
        double meshCellSizeX=diffX/numOfX;
        double meshCellSizeY=diffY/numOfY;

        // 標高データ読み込み
        nodes=doc.getElementsByTagName(TUPPLE_LIST);
        Node altitudeNode=nodes.item(0);
        String altitudes=altitudeNode.getTextContent();
        String[] altitudeSet=altitudes.split("[0-9]\n"); // "その他,数値"というフォーマット

        MeshCell[][] cells=new MeshCell[numOfY+1][numOfX+1];
        int counter=0;
        for (int y=0;y<=numOfY;y++){
            for (int x=0;x<=numOfX;x++){
                String line=altitudeSet[counter];
                String[] elements=line.split(",");
                double altitude=Double.valueOf(elements[1]);
                MeshCell cell=new MeshCell();
                if (altitude<0){
                    cell.setKind(CellKind.ShallowWater);
                    if (altitude<=999.9){
                        cell.setKind(CellKind.DeepWater);
                    }
                    altitude=0;
                }
                cell.setAltitude(altitude);
                cells[numOfY-y][x]=cell;
                counter++;
            }
        }

        SimpleMeshGeometry geo=new SimpleMeshGeometry();
        geo.setMesh(cells);
        geo.setMeshCellSize(meshCellSizeX, meshCellSizeY);
        geo.setStartPosition(lx, ly, 0);

        return geo;
    }


}
