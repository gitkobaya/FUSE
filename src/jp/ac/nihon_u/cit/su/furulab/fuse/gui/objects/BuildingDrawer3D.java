package jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.jogamp.graph.curve.OutlineShape.VerticesState;

import jnr.ffi.Struct.size_t;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Building;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Vector;
import k7system.gpuobjects.BasicMaterial;
import k7system.gpuobjects.VertexArrayObject;
import k7system.gpuobjects.VertexPackage;

public class BuildingDrawer3D extends ObjectDrawer3D{

    private static BasicMaterial buildingMat;

    static{
        buildingMat=new BasicMaterial();
        buildingMat.setColor(0.75f, 0.75f, 0.75f);
        buildingMat.setSpecularColor(0.1f, 0.1f, 0.1f);
    }

    public BuildingDrawer3D() {
        System.out.println("DEBUG: BuildingDrawer is initialized");
    }

    /** もっとも東側の点を取得します */
    public Position getEastestPoint(List<Position> posList){
        Position eastest=null;
        for (Position pos:posList){
            if (eastest==null || eastest.getX()<pos.getX()){
                eastest=pos;
            }
        }
        return eastest;
    }

    /** 左回りかどうかを判定します<br>
     * 東側の壁が直線だった場合，判定に失敗する場合があります */
    public boolean isLeftHanded(List<Position> posList){
        boolean result=true;
        Position eastest=this.getEastestPoint(posList);
        Position next=null;
        Position previous=null;
        int indexOfEastest=posList.indexOf(eastest); // 最東端を取得
        int indexOfNext=-1;
        int indexOfPrevious=-1;

        // 最東端の一つ前の座標を取得
        if (indexOfEastest==0){
            indexOfPrevious=posList.size()-2; // 最初と最後は同じ値なので
        }else{
            indexOfPrevious=indexOfEastest-1;
        }
        previous=posList.get(indexOfPrevious);

        // 最東端の一つ後の座標を取得
        if (indexOfEastest==posList.size()-1){
            indexOfNext=0;
        }else{
            indexOfNext=indexOfEastest+1;
        }
        next=posList.get(indexOfNext);

        Vector vecPrev=previous.getVectorTo(eastest);
        Vector vecNext=eastest.getVectorTo(next);
        Vector gaiseki=vecPrev.getCross3(vecNext);
        if (gaiseki.getZ()>0){
            result=true;
        }else if (gaiseki.getZ()<0){
            result=false;
        }

        return result;
    }

    /** 引数で与えられた多角形から三角形を切り出します<br>
     * 引数で与えられたpolygonはその分小さくなります<br>
     * ポリゴンは，始点と終点に同じ座標が設定されているものとします */
    public int[] cutTriangle(List<Position> polygon, List<Position> originalPol){
        int[] result=null;
        int size=polygon.size();
        double vecZ=-1;
        for (int i=0;i<size-2;i++){
            Position current=polygon.get(i);
            Position next=polygon.get(i+1);
            Vector vecCurrentToNext=current.getVectorTo(next); // 始点から次の点へ向かうベクトル
            Vector vecNextToNextNext=next.getVectorTo(polygon.get(i+2)); // 始点から次の点へ向かうベクトル
            Vector vecCurrentToNextNext=current.getVectorTo(polygon.get(i+2));// 始点から次の次の点へ向かうベクトル
            vecZ=vecCurrentToNext.getCross3(vecCurrentToNextNext).getZ();
            if (vecZ>0){ //i, i+1, i+2が左回りだった場合
                if (size-i>3){ // まだ多角形が4角形以上の場合
                    boolean flag=true;
                    // 残りの頂点全てに対して、i,i+1,i+2の三角形の内側にいない事を確認
                    for (int j=i+3;j<size-1;j++){
                        Vector vecCheck=current.getVectorTo(polygon.get(j));
                        Vector vecCheck2=next.getVectorTo(polygon.get(j));
                        if (vecCurrentToNext.getCross3(vecCheck).getZ()>=0 && vecCheck.getCross3(vecCurrentToNextNext).getZ()>=0 &&
                                vecNextToNextNext.getCross3(vecCheck2).getZ()>=0){
                            flag=false; // 三角カット失敗
                            break;
                        }
                    }
                    if (flag){ // 三角カット成功の場合
                        result=new int[3];
                        result[0]=originalPol.indexOf(polygon.get(i));
                        result[1]=originalPol.indexOf(polygon.get(i+1));
                        result[2]=originalPol.indexOf(polygon.get(i+2));
                        polygon.remove(i+1);
                        break;
                    }else{
                        //System.out.println("debug: vtx No.:"+i+"/"+size+"is not good");
                    }
                }else{ // 残った三角形をそのまま返す
                    result=new int[3];
                    result[0]=originalPol.indexOf(polygon.get(i));
                    result[1]=originalPol.indexOf(polygon.get(i+1));
                    result[2]=originalPol.indexOf(polygon.get(i+2));
                    polygon.clear();
                    break;
                }
            }else{
                //System.out.println("error: vtx No.:"+i+"/"+size);
            }
        }
        /*
        if (result==null){
            System.out.println("finalError");
            double ox=polygon.get(0).getX();
            double oy=polygon.get(0).getY();
            System.out.println("Current Shape");
            for (Position p:polygon){
                System.out.println((p.getX()-ox)+","+(p.getY()-oy));
            }
            System.out.println("Original Shape");
            for (Position p:originalPol){
                System.out.println((p.getX()-ox)+","+(p.getY()-oy));
            }
        }
         */
        return result;
    }

