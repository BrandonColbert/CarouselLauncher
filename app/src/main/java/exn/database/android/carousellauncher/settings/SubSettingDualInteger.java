package exn.database.android.carousellauncher.settings;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import exn.database.android.carousellauncher.R;
import exn.database.android.carousellauncher.handler.AnimationHandler;
import exn.database.android.carousellauncher.handler.InteractionHandler;
import exn.database.android.carousellauncher.handler.RenderHandler;
import exn.database.android.carousellauncher.handler.SaveHandler;
import exn.database.android.carousellauncher.handler.ViewHandler;
import exn.database.android.carousellauncher.main.CarouselLauncher;

public class SubSettingDualInteger extends SubSettingInteger {
    protected int valueVertical;

    public SubSettingDualInteger(String key, String title, String description, int max, int baseValue, int min, int step) {
        super(key, title, description, max, baseValue, min, step);
    }

    protected int getDirectionValue(boolean direction) {
        return direction ? value : valueVertical;
    }

    public int getValue(boolean direction) { return baseValue + getDirectionValue(direction); }

    public void load(String loadedValue) {
        super.load(loadedValue);
    }

    public void extraLoad() {
        valueVertical = Integer.valueOf(SaveHandler.loadData(getKey()+"_v"));
    }

    public void extraSave() {
        SaveHandler.saveData(getKey()+"_v", String.valueOf(valueVertical));
    }

    public void localReset() {
        super.localReset();
        valueVertical = value;
    }

    public String getDisplayValue() {
        return getDisplayValue(true) + " | " + getDisplayValue(false);
    }

    public String getDisplayValue(boolean direction) {
        return String.valueOf(getValue(direction));
    }

    @Override
    public void onTap(View view) {
        ViewHandler.clearViewsOnAndAboveLayer(ViewHandler.LAYER_HOME_OVERLAY);
        sub = ViewHandler.addView( R.layout.ss_dual_integer_layout, ViewHandler.LAYER_SUB_OVERLAY);
        AnimationHandler.animateView(sub, android.R.anim.fade_in);
        sub.setBackgroundColor(Color.TRANSPARENT);
        TextView integerCheck = (TextView)CarouselLauncher.getLauncher().findViewById(R.id.integerCheck);
        if(integerCheck != null) {
            integerCheck.setText(getTitle() + ": ");
        }
        DualModifyLookViewHandler modHandler = new DualModifyLookViewHandler(sub,
                value, valueVertical,
                (TextView)CarouselLauncher.getLauncher().findViewById(R.id.integerValue),
                (SeekBar)CarouselLauncher.getLauncher().findViewById(R.id.integerSlider),
                (SeekBar)CarouselLauncher.getLauncher().findViewById(R.id.integerSliderV));
        if(integerCheck != null) {
            integerCheck.setOnClickListener(modHandler);
        }
        View integerValue = CarouselLauncher.getLauncher().findViewById(R.id.integerValue);
        if(integerValue != null) {
            integerValue.setOnClickListener(modHandler);
        }
        View integerReset = CarouselLauncher.getLauncher().findViewById(R.id.integerReset);
        if(integerReset != null) {
            integerReset.setOnClickListener(modHandler);
        }
    }

    protected class DualModifyLookViewHandler implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
        public View view;
        public TextView text;
        public SeekBar bar;
        public SeekBar barV;

        public DualModifyLookViewHandler(View view, int progress, int progressV, TextView text, SeekBar bar, SeekBar barV) {
            this.view = view;
            this.text = text;
            this.bar = bar;
            bar.setOnSeekBarChangeListener(this);
            this.barV = barV;
            float softButtons = 0;
            if(CarouselLauncher.getLauncher().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    Display display = CarouselLauncher.getLauncher().getWindowManager().getDefaultDisplay();
                    DisplayMetrics metrics = new DisplayMetrics();
                    display.getMetrics(metrics);
                    int currentWidth = metrics.widthPixels;
                    display.getRealMetrics(metrics);
                    int originalWidth = metrics.widthPixels;
                    if(originalWidth > currentWidth) {
                        softButtons = CarouselLauncher.cHome.getUIDisplacement() * 0.4f;
                    }
                }
                barV.setScaleX(0.4f);
                barV.setScaleY(0.4f);
            }
            barV.setX(RenderHandler.centerX - softButtons);
            barV.setY(RenderHandler.centerY);
            barV.setOnSeekBarChangeListener(this);
            setupLook(progress, progressV);
        }

        public void setupLook(int progress, int progressV) {
            bar.setMax((max-min) / step);
            bar.setProgress((progress-min) / step);
            barV.setMax(bar.getMax());
            barV.setProgress((progressV-min) / step);
            text.setText(getDisplayValue());
        }

        public void onClick(View v) {
            if(v.getId() == R.id.integerCheck || v.getId() == R.id.integerValue) {
                SettingsManager.saveSetting(instance);
                ViewHandler.removeView(view);
                InteractionHandler.toggleSettings(true, true);
            }
            else if(v.getId() == R.id.integerReset) {
                reset();
                setupLook(value, valueVertical);
            }
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(seekBar.getId() == barV.getId()) {
                valueVertical = min + (progress * step);
            }
            else {
                value = min + (progress * step);
            }
            text.setText(getDisplayValue());
            ViewHandler.findScreenScale();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}
