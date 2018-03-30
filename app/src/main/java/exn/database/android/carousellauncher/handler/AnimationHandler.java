package exn.database.android.carousellauncher.handler;

import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;

import java.util.Map;

import exn.database.android.carousellauncher.app.App2D;
import exn.database.android.carousellauncher.main.CarouselLauncher;
import exn.database.android.carousellauncher.settings.SettingsManager;

public class AnimationHandler {
    public static void animateView(View view, int id) {
        if(id > 0 && useAnimations()) {
            view.startAnimation(AnimationUtils.loadAnimation(CarouselLauncher.getLauncher(), id));
        }
    }

    public static void animateLayer(int layer, int id) {
        if(id > 0 && useAnimations()) {
            for(Map.Entry<View, Integer> set : ViewHandler.getViewChildren().entrySet()) {
                if(set.getValue() == layer) {
                    animateView(set.getKey(), id);
                }
            }
        }
    }

    public static void editPoke(App2D app) {
        app.inflate(60 * (app.deletable ? 1 : -1));
    }

    public static void pulseApps(float x, float y) {
        if(useAnimations()) {
            for (final App2D app : AppHandler.apps) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        app.inflate(30);
                    }
                }, Math.round(
                    (Math.abs(RenderHandler.getScrollX(x)/RenderHandler.zoom-app.getStaticX()) +
                    (Math.abs(RenderHandler.getScrollY(y)/RenderHandler.zoom-app.getStaticY()))
                    ) * 0.15));
            }
        }
    }

    public static boolean useAnimations() {
        return SettingsManager.useAnimations.asBoolean().getValue();
    }

    public static void expandEditCircle() {
        if(useAnimations()) {
            new Thread(){
                @Override
                public void run() {
                    RenderHandler.editCircleSize = 0;
                    try {
                        while (RenderHandler.editCircleSize < 1) {
                            sleep(5);
                            RenderHandler.editCircleSize += 0.1f;
                        }
                    } catch (Exception e) {
                        RenderHandler.editCircleSize = 1;
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        else {
            RenderHandler.editCircleSize = 1;
        }
    }

    public static void compressEditCircle() {
        if(useAnimations()) {
            new Thread(){
                @Override
                public void run() {
                    try {
                        while (RenderHandler.editCircleSize > 0) {
                            sleep(5);
                            RenderHandler.editCircleSize -= 0.1f;
                        }
                        InteractionHandler.editActive = false;
                    } catch (Exception e) {
                        RenderHandler.editCircleSize = 0;
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        else {
            RenderHandler.editCircleSize = 0;
            InteractionHandler.editActive = false;
        }
    }
}
