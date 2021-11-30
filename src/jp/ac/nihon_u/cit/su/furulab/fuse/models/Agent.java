package jp.ac.nihon_u.cit.su.furulab.fuse.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import jp.ac.nihon_u.cit.su.furulab.fuse.Attribute;
import jp.ac.nihon_u.cit.su.furulab.fuse.AttributeManager;
import jp.ac.nihon_u.cit.su.furulab.fuse.Environment;
import jp.ac.nihon_u.cit.su.furulab.fuse.Message;
import jp.ac.nihon_u.cit.su.furulab.fuse.NodeManager;
import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.ai.FuseTaskManager;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.DataContainer;
import jp.ac.nihon_u.cit.su.furulab.fuse.save.SaveDataPackage;
import jp.ac.nihon_u.cit.su.furulab.fuse.thread.ExecThreadBase;

/** エージェントクラスです。これを拡張してシミュレーション用のエージェントを作成します。<br>
 *   This is Agent class. You can make your own agents by extending this class. */
public abstract class Agent extends VirtualObject implements MessageReciever{
    public static long BROADCAST_AGENT_ID=-1;

    private SimulationEngine engine;
    private Queue<Message> messageBox=new LinkedList<Message>(); // サーバから受信したメッセージを保存します
    private AttributeManager attrManager;

    private ExecThreadBase myThread=null; //自分の所属するスレッド

    private boolean isMoveByEngine=false; // forwardを呼び出したときにtrueになり、そのサイクルが終わるとまたfalseになる
    private boolean exitFlag=false; // エンジンがこのフラグを感知すると離脱

    //x,y,zの順。エンジンに移動を任せた場合、この値を参照され、次のステップでその分座標が変化
    private double[] velocity=new double[]{0,0,0};
    private double[] direction=new double[]{0,0,0};

    /** 直前の1サイクルの処理にかかった実行時間 */
    private long spentTime=1; // 初期値に0を入れると初回のスレッド割り振りがおかしくなる

    /** このエージェントの処理時間の合計 */
    private long totalSpentTime=0;

    // ローカルのノードメッシュマネージャ
    private NodeManager nodeManager=new NodeManager(); // エージェント自身にとってのノードメッシュ

    /** エージェントのタスクマネージャ */
    private FuseTaskManager manager=new FuseTaskManager(this);

    // ロギング用変数
    private int numOfSendMessage;

    /** エージェントのIDを設定します<br>
     * ただし，このエージェントがBroadcastAgentだった場合，IDは必ずBROADCAST_AGENT_IDになります．<br>
     * This method defines Agent ID<br>
     * If this Agent is BroadcastAgent, then the ID is always BROADCAST_AGENT_ID.*/
    @Override
    public void setId(long id){
        long i=id;
        if (this instanceof BroadCastAgent){
            i=-1;
        }
        super.setId(i);
    }

    /** 自分が所属するシミュレーションエンジンを取得します。<br>
     * This method returns Simulation Engine that this Agent belongs to.*/
    public SimulationEngine getEngine(){
        return engine;
    }

    /** 自分が所属するシミュレーションエンジンへの参照を設定します。<br>
     *  エンジンへの登録時に自動で呼ばれるため、シミュレーション開発者が操作する必要はありません。 <br>
     *  This method sets a reference to Simulation Engine to this agent.<br>
     *  This is called automatically when this Agent joins to Simulation Engine.*/
    public void setEngine(SimulationEngine eng){
        this.engine=eng;
        if (eng!=null){
            this.attrManager=eng.getAttrManager();
        }
        this.initialize();
    }

    /** エージェントがエンジンに登録されたときに行う処理があれば,このメソッドをオーバーライドして記述します
     * A method that called when this object is attached to simulation engine. You should override this method if you have initialization proccesses.*/
    public void initialize(){
        this.totalSpentTime=0;
    }

    /** このエージェントが現在所属する実行スレッドを取得します<br>
     * This method gets the thread that drives this Agent. */
    public ExecThreadBase getMyThread(){
        return this.myThread;
    }

