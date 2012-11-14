package com.ccapps.android.hextd.metagame;

import com.ccapps.android.hextd.gamedata.Creep;

/*****************************************************
 Garden Hex Tower Defense
 Charles Capps & Joseph Lee
 ID:  920474106
 CS 313 AI and Game Design
 Fall 2012
 *****************************************************/
public interface GameManager {

    public void tick();
    public void creepHitsGoal(Creep creep);

    public Player getPlayer();
    public void setPlayer(Player player);

}
