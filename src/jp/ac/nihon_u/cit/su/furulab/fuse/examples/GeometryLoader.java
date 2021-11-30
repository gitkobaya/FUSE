package jp.ac.nihon_u.cit.su.furulab.fuse.examples;

import java.io.File;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.Geometry;

/** ファイルからジオメトリを読み込むローダーです */
public interface GeometryLoader<T extends Geometry> {

    public T loadGeometry(List<File> files);

    public T loadGeometry(File file);

}
