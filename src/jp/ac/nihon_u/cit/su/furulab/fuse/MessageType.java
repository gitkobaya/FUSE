package jp.ac.nihon_u.cit.su.furulab.fuse;

/** メッセージタイプです。目安です。 */
public enum MessageType {
    System, // システムメッセージ
    Event, // イベントメッセージ
    Command, // 命令メッセージ
    Report,// 報告メッセージ
    Other,// その他のメッセージ
}
