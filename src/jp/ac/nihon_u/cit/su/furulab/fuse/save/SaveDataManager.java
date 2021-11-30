package jp.ac.nihon_u.cit.su.furulab.fuse.save;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jnr.ffi.annotations.In;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** セーブデータの保存と復元を担当するクラスです<br>
 * スレッドセーフではないので気を付けてください */
public class SaveDataManager {

    public static final String CLASS_ATTR="class";
    public static final String ID_ATTR="ID";
    public static final String NAME_ATTR="name";
    public static final String HASH_ATTR="hash";
    public static final String TYPE_ATTR="type";
    public static final String DUMMY_ATTR="dummy";
    private static Map<Integer, Savable> objectMap=new HashMap<Integer, Savable>(); // リストア時に使用。static宣言はしたくないのだが他にやり方が思い浮かばない
    private static Map<Integer, SaveDataPackage> packageMap=new HashMap<Integer, SaveDataPackage>(); // セーブ時に使用。static宣言はしたくないのだが他にやり方が思い浮かばない

    /** セーブデータをXML化してファイルに保存します */
    public static void saveSimulation(File saveFile,SaveDataPackage saveData){
        DocumentBuilderFactory dbFactory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder=null;
        Document document=null;

        // パッケージキャッシュをクリア
        clearPackageCache();

        try {
            builder=dbFactory.newDocumentBuilder();
            document=builder.newDocument();

            System.out.println("DEBUG: saving simulation");

            document.appendChild(SaveDataManager.saveData(saveData, document));

            TransformerFactory tfactory = TransformerFactory.newInstance();
            tfactory.setAttribute("indent-number", new Integer(4)); // これを指定しないとインデントがされない
            Transformer transformer = tfactory.newTransformer();

            // encoding="Shift_JIS"を指定
            transformer.setOutputProperty(OutputKeys.ENCODING, "Shift_JIS");

           // インデントを行う
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.transform(new DOMSource(document), new StreamResult(saveFile));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }catch (TransformerException e) {
            e.printStackTrace();
        }

    }

    /** SaveDataの構成要素をXMLのエレメントに分解します */
    private static Element saveData(SaveDataPackage data, Document doc){
        Element element=doc.createElement("data");

        // オーナークラスを登録
        element.setAttribute(CLASS_ATTR, data.getOwnerClassName());

        // オーナー名を登録
        element.setAttribute(NAME_ATTR, data.getName());

        // オブジェクトのハッシュ値を登録
        element.setAttribute(HASH_ATTR,String.valueOf(data.getOwnerHash()));

        // このエレメントがダミーデータかどうかを登録
        if (data.isDummy()){
            element.setAttribute(DUMMY_ATTR, "true");
        }

        // もしそのデータのオーナーがVirtualObjectだったら
        if (data.getOwnerId()>0){
            element.setAttribute(ID_ATTR,String.valueOf(data.getOwnerId()));
        }

        // 変数を抽出
        for (DataContainer cont:data.getAllData()){
            Element elem=doc.createElement(cont.getName());
            Object val=cont.getData();
            String valType;
            if (val!=null){
                elem.setTextContent(val.toString());
                // 変数の型を設定
                valType=val.getClass().getName();
            }else{
                valType="null";
                elem.setTextContent("null");
            }
            elem.setAttribute(TYPE_ATTR, valType);
            element.appendChild(elem);

        }

        // 子オブジェクトを抽出
        for (SaveDataPackage pack:data.getAllChildren()){
            Element elem=SaveDataManager.saveData(pack, doc);
            element.appendChild(elem);
        }
        return element;
    }

    /** classアトリビュートがNULLになっているSDPを作成して返します */
    public static SaveDataPackage restoreSimulation(File filename){
        SaveDataPackage saveData=null;
        DocumentBuilderFactory dbFactory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder=null;
        Document doc=null;

        // オブジェクトキャッシュを削除
        clearObjectCache();

        // Dataを全部抽出
        try{
            builder=dbFactory.newDocumentBuilder();
            doc=builder.parse(filename);
        }catch(Exception e){
            e.printStackTrace();
        }

        NodeList list=doc.getChildNodes();

        // ノードリストからセーブデータの抽出
        for(Element elem:XmlManager.getElements(list)){
            // classアトリビュートが存在した場合
            if (!elem.getAttribute(CLASS_ATTR).equals("")){
                saveData=SaveDataManager.restoreData(elem);
            }
        }

        return saveData;
    }

