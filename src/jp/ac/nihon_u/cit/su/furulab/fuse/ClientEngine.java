package jp.ac.nihon_u.cit.su.furulab.fuse;

/** クライアントモードで動作させる際のダミー */
public class ClientEngine extends SimulationEngine{

    /** 初期状態でサーバと同じ環境を持っていなければならない */
    public ClientEngine(Environment env) {
        super(env);
    }


}
