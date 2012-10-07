package com.ccapps.android.hextd.draw;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import com.ccapps.android.hextd.gamedata.Tower;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: charles
 * Date: 9/26/12
 * Time: 10:57 AM
 */

/**
 * Singleton design pattern.
 */
public class HexGrid extends Drawable {

    //STATICS
    private static HexGrid GRID;
    public static float Y_MIN = -5.f;
    public static float Y_MAX;
    public static PointF MARGIN = new PointF(5.f, 25.f); //pixels
    public static PointF GLOBAL_OFFSET = new PointF(0.f, 0.f);


    /**
     * Must be called before getting an instance...
     *
     * @param topLeft
     * @param numHorizontal
     * @param numVertical
     */
    public static void initHexGrid(PointF topLeft, int numHorizontal, int numVertical, float sideLength, Point screenSize, PointF margin ) {

        Hexagon.setGlobalSideLength(sideLength);
        HexGrid.GRID = new HexGrid(numHorizontal, numVertical, screenSize, topLeft);
        shiftTopLeft(topLeft);
    }


    public static void shiftTopLeft(PointF delta) {

        float newX = HexGrid.GLOBAL_OFFSET.x + delta.x;

        float newY = HexGrid.GLOBAL_OFFSET.y + delta.y;

        if ( -newY < Y_MIN || -newY > Y_MAX) {
            return;
        }
        HexGrid.GLOBAL_OFFSET.x = newX;
        HexGrid.GLOBAL_OFFSET.y = newY;

        GRID.gridPath.offset(delta.x, delta.y);

        for (Tower t: GRID.towersOnGrid) {
           // t.getHex().invalidatePath(delta);
            for (Hexagon h: t.getAttackHexes()) {
                h.invalidatePath(delta);
            }
        }

        for (Hexagon h: GRID.goalHexes) {
            h.invalidatePath(delta);
        }

    }

    public static boolean isInitialized() {
        return GRID != null;
    }

    public static HexGrid getInstance()  {
        return HexGrid.GRID;
    }

    //DATA
    private PointF topLeft;
    private int numHorizontal;
    private int numVertical;
    private Path gridPath;
    private Paint gridPaint;
    private List<Tower> towersOnGrid;
    private List<Hexagon> goalHexes;
    /**
     * A hex grid is laid out basically like a square grid w/ more connections.
     * Will create documentation eventually w/ pretty diagrams.
     */
    private Hexagon[][] hexMatrix;

    /**
     * @param numHorizontal - the number of hexagons left-to-right
     * @param numVertical - the number of hexagons top-to-bottom
     */
    private HexGrid(int numHorizontal, int numVertical, Point screenSize, PointF topLeft) {

        this.topLeft = topLeft;
        this.numHorizontal = numHorizontal;
        this.numVertical = numVertical;

        this.gridPath = new Path();
        this.towersOnGrid = new ArrayList<Tower>();
        this.goalHexes = new ArrayList<Hexagon>();
        this.gridPaint = new Paint();
        this.gridPaint.setColor(Color.GREEN);
        this.gridPaint.setStrokeWidth(1);
        this.gridPaint.setStyle(Paint.Style.STROKE);
        
        this.hexMatrix = new Hexagon[numVertical][numHorizontal];  //rows / columns starting from top-left (math matrix style)

        float a = Hexagon.getGlobalSideLength();
        float h = a*Hexagon.sqrt3/2.f;

        //Generate the appropriate hexagons
        for (int j = 0; j < numVertical; j++) {
            for (int i = 0; i < numHorizontal; i++) {
                float hOffset = 3.f*a*(float)i/2.f;
                float vOffset;
                if (i % 2 == 0) {
                    vOffset = 2.f*h*(float)j;
                } else {
                    vOffset = 2.f*h*(float)j - h;
                }
                hexMatrix[j][i] = new Hexagon(new PointF(topLeft.x + hOffset, topLeft.y + vOffset),
                        new Point(j, i));
                hexMatrix[j][i].addPathTo(this.gridPath);
            }
        }

        //link up hexagons
        for (int i = 0; i < numVertical; i++) {
            for (int j = 0; j < numHorizontal; j++) {
                Hexagon[] nbrs = new Hexagon[6];
                int numFound = 0;
                for (int k = -1; k <= 1; k++) {
                    for (int l = -1; l <= 1; l++) {
                        if ( l == 0 && k == 0 ) { //no edge to self
                            continue;
                        }
                        //out of bounds
                        if (i+k < 0 || i + k > numVertical - 1 || j + l < 0 || j + l > numHorizontal - 1) {
                            continue;
                        }
                        //Even columns only corner links in down a row
                        if ( k == -1 && ( l == 1 ||  l == -1) && j % 2 == 0 ) {
                            continue;
                        }
                        //Odd columns only have corner links up a row
                        if ( k == 1 && ( l == 1 || l == -1) && j % 2 == 1 ) {
                            continue;
                        }
                        nbrs[numFound++] = hexMatrix[i+k][j+l];
                       // Verifying the proper edges are created!
                       // Logger.getAnonymousLogger().log(Level.SEVERE, "Hex ("  + i + "," + j +
                       //         ") has nbr at (" + (i+k) + ", " + (j+l) + ")");
                    }
                }
                hexMatrix[i][j].setNeighbors(nbrs);

            }
        }

        float gridHeight = (float)numVertical*2.f*h + h ;
        Y_MIN = -h - (float)MARGIN.y;
        Y_MAX = gridHeight + HexGrid.MARGIN.y*7.f + 2.f*h - (float)screenSize.y;
    }

    public void initAllPaths() {
        this.gridPath = new Path();
        for (Hexagon[] hs: hexMatrix) {
            for (Hexagon h: hs) {
                h.initPath();
                h.addPathTo(gridPath);
            }
        }
    }

    public void invalidateAllPaths(PointF delta) {
        for (Hexagon[] hs: hexMatrix) {
            for (Hexagon h: hs) {
                h.invalidatePath(delta);
            }
        }
    }

    public void reset() {
        shiftTopLeft(new PointF(-GLOBAL_OFFSET.x, -GLOBAL_OFFSET.y));
        initAllPaths();
        shiftTopLeft(topLeft);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(gridPath, gridPaint);
        for (Tower t: towersOnGrid) {
            t.draw(canvas);
        }
        for (Hexagon h: goalHexes) {
            h.draw(canvas);
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

    /***********************SETTERS / GETTERS*******************************/
    public int getNumVertical() {
        return numVertical;
    }

    public int getNumHorizontal() {
        return numHorizontal;
    }

    public List<Hexagon> getGoalHexes() {
        return goalHexes;
    }

    public Hexagon get(int r, int c) {
        return hexMatrix[r][c];
    }

    public void setTower(int r, int c, Tower tower) {
        hexMatrix[r][c].setTower(tower);
        towersOnGrid.add(tower);
    }

    public List<Tower> getTowersOnGrid() {
        return towersOnGrid;
    }
}
