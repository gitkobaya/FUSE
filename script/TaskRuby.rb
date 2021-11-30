require  'java'
require 'fuse.jar'

import 'jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.TaskByScript'

# Rubyによるタスククラス
class TaskRuby < TaskByScript

  # 初期化処理
  def initialize(*args)
    #JRubyのバグのせいで可変長引数にめんどくさい処理が必要
    #objs=[args.size]
    objs=Array.new
    args.each{|obj|
      objs.push(obj)
    }
    java_args=objs.to_java(:object)

    #p "in initialize of taskruby",args,java_args,java_args[0]
    super(self.class.to_s.to_java, java_args)

    @conditions=Array.new # クラスの配列
    @action_pack=nil # クラス
  end

  # コストの設定(set_own_costと同じ効果)
  def set_cost(cost)
    @cost=cost
  end

  # コストの設定
  def set_own_cost(cost)
    @cost=cost
  end

  # 条件クラスの取得
  def get_conditions
    return @conditions
  end

  # 条件クラスの追加
  def add_condition(condition_class,*args)

    #p "debug: in #{self.class.name} adding #{condition_class.name} #{args}"

    if (args.length>0)
      #p "debug: arguments exist #{args}"
      @conditions.push([condition_class, args]) # 配列にしてからセット
    else
      @conditions.push([condition_class])
    end
  end

  # アクションクラスの設定
  def set_action(action_class, *args)
    if (args.length>0)
      @action_pack=[action_class, args]
    else
      @action_pack=[action_class]
    end
    #p @action_pack
  end

  # エージェントにアクセスするためのインターフェースをもらうメソッド
  def get_interface
    return getInterface()
  end

  # Javaから呼ばれるdefineメソッドです
  # ここで条件のインスタンスが生成されるため，この時点で条件から呼び出すタスクが確定している必要があります
  def define
    fields=getFields()
    begin
      if (fields==nil || fields.length==0)
        define_task # 引数が無い場合
      else
        # p "in define of taskruby ",self,fields.length,fields[0]
        define_task(*fields.to_a) # 引数がある場合
      end
    rescue =>ex
      # 例外が起こった場合，それを表示
      warn "ERROR: in define_task of #{self.class.name} <#{ex.class.name}: #{ex.to_s}> #{fields}"
      warn "#{ex.backtrace}"
      raise RuntimeError.new("Critical Error Occurd in #{self.class.name}")
    end

    # 条件をJava側に設定
    begin
      @conditions.each{|pack|
        # Java用の条件オブジェクトを設定
        condition=pack[0].new(*pack[1])
        addCondition(condition)
      }
    rescue =>ex
      # 例外が起こった場合，それを表示
      warn "ERROR: in adding condition of define_task of #{self.class.name} <#{ex.class.name}: #{ex.to_s}> #{fields}"
      warn "#{ex.backtrace}"
      raise RuntimeError.new("Critical Error Occurd in #{self.class.name}")
    end

    # コストをJava側に設定
    setOwnCost(@cost)

    # アクションをJava側に設定
    begin
      if (@action_pack!=nil) then
        action=@action_pack[0].new(*@action_pack[1])
        setAction(action)
      end
    rescue =>ex
      # 例外が起こった場合，それを表示
      warn "ERROR: in adding action #{@action_pack[0].to_s} of define_task of #{self.class.name} <#{ex.class.name}: #{ex.to_s}> #{fields}"
      warn "#{ex.backtrace}"
      raise RuntimeError.new("Critical Error Occurd in #{self.class.name}")
    end
  end

  # Taskとしての動作を定義します
  # かならずオーバーライドする必要があります
  def define_task(*args)
    raise NotImplementedError.new("#{self.class.name} is an abstract method.")
  end

end
