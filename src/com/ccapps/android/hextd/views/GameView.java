package com.ccapps.android.hextd.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;
import android.widget.RelativeLayout;
import com.ccapps.android.hextd.activities.GameLogicThread;
import com.ccapps.android.hextd.draw.HexGrid;
import com.ccapps.android.hextd.gamedata.StaticData;
import com.ccapps.android.hextd.metagame.Player;

import java.util.logging.Logger;

/*****************************************************
 Garden Hex Tower Defense
 Charles Capps & Joseph Lee
 ID:  920474106
 CS 313 AI and Game Design
 Fall 2012
 *****************************************************/

//CLC: Original Code Begin
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    public static TowerMenuView towerMenu = null;

    private GameViewThread gameViewThread;
    private GestureDetector gestureDetector;
    private GameLogicThread gameLogicThread;
    private Logger l = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private Context context;
    private View gameActivityView;

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
        getHolder().addCallback(this);
        this.context = context;
    }

    public void setGameLogicThread(GameLogicThread gameLogicThread) {
        this.gameLogicThread = gameLogicThread;
    }

    public void stopDrawing() {
        this.gameViewThread.postSuspendMe();
    }

    public void startDrawing() {
        if (this.gameViewThread != null) {
            gameViewThread.postNeedsDrawing();
            gameViewThread.unSuspendMe();
        }
    }

    public void postNeedsDrawing() {
        this.gameViewThread.postNeedsDrawing();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean result = gestureDetector.onTouchEvent(e);
        if (!result) {
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                return true;
            }
            if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                return true;
            }
            if (e.getActionMasked() == MotionEvent.ACTION_CANCEL){
                return true;
            }
        }
        return result;
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        this.gameActivityView = (RelativeLayout)getParent();
        gameViewThread = new GameViewThread(getHolder());
        gestureDetector = new GestureDetector(this.getContext(), new GameTouchListener(gameViewThread, gameActivityView));
        requestFocusFromTouch();
        this.bringToFront();
        gameViewThread.start();
        gameLogicThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
       // gameViewThread.drawGrid();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * From docs, looks like if I do it this way w / a custom thread, there's no need to even pause the thread.
     * I can just draw "as fast as your thread is capable". http://developer.android.com/guide/topics/graphics/2d-graphics.html
     */
    public class GameViewThread extends Thread {
        private SurfaceHolder sh;
        private boolean needsDrawing;
        private PointF gridShiftValue;
        private boolean isRunning;
        private HexGrid gridInstance;
        private static final long PAUSE_TIME = 5; // limit to 60 fps, reduce computations
        private static final float VELOCITY_FACTOR = 1.5f;
        private Paint textPaint;
        private Player player;

        public GameViewThread(SurfaceHolder sh) {
            super();
            this.sh = sh;
            this.needsDrawing = true;
            this.gridInstance = HexGrid.getInstance();
            this.isRunning = false;
            this.textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(20f);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setAntiAlias(true);
            this.player = Player.getInstance();

        }

        @Override
        public void run() {
            this.isRunning = true;
            eventLoop();
        }

        public void postSuspendMe() {
            this.isRunning = false;
        }

        private void drawGrid() {

            if (this.needsDrawing) {
                Canvas c = sh.lockCanvas();

                //c.drawColor(Color.BLACK);  //avoid lingering artifacts from previous draw
                drawGrass(c);

                gridInstance.draw(c);

                drawInfoBox(c);

                this.needsDrawing = false;

                sh.unlockCanvasAndPost(c);
            }

        }

        private void drawInfoBox(Canvas c) {
            c.drawText("Money: $" + player.getMonies(), 20.f, 25.f, textPaint);
            c.drawText("Life: " + player.getLife(), 20.f, 50.f, textPaint);
        }

        private void drawGrass(Canvas c) {
            Bitmap grass = StaticData.GRASS;
            final int H = StaticData.DEFAULT_SCREEN_SIZE.getHeight();
            final int grassW = grass.getWidth();
            final int grassH = grass.getHeight();
            int maxHeightDrawn = 0;
            while(maxHeightDrawn < H) {

                int startY = ((maxHeightDrawn - (int)HexGrid.GLOBAL_OFFSET.y) % grassH);
                if (startY < 0)
                    startY += grassH;
                int htToDraw = grassH - startY;

                Rect src = new Rect(0, startY, grassW, grassH);
                Rect dest = new Rect(0,maxHeightDrawn,grassW, maxHeightDrawn + htToDraw);
                c.drawBitmap(grass,src,dest,null);
                maxHeightDrawn += htToDraw;
            }
        }

        private void shiftGrid() {
            if (this.gridShiftValue != null) {
                HexGrid.shiftTopLeft(gridShiftValue);
                this.gridShiftValue = null;
                this.needsDrawing = true;
            }
        }

        public void postShiftGrid(float x, float y) {
            x *= VELOCITY_FACTOR;
            y *= VELOCITY_FACTOR;
            this.gridShiftValue = new PointF(x, y);
        }

        private void pauseMe(long time) {
            try {
                synchronized (this) {
                    this.wait(time);
                }
            } catch (InterruptedException e) {
                //pwn
            }
        }

        private void suspendMe() {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                //pwn
            }
        }

        public void unSuspendMe() {
            this.isRunning = true;
            synchronized (this) {
                this.interrupt();
            }

        }

        public void postNeedsDrawing() {
            this.needsDrawing = true;
        }

        /**
         * Wait, then shift the grid if necessary, then draw the grid
         */
        private void eventLoop() {

            while ( true ) {
                if (isRunning) {
                    pauseMe(PAUSE_TIME);
                    shiftGrid();
                    drawGrid();
                } else {
                    suspendMe();
                }
            }


        }


    }
}
//CLC: Original Code End
