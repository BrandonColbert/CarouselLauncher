package exn.database.android.carousellauncher.settings;

import android.graphics.Color;
import android.view.View;
import android.widget.SeekBar;

import exn.database.android.carousellauncher.R;
import exn.database.android.carousellauncher.handler.AnimationHandler;
import exn.database.android.carousellauncher.handler.InteractionHandler;
import exn.database.android.carousellauncher.handler.ViewHandler;
import exn.database.android.carousellauncher.main.CarouselLauncher;

public class SubSettingColor extends SubSettingString {
    private SubSettingColor instance;
    private View sub;

    public SubSettingColor(String key, String title, String description, String value) {
        super(key, title, description, value);
        instance = this;
    }

    public SubSettingColor(String key, String title, String description, int value) {
        this(key, title, description, String.valueOf(value));
    }

    public void load(String loadedValue) {
        if(loadedValue.isEmpty()) {
            reset();
        } else {
            value = loadedValue;
        }
    }

    public String save() {
        return value;
    }

    public String getDisplayValue() {
        return String.format("#%06X", 0xFFFFFF & Integer.valueOf(value));
    }

    public void onExit() {
        super.onExit();
        if(sub != null) {
            ViewHandler.removeView(sub);
            sub = null;
        }
    }

    public void onTap(View view) {
        ViewHandler.clearViewsOnAndAboveLayer(ViewHandler.LAYER_HOME_OVERLAY);
        sub = ViewHandler.addView(R.layout.ss_color_layout, ViewHandler.LAYER_SUB_OVERLAY);
        AnimationHandler.animateView(sub, android.R.anim.fade_in);
        sub.setBackgroundColor(Color.TRANSPARENT);
        ModifyLookViewHandler modHandler = new ModifyLookViewHandler(sub, getValue(),
                (SeekBar)CarouselLauncher.getLauncher().findViewById(R.id.colorRed),
                (SeekBar)CarouselLauncher.getLauncher().findViewById(R.id.colorGreen),
                (SeekBar)CarouselLauncher.getLauncher().findViewById(R.id.colorBlue));
        View colorCheck = CarouselLauncher.getLauncher().findViewById(R.id.colorCheck);
        if(colorCheck != null) {
            colorCheck.setOnClickListener(modHandler);
        }
        View colorReset = CarouselLauncher.getLauncher().findViewById(R.id.colorReset);
        if(colorReset != null) {
            colorReset.setOnClickListener(modHandler);
        }
    }

    private class ModifyLookViewHandler implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
        public int max, step;
        public View view;
        public SeekBar red, green, blue;

        public ModifyLookViewHandler(View view, String color, SeekBar barRed, SeekBar barGreen, SeekBar barBlue) {
            this.view = view;
            red = barRed;
            green = barGreen;
            blue = barBlue;
            red.setOnSeekBarChangeListener(this);
            green.setOnSeekBarChangeListener(this);
            blue.setOnSeekBarChangeListener(this);
            max = 255;
            step = 5;
            setupLook(color);
        }

        public void setupLook(String color) {
            int c = Integer.valueOf(color);
            setupBar(Color.red(c), red);
            setupBar(Color.green(c), green);
            setupBar(Color.blue(c), blue);
        }

        public void setupBar(int color, SeekBar bar) {
            bar.setMax(max/step);
            bar.setProgress(color/step);
        }

        public void onClick(View v) {
            if(v.getId() == R.id.colorCheck) {
                SettingsManager.saveSetting(instance);
                ViewHandler.removeView(view);
                InteractionHandler.toggleSettings(true, true);
            }
            else if(v.getId() == R.id.colorReset) {
                reset();
                setupLook(getValue());
            }
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            value = barsToRGBString();
        }

        public String barsToRGBString() {
            return String.valueOf(Color.argb(255, red.getProgress() * step, green.getProgress() * step, blue.getProgress() * step));
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}
