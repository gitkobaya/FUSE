require 'java'
require 'fuse.jar'

import 'jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.ActionByScript'

# JRubyのインターフェースとなるアクションクラス
# 実行に時刻を消費しないノータイムアクションを定義
class ActionRubyNotime < ActionRuby

  # イニシャライザで引数を指定
  def initialize (*args)
    super(*args)
    setNotimeActionFlag(true)
  end

end