package exn.database.android.carousellauncher.handler;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.concurrent.TimeUnit;

import exn.database.android.carousellauncher.R;
import exn.database.android.carousellauncher.app.App2D;
import exn.database.android.carousellauncher.main.CarouselLauncher;
import exn.database.android.carousellauncher.settings.SettingsManager;

public class InteractionHandler implements SaveHandler.StateSaver {
    public static SearchHandler searchHandler = new SearchHandler();
    private static final EditPressDetector editPressDetector = new EditPressDetector();
    public static String searchText;
    public static int lastDist, startX, startY, moveX, moveY;
    public static boolean panning, zooming, settingsActive, searchActive, editActive, onHome = true;

    public static class SearchHandler implements TextWatcher, View.OnClickListener, View.OnFocusChangeListener {
        private EditText editText;
        private boolean keyboardShown;
        public void setupEditText() {
            editText = (EditText)CarouselLauncher.getLauncher().findViewById(R.id.homeText);
            if(editText != null) {
                editText.addTextChangedListener(searchHandler);
                editText.setOnClickListener(searchHandler);
                editText.setOnFocusChangeListener(searchHandler);
            }
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            searchText = s.toString();
            for (App2D app : AppHandler.apps) {
                app.hidden = !(s.length() <= 0 || app.label.toLowerCase().contains(searchText.toLowerCase()));
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void afterTextChanged(Editable s) {}
        public void onClick(View v) {
            keyboardShown = true;
        }
        public void onFocusChange(View v, boolean hasFocus) {
            keyboardShown = hasFocus;
        }

        public void hideKeyboard() {
            if(keyboardShown) {
                InputMethodManager keyboard = (InputMethodManager)CarouselLauncher.getLauncher().getSystemService(Activity.INPUT_METHOD_SERVICE);
                if(CarouselLauncher.getLauncher().getCurrentFocus() == null) {
                    keyboard.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
                else {
                    View view = CarouselLauncher.getLauncher().getCurrentFocus();
                    if(view != null) {
                        keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                searchHandler.keyboardShown = false;
            }
        }
    }

    public static class EditPressDetector implements Runnable {
        public Handler handler = new Handler();
        private boolean tryingDetect, justDetected;
        private int ex, ey;

        public void detectLongPress(int x, int y) {
            if(onHome && !editActive && !tryingDetect) {
                ex = x;
                ey = y;
                tryingDetect = true;
                handler.postDelayed(editPressDetector, TimeUnit.SECONDS.toMillis(1));
            }
        }

        public void cancelEditMode() {
            if(tryingDetect) {
                ex = 0;
                ey = 0;
                tryingDetect = false;
                if(editActive) {
                    AnimationHandler.compressEditCircle();
                }
                editPressDetector.handler.removeCallbacks(editPressDetector);
            }
        }

        public void run() {
            editActive = true;
            tryingDetect = false;
            justDetected = true;
            AnimationHandler.pulseApps(ex, ey);
            AnimationHandler.expandEditCircle();
        }
    }

    public static void onTapUp(int x, int y) {
        editPressDetector.cancelEditMode();
        if(!editPressDetector.justDetected && !panning && !ViewHandler.hasViewOnOrAboveLayer(ViewHandler.LAYER_HOME_OVERLAY, true, true)) {
            if(AppHandler.isLoaded) {
                for (App2D app : AppHandler.apps) {
                    if (!app.hidden && isTappedIn(x, y, app.getX(), app.getY(), ViewHandler.screenScale)) {
                        AppHandler.launchApp(app);
                        break;
                    }
                }
            }
        }
        editPressDetector.justDetected = false;
        panning = false;
        zooming = false;
        lastDist = 0;
    }

    public static void onTapDown(int x, int y) {
        startX = x;
        startY = y;
        moveX = 0;
        moveY = 0;
        PhysicsHandler.velocityX = 0;
        PhysicsHandler.velocityY = 0;
        editPressDetector.detectLongPress(x, y);
        if(onHome) {
            searchHandler.hideKeyboard();
        }
    }

    public static void onDualFingerMove(int x1, int y1, int x2, int y2,boolean invertZoom) {
        int x = x1 - x2;
        int y = y1 - y2;
        int dist = (int)Math.sqrt((x * x) + (y * y));

        if(panning && zooming) {
            editPressDetector.cancelEditMode();
            float inc = (lastDist - dist) * 0.001f * (invertZoom ? -1 : 1);
            if(inc != 0f && 0.2f < RenderHandler.zoom - inc && RenderHandler.zoom - inc < 2f) {
                RenderHandler.zoom -= inc;
            }
        }

        lastDist = dist;
        panning = true;
        zooming = true;
        PhysicsHandler.velocityX = 0;
        PhysicsHandler.velocityY = 0;
    }

    public static void onFingerMove(int x, int y, double sensitivity) {
        if(!zooming) {
            moveX = x - startX;
            moveY = y - startY;
            int movement = Math.abs(moveX) + Math.abs(moveY);
            double sense = 3;
            double panSense = ViewHandler.screenScale / (sense * 2);
            panSense = panSense < sense ? sense : panSense;
            if (panning) {
                double mod = (1/RenderHandler.zoom) * sensitivity;
                PhysicsHandler.velocityX += moveX * mod;
                PhysicsHandler.velocityY += moveY * mod;
                startX = x;
                startY = y;
            } else if (movement > panSense) {
                panning = true;
                editPressDetector.cancelEditMode();
            }
        }
    }

    public static void toggleSearch(boolean state, boolean animate) {
        editActive = false;
        searchActive = state;
        ViewHandler.fullscreen(!state);
        if(state) {
            AnimationHandler.pulseApps(-RenderHandler.getScrollX(0), -RenderHandler.getScrollY(0));
            View view = ViewHandler.addView(R.layout.home_search, ViewHandler.LAYER_HOME);
            view.setBackgroundColor(Color.TRANSPARENT);
            if (animate) {
                AnimationHandler.animateView(view, android.R.anim.slide_in_left);
            }
            searchHandler.setupEditText();
        }
        else {
            searchText = "";
            ViewHandler.clearViewsOnLayer(ViewHandler.LAYER_HOME, android.R.anim.slide_out_right);
            for(App2D app : AppHandler.apps) {
                app.hidden = false;
            }
        }
    }

    public static void toggleSettings(boolean state, boolean animate) {
        editActive = false;
        if(state) {
            searchHandler.hideKeyboard();
            toggleSearch(false, true);
        }
        settingsActive = state;
        ViewHandler.fullscreen(!state);
        if(state) {
            if(ViewHandler.hasViewOnOrAboveLayer(ViewHandler.LAYER_HOME_OVERLAY, true, true)) {
                boolean subAnim = ViewHandler.hasViewOnLayer(ViewHandler.LAYER_HOME_OVERLAY);
                SettingsManager.exitSettingsView();
                if(subAnim) {
                    if (animate) {
                        AnimationHandler.animateLayer(ViewHandler.LAYER_HOME_OVERLAY, 0);
                    }
                    ViewHandler.clearViewsOnAndAboveLayer(ViewHandler.LAYER_SUB_OVERLAY, 0);
                    return;
                }
            }

            View view = ViewHandler.addView(R.layout.activity_main, ViewHandler.LAYER_HOME_OVERLAY);
            if(animate) {
                AnimationHandler.animateView(view, android.R.anim.fade_in);
            }
            SettingsManager.launchCustomizationHandler();
        }
        else {
            SettingsManager.exitSettingsView();
            ViewHandler.clearViewsOnAndAboveLayer(ViewHandler.LAYER_HOME_OVERLAY, 0);
            ViewHandler.findScreenScale();
        }
    }

    public static void onLauncherApp() {
        if(ViewHandler.hasViewOnLayer(ViewHandler.LAYER_HOME)) {
            RenderHandler.zoom = 1;
            toggleSettings(true, true);
        }
        else {
            toggleSearch(true, true);
        }
    }

    public static void onBackPressed() {
        if(editActive) {
            editPressDetector.tryingDetect = true;
            editPressDetector.cancelEditMode();
        }
        else if(!ViewHandler.hasViewOnOrAboveLayer(ViewHandler.LAYER_HOME, true, true)) {
            toggleSearch(true, true);
        }
        else if(ViewHandler.hasViewOnLayer(ViewHandler.LAYER_HOME)) {
            toggleSearch(false, true);
        }
        else if(ViewHandler.hasViewOnLayer(ViewHandler.LAYER_SUB_OVERLAY)) {
            toggleSettings(true, true);
        }
        else if(ViewHandler.hasViewOnLayer(ViewHandler.LAYER_HOME_OVERLAY)) {
            toggleSettings(false, false);
            AnimationHandler.animateView(CarouselLauncher.cHome, android.R.anim.fade_in);
            onHome = true;
        }
    }

    public static void onHomePressed() {
        if(onHome) {
            if(ViewHandler.hasViewOnOrAboveLayer(ViewHandler.LAYER_HOME_OVERLAY, true, true)) {
                toggleSettings(false, false);
                AnimationHandler.animateView(CarouselLauncher.cHome, android.R.anim.fade_in);
            } else if (ViewHandler.hasViewOnLayer(ViewHandler.LAYER_HOME)) {
                toggleSearch(false, true);
            } else if(AppHandler.apps.size() < 1) {
                AppHandler.forceReloadApps();
            } else {
                if(editActive) {
                    editPressDetector.tryingDetect = true;
                    editPressDetector.cancelEditMode();
                }
                else if (RenderHandler.scrollX != 0 || RenderHandler.scrollY != 0 || RenderHandler.zoom != 1) {
                    if(SettingsManager.useAnimations.asBoolean().getValue()) {
                        PhysicsHandler.goHome();
                    } else {
                        RenderHandler.scrollX = 0;
                        RenderHandler.scrollY = 0;
                        RenderHandler.zoom = 1;
                    }
                } else {
                    AppHandler.calcAppLoc();
                }
            }
        }
        else {
            onHome = true;
        }
    }

    public static boolean isTappedIn(int x, int y, int appX, int appY, int size) {
        float xs = RenderHandler.getScrollX(x);
        float ys = RenderHandler.getScrollY(y);

        float left = (appX - size / 2f) * RenderHandler.zoom;
        float right = (appX + size / 2f) * RenderHandler.zoom;
        float bottom = (appY - size / 2f) * RenderHandler.zoom;
        float top = (appY + size / 2f) * RenderHandler.zoom;

        return left <= xs && xs <= right && bottom <= ys && ys <= top;
    }

    public void saveState(Bundle bundle) {
        if(settingsActive) {
            bundle.putBoolean("SettingsActive", true);
        }
        else if(searchActive) {
            bundle.putBoolean("SearchActive", true);
            bundle.putString("SearchText", searchText);
        }
    }

    public void reloadState(Bundle bundle) {
        if(bundle.getBoolean("SettingsActive")) {
            toggleSettings(false, false);
            toggleSettings(true, false);
        }
        else if(bundle.getBoolean("SearchActive")) {
            toggleSearch(false, false);
            toggleSearch(true, false);
            EditText homeText = (EditText)CarouselLauncher.getLauncher().findViewById(R.id.homeText);
            if(homeText != null) {
                homeText.setText(bundle.getString("SearchText", searchText));
            }
        }
    }
}
