package jp.ac.nihon_u.cit.su.furulab.fuse.examples;
import java.util.ArrayList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.FileDataProperty;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Geometry;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.LoadableObject;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.LoadbleObjectImpl;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** ジオメトリはプログラマが実装します。<br>
 * ジオメトリの1例として，2Dメッシュのジオメトリを提供します。<br>
 * 現在、セルの座標は囲碁型となっています。 */
public class SimpleMeshGeometry extends Geometry{
    private MeshCell[][] mesh;
    private Class<? extends MeshCell> cellClass=MeshCell.class;
    private int sizeXint,sizeYint;
    private double cellSizeX; //1メッシュの大きさ(m)
    private double cellSizeY; //1メッシュの大きさ(m)
    private double geoSizeX=0;
    private double geoSizeY=0;
    private double geoSizeZ=0;

    public static final int IGO_TYPE=1;
    public static final int OTHELLO_TYPE=2;
    /** 囲碁型かオセロ型かを設定 */
    private int meshFormat=OTHELLO_TYPE;

    /** コンストラクタです */
    public SimpleMeshGeometry() {
    }

    /** コンストラクタです<br>
     * ジオメトリの大きさ（セル数）を決定します */
    public SimpleMeshGeometry(int sx, int sy) {
        sizeXint=sx;
        sizeYint=sy;
        mesh=this.createMesh(sx, sy);
    }

    /** メッシュセルのクラスも同時に設定するコンストラクタです<br>
     * 引数としてMeshCellクラスを継承したクラスを指定すると、そのクラスを利用してメッシュを作成します。 */
    public SimpleMeshGeometry(int sx, int sy, Class<? extends MeshCell> cellClass) {
        sizeXint=sx;
        sizeYint=sy;
        this.setCellClass(cellClass);
        mesh=this.createMesh(sx, sy);
    }

    /** メッシュのフォーマットを取得します<br>
     * このクラスのstatic変数であるIGO_TYPEまたはOTHELLO_TYPEが返ってきます。<br>
     * 囲碁型はメッシュの中央に基準座標があり、オセロ型は メッシュの左下に基準座標があります。*/
    public int getMeshFormat(){
        return meshFormat;
    }

    /** メッシュのフォーマットを設定します<br>
     * このクラスのstatic変数であるIGO_TYPEまたはOTHELLO_TYPEで指定します。 */
    public void setMeshFormat(int type){
        if (type!=IGO_TYPE && type!=OTHELLO_TYPE){
            // あとで何か例外を返すようにします
        }else{
            this.meshFormat=type;
        }
    }

    /** メッシュをセットします<br>
     * ジオメトリのメッシュクラスも同時に更新されます */
    public void setMesh(MeshCell[][] mesh){
        this.mesh=mesh;
        if (mesh!=null && mesh[0]!=null && mesh[0][0]!=null){
            this.cellClass=mesh[0][0].getClass();
        }
    }

