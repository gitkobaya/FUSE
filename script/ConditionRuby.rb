require 'java'
require 'fuse.jar'

import 'jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.ConditionByScript'
import 'jp.ac.nihon_u.cit.su.furulab.fuse.ai.script.AgentByScript'
import 'jp.ac.nihon_u.cit.su.furulab.fuse.ai.Truth'

# JRubyのインターフェースとなる条件クラス
# 条件オブジェクト自体は解決のための具体的タスクを設定せず，抽象的な識別子として扱います．
# 解決のためのタスクオブジェクトはそれぞれの条件ごとに外部から設定されます．
class ConditionRuby < ConditionByScript
  NONAME_TRUE='DEFAULT_TASK_TO_BE_TRUE'
  NONAME_KNOWN='DEFAULT_TASK_TO_BE_KNOWN'

  # ワイルドカードの糖衣構文
  WILDCARD=nil
  WCARD=WILDCARD

  @@task_to_be_true=Hash.new  #条件を真にするための条件名とタスクマップの関係
  @@task_to_be_known=Hash.new #条件を既知にするための条件名とタスクマップの関係

  # インスタンス変数を初期化しておく
  def initialize(*args)
    #JRubyのバグのせいで可変長引数にめんどくさい処理が必要
    objs=Array.new
    args.each{|obj|
      objs.push(obj)
    }
    java_args=objs.to_java(:object)

    # おまじない
    #if (args.size==0) then
    #  java_args=nil
    #end

    super(self.class.to_s.to_java,java_args)

    # 鋳型からタスクインスタンスを作るための情報
    @arglist_to_be_true_with_task=Hash.new  # 条件を真にするためのタスクの名前と引数リストの関係
    @arglist_to_be_known_with_task=Hash.new # 条件を既知にするためのタスクの名前と引数リストの関係
    @result=Truth::TRUE # この条件の評価結果(デフォルトでTRUE)

  end

  # 記憶の取得メソッド
  # 指定した識別情報に基づいて記憶を取得します
  # 識別情報は配列で与えます
  def read(identifiers)
    articles=readArticles(identifiers.to_java)
    return articles.to_a # Java配列をRuby配列に変換
  end

  # 記憶のデータのみを取得します
  # 識別情報は配列で与えます
  def read_datum(identifiers)
    articles=read(identifiers)
    infos=[]
    articles.each{|art|
      infos.push(art.getInformation)
    }
    return infos
  end

  # エージェントにアクセスするためのメソッド
  # ただし，シミュレーションによってはエージェントそのものの受け渡しが禁止される場合があります
  # その場合は，インターフェースを利用してエージェントとの情報交換を行います
  def get_agent
    return  getAgent() #javaのエージェントオブジェクトを戻す
  end

  # エージェントにアクセスするためのインターフェースをもらうメソッド
  def get_interface
    return getInterface()
  end

  # 条件を真にするためのタスクに食わせる引数を追加
  # このメソッドが呼ばれた時点で結果がFALSEであることは確定
  def add_args_to_be_true(*args) # 可変長引数
    add_task_to_be_true(NONAME_TRUE, *args) # 無名のタスクとして登録
    @result=Truth::FALSE
  end

  # 条件を真にするためのタスク(名前指定)に食わせる引数を追加
  # このメソッドが呼ばれた時点で結果がFALSEであることは確定
  def add_task_to_be_true(taskname, *args) # 可変長引数
    # そのタスク識別名の引数セットのリストを取得
    argslist=@arglist_to_be_true_with_task[taskname]

    # 存在しないならマップに追加
    if (argslist==nil)
      @arglist_to_be_true_with_task[taskname]=Array.new
      argslist=@arglist_to_be_true_with_task[taskname]
    end
    argslist.push(args) # 引数セットを追加
    @result=Truth::FALSE
  end

  # 条件を既知にするためのタスクに食わせる引数を追加
# このメソッドが呼ばれた時点で結果がUNKNOWNであることは確定
  def add_args_to_be_known(*args) # 可変長引数
    add_task_to_be_known(NONAME_KNOWN,*args) # 無名のタスクとして登録
    @result=Truth::UNKNOWN
  end

  # 条件を既知にするためのタスク(名前指定)に食わせる引数を追加