    /** このエージェントを実行する実行スレッドを指定します<br>
     * Set an execution thread of the Agent. */
    public void setMyThread(ExecThreadBase myThread){
        this.myThread=myThread;
    }

    /** このエージェントのタスクマネージャを取得します */
    public FuseTaskManager getTaskManager(){
        return this.manager;
    }


    /** シミュレーションエンジンにメッセージを送るメソッドです<br>
     * メッセージに既に宛先が登録されている場合に使えます */
    public void sendMessage(Message mess){
        mess.setFromAgent(this);
        this.numOfSendMessage++;
        this.getMyThread().sendMessage(mess);
    }

    /** シミュレーションエンジンにメッセージを送るメソッドです<br>
     * 既に宛先がメッセージに登録されている場合に使えます */
    public void sendMessages(List<Message> messages){
        for (Message mess:messages){
            mess.setFromAgent(this);
        }
        this.numOfSendMessage+=messages.size();
        this.getMyThread().sendMessages(messages);
    }

    /** 宛先を指定して，シミュレーションエンジンにメッセージを送るメソッドです<br>
     * メッセージオブジェクトに宛先が設定されていた場合でも，引数での指定が優先されます． */
    public void sendMessage(Message mess, long id){
        mess.setFromAgent(this);
        mess.setToAgent(id);
        this.sendMessage(mess);
    }

    /** 自分に届いたメッセージをすべて取得します<br>
     * この処理によってエージェントのメッセージボックスは空になります．<br>
     * Get all messages that the agent recieved. <br>
     * After executing the method, the message box of the agent becomes empty.*/
    public List<Message> getAllMessages(){
        LinkedList<Message>result=new LinkedList<Message>(this.messageBox);
        this.messageBox.clear();
        return result;
    }

    /** 公開するアトリビュートをまとめて設定します */
    public void publishAttributes(List<Attribute> attributes){
        for (Attribute attr:attributes){
            attr.setOwner(this);
        }
        this.attrManager.addPublishAttributes(attributes);
    }

    /** 公開するアトリビュートを設定します */
    public void publishAttribute(Attribute attribute){
        attribute.setOwner(this);
        this.attrManager.addPublishAttribute(attribute);
    }

    /** 公開するアトリビュートを設定します */
    public void publishAttribute(String name, Object attrValue){
        Attribute attribute=new Attribute(name, attrValue);
        attribute.setOwner(this);
        this.publishAttribute(attribute);
    }

    /** 他のエージェントが公開したアトリビュートの値を取得します<br>
     * その場で取得するため，一般のsubscribeとはちょっと違いますが，メソッド名としてはsubscribeを採用します */
    public Object subscribeAttributeValue(long attrKey){
        Object result=null;
        Attribute attr=this.attrManager.getAttribute(attrKey);
        if (attr!=null){
            result=attr.getAttributeValue();
        }
        return result;
    }

    /** 他のエージェントが公開したアトリビュートを取得します*/
    public Attribute subscribeAttribute(long attrKey){
        return this.attrManager.getAttribute(attrKey);
    }

    /** 他のエージェントが公開したアトリビュートを取得します */
    public Attribute subscribeAttribute(long targetId, String attrName){
        return this.attrManager.getAttribute(AttributeManager.getAttributeHash(targetId, attrName));
    }

    /** 他のエージェントが公開したアトリビュートを取得します<br>
     * その場で取得するため，一般のsubscribeとはちょっと違いますが，メソッド名としてはsubscribeを採用します */
    public Object subscribeAttributeValue(long targetId, String attrName){
        return this.subscribeAttributeValue(AttributeManager.getAttributeHash(targetId, attrName));
    }

    /** 他のエージェントが公開したアトリビュートを取得します<br>
     * 指定した種類のエージェントがパブリッシュしているアトリビュートを全て取得します．<br>
     * その場で取得するため，一般のsubscribeとはちょっと違いますが，メソッド名としてはsubscribeを採用します． */
    public List<Object> subscribeAttributeByClass(Class<? extends VirtualObject> classObj, String attrName){
        return this.subscribeAttributeByClass(classObj, attrName);
    }