    /** リストア用のオブジェクトキャッシュを初期化します */
    public static void clearObjectCache(){
        objectMap.clear();
    }

    /** リストア用のオブジェクトキャッシュからオブジェクトを取得します */
    protected static Savable getObject(int key){
        return objectMap.get(key);
    }

    /** リストア用のオブジェクトキャッシュにオブジェクトを登録します */
    protected static void putObject(int key, Savable object){
        objectMap.put(key, object);
    }

    /** セーブ用のパッケージキャッシュを初期化します */
    public static void clearPackageCache(){
        packageMap.clear();
    }

    /** セーブ用のパッケージキャッシュからパッケージを取得します */
    protected static SaveDataPackage getPackage(int key){
        return packageMap.get(key);
    }

    /** セーブ用のパッケージキャッシュにパッケージを登録します */
    protected static void putPakage(int key, SaveDataPackage pack){
        packageMap.put(key, pack);
    }


    /** エレメントからセーブデータを復元します */
    private static SaveDataPackage restoreData(Element elem){
        SaveDataPackage saveData=new SaveDataPackage(null);

        saveData.setOwnerClassName(elem.getAttribute(CLASS_ATTR));

        // IDアトリビュートが存在した場合
        String idStr=elem.getAttribute(ID_ATTR);
        if (idStr!=""){
            saveData.setOwnerId(Long.parseLong(idStr));
        }

        // nameアトリビュートが存在した場合
        String name=elem.getAttribute(NAME_ATTR);
        if (name!=""){
            saveData.setName(name);
        }

        // hashアトリビュートが存在した場合
        String hash=elem.getAttribute(HASH_ATTR);
        if (hash!=""){
            saveData.setOwnerHash(new Integer(hash));
        }

        // ダミーアトリビュートが存在した場合
        String dummy=elem.getAttribute(DUMMY_ATTR);
        if (dummy!=""){
            saveData.setDummy();
        }

        NodeList list=elem.getChildNodes();

        // ノードリストからセーブデータの抽出
        for(Element el:XmlManager.getElements(list)){

            // classアトリビュートが存在した場合 子オブジェクトが存在する
            if (!el.getAttribute(CLASS_ATTR).equals("")){
                saveData.addChildPackage(SaveDataManager.restoreData(el));
            }else{
                String value=el.getFirstChild().getNodeValue();
                saveData.addData(el.getNodeName()+":string",value); // String形式で保存

                String type=el.getAttribute(TYPE_ATTR);
                if (type!=""){
                    Object rawValue=decodeElement(value, type);
                    saveData.addData(el.getNodeName()+":value",rawValue); // 元の形式で保存
                    saveData.addData(el.getNodeName(),rawValue); // 元の形式で保存
                }
            }
        }

        return saveData;
    }

    /** 文字列をオブジェクトに変換して返します<br>
     * プリミティブ型と列挙型に対応しています */
    public static Object decodeElement(String str, String type){
        Object result=null;
        try{
            Class thisClass=Class.forName(type);
            if (thisClass!=null){
                if (thisClass.equals(Boolean.class)){
                    result=Boolean.parseBoolean(str);
                }else if (thisClass.equals(Short.class)){
                    result=Short.parseShort(str);
                }else if (thisClass.equals(Integer.class)){
                    result=Integer.parseInt(str);
                }else if (thisClass.equals(Long.class)){
                    result=Long.parseLong(str);
                }else if (thisClass.equals(Float.class)){
                    result=Float.parseFloat(str);
                }else if (thisClass.equals(Double.class)){
                    result=Double.parseDouble(str);
                }else if (thisClass.equals(String.class)){
                    result=str;
                }else if (Enum.class.isAssignableFrom(thisClass)){
                    result=Enum.valueOf(thisClass, str);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

}
