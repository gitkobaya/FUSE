package jp.ac.nihon_u.cit.su.furulab.fuse;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/* FuseNodeクラス
 * @date 2012/10/03
 * @author yuki_shiho
 */
public class FuseNode implements Savable{
    // メンバー変数
    private long id;
    private double[] elements;
    private Position position;
    private LinkedList<FuseLink> links=new LinkedList<FuseLink>();

    /** 汎用の属性情報です */
    private Object property=null;

    public FuseNode(double[] elm) {
        this.id=this.hashCode();
        this.elements=elm;
        this.position=new Position(this.elements);
    }
 
    public FuseNode(double x, double y){
        this.id=this.hashCode();
        this.elements=new double[]{x,y,0};
        this.position=new Position(this.elements);
    }

    public FuseNode(double x, double y, double z){
        this.id=this.hashCode();
        this.elements=new double[]{x,y,z};
        this.position=new Position(this.elements);
    }

    /** コンストラクタ */
    public FuseNode( long  id, double[] elm ) {
        this.id = id;
        this.elements = elm;
        this.position=new Position(elements);
    }

    public FuseNode(long id, double x, double y) {
        // double配列を作成して上のコンストラクタを呼んでるよん
        this( id, new  double[]{x, y, 0} );
    }

    public FuseNode(long id, double x, double y, double z) {
        this(id, new double[]{x, y, z});
    }

    /** 汎用属性情報を取得します */
    public Object getProperty(){
        return this.property;
    }

    /** 汎用属性情報を設定します */
    public void setProperty(Object property){
        this.property=property;
    }

    /** IDの取得 */
    public long getId(){
        return this.id;
    }

    /** このノードから出発しているlinkの数を取得します */
    public int getNumOfLinks(){
        return this.links.size();
    }

    /** linkの追加
     * 引数：linkのインスタンス */
    public void addLink(FuseLink link) {
        this.links.add(link);
    }

    /** linkの追加
     * 引数：他のノードとそのノードまでの距離 */
    public void addLink(FuseNode node, double distance) {
        FuseLink link=new FuseLink(this, node, distance);
        this.addLink(link);
    }

    /** linkの削除
     * 引数：linkのインスタンス */
    public void removeLink(FuseLink link) {
        this.links.remove(link);
    }

    /** linkの全削除です<br>
     * ただし、自分からのリンクは切断されるが自分を対象としたリンクは残ります */
    public void removeAllLinks() {
        this.links.clear();
    }

    /** linkの全削除です<br>
     * このノードに向かうリンクも切断します */
    public void removeAllLinksCompletely(){
        for(FuseLink link:this.getLinks()){
            FuseNode target=link.getDestination();
            target.removeLink(target.getLink(this));
        }
        this.removeAllLinks();
    }

    /** x, y, z座標の取得
     * 戻り値：double[] elements;
     */
    public double[] getPosByArray() {
        return this.elements;
    }

    /** x, y, z座標の取得
     * 戻り値：Position;
     */
    public Position getPosition(){
        return this.position;
    }

    /** このノードに隣接したノードのリストを返します */
    public List<FuseNode> getNextNodes(){
        List<FuseNode> nexts=new ArrayList<FuseNode>(this.getNumOfLinks());
        for (FuseLink link:this.getLinks()){
            nexts.add(link.getDestination());
        }
        return nexts;
    }

    /** linkの取得です<br>
     * 相手ノードを指定することでそこに至るリンクを取得します．<br>
     * そのようなリンクが無ければnullが返ります */
    public FuseLink getLink(FuseNode targetNode){
        FuseLink result=null;
        for(FuseLink link:this.getLinks()){
            if (link.getDestination()==targetNode){
                result=link;
                break;
            }
        }
        return result;
    }

    /** 全Linkの取得<br>
     * 戻り値：LinkedList<Edge> link;
     */
    public LinkedList<FuseLink> getLinks() {
        return this.links;
    }

    /** コピーメソッド<br>
     * 座標は新しい配列を作ってコピー元から複製されていますが、<br>
     * リンクについてはコピー元と同じインスタンスを参照しているので注意すること */
    @Override
    public FuseNode clone(){
        double[] pos=this.getPosByArray();
        FuseNode newNode=new FuseNode(this.getId(),pos[0],pos[1],pos[2]);

        for(FuseLink link:this.getLinks()){
            newNode.addLink(link);
        }

        return newNode;
    }

    /** リンクを無視してノードのみを複製します */
    public FuseNode simpleClone(){
        double[] pos=this.getPosByArray();
        FuseNode newNode=new FuseNode(this.getId(),pos[0],pos[1],pos[2]);
        return newNode;
    }

    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage sdp=new SaveDataPackage(this);
        sdp.addData("nodex",elements[0]);
        sdp.addData("nodey",elements[1]);
        sdp.addData("nodez",elements[2]);
        sdp.addChildPackage(this.position);
        return sdp;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        this.elements=new double[3];
        this.elements[0]=(Double)saveData.getData("nodex:value");
        this.elements[1]=(Double)saveData.getData("nodey:value");
        this.elements[2]=(Double)saveData.getData("nodez:value");
        for(SaveDataPackage pack:saveData.getAllChildren()){
            if (pack.getOwnerClass().equals(Position.class)){
                this.position=(Position)pack.restore();
            }
        }
        return this;
    }
}