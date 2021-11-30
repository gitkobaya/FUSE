package samples;

import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.logging.Loggable;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.logging.ObjectStatus;
import jp.ac.nihon_u.cit.su.furulab.fuse.util.Position;

public class SimpleAgent extends Agent implements Loggable{
    private Side side=Side.BLUE_FORCE;
    private int hitPoint=500;

    /** 陣営を取得します */
    public Side getSide() {
        return side;
    }

    /** 陣営を設定します */
    public void setSide(Side side) {
        this.side = side;
    }

    /** HPを取得します */
    public int getHp(){
        return this.hitPoint;
    }

    @Override
    public void action(long timeStep) {
        SimulationEngine engine=this.getEngine();
        List<Agent> agts=engine.getAllAgents();

        SimpleAgent nearest=null;
        double distance=Double.MAX_VALUE;
        for (Agent agt:agts){
            double dist=this.getPosition().getDistance(agt.getPosition());
            if (distance>dist && agt instanceof SimpleAgent && ((SimpleAgent)agt).getSide()!=this.getSide()){
                nearest=(SimpleAgent)agt;
                distance=dist;
            }
        }

        Position otherPos=nearest.getPosition();

        this.setYaw(Math.atan2(otherPos.getY()-this.getY(), otherPos.getX()-this.getX()));
        this.forward(1);
    }

    @Override
    public boolean isChanged() {
        return true;
    }

    @Override
    public ObjectStatus getObjectStatus() {
        ObjectStatus status=new ObjectStatus(this);
        status.addData("hp", this.hitPoint);
        return status;
    }

    @Override
    public void setObjectStatus(ObjectStatus status) {
        this.hitPoint=(Integer)status.getData("hp");
    }

    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=super.saveStatus();
        pack.addData("hitPoint", this.hitPoint);
        pack.addData("side", this.side);
        return pack;
    }

    @Override
    public Agent restoreStatus(SaveDataPackage saveData) {
        super.restoreStatus(saveData);
        this.hitPoint=(Integer)saveData.getData("hitPoint");
        this.side=(Side)saveData.getData("side");
        return this;
    }

}

enum Side {
    RED_FORCE,
    BLUE_FORCE,
}