    public MeshCell[][] createMesh(int sx,int sy){
        MeshCell[][] tempMesh=new MeshCell[sy][];

        // メッシュを作成します
        tempMesh=new MeshCell[sy][];
        for(int y=0;y<tempMesh.length;y++){
            tempMesh[y]=new MeshCell[sx];
            for(int x=0;x<tempMesh[y].length;x++){
                try {
                    tempMesh[y][x]=cellClass.newInstance();
                    tempMesh[y][x].setMeshX(x);
                    tempMesh[y][x].setMeshY(y);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                tempMesh[y][x].setAltitude(0.0);
                tempMesh[y][x].setKind(CellKind.Field);
            }
        }

        this.checkStatus();
        return tempMesh;
    }

    /** メッシュセルに利用されているクラスを取得します<br>
     * 返り値はインスタンスではなくクラスオブジェクトになります． */
    public Class<?> getCellClass(){
        return this.cellClass;
    }

    /** メッシュセルに利用するクラスを登録します<br>
     * メッシュセルとして、exempleで用意したMeshCellクラス以外を利用したい場合、このメソッドで登録してください<br>
     * このメソッドは特殊で、引数としてインスタンスではなくクラスを取ります。<br>
     * MeshCellを継承したクラスの.classを指定してください。 */
    protected void setCellClass(Class<? extends MeshCell> cellClass){
        this.cellClass=cellClass;
    }

    @Override
    public double getSizeX() {
        return this.geoSizeX;
    }

    @Override
    public double getSizeY() {
        return this.geoSizeY;
    }

    @Override
    public double getSizeZ() {
        return this.geoSizeZ;
    }


    /** 経路探索用のノードメッシュを作成します<br>
     * 距離はgetActualDistanceメソッドで計算されるため，標準以外の距離計算をする場合はそちらのメソッドをオーバーライドしてください．
     * @param step 引数でノードの間隔を指定します．ここで1を設定すると地形メッシュと同じ密度でノードメッシュが生成されます． */
    @Override
    public NodeManager createNodeSet(int step) {
        NodeManager manager=new NodeManager();

        System.out.println("DEBUG: making NodeMesh...");

        // まずノードを作る
        FuseNode[][] nodes=this.createNodes(step);

        // 自分の周囲のノードに対してリンクを貼る
        int sinchoku=0,prev=-1;
        int lengthX=nodes[0].length;
        int lengthY=nodes.length;
        for(int y=0;y<lengthY;y++){
            for(int x=0;x<lengthX;x++){
                FuseNode startNode=nodes[y][x];
                Position posStart=new Position(startNode.getPosByArray());
                for(int i=-1;i<=1;i++){
                    for(int j=-1;j<=1;j++){
                        int tempX=x+j;
                        int tempY=y+i;
                        if (!(tempX==x && tempY==y) && 0<=tempX && tempX<lengthX && 0<=tempY && tempY<lengthY){
                            FuseNode tempNode=nodes[tempY][tempX];
                            Position posEnd=new Position(tempNode.getPosByArray());

                            double dist=this.getActualDistance(posStart, posEnd);

                            FuseLink tempLink=new FuseLink(startNode,tempNode,dist);
                            startNode.addLink(tempLink);
                        }
                    }
                }
            }
            sinchoku=(y*10/lengthY);
            if (sinchoku!=prev){
                System.out.print("*");
                prev=sinchoku;
            }
        }
        System.out.println("");
        manager.setNodes(nodes);

        return manager;
    }

    /** ノードメッシュ（リンクなし）を作成します<br>
     * 非常に汚い実装ですがとりあえず */
    public FuseNode[][] createNodes(int step){
        List<FuseNode> nodes=new ArrayList<FuseNode>();
        FuseNode[][] results;

        // まずノードを作る
        int id=0;
        int lengthX=0;
        int lengthY=0;
        for(int y=0;y<mesh.length;y+=step){
            for(int x=0;x<mesh[0].length;x+=step){
                if (y==0) {
                    lengthX++;
                }
                double posX=x*this.cellSizeX;
                double posY=y*this.cellSizeY;
                FuseNode node=new FuseNode(id, new double[]{posX,posY,mesh[y][x].getAltitude()});
                nodes.add(node);
                id++;
            }
            lengthY++;
        }

        results=new FuseNode[lengthY][lengthX];
        for(int y=0;y<lengthY;y++){
            for(int x=0;x<lengthX;x++){
                results[y][x]=nodes.get(y*lengthX+x);
            }
        }

        return results;
    }

    /** 2つのセル間の実質距離を取得します<br>
     * ここで返される距離は、単純な物理的距離ではなく、地形効果等が加味されています。<br>
     * このメソッドをオーバーライドすることで地形特有の要素を含めることができます。 */
    public double getActualDistance(Position start, Position end){

        double dist=start.getDistance(end);

        // 高低差による重みづけ
        // 上りはすごく重く、下りは多少重く
        double diffAlti=start.getZ()-end.getZ();
        if (diffAlti>0){
            dist+=Math.abs(diffAlti);
        }else if (diffAlti<0){
            dist+=Math.abs(diffAlti);
        }

        // 水領域の重みづけ
        double weight=0;
        MeshCell startMesh=this.getMeshCell(start.getX(),start.getY());
        MeshCell endMesh=this.getMeshCell(end.getX(),end.getY());
        if (startMesh==null || end==null){
            System.out.println("ERROR: startCell:"+startMesh+" endCell:"+endMesh);
            System.out.println("ERROR: startPos:"+start+" endPos:"+end);
            System.exit(-1);
        }
        if (startMesh.getKind()==CellKind.ShallowWater){
            weight=weight+dist*5;
        }
        if (endMesh.getKind()==CellKind.ShallowWater){
            weight=weight+dist*5;
        }
        dist=dist+weight;

        return dist;
    }

    /** 部分ノードメッシュを作成します */
    public NodeManager createLocalNodeMesh(Position start, Position end,double gridSize){
        NodeManager manager=new NodeManager();

        Position southWest=new Position();
        Position northEast=new Position();

        if (start.getX()<end.getX()){
            southWest.setX(start.getX());
            northEast.setX(end.getX());
        }else{
            southWest.setX(end.getX());
            northEast.setX(start.getX());
        }

        if (start.getY()<end.getY()){
            southWest.setY(start.getY());
            northEast.setY(end.getY());
        }else{
            southWest.setY(end.getY());
            northEast.setY(start.getY());
        }

        double startX=southWest.getX();
        double sizeX=northEast.getX()-southWest.getX();
        double startY=southWest.getY();
        double sizeY=northEast.getY()-southWest.getY();

        // まずノードを作る
        int id=0;
        for(double y=startY;y<startY+sizeY;y+=gridSize){
            for(double x=startX;x<startX+sizeX;x+=gridSize){
                FuseNode node=new FuseNode(id, new double[]{x,y,this.getAltitude(x, y)});
                manager.addNode(node);
                id++;
            }
        }

        // 自分の周囲のノードに対してリンクを貼る
        int lengthX=(int)(Math.ceil((double)sizeX/gridSize));
        int lengthY=(int)(Math.ceil((double)sizeY/gridSize));
        for(int y=0;y<lengthY;y++){
            for(int x=0;x<lengthX;x++){
                FuseNode startNode=manager.getAllReference().get(y*lengthX+x);
                Position posStart=new Position(startNode.getPosByArray());
                for(int i=-1;i<=1;i++){
                    int tempY=y+i;
                    for(int j=-1;j<=1;j++){
                        int tempX=x+j;
                        if (!(tempX==x && tempY==y) && 0<tempX && tempX<lengthX && 0<tempY && tempY<lengthY){
                            FuseNode tempNode=manager.getAllReference().get(tempY*lengthX+tempX);
                            Position posEnd=new Position(tempNode.getPosByArray());
                            double dist=this.getActualDistance(posStart,posEnd);
                            FuseLink tempLink=new FuseLink(startNode,tempNode,dist);
                            startNode.addLink(tempLink);
                        }
                    }
                }
            }
        }
        return manager;
    }

    /** 1メッシュの大きさを取得します */
    public double getMeshCellSizeX(){
        return cellSizeX;
    }

    /** 1メッシュの大きさを取得します */
    public double getMeshCellSizeY(){
        return cellSizeY;
    }


    /** 1メッシュの大きさを設定します<br>
     * 単位は特に定めていませんが，エージェントなどと合わせるのがよいでしょう． */
    public void setMeshCellSize(double size){
        this.setMeshCellSize(size, size);
    }

    /** セルサイズを指定します */
    public void setMeshCellSize(double xSize, double ySize){
        this.cellSizeX=xSize;
        this.cellSizeY=ySize;
        this.checkStatus();
    }

    /** 解像度を取得します<br>
     * このクラスではメッシュセルのXサイズと一致します */
    @Override
    public double getResolution() {
        return cellSizeX;
    }


    @Override
    public double getResolutionHorizontal() {
        return cellSizeX;
    }

    @Override
    public double getResolutionVertical() {
        return cellSizeY;
    }

    /** 解像度を設定します<br>
     * このクラスではメッシュセルのサイズを設定することになります */
    @Override
    public void setResolution(double res) {
        this.setMeshCellSize(res);
    }

    /** メッシュが横方向に何個あるかを取得します */
    public int getNumOfMeshX(){
        return mesh[0].length;
    }

    /** メッシュが縦方向に何個あるかを取得します */
    public int getNumOfMeshY(){
        return mesh.length;
    }

    /** メッシュ情報の取得 */
    public MeshCell[][] getMesh(){
        return mesh;
    }

    /** 部分メッシュを取得します<br>
     * 範囲をワールド座標で指定します。この範囲に部分的にかかったセルも含まれます */
    public MeshCell[][] getLocalMesh(double startX, double startY, double sizeX, double sizeY){
        int meshSizeY=(int)Math.ceil(sizeY/cellSizeX); // 切り上げ
        int meshSizeX=(int)Math.ceil(sizeX/cellSizeY); // 切り上げ
        MeshCell[][] local=new MeshCell[meshSizeY][meshSizeX];

        for (int meshY=0;meshY<meshSizeY;meshY++){
            for (int meshX=0;meshX<meshSizeY;meshX++){
                double x=meshX*cellSizeX+startX;
                double y=meshY*cellSizeY+startY;
                local[meshY][meshX]=this.getMeshCell(x, y);
            }
        }

        return local;
    }

    /** 部分メッシュを取得します<br>
     *  範囲をセル単位で指定します。*/
    public MeshCell[][] getLocalMeshByInt(int startX, int startY, int sizeX, int sizeY){
        MeshCell[][] local=new MeshCell[sizeY][sizeX];
        MeshCell[][] target=this.getMesh();

        for (int meshY=0;meshY<sizeY;meshY++){
            int y=meshY+startY;
            int sx=startX;
            int width=sizeX;

            // 元のメッシュからはみ出さないように
            if (0<=y && y<target.length){

                if (sx<0){
                    sx=0;
                }else if (target[y].length<sx+width){
                    width=target[y].length-sx-1;
                }
                System.arraycopy(target[y],sx, local[meshY], 0, width);
            }
        }

        return local;
    }

    /** ある座標のメッシュセルを取得します<br>
     * 参照が返されますので、戻ってきたオブジェクトを変更するとマスターが変更されます。<br>
     * メッシュの範囲外を指定した場合、nullが返されます。<br>
     * また、ここで指定するxとyがm単位であらわす座標であることに注意してください。(n番目ではない) */
    public MeshCell getMeshCell(double x,double y){
        double offset=0;
        if (this.meshFormat==OTHELLO_TYPE){
            offset=0.5;
        }
        double checkX=(x-this.getStartX())/cellSizeX+offset;
        double checkY=(y-this.getStartY())/cellSizeY+offset;
        MeshCell result=null;

        if (0<=checkX && checkX<mesh[0].length && 0<=checkY && checkY<mesh.length) {
            result=mesh[(int)(checkY)][(int)(checkX)];
        }
        return result;
    }

    /** メッシュセルを更新します。
     * m単位の座標ではなくn番目で指定します */
    public void setMeshCell(int x, int y, MeshCell cell){
        mesh[x][y]=cell;
    }

    /** 2点間の見通しを取得します<br>
     * 高度の取得時には，地形メッシュに対してバイリニア補間を掛けています． */
    @Override
    public boolean getLos(Position start, Position end) {
        int devide=(int)((start.getDistance(end))/this.getMeshCellSizeX()+1); // 最低でも1
        boolean result=true;
        for(int step=0;step<devide;step++){
            double x=(start.getX()*(devide-step)+end.getX()*step)/devide;
            double y=(start.getY()*(devide-step)+end.getY()*step)/devide;
            double z=(start.getZ()*(devide-step)+end.getZ()*step)/devide;
            double altitude=this.getAltitudeBilinearIp(x, y);
            if (z<altitude){
                result=false;
                break;
            }
        }

        return result;
    }

    /** ある座標の高度を取得します */
    @Override
    public double getAltitude(double x,double y) {
        double altitude=DEFAULT_ALTITUDE;
        MeshCell cell=this.getMeshCell(x, y);
        if (cell!=null){
            altitude=cell.getAltitude();
        }
        return altitude;
    }

    /** ある座標のバイリニア補完された高度を取得します */
    public double getAltitudeBilinearIp(double x, double y){
        // ワールド座標をメッシュセルに正規化
        double checkX=(x-this.getStartX())/cellSizeX;
        double checkY=(y-this.getStartY())/cellSizeY;

        // 基準座標(対象座標を含む2x2メッシュの左下座標)決定
        int baseX=(int)(checkX);
        int baseY=(int)(checkY);

        double[][] altitude=new double[2][2];

        MeshCell[][] mesh=this.getMesh();
        for (int i=0;i<=1;i++){
            for (int j=0;j<=1;j++){
                altitude[i][j]=DEFAULT_ALTITUDE;
                if (0<=baseX+j && baseX+j<mesh[0].length && 0<=baseY+i && baseY+i<mesh.length) {
                    altitude[i][j]=mesh[baseY+i][baseX+j].getAltitude();
                }
            }
        }

        double diffX=checkX-baseX;
        double diffY=checkY-baseY;
        double interpolatedAltitude=(1-diffX)*(1-diffY)*altitude[0][0]+(1-diffX)*diffY*altitude[1][0]+diffX*(1-diffY)*altitude[0][1]+diffX*diffY*altitude[1][1];

        return interpolatedAltitude;
    }

    /** ジオメトリの大きさやメッシュの縦横の要素数などのチェック */
    private void checkStatus(){
        if (this.mesh!=null){
            this.sizeYint=this.getMesh().length;
            this.sizeXint=this.getMesh()[0].length;
            this.geoSizeY=this.cellSizeY*this.getMesh().length;
            this.geoSizeX=this.cellSizeX*this.getMesh()[0].length;
        }
    }

    /** クローンメソッドのオーバーライド<br>
     * メッシュまでコピーを作成したインスタンスを作成します。 */
    @Override
    public SimpleMeshGeometry clone(){
        SimpleMeshGeometry result=new SimpleMeshGeometry(this.sizeXint, this.sizeYint, this.cellClass);

        // メッシュをコピー
        MeshCell[][] cloneMess=new MeshCell[this.mesh.length][];
        for (int y=0;y<cloneMess.length;y++) {
            int length=this.mesh[y].length;
            cloneMess[y]=new MeshCell[length];
            for (int x=0;x<length;x++){
                cloneMess[y][x]=((MeshCell)this.mesh[y][x]).clone();
            }
        }

        System.out.println("DEBUG: Clone geometory");

        return result;
    }

    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage sdp=super.saveStatus();
        sdp.addData("nummesh_x", this.sizeXint);
        sdp.addData("nummesh_y", this.sizeYint);
        sdp.addData("meshsize", this.cellSizeX);
        sdp.addData("meshsizeY", this.cellSizeY);
        sdp.addData("meshclass",this.mesh[0][0].getClass().getName()); // 名前で登録

        if (this.isImmutable()){
            // 固定であれば特に何もしない
        }else{

            //変化があった場合，変化したメッシュのみを保存する
            for (int y=0;y<this.mesh.length;y++){
                for (int x=0;x<this.mesh[y].length;x++){
                    MeshCell cell=this.mesh[y][x];
                    if (cell.isChanged()){
                        sdp.addChildPackage(cell);
                    }
                }
            }
        }
        return sdp;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        super.restoreStatus(saveData);
        this.sizeXint=(Integer)saveData.getData("nummesh_x");
        this.sizeYint=(Integer)saveData.getData("nummesh_y");
        this.cellSizeX=(Double)saveData.getData("meshsize");
        this.cellSizeY=(Double)saveData.getData("meshsizeY");
        try{
            this.setCellClass((Class<? extends MeshCell>)Class.forName((String)saveData.getData("meshclass")));
        }catch(Exception e){
            e.printStackTrace();
        }

        // メッシュを復元
        this.mesh=this.createMesh(this.sizeXint, this.sizeYint);
        for (SaveDataPackage child:saveData.getAllChildren()){
            if (MeshCell.class.isAssignableFrom(child.getOwnerClass())){
                MeshCell cell=(MeshCell)child.restore();
                if (cell==null){
                    System.exit(-1);
                }
                cell.setChangedFlag(false); // 念のため変化フラグを倒しておく
                this.mesh[cell.getMeshX()][cell.getMeshY()]=cell;
            }
        }

        return this;
    }
}
