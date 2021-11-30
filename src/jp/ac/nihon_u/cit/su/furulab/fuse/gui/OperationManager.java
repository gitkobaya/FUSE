package jp.ac.nihon_u.cit.su.furulab.fuse.gui;

import java.util.Stack;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;

/** GUI操作を管理するためのクラスです */
public class  OperationManager <T>{
    private T operationTarget;
    private Stack<Operation<T>> undoStack=new Stack<Operation<T>>();
    private Stack<Operation<T>> redoStack=new Stack<Operation<T>>();

    public OperationManager(T target) {
        this.setOperationTarget(target);
    }

    public void setOperationTarget(T target){
        operationTarget=target;
    }

    /** 操作を実行します */
    public void exec(Operation<T> ope){
        ope.execute(operationTarget);
        undoStack.push(ope);
        redoStack.clear();
    }

    /** 操作を取り消します */
    public void undo(){
        Operation<T> ope=null;
        if (!undoStack.isEmpty()){
            ope=undoStack.pop();
            ope.undo(operationTarget);
            System.out.println("DEBUG: undo "+ope.getClass());
            redoStack.push(ope);
        }

        // 操作IDが同じ場合、連続でUndoする
        if (!undoStack.isEmpty()){
            Operation next=undoStack.lastElement();
            if (ope.getId()==next.getId()){
                this.undo();
            }
        }
    }

    /** 取り消した操作を復元します */
    public void redo(){
        Operation ope=null;
        if (!redoStack.isEmpty()){
            ope=redoStack.pop();
            ope.execute(operationTarget);
            undoStack.push(ope);
        }

        // 操作IDが同じ場合、連続でRedoする
        if (!redoStack.isEmpty()){
            Operation next=redoStack.lastElement();
            if (ope.getId()==next.getId()){
                this.redo();
            }
        }
    }

    /** 最後に実施したオペレーション(ユーザーの操作)を確認します<br>
     * 何もオペレーションを実施していない場合， nullが返ります */
    public Operation showLatestExec(){
        Operation result=null;
        if (this.undoStack.size()>0){
            result=this.undoStack.peek();
        }
        return result;
    }

    /** スタックを消去します */
    public void clear(){
        undoStack.clear();
        redoStack.clear();
    }
}