    /** 多角形の三角分割を実施します<br>
     * 引数で対象となるポリゴンの含まれる配列，
     * 最初の頂点のオフセット(要素単位)，頂点の数を指定します */
    public List<Integer> divideToTriangle(List<Position> polygon){
        List<Position> dummy=new ArrayList<Position>(polygon); // いったんコピーする
        List<Integer> indices=new ArrayList<Integer>(polygon.size());

        while(dummy.size()>0){
            int[] idxs=this.cutTriangle(dummy, polygon);
            if (idxs!=null){
                for (int i:idxs){
                    indices.add(i);
                }
            }else{
                break;
            }
        }

        return indices;
    }

    /** 建物オブジェクトを登録します */
    @Override
    public void setVirtualObject(VirtualObject obj) {
        super.setVirtualObject(obj);
        Building building=(Building)obj;
        List<Position> shape=building.getShape();
        float height=(float)building.getHeight();
        int size=shape.size();
        boolean turnLeft=this.isLeftHanded(shape);
        if (!turnLeft){
            Collections.reverse(shape);
        }

        int wallVertices=size*2*2*3;
        float[] vertices=new float[wallVertices+size*3]; // 上下及びフラットシェーディング用+屋根用
        List<Integer> indices=new ArrayList<Integer>();

        // 頂点登録
        int offset=4*3;
        for (int i=0;i<size-1;i++){
            Position currentPos=shape.get(i);
            // 4頂点を登録
            vertices[i*offset]=(float)currentPos.getX();
            vertices[i*offset+1]=(float)currentPos.getY();
            vertices[i*offset+2]=(float)(currentPos.getZ()+building.getPosition().getZ());

            vertices[i*offset+3]=(float)currentPos.getX();
            vertices[i*offset+4]=(float)currentPos.getY();
            vertices[i*offset+5]=(float)(currentPos.getZ()+building.getPosition().getZ())+height;

            int next=i+1;
            Position nextPos=shape.get(next);
            vertices[i*offset+6]=(float)nextPos.getX();
            vertices[i*offset+7]=(float)nextPos.getY();
            vertices[i*offset+8]=(float)(nextPos.getZ()+building.getPosition().getZ());

            vertices[i*offset+9]=(float)nextPos.getX();
            vertices[i*offset+10]=(float)nextPos.getY();
            vertices[i*offset+11]=(float)(nextPos.getZ()+building.getPosition().getZ())+height;

            // 屋根用の頂点を登録しておきます
            vertices[wallVertices+i*3]=vertices[i*offset+3];
            vertices[wallVertices+i*3+1]=vertices[i*offset+4];
            vertices[wallVertices+i*3+2]=vertices[i*offset+5];

            indices.add(i*4);
            indices.add(i*4+2);
            indices.add(i*4+3);
            indices.add(i*4);
            indices.add(i*4+3);
            indices.add(i*4+1);
        }

        // 屋根掛け
        List<Integer> roof=this.divideToTriangle(shape);
        for (Integer index:roof){
            indices.add(index+wallVertices/3);
        }

        VertexArrayObject vao=new VertexArrayObject();
        vao.setVertices(vertices);
        vao.setIndices(indices);
        vao.createNormals();

        VertexPackage pack=new VertexPackage(vao, buildingMat);
        this.addVertexPackage(pack);
    }

}
