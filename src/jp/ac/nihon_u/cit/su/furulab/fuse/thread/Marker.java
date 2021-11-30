package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

/** スレッドの担当エージェントを指示するための構造体です */
public class Marker {
    public int start;
    public int size;

    public Marker(int start, int size) {
        this.start=start;
        this.size=size;
    }
}
