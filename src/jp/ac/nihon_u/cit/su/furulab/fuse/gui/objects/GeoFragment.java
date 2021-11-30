package jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jruby.ir.operands.Array;

import jp.ac.nihon_u.cit.su.furulab.fuse.examples.SimpleMeshGeometry;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Geometry;
import k7system.Model3D;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.Material;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;

/** 地形データを分割したものです */
public class GeoFragment extends Model3D{

    private List<Float> vertices=null;
    private int xSize=0;
    private int ySize=0;
    private Geometry geometry; // 親となる地形オブジェクト

    /** コンストラクタで地形断片モデルを作成する範囲とジオメトリオブジェクトを指定します．*/
    public GeoFragment(double startX, double startY, double sizeX, double sizeY, Geometry geo, BasicMaterial mat) {
        double resolutionX=geo.getResolutionHorizontal();
        double resolutionY=geo.getResolutionVertical();
        this.geometry=geo;
        this.vertices=new ArrayList<Float>((int)(sizeX*sizeY/resolutionX/resolutionY)+1);
        List<Integer> indices=new ArrayList<Integer>((int)(sizeX*sizeY/resolutionX/resolutionY)+1);
        List<Float> uvs=new ArrayList<Float>((int)(sizeX*sizeY/resolutionX/resolutionY)+1);

        int xCount=0; // ループが回った回数を数えるカウンタ．浮動小数点なので念のため
        int yCount=0;
        // 端は「含む」
        double currentAltitude=0;
        for (double y=startY;y<(startY+sizeY)+resolutionY;y+=resolutionY){ // 誤差吸収用にちょっと大きめ
            double prevAlti=SimpleMeshGeometry.DEFAULT_ALTITUDE;
            for (double x=startX;x<(startX+sizeX)+resolutionX;x+=resolutionX){
                currentAltitude=(float)geo.getAltitude(x, y);
                // 応急措置，端座標を取れない抜本的な原因がわかったら取り除く
                if (startX<x && currentAltitude<0){
                    currentAltitude=prevAlti;
                }
                if (currentAltitude<0){ // マイナスを認めない(オランダ禁止)
                    currentAltitude=0;
                }

                this.vertices.add((float)x);
                this.vertices.add((float)y);
                this.vertices.add((float)currentAltitude);

                // UV座標を登録
                double texelSizeX=resolutionX/sizeX;
                double texelSizeY=resolutionY/sizeY;
                float u=(float)((x-startX+resolutionX/2)/(sizeX+resolutionX)); // 端を重複させるため，頂点が一つ多いことに注意
                float v=(float)((y-startY+resolutionY/2)/(sizeY+resolutionX));

                // テクスチャ座標がループしないように調整
                if (u<texelSizeX/2){
                    u=(float)texelSizeX/2;
                }else if (u>1-texelSizeX/2){
                    u=1-(float)texelSizeX/2;
                }
                if (v<texelSizeY/2){
                    v=(float)texelSizeY/2;
                }else if (v>1-texelSizeY/2){
                    v=1-(float)texelSizeY/2;
                }
                uvs.add(u); // 0-1の範囲に規格化しておく
                uvs.add(v);

                if (yCount==0){
                    xCount++;
                }
                prevAlti=currentAltitude;
            }
            yCount++;
        }

        this.xSize=xCount;
        this.ySize=yCount;

        // ポリゴンの生成
        int counter=0;
        for (int y=0;y<yCount-1;y++){
            for (int x=0;x<xCount-1;x++){
                indices.add(y*xCount+x);
                indices.add(y*xCount+x+1);
                indices.add((y+1)*xCount+x);

                indices.add(y*xCount+x+1);
                indices.add((y+1)*xCount+x+1);
                indices.add((y+1)*xCount+x);
                counter+=6; // 6要素追加
            }
        }

        // リストから配列へ変換
        float[] vertexArray=new float[xCount*yCount*3];
        for (int i=0;i<xCount*yCount*3;i++){
            vertexArray[i]=vertices.get(i);
        }

        float[] uvArray=new float[xCount*yCount*2];
        for (int i=0;i<xCount*yCount*2;i++){
            uvArray[i]=uvs.get(i);
        }

        int[] indexArray=new int[counter];
        for (int i=0;i<counter;i++){
            indexArray[i]=indices.get(i);
        }

        VertexArrayObject vao=new VertexArrayObject();
        vao.setVertices(vertexArray);
        vao.setTexCoords(uvArray);
        vao.setIndices(indexArray);
        vao.createNormals();

        VertexPackage vPack=new VertexPackage(vao,mat);
        this.addVertexPackage(vPack);

        // デバッグ用のバウンディングボックス
        /*
        VertexArrayObject bVao=new VertexArrayObject();
        float[] bVertices=vPack.getBoundingBoxFlat();
        bVao.setVerteces(bVertices);
        bVao.setIndices(new int[]{
                0,1,2,2,3,0,
                6,5,4,4,7,6});
        bVao.createNormals();
        BasicMaterial debugMat=new BasicMaterial();
        debugMat.setColor(1, 1, 0, 0.5f);
        VertexPackage debugVp=new VertexPackage(bVao, debugMat);
        */
        //this.setTransparent(Model3D.TRANSPARENT_AVERAGE);
    }

    /** 継承用 */
    protected GeoFragment(){

    }

    /** この地形フラグメントの所属するジオメトリオブジェクトを取得 */
    public Geometry getGeometryObject(){
        return this.geometry;
    }

    /** X方向の頂点数を取得します(メッシュ数じゃないことに注意) */
    public int getNumOfVerticesX(){
        return this.xSize;
    }

    /** Y方向の頂点数を取得します(メッシュ数じゃないことに注意) */
    public int getNumOfVerticesY(){
        return this.ySize;
    }


    /** 頂点情報を取得します */
    public List<Float> getVertices(){
        return this.vertices;
    }

    /** この地形断片のマテリアルを取得します */
    public BasicMaterial getMaterial(){
        return this.getVertexPackages().get(0).getMaterial();
    }
}
