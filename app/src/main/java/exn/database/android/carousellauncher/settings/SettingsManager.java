package exn.database.android.carousellauncher.settings;

import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import exn.database.android.carousellauncher.R;
import exn.database.android.carousellauncher.app.AppUsage;
import exn.database.android.carousellauncher.app.EnumAppSorting;
import exn.database.android.carousellauncher.app.EnumAppStyle;
import exn.database.android.carousellauncher.handler.AppHandler;
import exn.database.android.carousellauncher.handler.SaveHandler;
import exn.database.android.carousellauncher.handler.ViewHandler;
import exn.database.android.carousellauncher.main.CarouselLauncher;

public class SettingsManager {
    private static MiniImp miniImp;
    private static List<SubSetting> settings;
    public static SubSetting circularApps = new SubSettingBoolean("circular_apps", "Circular Apps", "All apps appear in the shape of a circle", true);
    public static SubSetting appSize = new SubSettingInteger("app_size", "App Size", "", 100, 80, 50, 1);
    public static SubSetting boundaries = new SubSettingBoolean("boundaries", "Boundaries", "Prevent scrolling beyond visible apps", true);
    public static SubSetting appLabels = new SubSettingBoolean("app_labels", "App Labels", "Display app name under its icon", true);
    public static SubSetting useAnimations = new SubSettingBoolean("use_animations", "Animations", "Whether or not to use animations", true);
    public static SubSetting invertZoom = new SubSettingBoolean("invert_zoom", "Invert Zoom", "Swap actions from zoom gestures", false);
    public static SubSetting launcherCentered = new SubSettingBoolean("launch_centered", "Launcher Centered", "Keep launcher icon in the center of layouts", true);
    public static SubSetting labelSize = new SubSettingPercent("label_size", "Label Size", "The size of the label underneath an app", 20, 200, 50, 5);
    public static SubSetting friction = new SubSettingPercent("friction", "Friction", "Rate of speed loss when panning", 80, 120, 60, 1);
    public static SubSetting labelColor = new SubSettingColor("label_color", "Label Color", "Text color used for app labels", Color.WHITE);
    public static SubSetting sensitivity = new SubSettingPercent("sensitivity", "Sensitivity", "Distance to pan relative to finger distance", 25, 200, 50, 5);
    public static SubSetting edgeEffect = new SubSettingBoolean("edge_effect", "Edge Effect", "Shrinks app on the edge of the screen", false);
    public static SubSetting shrinkFactor = new SubSettingDualPercent("shrink_factor", "Edge", "Customize how much apps should shrink when closer to the edge of the screen", 100, 200, 50, 5);
    public static SubSetting spacing = new SubSettingDualInteger("spacing", "Spacing", "How far apps are shown from each other", 100, 50, 0, 1);
    public static SubSetting sortType = new SubSettingEnum("sorting_type", "Sorting Type", "How to sort apps", EnumAppSorting.class, EnumAppSorting.TIMES_OPENED);
    public static SubSetting layoutStyle = new SubSettingEnum("layout_style", "Layout Style", "Location of apps on home", EnumAppStyle.class, EnumAppStyle.CAROUSEL);
    public static SubSetting showBoundaries = new SubSettingBoolean("show_boundaries", "Show Boundaries", "Show boundaries surrounding apps", false);
    public static SubSetting boundaryColor = new SubSettingColor("boundary_color", "Boundary Color", "Color used to show boundary", Color.BLUE);
    public static SubSetting focusApps = new SubSettingBoolean("focus_apps", "Focus Apps", "Focus on an app when tapped", false);
    public static SubSetting resetUsage;
    public static SubSetting resetSettings;
    public static SubSetting forceAppReload;
    public static SubSetting defaultLauncher;

    public static void init() {
        settings = new ArrayList<>();
        settings.clear();
        setupActions();
        rcs(new SettingSeperator("Appearance"));
        rcs(appSize);
        rcs(circularApps);
        rcs(appLabels);
        rcs(labelSize.setUnusableMessage(appLabels.getTitle() + " must be enabled to use"));
        rcs(labelColor);
        rcs(edgeEffect);
        rcs(shrinkFactor.setUnusableMessage(edgeEffect.getTitle() + " must be disabled to use"));
        rcs(useAnimations);
        rcs(showBoundaries);
        rcs(boundaryColor);
        rcs(new SettingSeperator("Layout"));
        rcs(layoutStyle);
        rcs(sortType);
        rcs(spacing);
        rcs(launcherCentered);
        rcs(new SettingSeperator("Interaction"));
        rcs(sensitivity);
        rcs(invertZoom);
        rcs(focusApps);
        rcs(boundaries);
        rcs(friction);
        rcs(new SettingSeperator("Actions"));
        rcs(resetUsage);
        rcs(resetSettings);
        rcs(forceAppReload);
        rcs(defaultLauncher);

        if(SaveHandler.hasKey(getSaveKey())) {
            loadSettings();
        }
        else {
            SaveHandler.saveData(getSaveKey(), true);
            resetSettings();
        }

        setupConditionalSettings();

        miniImp = new MiniImp();
    }

    public static void launchCustomizationHandler() {
        ListView listView = (ListView)CarouselLauncher.getLauncher().findViewById(R.id.csettingsList);
        if(listView != null && miniImp != null) {
            listView.setAdapter(miniImp);
            listView.setOnItemClickListener(miniImp);
        }
    }

