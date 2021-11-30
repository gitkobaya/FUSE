package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import java.awt.event.*;
import java.util.List;

import javax.swing.event.MouseInputListener;


import jp.ac.nihon_u.cit.su.furulab.fuse.*;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

/** パネル操作用リスナーのスケルトンです */
public abstract class KeyAndMouseListner implements MouseInputListener,MouseWheelListener, KeyListener{

    private FusePanel simPanel;
    private int mouseX,mouseY,mDiffX,mDiffY;
    int mButton; // どのボタンを押しているか

    private boolean isDownCTRL=false;    // CTRLを押しているかどうかのフラグ
    private boolean isDownShift=false; // シフトを押しているかどうかのフラグ

    /** コンストラクタです */
    public KeyAndMouseListner(FusePanel panel) {
        simPanel=panel;
    }

    /** 登録されているSimulationPanelを取得します */
    public FusePanel getSimulationPanel(){
        return this.simPanel;
    }

    /** 現在のマウスX座標を取得します */
    public int getMouseX(){
        return mouseX;
    }

    /** 現在のマウスY座標を取得します */
    public int getMouseY(){
        return mouseY;
    }

    /** 現在のマウス座標にもっとも近いエージェントを取得します */
    public Agent pickAgent(MouseEvent e){
        Position pos=this.simPanel.getWorldPos(e.getX(), e.getY());
        Agent nearestAgent=this.simPanel.getSimulationEngine().getNearestAgentXY(pos.getX(),pos.getY());
        return nearestAgent;
    }

    /** 現在のマウス座標にもっとも近いエージェントで，かつ指定した範囲のエージェントを指定します．
     * 範囲指定の単位はピクセルです */
    public abstract Agent pickAgent(MouseEvent e,int range);

    /** 現在押しているボタンを取得します */
    public int getPressingButton(){
        return this.mButton;
    }

    /** マウスドラッグの監視<br>
     * マウスドラッグは，カメラの座標とフォーカスを制御します．フォーカスとは，カメラがどこを見ているかです．
     * マウスドラッグにより，カメラをカメラ座標系で水平移動させることでフォーカスを移動させます．*/
    @Override
    public void mouseDragged(MouseEvent e) {
        int currentX=e.getX();
        int currentY=e.getY();

        mDiffX=currentX-mouseX;
        mDiffY=currentY-mouseY;

        mouseX=currentX;
        mouseY=currentY;
        //System.out.println("DEBUG: currentX:"+currentX);

        // ドラッグの処理
        if (this.getPressingButton()==MouseEvent.BUTTON1){
            double focusX=simPanel.getFocusX()-mDiffX/simPanel.getDotsByMeter();
            double focusY=simPanel.getFocusY()+mDiffY/simPanel.getDotsByMeter();
            simPanel.setFocus(focusX, focusY);
        }

        if (this.getPressingButton()==MouseEvent.BUTTON2){
            double focusX=simPanel.getFocusX()-mDiffX/simPanel.getDotsByMeter();
            double focusY=simPanel.getFocusY()+mDiffY/simPanel.getDotsByMeter();
            simPanel.setFocus(focusX, focusY);
        }
        //System.out.println("DEBUG: marker dragged x:"+mDiffX+" y:"+mDiffY);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int currentX=e.getX();
        int currentY=e.getY();
        mDiffX=currentX-mouseX;
        mDiffY=currentY-mouseY;
        mouseX=currentX;
        mouseY=currentY;
    }

    /** マウスクリック時の処理 */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /** マウスホイール動作の処理<br>
     * 実際の挙動は実装に依存します */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // TODO 自動生成されたメソッド・スタブ

    }


    /** マウスボタンが押されていた場合 */
    @Override
    public void mousePressed(MouseEvent e) {
        mButton=e.getButton();

        // 表示パネルにフォーカスする
        simPanel.requestFocusInWindow();
    }

    /** マウスリリースの場合の処理 */
    @Override
    public void mouseReleased(MouseEvent e) {
        mButton=0;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    /** キータイプイベント */
    @Override
    public void keyTyped(KeyEvent e) {

        //System.out.println("DEBUG:key typed "+e.getKeyChar());
    }

    /** CTRLボタンが押されているかどうか<br>
     * ただし，keyPressedとKeyReleasedでsuperを呼び出していなければ正常に動作しません */
    public boolean isDownCtrl(){
        return this.isDownCTRL;
    }

    /** シフトボタンが押されているかどうか<br>
     * ただし，keyPressedとKeyReleasedでsuperを呼び出していなければ正常に動作しません */
    public boolean isDownShift(){
        return this.isDownShift;
    }

    /** キー押下イベント<br>
     * キーリピートが発生してよい操作はこちらに記述します。 */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_CONTROL){
            isDownCTRL=true;
        }
        if (e.getKeyCode()==KeyEvent.VK_SHIFT){
            isDownShift=true;
        }
    }

    /** キーリリースイベント <br>
     * キーリピートが発生すると困る処理はこちらに記述します。*/
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_CONTROL){
            isDownCTRL=false;
        }
        if (e.getKeyCode()==KeyEvent.VK_SHIFT){
            isDownShift=false;
        }

        // Z(UNDO)キー押下の処理
        if(e.getKeyCode()==KeyEvent.VK_Z){
            if (isDownCTRL){
                simPanel.undo();
                System.out.println("DEBUG: Undo by Z");
            }
        }

        // Y(REDO)キー押下の処理
        if(e.getKeyCode()==KeyEvent.VK_Y){
            if (isDownCTRL){
                simPanel.redo();
                System.out.println("DEBUG: Redo by Y");
            }
        }
    }

}
