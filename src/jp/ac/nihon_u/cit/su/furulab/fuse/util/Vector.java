package jp.ac.nihon_u.cit.su.furulab.fuse.util;

/** ベクトルクラスです<br>
 * 値を保持し，演算することができます．<br>
 * ベクトル長は任意の長さを設定可能です． */
public class Vector {
    private double[] elements;

    /** コンストラクタです */
    public Vector(double... elem) {
        elements=elem.clone();
    }

    /** コンストラクタで元データをコピーします */
    public Vector(Vector vec) {
        this.elements=vec.get();
    }

    public Vector(double x, double y){
        this(new double[]{x,y});
    }

    public Vector(double x, double y, double z){
        this(new double[]{x,y,z});
    }

    /** ベクトルのX成分を取得 */
    public double getX(){
        return this.elements[0];
    }

    /** ベクトルのY成分を取得 */
    public double getY(){
        return this.elements[1];
    }

    /** ベクトルのZ成分を取得 */
    public double getZ(){
        return this.elements[2];
    }


    /** 配列として取得 */
   public double[] get(){
       return elements.clone();
   }

   /** 可変長引数で要素を設定 */
   public void set(double... vectorArray){
       this.elements=vectorArray;
   }

    /** ベクトルの足し算
     * @throws Exception */
    public Vector add(Vector vec) throws RuntimeException{
        if (elements.length!=vec.get().length){
            throw new RuntimeException("Can't match the length of Vectors. me:"+elements.length+" attr:"+vec.get().length);
        }
        for(int i=0;i<elements.length;i++){
            elements[i]=elements[i]+vec.get()[i];
        }
        return this;
    }

    /** スカラーとベクトルの掛け算 */
    public Vector mult(double value){
        for(int i=0;i<elements.length;i++){
            elements[i]=elements[i]*value;
        }
        return this;
    }

    /** 内積の取得 */
    public double getDot(Vector vec) throws RuntimeException{
        double dot=0;
        if (elements.length!=vec.get().length){
            throw new RuntimeException("Can't match the length of Vectors. me:"+elements.length+" attr:"+vec.get().length);
        }
        for(int i=0;i<elements.length;i++){
            dot+=elements[i]*vec.get()[i];
        }
        return dot;
    }

    /** 外積の取得 */
    public Vector getCross3(Vector vec) throws RuntimeException{
        if (elements.length!=3 || vec.getDimension()!=3){
            throw new RuntimeException("Can't match the length of Vectors. me:"+elements.length+" attr:"+vec.get().length);
        }
        double[] vecElem=vec.get();
        return new Vector(elements[1]*vecElem[2]-elements[2]*vecElem[1], elements[2]*vecElem[0]-elements[0]*vecElem[2], elements[0]*vecElem[1]-elements[1]*vecElem[0]);
    }

    /** ベクトルの大きさの取得 */
    public double getAbs(){
        double result=0;
        for(int i=0;i<elements.length;i++){
            result+=elements[i]*elements[i];
        }
        return Math.sqrt(result);
    }

    /** ベクトルの大きさの取得 */
    public static double getAbs(Vector vec){
        return Vector.getAbs(vec.get());
    }

    /** ベクトルの大きさの取得 */
    public static double getAbs(double[] vec){
        double result=0;
        for(int i=0;i<vec.length;i++){
            result+=vec[i]*vec[i];
        }
        return Math.sqrt(result);
    }

    /** 正規化 */
    public Vector normalize(){
        double size=this.getAbs();
        for(int i=0;i<elements.length;i++){
            elements[i]=elements[i]/size;
        }
        return this;
    }

    /** 次元の取得 */
    public int getDimension(){
        return elements.length;
    }

    /** 次元の設定<br>
     * 現在より大きい次元を指定した場合はゼロで埋められ、
     * 小さい次元を指定した場合は切り捨てられます。 */
    public void setDimension(int dim){
        double[] newElem=new double[dim];

        for(int i=0;i<dim;i++){
            if (i<elements.length){
                newElem[i]=elements[i];
            }else{
                newElem[i]=0;
            }
        }
        elements=newElem; // 入れ替え
    }
}
