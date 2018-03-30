package exn.database.android.carousellauncher.settings;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import exn.database.android.carousellauncher.R;
import exn.database.android.carousellauncher.handler.AnimationHandler;
import exn.database.android.carousellauncher.handler.ViewHandler;
import exn.database.android.carousellauncher.main.CarouselLauncher;

public class SubSettingEnum extends SubSetting {
    private SubSetting instance;
    private View sub;
    private List<Enum> enums;
    private Class<? extends Enum> enumClass;
    private Enum defaultValue;
    private Enum value;
    private SSAction subTapAction;
    private boolean hasSubTapAction;

    public SubSettingEnum(String key, String title, String description, Class<? extends Enum> enumClass, Enum value) {
        super(key, title, description);
        instance = this;
        this.enumClass = enumClass;
        defaultValue = value;
        enums = new ArrayList<>(EnumSet.allOf(enumClass));
    }

    public Enum getValue() {
        return value;
    }

    @Override
    public SubSetting setTapAction(SSAction action) {
        hasSubTapAction = true;
        subTapAction = action;
        return this;
    }

    public void load(String loadedValue) {
        value = Enum.valueOf(enumClass, loadedValue);
    }

    public String save() {
        return value.name();
    }

    public void localReset() {
        value = defaultValue;
    }

    public String getDisplayValue() {
        return (value instanceof DisplayableValue) ? ((DisplayableValue)value).getDisplayValue() : "";
    }

    public Object getObjectValue() {
        return (value instanceof ObjectValue) ? ((ObjectValue)value).getObjectValue() : null;
    }

    public void setValue(Enum value) {
        this.value = value;
    }

    public void onExit() {
        super.onExit();
        if(sub != null) {
            AnimationHandler.animateView(sub, android.R.anim.slide_out_right);
            ViewHandler.removeView(sub);
            sub = null;
        }
    }

    public void onTap(View view) {
        ModifyLookViewHandler lookViewHandler = new ModifyLookViewHandler();
        sub = ViewHandler.addView(R.layout.ss_enum_layout, ViewHandler.LAYER_SUB_OVERLAY);
        sub.setBackgroundColor(Color.TRANSPARENT);
        ListView listView = (ListView)CarouselLauncher.getLauncher().findViewById(R.id.enumList);
        if (listView != null) {
            listView.setAdapter(lookViewHandler);
            listView.setOnItemClickListener(lookViewHandler);
        }
        View enumBorder = CarouselLauncher.getLauncher().findViewById(R.id.enumBorder);
        if(enumBorder != null) {
            enumBorder.setOnClickListener(lookViewHandler);
        }
        AnimationHandler.animateView(sub, android.R.anim.slide_in_left);
    }

    private class ModifyLookViewHandler extends ArrayAdapter<Enum> implements AdapterView.OnItemClickListener, View.OnClickListener {
        public ModifyLookViewHandler() {
            super(CarouselLauncher.getLauncher(), R.layout.ss_layout_2, enums);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(position <= enums.size()) {
                Enum theEnum = enums.get(position);
                view = CarouselLauncher.getLauncher().getLayoutInflater().inflate(R.layout.ss_layout_2, parent, false);
                ((TextView)view.findViewById(R.id.l2Title)).setText((theEnum instanceof DisplayableValue) ? ((DisplayableValue)theEnum).getDisplayValue() : "");
            }
            return view;
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position <= enums.size()) {
                value = enums.get(position);
                if(hasSubTapAction) {
                    subTapAction.action();
                }
                ((TextView)view.findViewById(R.id.l2Title)).setText((value instanceof DisplayableValue) ? ((DisplayableValue)value).getDisplayValue() : "");
                instance.display(instance.getView());
                SettingsManager.saveSetting(instance);
                onExit();
            }
        }

        public void onClick(View v) {
            onExit();
        }
    }
}
