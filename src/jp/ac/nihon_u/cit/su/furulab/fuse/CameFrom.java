package jp.ac.nihon_u.cit.su.furulab.fuse;

public class CameFrom {
	
	// メンバー変数
	private FuseNode currnt, prev;
	private double distance;
	private double totalScore;
	
    /** コンストラクタです。
    * 引数は現在のノード、一つ前のノード、スタート位置からの距離、トータルスコアです（スタートからの距離＋ヒューリスティック）。*/
    public CameFrom(FuseNode current, FuseNode prev, double distance, double totalScore) {
    	this.currnt = current;
    	this.prev = prev;
    	this.distance = distance;
    	this.totalScore = totalScore;
    }
    
    /** 現在のノードを返すメソッド */
    public FuseNode getCurrentNode() {
    	return this.currnt;
    }
    
    /** 一つ前のノードを返すメソッド */
    public FuseNode getPrevNode() {
    	return prev;
    }
    
    /** スタート位置からの距離を返すメソッド */
    public double getDist() {
    	return this.distance;
    }
    
    /** トータルスコア（スタートからの距離＋ヒューリスティック）を返すメソッド */
    public double getTotalCost() {
    	return this.totalScore;
    }
}
