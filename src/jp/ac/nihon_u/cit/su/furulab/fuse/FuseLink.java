package jp.ac.nihon_u.cit.su.furulab.fuse;

/* FuseLinkクラス
 * FuseNodeの接続情報
 * @date 2012/10/03
 * @author yuki_shiho
 */
public class FuseLink {

    // メンバー変数
    private FuseNode start, end;
    private double distance;


    /** コンストラクタです。<br>
    * 引数はスタートノード、エンドノード、その距離です。*/
    public FuseLink(FuseNode start, FuseNode end, double distance) {
       this.start = start;
       this.end = end;
       this.distance = distance;
    }
 
	/** Linkの距離を返すメソッド<br>
	 * 経路探索に使う場合の距離を表すため，必ずしも物理的な距離とは一致しない場合があります．
	 * 戻り値：double distance*/
	public double getDistance() {
		return this.distance;
	}

	/** Linkの距離を設定するメソッド */
        public void setDistance(double dist) {
            this.distance=dist;
        }

       /** 接続元のFuseNodeを返すメソッド
         * 戻り値：FuseNode start */
        public FuseNode getStart() {
                return this.start;
        }


	/** 接続先のFuseNodeを返すメソッド
	 * 戻り値：FuseNode destination*/
	public FuseNode getDestination() {
		return this.end;
	}
}