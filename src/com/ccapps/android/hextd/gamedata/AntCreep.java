package com.ccapps.android.hextd.gamedata;

import android.graphics.Canvas;
import com.ccapps.android.hextd.algorithm.CreepAlgorithm;
import com.ccapps.android.hextd.draw.CreepDrawable;
import com.ccapps.android.hextd.draw.Hexagon;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: charles
 * Date: 10/15/12
 * Time: 2:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class AntCreep implements Creep {

    private int direction;
    private int speed;
    private CreepDrawable creepDrawable;
    private List<Hexagon> path;
    private Hexagon hex;
    private Hexagon goalHex;
    private int hitpoints;
    private CreepAlgorithm algorithm;
    private int tick;

    public AntCreep(Hexagon hex, Hexagon goalHex, CreepAlgorithm algorithm) {
        this.hex = hex;
        this.goalHex = goalHex;
        this.algorithm = algorithm;
        this.algorithm.setCreep(this);
        this.direction = 0;
        this.hitpoints = 1000;

        this.creepDrawable = new CreepDrawable(this, StaticData.ANT);
        this.tick = 0;
        this.speed = 4;

        evaluateRoute();
    }

    @Override
    public int getDirection() {
        return direction;
    }

    @Override
    public void setDirection(int direction) {
        this.direction = direction;
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
    public CreepDrawable getCreepDrawable() {
        return creepDrawable;
    }

    @Override
    public void setCreepDrawable(CreepDrawable creepDrawable) {
        this.creepDrawable = creepDrawable;
    }

    @Override
    public List<Hexagon> getPath() {
        return path;
    }

    @Override
    public void setPath(List<Hexagon> path) {
        this.path = path;
    }

    @Override
    public Hexagon getHex() {
        return hex;
    }

    @Override
    public void setHex(Hexagon hex) {
        this.hex = hex;
    }

    @Override
    public Hexagon getGoalHex() {
        return goalHex;
    }

    @Override
    public void setGoalHex(Hexagon goalHex) {
        this.goalHex = goalHex;
    }

    @Override
    public int getHitpoints() {
        return hitpoints;
    }

    @Override
    public void setHitpoints(int hitpoints) {
        this.hitpoints = hitpoints;
    }

    @Override
    public void loseHitpoints(int hp) {
        this.hitpoints -= hp;
    }

    @Override
    public CreepAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public void setAlgorithm(CreepAlgorithm creepAlgorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void evaluateRoute() {
        if (hitpoints > 0 && algorithm.pathNeedsEvaluation()) {
            this.path = algorithm.buildPath(hex, goalHex);
        }
    }

    @Override
    public void move() {
        if (++tick % speed == 0 && hitpoints > 0) {
            if (path.size() <= 0) {
                return;
            }
            evaluateRoute();
            hex.setCreep(null);
            hex = path.remove(0);
            hex.setCreep(this);
            creepDrawable.updateLocation();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        creepDrawable.draw(canvas);
    }
}
