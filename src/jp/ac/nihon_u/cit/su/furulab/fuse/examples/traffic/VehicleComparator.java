package jp.ac.nihon_u.cit.su.furulab.fuse.examples.traffic;

import java.util.Comparator;

import jp.ac.nihon_u.cit.su.furulab.fuse.models.VirtualObject;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

public class VehicleComparator implements Comparator<VirtualObject>{
    private Position start;

    public VehicleComparator(Position start) {
        this.start=start;
    }

    @Override
    public int compare(VirtualObject o1, VirtualObject o2) {
        int result=0;
        double dist1=this.start.getDistance(o1.getPosition());
        double dist2=this.start.getDistance(o2.getPosition());
        if (dist1-dist2<0){
            result=-1;
        }else if (dist1-dist2>0){
            result=1;
        }

        return result;
    }

}
