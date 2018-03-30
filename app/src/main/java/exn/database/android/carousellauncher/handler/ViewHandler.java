package exn.database.android.carousellauncher.handler;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import exn.database.android.carousellauncher.main.CarouselLauncher;
import exn.database.android.carousellauncher.settings.SettingsManager;

public class ViewHandler {
    private static Map<View, Integer> views = new Hashtable<>();
    public static int screenScale;
    public static int appSize;

    public static final int LAYER_HOME = 0;
    public static final int LAYER_HOME_OVERLAY = 1;
    public static final int LAYER_SUB_OVERLAY = 2;

    public static void findScreenScale() {
        int width = RenderHandler.centerX * 2;
        int height = RenderHandler.centerY * 2;
        double estSize = (width * height) / (1440 * 2560) * 50 + SettingsManager.appSize.asInteger().getDefaultValue();
        screenScale = (int) Math.round((width * height) / (estSize * estSize));
        estSize = (width * height) / (1440 * 2560) * 50 + SettingsManager.appSize.asInteger().getInvertedValue();
        appSize = (int) Math.round((width * height) / (estSize * estSize));
        AppHandler.calcAppLoc();
    }

    public static View addView(int id, int layer) {
        View view = null;
        if(hasViewGroup()) {
            view = CarouselLauncher.getLauncher().getLayoutInflater().inflate(id, null);
            views.put(view, layer);
            getViewMatrix().addView(view);
        }
        return view;
    }

    public static void removeView(View view) {
        if(hasViewGroup()) {
            getViewMatrix().removeView(view);
            views.remove(view);
        }
    }

    public static void clearViewsOnAndAboveLayer(int layer) {
        clearViewsOnAndAboveLayer(layer, 0);
    }

    public static void clearViewsOnAndAboveLayer(int layer, int animID) {
        List<View> viewsToRemove = new ArrayList<>();
        for(Map.Entry<View, Integer> set : views.entrySet()) {
            if(set.getValue() >= layer) {
                viewsToRemove.add(set.getKey());
            }
        }
        for(View view : viewsToRemove) {
            AnimationHandler.animateView(view, animID);
            removeView(view);
        }
    }

    public static Map<View, Integer> getViewChildren() {
        return views;
    }

    public static void clearViewsOnAboveLayer(int layer) {
        clearViewsOnLayer(layer, 0);
    }

    public static void clearViewsOnLayer(int layer, int animID) {
        List<View> viewsToRemove = new ArrayList<>();
        for(Map.Entry<View, Integer> set : views.entrySet()) {
            if(set.getValue() == layer) {
                viewsToRemove.add(set.getKey());
            }
        }
        for(View view : viewsToRemove) {
            AnimationHandler.animateView(view, animID);
            removeView(view);
        }
    }

    public static boolean hasViewOnLayer(int layer) {
        return hasViewOnOrAboveLayer(layer, true, false);
    }

    public static boolean hasViewOnOrAboveLayer(int layer, boolean on, boolean above) {
        for(Map.Entry<View, Integer> set : views.entrySet()) {
            if(on && set.getValue() == layer) {
                return true;
            }
            else if(above && set.getValue() > layer) {
                return true;
            }
        }
        return false;
    }

    public static ViewGroup getViewMatrix() {
        return (ViewGroup)CarouselLauncher.cHome.getParent();
    }

    public static boolean hasViewGroup() {
        return CarouselLauncher.cHome.getParent() != null && CarouselLauncher.cHome.getParent() instanceof ViewGroup;
    }

    public static void fullscreen(boolean value) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int flag = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            if(value) {
                CarouselLauncher.getLauncher().getWindow().addFlags(flag);
            } else {
                CarouselLauncher.getLauncher().getWindow().clearFlags(flag);
            }
        }
    }
}
