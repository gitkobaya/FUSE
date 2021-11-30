package jp.ac.nihon_u.cit.su.furulab.fuse.save;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** XMLの操作関係のクラスです */
public class XmlManager {

    /** ノードリストをリストにします */
    public static List<Element> getElements(NodeList list){
        ArrayList<Element> elements=new ArrayList<Element>();

        int length=list.getLength();
        for(int i=0;i<length;i++){
            Node n=list.item(i);
            // ノードがエレメントだった場合のみリストに追加する
            if (n.getNodeType()==Node.ELEMENT_NODE){
                elements.add((Element)n);
            }
        }
        return elements;
    }
}
