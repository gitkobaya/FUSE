package jp.ac.nihon_u.cit.su.furulab.fuse.gui.aiview;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

import jp.ac.nihon_u.cit.su.furulab.fuse.ai.Condition;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseAIBase;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.AIBaseByScript;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.ActionByScript;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.ConditionByScript;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.TaskByScript;

/** タスクや条件を表示するための基本クラスです<br> */
abstract class GUIPart extends JComponent{
    public static final int DEFAULT_XSIZE=160;
    public static final int DEFAULT_YSIZE=16;

    private List<GUIPart> parents=new ArrayList<GUIPart>();

    private FuseAIBase origin;
    private int type;

    private Font font=new Font("Gothic", Font.PLAIN, 14);
    private List<String> fieldsDatum=new ArrayList<String>(); // 表示用のフィールドデータ

    /** コンストラクタで構成要素を指定します */
    public GUIPart(FuseAIBase base) {
        this.origin=base;

        // 標準サイズの設定
        this.setSize(DEFAULT_XSIZE, DEFAULT_YSIZE);

        if (this.origin instanceof AIBaseByScript){
            // フィールド解析(スクリプトの場合)
            AIBaseByScript target=(AIBaseByScript)this.origin;
            for (int i=0;i<target.getFields().length;i++){
                Object field=target.getFields()[i];
                String data="NULL";
                if (field!=null){
                    data=field.getClass().getSimpleName()+" "+field.toString();
                }
                this.fieldsDatum.add(data);
            }
        }else{
            // フィールド解析(ネイティブの場合)
            Field[] fields=base.getClass().getDeclaredFields();
            for (Field f:fields){

                if ( !Modifier.isPublic(f.getModifiers())) {
                    f.setAccessible(true);
                }

                String line=f.getType().getSimpleName()+" "+f.getName();
                try{
                    line=new String(line+":"+f.get(base));
                    this.fieldsDatum.add(line);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        // 名前の設定
        if (this.getAIBase() instanceof TaskByScript){
            TaskByScript thisTask=(TaskByScript)this.getAIBase();
            this.setName(thisTask.getName());
        }else if (this.getAIBase() instanceof ConditionByScript){
            ConditionByScript thisCond=(ConditionByScript)this.getAIBase();
            this.setName(thisCond.getName());
        }else if (this.getAIBase() instanceof ActionByScript){
            ActionByScript thisAct=(ActionByScript)this.getAIBase();
            this.setName(thisAct.getName());
        }else{
            this.setName(this.getAIBase().getClass().getSimpleName());
        }

    }

    /** マウスインプットリスナーを設定します */
    public void setMouseInputListner(MouseInputListener listner){
        this.addMouseListener(listner);
        this.addMouseMotionListener(listner);
    }

    /** AIエレメントのフィールド情報を取得します */
    public List<String> getFieldDatum(){
        return this.fieldsDatum;
    }

    /** AIエレメントのフィールド情報を指定した座標に書き込みます */
    public void drawFields(int x, int y, Graphics g){
        int fontSize=g.getFont().getSize();
        FuseAIBase base=this.getAIBase();
        for (int i=0;i<this.fieldsDatum.size();i++){
            g.drawString(this.fieldsDatum.get(i), x, y+fontSize*i);
        }
    }

    /** このパーツに対応する構成要素を返します */
    public FuseAIBase getAIBase(){
        return this.origin;
    }

    /** 構成要素のタイプを取得します */
    public int getCaspaType(){
        return this.type;
    }

    /** 親になるパーツを登録します<br>
     * 外から呼ばれることはありません */
    protected void addParent(GUIPart parent){
        this.parents.add(parent);
    }

    /** 親パーツ一覧を取得します */
    public List<GUIPart> getParentParts(){
        return this.parents;
    }

    /** このパーツのあるべき大きさを取得します */
    abstract public int getEstimateWidth();

    /** このパーツのあるべき大きさを取得します */
    abstract public int getEstimateHeight();

    /** フォントサイズを取得します */
    public int getFontSize(){
        return this.font.getSize();
    }

    /** フォントを取得します */
    public Font getFont(){
        return this.font;
    }

    /** フォントを設定します */
    public void setFont(Font font){
        this.font=font;
    }

    /** 描画メソッド */
    @Override
    public void paintComponent(Graphics g){
        g.setColor(Color.black);
        g.drawLine(0, 0, this.getWidth(), this.getHeight());

        System.out.println("DEBUG: GUI"+this.getClass());
    }
}