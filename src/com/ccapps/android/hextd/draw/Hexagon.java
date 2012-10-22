package com.ccapps.android.hextd.draw;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import com.ccapps.android.hextd.gamedata.Creep;
import com.ccapps.android.hextd.gamedata.Tower;

import javax.xml.transform.Source;

/**
 * Created with IntelliJ IDEA.
 * User: charles
 * Date: 9/26/12
 * Time: 9:04 AM
 */
public class Hexagon extends Drawable implements Comparable<Hexagon> {

    public static enum STATE {
        NORMAL, NORMAL_DRAW, GOAL, SOURCE, SELECTED, ATTACKED
    }

    /********************STATICS****************************/
    public static final PointF[] hexPoints;
    public static final float sqrt3;
    private static float sideLength ;
    public static float height;

    static {
        sqrt3 = (float)Math.sqrt(3.);
        hexPoints = new PointF[6];
        setGlobalSideLength(40.f);

    }

    /**
     * Set new side length and compute the relative coordinates of a hexagon's vertices based on the new length;
     *
     * @param newLength
     */
    public static void setGlobalSideLength(float newLength) {
        Hexagon.sideLength = newLength;
        height = sideLength*sqrt3/2.f;
        hexPoints[0] = new PointF(-sideLength, 0.f);
        hexPoints[1] = new PointF((-sideLength/2.f), height);
        hexPoints[2] = new PointF((sideLength/2.f), height);
        hexPoints[3] = new PointF(sideLength, 0.f);
        hexPoints[4] = new PointF((sideLength/2.f), -height);
        hexPoints[5] = new PointF((-sideLength/2.f), -height);
    }

    public static float getGlobalSideLength() {
        return Hexagon.sideLength;
    }

    /************************NON-STATICS************************************/

    private Path hexPath;
    private Paint hexPaint;
    private PointF center;
    private Point gridPosition;
    private Hexagon[] neighbors;
    private Tower tower;
    private Creep creep;
    private boolean wasInvalidated = false;
    private STATE myState;
    private STATE myDefaultState;
    private int creepWeight;

    public Hexagon(PointF center, Point gridPosition) {
        this.center = center;
        this.gridPosition = gridPosition;
        initPath();
        hexPaint = new Paint();
        hexPaint.setColor(Color.GREEN);
        hexPaint.setStrokeWidth(1);
        hexPaint.setStyle(Paint.Style.STROKE);
        this.myState = this.myDefaultState = STATE.NORMAL;
        //neighbors to be set by the HexGrid instance.
        this.creepWeight = 0;
    }

    /***********************DRAWING RELATED**************************/
    /**
     * Determines path based on current side length and center
     */
    public void invalidatePath(PointF delta) {
        if (!wasInvalidated) {
            hexPath.offset(delta.x, delta.y);
            wasInvalidated = true;
        }
    }

    public void clearWasInvalidated() {
        this.wasInvalidated = false;
    }

    public void setState(STATE myState) {
        this.myState = myState;
        switch ( myState ) {
            case NORMAL: hexPaint.setColor(Color.GREEN); break;
            case GOAL: hexPaint.setColor(Color.BLUE); myDefaultState = STATE.GOAL; break;
            case SELECTED: hexPaint.setColor(Color.YELLOW); break;
            case ATTACKED: if (myDefaultState == STATE.NORMAL) hexPaint.setColor(Color.RED); break;
        }
    }

    public void setStateToDefault() {
        myState = myDefaultState;
        switch ( myDefaultState ) {
            case NORMAL: hexPaint.setColor(Color.GREEN); break;
            case GOAL: hexPaint.setColor(Color.BLUE); break;
        }
    }

    public void initPath() {
        hexPath = new Path();
        hexPath.moveTo(hexPoints[0].x, hexPoints[0].y);
        for (int i = 0; i < 6; i++) {
            hexPath.lineTo(hexPoints[(i+1)%6].x, hexPoints[(i+1)%6].y);
        }
        hexPath.close();
        hexPath.offset(center.x + HexGrid.GLOBAL_OFFSET.x, center.y + HexGrid.GLOBAL_OFFSET.y);
    }

    public void addPathTo(Path p) {
        p.addPath(this.hexPath);
    }

    @Override
    public void draw(Canvas canvas) {
        //Draw outline of hex or something else TBD
        if (myState != STATE.NORMAL) {
            canvas.drawPath(this.hexPath, this.hexPaint);
        }
    }

    @Override
    public void setAlpha(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getOpacity() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /************************SETTERS / GETTERS************************************/
    public PointF getCenter() {
        return center;
    }

    public void setCenter(PointF center) {
        this.center = center;
    }

    public Hexagon[] getNeighbors() {
        return this.neighbors;
    }

    public void setNeighbors(Hexagon[] neighbors) {
        this.neighbors = neighbors;
    }

    public Point getGridPosition() {
        return gridPosition;
    }

    public void setTower(Tower tower) {
        this.tower = tower;
    }

    public Tower getTower() {
        return this.tower;
    }

    public void setCreep(Creep creep) {
        this.creep = creep;
    }

    public Creep getCreep() {
        return creep;
    }
    /*****************************GAME LOGIC************************************/
    /**
     * Fire events when a square is attacked
     */
    public void attacked(int dmg) {
        if (dmg > 0) {
            setState(STATE.ATTACKED);
        }
        else {
            setStateToDefault();
        }
        if (this.getCreep() != null) {
            this.getCreep().loseHitpoints(dmg);
        }
    }

    public boolean isEmpty() {
        return tower == null && creep == null;

    }

    /************************STANDARD METHODS**************************************/
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Hexagon)) {
            return false;
        }
        Hexagon h = (Hexagon)o;
        if (h.getCenter().equals(this.center)) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(Hexagon hexagon) {
        if (this.equals(hexagon)) {
            return 0;
        }
        if (this.center.x < hexagon.center.x || this.center.y < hexagon.center.y) {
            return -1;
        }
        return 1;
    }

    public int getCreepWeight() {
        return this.creepWeight;
    }

    public void setCreepWeight(int weight) {
        this.creepWeight = weight;
    }
}
