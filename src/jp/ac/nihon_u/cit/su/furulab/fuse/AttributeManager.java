package jp.ac.nihon_u.cit.su.furulab.fuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;

/** アトリビュートを管理するクラスです */
public class AttributeManager implements Savable{
    private static AttributeManager manager;
    private ReadWriteLock attributesUpdateLock=new ReentrantReadWriteLock(); // アトリビュートメイン領域の更新中ロックです
    private ReadWriteLock tempAttributesLock=new ReentrantReadWriteLock(); // 一時領域のロックです

    private Map<Long,Attribute> attributes=new HashMap<Long, Attribute>(); // 現在保持しているアトリビュート
    private Map<Long,HashSet<Attribute>> agtIdToAttr=new HashMap<Long, HashSet<Attribute>>(); // エージェントIDとアトリビュートキーの関係
    private Map<Integer,HashSet<Attribute>> classHashToAttr=new HashMap<Integer, HashSet<Attribute>>(); // クラスのハッシュ値とアトリビュートキーの関係

    private HashSet<Attribute> removeTargetAttributes=new HashSet<Attribute>(); // 削除対象のアトリビュートです
    private Map<Long,Attribute> tempAttributes=new HashMap<Long, Attribute>(); // 一時アトリビュート

    private AttributeManager() {
    }

    /** シングルトンパターンのためのメソッドです */
    public static AttributeManager getInstance(){
        if (manager==null){
            manager=new AttributeManager();
        }
        return manager;
    }

    /** 一時アトリビュートをまとめて追加します<br>
     * エージェントからのアトリビュート更新要求はまずここに蓄えられます */
    public void addPublishAttributes(List<Attribute> attributes){
        try{
            this.tempAttributesLock.writeLock().lock();
            for (Attribute attr:attributes){
                this.tempAttributes.put(attr.getAttrHash(), attr);
            }
        }finally{
            this.tempAttributesLock.writeLock().unlock();
        }
    }

    /** 一時アトリビュートを追加します<br>
     * エージェントからのアトリビュート更新要求はまずここに蓄えられます */
    public void addPublishAttribute(Attribute attr){
        this.tempAttributesLock.writeLock().lock();
        try{
            this.tempAttributes.put(attr.getAttrHash(), attr);
        }finally{
            this.tempAttributesLock.writeLock().unlock();
        }
    }


    /** アトリビュートを削除します */
    public void removeAttribute(Long attrKey){
        Lock tempAttrWriteLock=this.tempAttributesLock.writeLock();
        try{
            tempAttrWriteLock.lock();
            Attribute removedAttr=this.tempAttributes.remove(attrKey);
            if (removedAttr!=null){
                this.removeTargetAttributes.add(removedAttr);
            }
        }finally{
            tempAttrWriteLock.unlock();
        }
    }

    /** 退場したエージェントのアトリビュートを削除します<br>
     * 更新した後に実施します */
    public void deleteDeadAgentAttributes(List<Long> deadAgentIds){
        try{
            this.attributesUpdateLock.writeLock().lock();
            for (Long id:deadAgentIds){
                HashSet<Attribute> attrSet=this.agtIdToAttr.get(id);
                if (attrSet!=null){
                    for (Attribute attr:attrSet){
                        this.attributes.remove(attr.getAttrHash());
                    }
                    this.agtIdToAttr.remove(id);
                }
            }
        }finally{
            this.attributesUpdateLock.writeLock().unlock();
        }
    }

    /** アトリビュートを取得します */
    public Attribute getAttribute(long attrKey){
        Attribute result=null;
        this.attributesUpdateLock.readLock().lock();
        try{
            result=this.attributes.get(attrKey);
        }finally{
            this.attributesUpdateLock.readLock().unlock();
        }
        return result;
    }

    /** エージェントIDを指定してアトリビュートを取得します */
    public List<Attribute> getAttributesByAgentId(long agentId){
        HashSet<Attribute> attributes=this.agtIdToAttr.get(agentId);
        return new ArrayList<Attribute>(attributes);
    }

    /** エージェントのクラスを指定してアトリビュートを取得します */
    public List<Attribute> getAttributesByClass(Class<? extends VirtualObject> classObj){
        HashSet<Attribute> attributes=this.classHashToAttr.get(classObj.getName().hashCode());
        return new ArrayList<Attribute>(attributes);
    }

    /** 一時アトリビュートを保存アトリビュートへ更新します */
    public void updateAttributes(){
        Lock attrWriteLock=this.attributesUpdateLock.writeLock();
        Lock tempAttrWriteLock=this.tempAttributesLock.writeLock();

        try{
            attrWriteLock.lock();
            tempAttrWriteLock.lock();
            for (Attribute attr:this.tempAttributes.values()){
                this.attributes.put(attr.getAttrHash(), attr);

                // エージェントIDとアトリビュートIDの関係を設定する
                long ownerId=attr.getOwnerId();
                HashSet<Attribute> attrs=this.agtIdToAttr.get(ownerId);
                if (attrs==null){
                    attrs=new HashSet<Attribute>();
                    this.agtIdToAttr.put(ownerId, attrs);
                }
                attrs.add(attr);

                // エージェントクラスとアトリビュートIDの関係を設定する
                long ownerClassName=attr.getOwnerClassNameHash();
                attrs=this.classHashToAttr.get(ownerClassName);
                if (attrs==null){
                    attrs=new HashSet<Attribute>();
                    this.agtIdToAttr.put(ownerClassName, attrs);
                }
                attrs.add(attr);
            }
            // 削除されたアトリビュートを反映します
            for (Attribute removed:this.removeTargetAttributes){
                this.attributes.remove(removed);
            }
            this.tempAttributes.clear();
            this.removeTargetAttributes.clear();
        }finally{
            attrWriteLock.unlock();
            tempAttrWriteLock.unlock();
        }
    }

    /** アトリビュートのハッシュ値を計算します */
    public static long getAttributeHash(long agentId, String attributeName){
        return (agentId^attributeName.hashCode());
    }

    /** セーブがかかるタイミングでは一時アトリビュートは存在しないはずなので，
     * メインのアトリビュートを保存します */
    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=new SaveDataPackage(this);
        this.attributesUpdateLock.readLock().lock();
        try{
            for (Attribute attr:this.attributes.values()){
                pack.addChildPackage(attr);
            }
        }finally{
            this.attributesUpdateLock.readLock().unlock();
        }
        return pack;
    }

    @Override
    public Savable restoreStatus(SaveDataPackage saveData) {
        this.attributesUpdateLock.writeLock().lock();
        this.attributes.clear();
        this.tempAttributes.clear();
        try{
            List<SaveDataPackage> children=saveData.getAllChildren();
            for (SaveDataPackage child:children){
                Attribute attr=(Attribute)child.restore();
                this.attributes.put(attr.getAttrHash(), attr);
            }
        }finally{
            this.attributesUpdateLock.writeLock().unlock();
        }
        return this;
    }
}
