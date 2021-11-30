require 'java'
require 'fuse.jar'

import 'jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.ActionByScript'

# JRubyのインターフェースとなるアクションクラス
class ActionRuby < ActionByScript

  # イニシャライザで引数を指定
  def initialize (*args)
    #JRubyのバグのせいで可変長引数にめんどくさい処理が必要
    objs=Array.new
    args.each{|obj|
      objs.push(obj)
    }
    java_args=objs.to_java(:object)

    begin
      super(self.class.to_s.to_java, java_args)
    rescue =>err
       warn ("ERROR: #{err} in #{self.name} args:#{java_args}")
    end

    f=*getFields()
    #puts "debug: action name:#{self.class} jname:#{self.class.to_s.to_java} in initializer args:#{args} objs:#{objs} java_args:#{java_args} fields:#{f}"
  end

  # Java側から呼ばれるメソッド
  def action
    begin
      fields=getFields()
      if (fields==nil || fields.length==0)
       # puts "debug: in define of ActionRuby with no arguments"
        define_action # 引数が無い場合
      else
        #puts "debug: in define of ActionRuby "+self.name+"  :"+fields.length.to_s+" :"+fields.to_a.to_s
        define_action(*fields.to_a)
      end
    rescue NativeException => ex
      warn  "ERROR:  in action of #{self.name} "
      warn ex.cause.printStackTrace
    rescue => ex
      warn  "ERROR:  in action of #{self.name} <#{ex.class.name}: #{ex.to_s}>"
    end
  end

  # エージェント取得メソッド
  def get_agent
    java_agent=getAgent
    return java_agent
  end

  # エージェントインターフェース取得メソッド
  def get_interface
    #puts "debug: get interface"
    return getInterface()
  end

  # 記憶への書き込みメソッド
  # 寿命を設定しない場合は無限として扱われます
  def write(identifiers, data, lifetime=0)
    writeToMemory(identifiers.to_java, data, lifetime)
  end

  # 記憶の削除メソッド
  # オブジェクトを指定して削除します
  def erase_article(article)
    eraseArticle(article)
  end

  # 記憶の削除メソッド
  # 識別情報を使って削除します
  def erace_articles(ids)
    eraseArticles(ids.to_java)
  end

  # 記憶の更新メソッド
  # 古い記憶を新しい記憶で置き換えます
  def replace(new_article,old_article)
    replace(new_article, old_article)
  end

  # 条件に実際のタスククラスを設定してルールを実体化します
  # かならずオーバーライドする必要があります
  def define_action(*args)
    raise NotImplementedError.new("#{self.class.name} is an abstract method.")
  end


end