    public static void loadSettings() {
        for(SubSetting setting : settings) {
            loadSetting(setting);
        }
    }

    public static void loadSetting(SubSetting setting) {
        try {
            setting.load(SaveHandler.loadData(setting.getKey()));
            setting.extraLoad();
        }
        catch(Exception e) {
            resetSetting(setting);
        }
    }

    public static void saveSettings() {
        for(SubSetting setting : settings) {
            saveSetting(setting);
        }
    }

    public static void saveSetting(SubSetting setting) {
        SaveHandler.saveData(setting.getKey(), setting.save());
        setting.extraSave();
    }
    
    private static void resetAppUsage() {
        for(AppUsage app : AppHandler.apps) {
            app.timesOpened = 0;
            app.saveUsage();
        }
    }

    public static void exitSettingsView() {
        for(SubSetting setting : settings) {
            setting.onExit();
            if(setting.getView() != null) {
                ViewHandler.removeView(setting.getView());
            }
            saveSetting(setting);
            loadSetting(setting);
        }
    }

    public static void resetSettings() {
        for(SubSetting setting : settings) {
            resetSetting(setting);
            setupConditionalSettings();
        }
    }

    public static void resetSetting(SubSetting setting) {
        setting.reset();
        if(setting.getView() != null) {
            setting.display(setting.getView());
        }
    }

    public static void requestDefaultLauncher() {
        CarouselLauncher launcher = CarouselLauncher.getLauncher();
        PackageManager packageManager = launcher.getPackageManager();
        ComponentName componentName = new ComponentName(launcher, CarouselLauncher.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        launcher.getPackageManager().clearPackagePreferredActivities(launcher.getPackageName());
        launcher.startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public static void setupConditionalSettings() {
        shrinkFactor.setUsable(!edgeEffect.asBoolean().getValue());
        labelSize.setUsable(appLabels.asBoolean().getValue());
    }

    public static void setupActions() {
        circularApps.setTapAction(new SubSetting.SSAction() {
            public void action() {
                AppHandler.forceReloadApps();
                CarouselLauncher.showMessage("Apps are " + (circularApps.asBoolean().getValue() ? "now" : "no longer") + " circular");
            }
        });
        sortType.setTapAction(new SubSetting.SSAction() {
            public void action() {
                switch((EnumAppSorting)sortType.asEnum().getValue()) {
                    case USAGE:
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 ) {
                            boolean hasUsageAccess = CarouselLauncher.hasPermission(AppOpsManager.OPSTR_GET_USAGE_STATS, true);
                            if(!hasUsageAccess) {
                                CarouselLauncher.requestPermission(Settings.ACTION_USAGE_ACCESS_SETTINGS, true, new CarouselLauncher.PermissionRequester() {
                                    public void permissionResult(boolean granted) {
                                        sortType.asEnum().setValue(EnumAppSorting.USAGE);
                                    }
                                });
                            }
                        } else {
                            CarouselLauncher.showMessage("This feature requires Lollipop 5.1 or higher");
                            sortType.reset();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        edgeEffect.setTapAction(new SubSetting.SSAction() {
            public void action() {
                shrinkFactor.setUsable(!edgeEffect.asBoolean().getValue());
                shrinkFactor.display(shrinkFactor.getView());
            }
        });
        appLabels.setTapAction(new SubSetting.SSAction() {
            public void action() {
                labelSize.setUsable(appLabels.asBoolean().getValue());
                labelSize.display(labelSize.getView());
            }
        });
        resetUsage = new SubSettingAction("Reset Usage", "", new SubSetting.SSAction() {
            public void action() {
                resetAppUsage();
                CarouselLauncher.showMessage("App usage has been reset");
            }
        });
        resetSettings = new SubSettingAction("Reset Settings", "", new SubSetting.SSAction() {
            public void action() {
                resetSettings();
                CarouselLauncher.showMessage("Settings have been reset");
            }
        });
        forceAppReload = new SubSettingAction("Reload Apps", "", new SubSetting.SSAction() {
            public void action() {
                AppHandler.forceReloadApps();
                CarouselLauncher.showMessage("Apps have been reloaded");
            }
        });
        defaultLauncher = new SubSettingAction("Default Launcher", "", new SubSetting.SSAction() {
            public void action() {
                requestDefaultLauncher();
            }
        });
    }

    public static String getSaveKey() {
        return "settings_save_key";
    }

    private static void rcs(SubSetting set) {
        settings.add(set);
    }

    private static class MiniImp extends ArrayAdapter<SubSetting> implements AdapterView.OnItemClickListener {
        public MiniImp() {
            super(CarouselLauncher.getLauncher(), R.layout.ss_layout, settings);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(position <= settings.size()) {
                SubSetting setting = settings.get(position);
                view = CarouselLauncher.getLauncher().getLayoutInflater().inflate(setting.getSettingViewID(), parent, false);
                setting.display(view);
            }
            return view;
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position <= settings.size()) {
                exitSettingsView();
                SubSetting setting = settings.get(position);
                if(setting.isUsable()) {
                    setting.load(SaveHandler.loadData(setting.getKey()));
                    setting.onTap(view);
                    if (setting.hasTapAction) {
                        setting.tapAction.action();
                    }
                } else {
                    CarouselLauncher.showMessage(setting.getUnusableMessage());
                }
            }
        }
    }
}