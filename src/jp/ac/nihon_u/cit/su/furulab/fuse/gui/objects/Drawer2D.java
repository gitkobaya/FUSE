package jp.ac.nihon_u.cit.su.furulab.fuse.gui.objects;

import java.awt.Graphics;

import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel;
import jp.ac.nihon_u.cit.su.furulab.fuse.gui.FusePanel2D;

/** FusePanelで描画するオブジェクトの共通機能です */
public interface Drawer2D {

    /** この描画オブジェクトの優先順位を設定します<br>
     * プライオリティの小さいものほど上に描画されます */
    public int getLayer();

    /** 描画メソッドです */
    public void draw(Graphics g, FusePanel2D panel);

}
