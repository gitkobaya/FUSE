package jp.ac.nihon_u.cit.su.furulab.fuse.thread;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.ac.nihon_u.cit.su.furulab.fuse.SimulationEngine;
import jp.ac.nihon_u.cit.su.furulab.fuse.models.Agent;

/** エージェントのサイクリック実行処理のためのスレッドです */
public class ExecThreadCyclic extends ExecThreadSimple{
    public static final int INITIAL_SIZE=500;

    public ExecThreadCyclic(SimulationEngine engine) {
        super(engine, new ArrayList<Agent>(INITIAL_SIZE));
    }

}

