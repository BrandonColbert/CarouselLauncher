package exn.database.android.carousellauncher.main;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import exn.database.android.carousellauncher.handler.AppHandler;
import exn.database.android.carousellauncher.handler.InteractionHandler;
import exn.database.android.carousellauncher.handler.PhysicsHandler;
import exn.database.android.carousellauncher.handler.RenderHandler;
import exn.database.android.carousellauncher.handler.ViewHandler;
import exn.database.android.carousellauncher.settings.SettingsManager;

public class CarouselView extends View implements Runnable {
    private final CarouselLauncher launcher;
    public boolean restoreStateExists;

    public CarouselView(Context context) {
        super(context);
        launcher = null;
    }

    public CarouselView(CarouselLauncher launcher, boolean restoreStateExists) {
        super(launcher);
        this.launcher = launcher;
        this.restoreStateExists = restoreStateExists;
    }

    public void update() {
        PhysicsHandler.returnHome();
        PhysicsHandler.velocity(SettingsManager.friction.asPercent().getPercentage(), SettingsManager.boundaries.asBoolean().getValue(), InteractionHandler.panning);
    }

    public void render() {
        RenderHandler.calcZoom();
        if(SettingsManager.showBoundaries.asBoolean().getValue()) {
            RenderHandler.renderBoundaries(Integer.valueOf(SettingsManager.boundaryColor.asString().getValue()));
        }
        if(AppHandler.isLoaded) {
            RenderHandler.renderApps(
                    AppHandler.apps,
                    SettingsManager.appLabels.asBoolean().getValue(),
                    Integer.valueOf(SettingsManager.labelColor.asString().getValue()),
                    SettingsManager.labelSize.asPercent().getPercentage()
            );
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        refreshWindow();
    }

    public void refreshWindow() {
        Point screenPixels = new Point();
        Display display = ((WindowManager)launcher.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(screenPixels);
        } else {
            display.getSize(screenPixels);
            screenPixels.y += getUIDisplacement();
        }
        int width = screenPixels.x;
        int height = screenPixels.y;
        RenderHandler.centerX = (int)(width*0.5);
        RenderHandler.centerY = (int)(height*0.5);
        if(!restoreStateExists) {
            ViewHandler.findScreenScale();
        }
    }

    public int getUIDisplacement() {
        if(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)) {
            Resources resources = launcher.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if(resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }

    public void run() {
        update();
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        RenderHandler.canvas = canvas;
        super.onDraw(canvas);
        render();
        postDelayed(this, 0);
    }

    public boolean onTouchEvent(MotionEvent event) {
        PhysicsHandler.goingHome = false;
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                InteractionHandler.onTapUp(x, y);
                break;
            case MotionEvent.ACTION_DOWN:
                InteractionHandler.onTapDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2) {
                    int x2 = (int) event.getX(1);
                    int y2 = (int) event.getY(1);
                    InteractionHandler.onDualFingerMove(x, y, x2, y2, SettingsManager.invertZoom.asBoolean().getValue());
                } else if (event.getPointerCount() == 1) {
                    InteractionHandler.onFingerMove(x, y, SettingsManager.sensitivity.asPercent().getPercentage());
                }
                break;
        }
        return true;
    }
}
