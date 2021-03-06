package com.ccapps.android.hextd.gamedata;

import com.ccapps.android.hextd.algorithm.CreepAlgorithm;
import com.ccapps.android.hextd.draw.HexGrid;
import com.ccapps.android.hextd.draw.Hexagon;

import java.lang.reflect.Constructor;
import java.util.logging.Level;

/*****************************************************
 Garden Hex Tower Defense
 Charles Capps & Joseph Lee
 ID:  920474106
 CS 313 AI and Game Design
 Fall 2012
 *****************************************************/

//CLC: Original Code Begin
public class CreepUtils {


    public static Creep getInstance(Class<? extends Creep> creepClass, Hexagon hex, Hexagon goalHex, CreepAlgorithm algorithm) {
        try {
            Constructor<? extends Creep> constructor = creepClass.getConstructor(Hexagon.class, Hexagon.class, CreepAlgorithm.class);
            return constructor.newInstance(hex, goalHex, algorithm);
        } catch (Exception e) {
            StaticData.l.log(Level.SEVERE, "Exception in CreepUtils in getInstance: " + e.getCause() + ": " + e.getCause().getMessage());
        }
        return null;
    }

    public static void addCreep(Class<? extends Creep> creepClass, int row, int col, Hexagon goalHex, Class<? extends CreepAlgorithm> algorithmClass) {
        HexGrid GRID = HexGrid.getInstance();
        CreepAlgorithm algorithm = null;
        try {
            Constructor<? extends CreepAlgorithm> constructor = algorithmClass.getConstructor();
            algorithm = constructor.newInstance();
        } catch (Exception e) {
            StaticData.l.log(Level.SEVERE, "Exception in CreepUtils: " + e.getCause() + ": " + e.getCause().getMessage());
        }

        Creep instance = getInstance(creepClass, GRID.get(row, col), goalHex, algorithm);
        GRID.addCreep(row, col, instance);
    }

    public static void addCreep(Class<? extends Creep> creepClass, Hexagon src, Hexagon goalHex, Class<? extends CreepAlgorithm> algorithmClass) {
        addCreep(creepClass, src.getGridPosition().x, src.getGridPosition().y, goalHex, algorithmClass);
    }
}
//CLC: Original Code End