    /** アトリビュートを削除します<br>
     * 削除したアトリビュートは他のエージェントから参照できなくなります．
     * ただし，自分が登録したアトリビュートしか削除できません */
    public void removeAttribute(String attrName){
        this.attrManager.removeAttribute(AttributeManager.getAttributeHash(this.getId(), attrName));
    }


    /** 自分が置かれている環境を取得します<br>
     * 内部的にはシミュレーションエンジン経由で取り出しています<br>
     * This method returns Environment class that this Agent is put on. */
    public Environment getEnvironment(){
        Environment result=null;
        if (engine!=null){
            result=engine.getEnvironment();
        }
        return result;
    }


    /** 自分のローカルノードメッシュを取得します */
    public NodeManager getNodeManager(){
        return nodeManager;
    }

    /** ローカルノードメッシュを設定します */
    public void setNodeManager(NodeManager man){
        nodeManager=man;
    }

    /** 移動処理をエンジンに任せるかどうかを設定します<br>
     * trueを設定すると，シミュレーションエンジンがupdateされるたびに，setSpeed(double)で指定した速度で移動します．<br>
     * 基本的に内部で呼び出される処理であり，ユーザーが触る必要はありません． */
    public void setMoveByEngine(boolean flag){
        isMoveByEngine=flag;
    }

    /** 移動処理がエンジンに任されているかのフラグを取得します */
    public boolean isSetMoveByEngine(){
        return isMoveByEngine;
    }

    /** 引数で指定した速度でエージェントを前進させます */
    public void forward(double speed){
        this.setMoveByEngine(true);
        this.setVelocityToDirection(speed);
    }

    /** Velocityとして指定された速度でエージェントを前進させます */
    public void forward(){
        this.setMoveByEngine(true);
    }

    /** シミュレーションエンジンからメッセージを送り込むメソッドです。<br>
     * シミュレーションエンジンから呼び出されるため、シミュレーション開発者が操作する必要はありません。 */
    public void recieveMessage(Message mess){
        messageBox.add(mess);
    }

    /** 自分に届いたメッセージを一通確認します */
    public Message getOneMessage(){
        Message mess=messageBox.poll();
        return mess;
    }


    /** 自分の速度を取得します<br>
     * This method returns the speed of the Agent. */
    public double getSpeed(){
        double speed=Math.sqrt(velocity[0]*velocity[0]+velocity[1]*velocity[1]+velocity[2]*velocity[2]);
        return speed;
    }

    /** 自分の速度を設定します */
    public void setSpeed(double sp){
        double currentSpeed=this.getSpeed();
        if (currentSpeed==0){
            velocity[0]=1.0; // 仮の数字
        }
        double rate=sp/this.getSpeed(); // 現在の速度との比を計算する
        velocity[0]*=rate;
        velocity[1]*=rate;
        velocity[2]*=rate;
    }

    /** 自分の速度を取得します */
    public double[] getVelocity(){
        return velocity.clone();
    }

    /** 自分の速度を設定します */
    public void setVelocity(double[] vel){
        velocity[0]=0;
        velocity[1]=0;
        velocity[2]=0;
        for(int i=0;i<vel.length && i<velocity.length;i++){
            velocity[i]=vel[i];
        }
    }

    /** 自分の向いている方向に速度を設定します */
    public void setVelocityToDirection(double sp){
        velocity[0]=direction[0];
        velocity[1]=direction[1];
        velocity[2]=direction[2];
        this.setSpeed(sp);
    }

    /** 自分の向いている方向(XY平面での方位角)をradianで取得します */
    public double getYaw(){
        double yaw=Math.atan2(direction[1], direction[0]);
        return yaw;
    }

    /** 自分の向いている方向(XY平面での方位角)をdegreeで取得します */
    public double getYawDegree(){
        double yaw=this.getYaw();
        yaw=yaw*180/Math.PI;
        return yaw;
    }

