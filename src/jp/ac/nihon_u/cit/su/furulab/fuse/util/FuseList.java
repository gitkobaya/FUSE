package jp.ac.nihon_u.cit.su.furulab.fuse.util;

import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.save.Savable;

/** Savableに対応したリストです */
public interface FuseList<T extends Savable> extends List<T>, Savable{

}
