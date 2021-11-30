package jp.ac.nihon_u.cit.su.furulab.fuse.models;

import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.Message;

/** シミュレーションエンジンを介してメッセージをやり取りするクラスはこのインターフェースを実装する必要があります */
public interface MessageReciever {

    /** シミュレーションエンジンからメッセージを送り込むメソッドで。<br>
     * シミュレーションエンジンから呼び出されます。 */
    public void recieveMessage(Message mess);

    /** 自分に届いたメッセージを一通確認します */
    public Message getOneMessage();

    /** 自分に届いたメッセージをすべて取得します<br>
     * この処理によってエージェントのメッセージボックスは空になります．<br>
     * Get all messages that the agent recieved. <br>
     * After executing the method, the message box of the agent becomes empty.*/
    public List<Message> getAllMessages();


}
