package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/** 高度に対応した色を取得するためのクラスです */
public class AltitudeColor {
    private ArrayList<ColorOfAltitude> colors=new ArrayList<ColorOfAltitude>();
    private Color defaultColor=new Color(192,255,144); // 薄黄緑

    private int gaugeWidth=24;
    private int gaugeHeight=128;
    private int devided=32; // ゲージ表示の細かさ
    private int altitudeNotch=1000;

    /** 引数なしで起動するとデフォルト設定が入ります */
    public AltitudeColor() {
        this.addAltitudeColor(0, defaultColor);
        this.addAltitudeColor(300, new Color(0,128,0)); // 暗い緑
        this.addAltitudeColor(500, new Color(0,96,0)); // 暗い緑
        this.addAltitudeColor(800, new Color(128,64,0)); // 茶色
        this.addAltitudeColor(1600, new Color(92,32,0)); // 暗い茶色
        this.addAltitudeColor(3000, Color.white); // 白
    }

    /** カラーを初期化します */
    public void clearColor(){
        this.colors.clear();
        this.addAltitudeColor(0, defaultColor);
    }

    /** 指定した高度に対応するカラーを取得します */
    public Color getColor(double altitude){
        Color col=defaultColor;

        // 面倒なのでリニアサーチ
        for(int i=0;i<colors.size();i++){
            // 色が見つからない場合は最高高度の色
            if (i==colors.size()-1){
                col=colors.get(i).getColor();
                break;
            }

            ColorOfAltitude col1=colors.get(i);
            ColorOfAltitude col2=colors.get(i+1);
            if (col1.getAltitude()<=altitude && altitude<col2.getAltitude()){
                // 2つの色の中間色を取得
                double dist=col2.getAltitude()-col1.getAltitude();
                double weight=(col2.getAltitude()-altitude)/dist;
                col=this.mixColor(col1.getColor(), col2.getColor(), weight);
                break;
            }
        }

        return col;
    }

    /** ある高度のカラーを設定します */
    public void addAltitudeColor(double altitude, Color color){
        ColorOfAltitude col=new ColorOfAltitude(altitude,color);
        this.addAltitudeColor(col);
    }

    /** ある高度のカラーを設定します */
    public void addAltitudeColor(ColorOfAltitude coa){
        colors.add(coa);
        Collections.sort(colors, new DataComparator());
    }

    /** 2色を任意の割合で混合します<br>
     * 第3引数はcolor1の割合を0から1までの範囲で設定します */
    public Color mixColor(Color col1, Color col2, double weightCol1){
        double r=col1.getRed()*weightCol1+col2.getRed()*(1.0-weightCol1);
        double g=col1.getGreen()*weightCol1+col2.getGreen()*(1.0-weightCol1);
        double b=col1.getBlue()*weightCol1+col2.getBlue()*(1.0-weightCol1);
        Color mixed=new Color((float)(r/256), (float)(g/256), (float)(b/256));
        return mixed;
    }

    /** カラーが設定してある最大高度を取得します */
    public double getMaxAltitude(){
        return colors.get(colors.size()-1).getAltitude();
    }

    /** カラーゲージを画面に表示します */
    public void drawGauge(Graphics g, int x, int y){
        int railWidth=2;

        g.setColor(new Color(0,0,0,128)); // 黒を指定
        g.fillRect(x, y, railWidth, gaugeHeight);
        g.fillRect(x+gaugeWidth-railWidth, y, railWidth, gaugeHeight);

        for (int i=0;i<devided;i++){
            Color col=this.getColor(this.getMaxAltitude()/devided*i);
            g.setColor(new Color(col.getRed(),col.getGreen(),col.getBlue(),224)); // 微妙に透明
            int starX=x+railWidth;
            int width=gaugeWidth-railWidth*2;
            int startY=y+gaugeHeight-(gaugeHeight/devided)*(i+1);
            int height=(gaugeHeight/devided);
            g.fillRect(starX, startY, width, height);
        }

        // 標高との対応を表示します
        Font font = new Font("Gothic", Font.PLAIN, 12);
        g.setFont(font);
        g.setColor(new Color(0,0,0,255));
        double max=this.getMaxAltitude();
        for (int i=0;i<max;i+=altitudeNotch){
            g.drawString(String.valueOf(i),x+gaugeWidth+railWidth,y+gaugeHeight-(int)(gaugeHeight*i/max)+font.getSize()/2);
        }
        g.drawString(String.valueOf((int)max),x+gaugeWidth+railWidth,y+font.getSize()/2);
    }
}

/** ここでしか使わないクラス */
class ColorOfAltitude{
    private double altitude;
    private Color color;

    /** コンストラクタの時点でパラメータを指定します */
    public ColorOfAltitude(double alti, Color col) {
        altitude=alti;
        color=col;
    }

    public double getAltitude(){
        return altitude;
    }

    public Color getColor(){
        return color;
    }

}

/** 標高値を使ってソートするためのクラス */
class DataComparator implements Comparator<ColorOfAltitude>{

    @Override
    public int compare(ColorOfAltitude o1, ColorOfAltitude o2) {
        return (int)(o1.getAltitude()-o2.getAltitude());
    }
  }