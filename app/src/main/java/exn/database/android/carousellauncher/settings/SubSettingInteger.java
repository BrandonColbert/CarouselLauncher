package exn.database.android.carousellauncher.settings;

import android.graphics.Color;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import exn.database.android.carousellauncher.handler.AnimationHandler;
import exn.database.android.carousellauncher.handler.InteractionHandler;
import exn.database.android.carousellauncher.handler.ViewHandler;
import exn.database.android.carousellauncher.main.CarouselLauncher;
import exn.database.android.carousellauncher.R;

public class SubSettingInteger extends SubSetting {
    protected SubSetting instance;
    protected View sub;
    protected int max;
    protected int baseValue;
    protected int value;
    protected int min;
    protected int step;

    public SubSettingInteger(String key, String title, String description, int max, int baseValue, int min, int step) {
        super(key, title, description);
        this.baseValue = baseValue;
        this.max = max - baseValue;
        this.min = min - baseValue;
        this.step = step;
        instance = this;
    }

    public int getInvertedValue() {
        return baseValue - value;
    }

    public int getDefaultValue() {
        return baseValue;
    }

    public int getValue() { return baseValue + value; }

    public void load(String loadedValue) {
        value = Integer.valueOf(loadedValue);
        if(!inRange()) {
            reset();
        }
    }

    public boolean inRange() {
        return min <= value && value <= max;
    }


    public String save() {
        return String.valueOf(value);
    }

    public void localReset() {
        value = 0;
    }

    public String getDisplayValue() {
        return String.valueOf(getValue());
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
        sub = ViewHandler.addView(R.layout.ss_integer_layout, ViewHandler.LAYER_SUB_OVERLAY);
        AnimationHandler.animateView(sub, android.R.anim.fade_in);
        sub.setBackgroundColor(Color.TRANSPARENT);
        TextView integerCheck = (TextView)CarouselLauncher.getLauncher().findViewById(R.id.integerCheck);
        if(integerCheck != null) {
            integerCheck.setText(getTitle() + ": ");
        }
        ModifyLookViewHandler modHandler = new ModifyLookViewHandler(sub, value, (TextView) CarouselLauncher.getLauncher().findViewById(R.id.integerValue), (SeekBar)CarouselLauncher.getLauncher().findViewById(R.id.integerSlider));
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

    protected class ModifyLookViewHandler implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
        public View view;
        public TextView text;
        public SeekBar bar;

        public ModifyLookViewHandler(View view, int progress, TextView text, SeekBar bar) {
            this.view = view;
            this.text = text;
            this.bar = bar;
            bar.setOnSeekBarChangeListener(this);
            setupLook(progress);
        }

        public void setupLook(int progress) {
            bar.setMax((max-min) / step);
            bar.setProgress((progress-min) / step);
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
                setupLook(value);
            }
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            value = min + (progress * step);
            text.setText(getDisplayValue());
            ViewHandler.findScreenScale();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}
