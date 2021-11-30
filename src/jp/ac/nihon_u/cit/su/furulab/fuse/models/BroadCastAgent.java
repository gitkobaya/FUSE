package jp.ac.nihon_u.cit.su.furulab.fuse.models;

/** メッセージ送信の際にこのクラスを指定するとブロードキャストになります<br>
 * このエージェントのIDは常に-1です。 */
public final class BroadCastAgent extends Agent {
    @Override
    public void action(long timeStep) {
    }

    @Override
    public long getId(){
        return -1;
    }
}