# このメソッドが呼ばれた時点で結果がUNKNOWNであることは確定
  def add_task_to_be_known(taskname, *args) # 可変長引数
    # そのタスク識別名の引数セットのリストを取得
    argslist=@arglist_to_be_known_with_task[taskname]

    # 存在しないならマップに追加
    if (argslist==nil)
      @arglist_to_be_known_with_task[taskname]=Array.new
      argslist=@arglist_to_be_known_with_task[taskname]
    end
    argslist.push(args) # 引数セットを追加
    @result=Truth::UNKNOWN
  end


  # FalseからTrueにするためのタスクを設定
  # 引数はインスタンスではなくクラスオブジェクトであることに注意
  # また引数の順番に注意．(文字列,クラス名)ではなく(クラス名,文字列)
  def ConditionRuby.set_task_to_be_true(task_class, task_name=NONAME_TRUE)
    my_name=self.name
    p "debug: cond name=#{my_name} set task #{task_class} as #{task_name}"

    # クラス名を指定するとタスク識別名とタスククラスの対応表をもらえる
    # クラス名を指定しない場合，デフォルトのNONAMEとみなされる
    name_map=@@task_to_be_true[my_name]
    if (name_map==nil)
      name_map=Hash.new
      @@task_to_be_true[my_name]=name_map
    end

    # 対応表に追加する(タスク識別名を指定しない場合，NONAMEが指定される)
    name_map[task_name]=task_class
  end

  # UnknownからKnownにするためのタスクを設定
  # 引数はインスタンスではなくクラスオブジェクトであることに注意
  # また引数の順番に注意．(文字列,クラス名)ではなく(クラス名,文字列)
  def ConditionRuby.set_task_to_be_known(task_class, task_name=NONAME_KNOWN)
    my_name=self.name
    p "debug: cond name=#{my_name} set task=#{task_class} to be known"

    # クラス名を指定するとタスク識別名とタスククラスの対応表をもらえる
    name_map=@@task_to_be_known[my_name]
    if (name_map==nil)
      name_map=Hash.new
      @@task_to_be_known[my_name]=name_map
    end

    # 対応表に追加する(タスク識別名を指定しない場合，NONAMEが指定される)
    name_map[task_name]=task_class
  end

  # 解決のためのタスクを設定
  # 引数はインスタンスではなくクラスオブジェクトであることに注意
  # タスク名についてデフォルトは許されない
  def ConditionRuby.set_task_to_solve(task_class, task_name)
    my_name=self.name
    p "debug: cond name=#{my_name} set task=#{task_class} to be known"

    # クラス名を指定するとタスク識別名とタスククラスの対応表をもらえる
    name_map=@@task_to_be_true[my_name]
    if (name_map==nil)
      name_map=Hash.new
      @@task_to_be_true[my_name]=name_map
    end
    name_map[task_name]=task_class

    name_map=@@task_to_be_known[my_name]
    if (name_map==nil)
      name_map=Hash.new
      @@task_to_be_known[my_name]=name_map
    end
    name_map[task_name]=task_class
  end

  # Javaから呼ばれる評価メソッド
  def evaluate
    fields=getFields  # 引数を取得
    temp_array=Array.new
    # おまじない
    fields.each{|val|
      temp_array.push(val)
      #p "debug: value!! #{val}"
    }
    #p "debug: evaluate #{self.name} #{temp_array}"
    begin
      result=evaluate_condition(*temp_array) # ここで評価することでタスクがセットされる
      if (result==nil || result==Truth::UNCERTAIN ||
        (result!=Truth::TRUE && result!=true && result!="true" && result!=Truth::FALSE && result!=false && result!="false" && result!=Truth::UNKNOWN  && result!="unknown")) then
        # 返り値がなかった場合はタスクの登録状況から結果を決定
        result=@result
      end
    rescue =>ex
      # 例外が起こった場合，それを表示
      warn "ERROR: in evaluate_condition of #{self.name} <#{ex.class.name}: #{ex.to_s}> #{temp_array}"
      ex.backtrace.each{|mess|
        warn "#{mess}"
      }
    end
    # インスタンスを生成して登録する
    if (result==Truth::TRUE || result==true || result=="true" ) then
      result=Truth::TRUE
    elsif(result==Truth::FALSE || result==false || result=="false") then
      task_map=@@task_to_be_true[self.name] # タスクマップ(タスク識別名とタスククラスの関係)を取得
      # taks_mapに何も入っていなければimpossible確定
      if (task_map==nil) then
        #p "debug: in evalutate of #{self.name} is impossible !"
        setImpossibleFlag()
      else
        if (@arglist_to_be_true_with_task!=nil) then
          @arglist_to_be_true_with_task.each {|taskname, argslist| # タスク識別名と引数の関係を順に取得
            task_class=task_map[taskname] # 実際のタスククラスを取得
            #p "debug: in evaluate of #{self.name}: Task to be true: #{taskname} is #{task_class} #{argslist}"
            argslist.each{|args|
              if (task_class!=nil) then
                addTask(task_class.new(*args)) # インスタンスを生成して登録
                #p "debug: #{taskname} on #{self.name} with #{args}"
              else
                p "debug: #{@arglist_to_be_true_with_task}"
                p "debug: Task <#{taskname}> of #{self.name} is not defined !"
              end
            }
          }
        end
      end
      result=Truth::FALSE
    elsif(result==Truth::UNKNOWN  || result=="unknown") then
      task_map=@@task_to_be_known[self.name] # タスクマップ(タスク識別名とタスククラスの関係)を取得
      # taks_mapに何も入っていなければimpossible確定
      if (task_map==nil) then
        #p "debug: in evalutate of #{self.name} is impossible !"
        setImpossibleFlag()
      else
        if (@arglist_to_be_known_with_task!=nil) then
          @arglist_to_be_known_with_task.each {|taskname, argslist| # タスク識別名と引数の関係を順に取得
            task_class=task_map[taskname] # 実際のタスククラスを取得
            #p "debug: in evaluate of #{self.name}: Task to be true: #{taskname} is #{task_class} #{argslist}"
            argslist.each{|args|
              if (task_class!=nil) then
                addTask(task_class.new(*args)) # インスタンスを生成して登録
                #p "debug: #{taskname} on #{self.name} with #{args}"
              else
                p "debug: #{@arglist_to_be_known_with_task}"
                p "debug: #{taskname} of #{self.name} is not defined !"
              end
            }
          }
        end
      end
      result=Truth::UNKNOWN
    elsif (result==nil)
      raise "ERROR: Condition Result is nil by #{self.name}"
    else
      raise "ERROR: Illegal Type of Condition Result :#{result} by #{self.name}"
    end
    return result
  end

  def ConditionRuby.debug_info
    p "Task to be true:#{@@task_to_be_true}  Task to be known:#{@@task_to_be_known}"
  end

  # 条件としての動作を定義します
  # かならずオーバーライドする必要があります
  def evaluate_condition(*args)
    raise NotImplementedError.new("#{self.class.name}'s evaluate_condition is an abstract method and it must be implemented !")
  end

end
