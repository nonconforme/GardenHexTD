package com.ccapps.android.hextd.metagame;

import com.ccapps.android.hextd.algorithm.AntCreepPathfinder;
import com.ccapps.android.hextd.algorithm.CreepAlgorithm;
import com.ccapps.android.hextd.algorithm.RandomWalkAlgorithm;
import com.ccapps.android.hextd.draw.Hexagon;
import com.ccapps.android.hextd.gamedata.AntCreep;
import com.ccapps.android.hextd.gamedata.Creep;
import com.ccapps.android.hextd.gamedata.CreepUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: charles
 * Date: 10/20/12
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasicCreepGenerator implements CreepGenerator{
    private Class<? extends Creep> creepClass;
    private Class<? extends CreepAlgorithm> creepAlgorithm;
    private int speed = 24;
    private int tick = 0;
    private List<Hexagon> sourceHexes;
    private Hexagon goalHex;

    public BasicCreepGenerator(List<Hexagon> sourceHexes, Hexagon goalHex) {
        this.sourceHexes = sourceHexes;
        this.goalHex = goalHex;
        this.creepClass = AntCreep.class;
        this.creepAlgorithm = AntCreepPathfinder.class;
    }

    @Override
    public void tick() {
        ++tick;
        if (tick % speed == 0) {
           for (Hexagon h: sourceHexes) {
               CreepUtils.addCreep(creepClass, h, goalHex, creepAlgorithm);
           }
        }
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public List<Hexagon> getSourceHexes() {
        return sourceHexes;
    }

    @Override
    public void setSourceHexes(List<Hexagon> sourceHexes) {
        this.sourceHexes = sourceHexes;
    }
}