    /** 自分の向き(XY平面での方位角)をradian設定します<br>
     *  ワールド座標でのXY平面で指定してしまうため、実質的に2Dシミュレーション専用APIです */
    public void setYaw(double yaw){
        direction[0]=Math.cos(yaw);
        direction[1]=Math.sin(yaw);
        this.rotate(yaw*180/Math.PI, 0, 0, 1);
    }

    /** 自分の向き(XY平面での方位角)をdegreeで設定します */
    public void setYawDegree(double yaw){
        this.setYaw(yaw/180*Math.PI);
    }

    /** 自分をシミュレーションから離脱させます。<br>
     * 実際には、シミュレーションエンジンが離脱フラグを認識した時点で離脱します。 */
    public void exitSimulation(){
        exitFlag=true;
    }

    public void cancellToExit(){
        exitFlag=false;
    }

    /** 終了フラグが立っているか確認します */
    public boolean isExitAgent(){
        return exitFlag;
    }

    /** 直前の処理でどのくらいの時間がかかったかをナノ秒単位で取得します */
    public long getProcessingTime(){
        return this.spentTime;
    }

    /** 現在までのこのエージェントの処理時間の総計をナノ秒単位で取得します */
    public long getTotakProcessingTime(){
        return this.totalSpentTime;
    }

    /** エージェントとしての基本はここで保存します。<br>
     * このクラスを継承したエージェントはこのメソッドをオーバーライドし、プラスアルファの部分を保存します。 */
    @Override
    public SaveDataPackage saveStatus() {
        SaveDataPackage pack=super.saveStatus();
        pack.addData("dirX",direction[0]);
        pack.addData("dirY",direction[1]);
        pack.addData("dirZ",direction[2]);

        SaveDataPackage msgPack=new SaveDataPackage(this);
        for(Message msg:messageBox){
            msgPack.addChildPackage(msg.saveStatus());
        }

        return pack;
    }

    /** エージェントの基本状態を復元します */
    @Override
    public Agent restoreStatus(SaveDataPackage saveData) {
        super.restoreStatus(saveData);
        List<DataContainer> containers=saveData.getAllData();
        for(DataContainer cont:containers){
            if (cont.getName().equals("dirX")){
                direction[0]=(Double.parseDouble(cont.getData().toString()));
            }
            if (cont.getName().equals("dirY")){
                direction[1]=(Double.parseDouble(cont.getData().toString()));
            }
            if (cont.getName().equals("dirZ")){
                direction[2]=(Double.parseDouble(cont.getData().toString()));
            }
        }

        // 子クラスの復元
        List<SaveDataPackage> children=saveData.getAllChildren();
        // メッセージの復元はAgentクラスで実施
        for (SaveDataPackage pack:children){

            try {
                Object owner;
                owner = (Class.forName(pack.getOwnerClassName())).newInstance();
                if (owner instanceof Message){
                    Message msg=new Message().restoreStatus(pack);
                    this.recieveMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    /** エンジンが直接呼び出すメソッドです<br>
     * Simulation engine calls this method. Don't override nor modify this.*/
    public final void callAction(long timeStep){
        long startTime=System.nanoTime();
        this.action(timeStep);
        long finishTime=System.nanoTime();
        this.spentTime=finishTime-startTime;

        // 時間がループしてしまった場合
        if (startTime>finishTime){
            this.spentTime=0;
        }
        this.totalSpentTime+=this.spentTime;
    }

    /** エンジンから呼ばれるコールバックメソッドです<br>
     * 引数として現在のtimeStep(1000msecとか)が指定されます */
    public abstract void action(long timeStep);

    /** リストア時、シミュレーションエンジンに登録されてから実施する作業を設定します<br>
     * そのような作業がないなら、特に記述する必要はありません */
    public void additionalRestore(SimulationEngine engine){

    }

    /** ログ分析用メソッドです */
    public int getNumOfSendMessage(){
        return this.numOfSendMessage;
    }

}
