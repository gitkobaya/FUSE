require 'java'
require 'fuse.jar'

import 'jp.ac.nihon_u.cit.su.furulab.fuse.ai.WhiteBoard'
import 'jp.ac.nihon_u.cit.su.furulab.fuse.ai.Truth'

# 定数定義
IMMORTAL=WhiteBoard.IMMORTAL
IMMEDIATE=WhiteBoard.IMMEDIATE

# JRubyのインターフェースとなるルールクラス
class FuseRule

  def initialize()
    @root_task_class=nil # インスタンスではなくクラス
    @root_task_args=nil # 配列になる
    self.instantiate
  end

  # ルートタスククラスの取得
  def get_root_task
    return @root_task_class
  end

  # ルートタスクインスタンスの取得
  def get_root_task_instance
    if (@root_task_class==nil)
      raise "FUSE Exception: Have not defined root task !"
    end
    if (@root_task_args!=nil && @root_task_args.length!=0)
      #p "in fuserule ",@root_task_args
      result=@root_task_class.new(*@root_task_args)
    else
      result=@root_task_class.new()
    end
    return result
  end

  # ルートタスクの名前を取得
  def get_root_task_name
    return @root_task_class.to_s
  end

  # ルートタスクの引数を取得
  def get_root_task_args
    return @root_task_args
  end

  # ルートタスクの設定
  def set_goal_task(task,*args)
    @root_task_class=task
    @root_task_args=args
  end

  # タスクインスタンスの生成
  # Javaは可変長引数→配列はできても配列→可変長引数にはできないため，引数は「1つの配列」として受け取っている
  def create_task(task_name, args)
    task_class=eval(task_name)
    instance=task_class.new(*args)
    return instance
  end

  # 条件に実際のタスククラスを設定してルールを実体化します
  # かならずオーバーライドする必要があります
  def instantiate
    raise NotImplementedError.new("#{self.class.name} is an abstract method.")
  end
end
