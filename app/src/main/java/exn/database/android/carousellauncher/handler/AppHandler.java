package exn.database.android.carousellauncher.handler;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import exn.database.android.carousellauncher.app.App2D;
import exn.database.android.carousellauncher.app.AppContainer;
import exn.database.android.carousellauncher.app.AppStyleTemplate;
import exn.database.android.carousellauncher.app.EnumAppSorting;
import exn.database.android.carousellauncher.main.CarouselLauncher;
import exn.database.android.carousellauncher.settings.SettingsManager;

public class AppHandler implements SaveHandler.StateSaver {
    public static ArrayList<App2D> apps = new ArrayList<>();
    public static int loadedApps;
    public static boolean isLoaded, showcase = false;

    public static void launchApp(App2D app) {
        CarouselLauncher launcher = CarouselLauncher.getLauncher();
        if(InteractionHandler.editActive) {
            AnimationHandler.editPoke(app);
            if(app.deletable) {
                uninstallApp(app);
            }
        }
        else {
            if(app.launchApp(launcher, launcher.getPackageManager())) {
                if (app.name.equalsIgnoreCase(launcher.getPackageName())) {
                    InteractionHandler.onLauncherApp();
                } else {
                    ViewHandler.fullscreen(true);
                    RenderHandler.zoom = 1;
                    InteractionHandler.onHome = false;
                    InteractionHandler.toggleSearch(false, true);
                }
                calcAppLoc();
                if(SettingsManager.focusApps.asBoolean().getValue()) {
                    RenderHandler.scrollX = -app.getStaticX();
                    RenderHandler.scrollY = -app.getStaticY();
                }
            } else {
                removeApp(app);
            }
        }
    }

    public static App2D appToBeUninstalled;
    public static void uninstallApp(App2D app) {
        try {
            appToBeUninstalled = app;
            CarouselLauncher.getLauncher().startActivityForResult(getDeleteIntent(app).putExtra(Intent.EXTRA_RETURN_RESULT, true), 1);
        } catch(Exception e) {
            CarouselLauncher.showMessage("Unable to uninstall " + app.label);
            e.printStackTrace();
        }
    }

    public static boolean canAppBeDeleted(AppContainer app) {
        try {
            return (CarouselLauncher.getLauncher().getPackageManager().getApplicationInfo(app.name, 0).flags & ApplicationInfo.FLAG_SYSTEM) == 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Intent getDeleteIntent(AppContainer app) {
        return new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", app.name, null));
    }

    public static void removeApp(App2D app) {
        apps.remove(app);
        loadedApps = apps.size();
        calcAppLoc();
    }

    public static void loadApps(boolean sort) {
        isLoaded = false;
        halfLoad(CarouselLauncher.getLauncher().getPackageManager(), sort);
        isLoaded = true;
    }

    public static void forceReloadApps() {
        loadedApps = 0;
        loadApps(true);
    }

    private static void fullLoad(PackageManager pkg, boolean sort) {
        List<ApplicationInfo> appList = pkg.getInstalledApplications(0);
        if(appList.size() != loadedApps) {
            apps.clear();
            for(ApplicationInfo info : appList) {
                if(pkg.getLaunchIntentForPackage(info.packageName) != null) {
                    App2D app = new App2D(info.packageName, info.loadLabel(pkg).toString(), info.loadIcon(pkg));
                    if (!appListContains(app)) {
                        apps.add(app);
                    }
                }
            }
            loadedApps = appList.size();
            if(sort) {
                calcAppLoc();
            }
        }
    }

    private static void halfLoad(PackageManager pkg, boolean sort) {
        List<ResolveInfo> infoList = pkg.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
        if(infoList.size() != loadedApps || loadedApps < 1) {
            apps.clear();
            for(ResolveInfo info : infoList) {
                App2D app = new App2D(info.activityInfo.packageName, info.loadLabel(pkg).toString(), info.activityInfo.loadIcon(pkg));
                if(!appListContains(app) && useableForShowcase(app)) {
                    apps.add(app);
                }
            }
            loadedApps = infoList.size();
            if(sort) {
                calcAppLoc();
            }
        }
    }

    public static void calcAppLoc() {
        sortApps();
        PhysicsHandler.resetBounds();
        ((AppStyleTemplate)SettingsManager.layoutStyle.asEnum().getObjectValue()).findLocation(apps);
    }

    public static boolean appListContains(AppContainer app) {
        for (AppContainer check : apps) {
            if (app.equals(check)) {
                return true;
            }
        }
        return false;
    }

    private static void sortByName(List<App2D> apps) {
        Collections.sort(apps, new Comparator<App2D>() {
            public int compare(App2D a1, App2D a2) {
                return a1.label.compareToIgnoreCase(a2.label);
            }
        });
    }

    public static void sortApps() {
        App2D launcherApp;
        switch(((EnumAppSorting)SettingsManager.sortType.asEnum().getValue())) {
            case NAME:
                sortByName(apps);
                if(SettingsManager.launcherCentered.asBoolean().getValue()) {
                    for(App2D app : apps) {
                        if(app.name.equalsIgnoreCase(CarouselLauncher.getLauncher().getPackageName())) {
                            apps.remove(app);
                            apps.add(0, app);
                            break;
                        }
                    }
                }
                break;
            case TIMES_OPENED:
                Map<Long, List<App2D>> sortMap = new TreeMap<>(Collections.reverseOrder());
                launcherApp = null;
                for(App2D app : apps) {
                    if(SettingsManager.launcherCentered.asBoolean().getValue() && app.name.equalsIgnoreCase(CarouselLauncher.getLauncher().getPackageName())) {
                        launcherApp = app;
                    }
                    else {
                        if (sortMap.containsKey(app.timesOpened)) {
                            sortMap.get(app.timesOpened).add(app);
                        } else {
                            List<App2D> subSort = new ArrayList<>();
                            subSort.add(app);
                            sortMap.put(app.timesOpened, subSort);
                        }
                    }
                }
                apps.clear();
                if(launcherApp != null) {
                    apps.add(launcherApp);
                }
                for(Long subSortSlot : sortMap.keySet()) {
                    List<App2D> subSort = sortMap.get(subSortSlot);
                    sortByName(subSort);
                    for(App2D app : subSort) {
                        apps.add(app);
                    }
                    subSort.clear();
                }
                break;
            case USAGE:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && CarouselLauncher.hasPermission(AppOpsManager.OPSTR_GET_USAGE_STATS, true)) {
                    List<App2D> unusedApps = new ArrayList<>(apps);
                    sortByName(unusedApps);
                    long time = System.currentTimeMillis();
                    long start = time - 2000000 * 1000;
                    Map<Long, App2D> usageMap = new TreeMap<>(Collections.reverseOrder());
                    launcherApp = null;
                    for(UsageStats usageStats : ((UsageStatsManager)CarouselLauncher.getLauncher().getSystemService(Context.USAGE_STATS_SERVICE)).queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, start, time)) {
                        for (App2D app : apps) {
                            if (app.name.equalsIgnoreCase(usageStats.getPackageName())) {
                                if (SettingsManager.launcherCentered.asBoolean().getValue() && app.name.equalsIgnoreCase(CarouselLauncher.getLauncher().getPackageName())) {
                                    launcherApp = app;
                                    break;
                                } else {
                                    usageMap.put(usageStats.getTotalTimeInForeground(), app);
                                    break;
                                }
                            }
                        }
                    }

                    apps.clear();

                    if(launcherApp != null) {
                        apps.add(launcherApp);
                    }

                    for(Map.Entry<Long, App2D> app : usageMap.entrySet()) {
                        System.out.println(app.getValue().label);
                        if(!apps.contains(app.getValue())) {
                            apps.add(app.getValue());
                        }
                    }

                    for(App2D app : unusedApps) {
                        boolean found = false;
                        for (App2D checkApp : apps) {
                            if (app.name.equalsIgnoreCase(checkApp.name)) {
                                found = true;
                                break;
                            }
                        }
                        if(!found) {
                            apps.add(app);
                        }
                    }
                } else {
                    SettingsManager.sortType.reset();
                    sortApps();
                }
                break;
            default:
                sortByName(apps);
                break;
        }
    }

    private static boolean useableForShowcase(App2D app) {
        return !showcase || checkPackage(app, "com.android") || checkPackage(app, "com.google") || checkPackage(app, "com.samsung") || checkPackage(app, "com.sec.android") || checkPackage(app, CarouselLauncher.getLauncher().getPackageName());
    }

    private static boolean checkPackage(App2D app, String name) {
        return app.name.toLowerCase().contains(name.toLowerCase());
    }

    public void saveState(Bundle bundle) {
        bundle.putInt("LoadedApps", loadedApps);
        bundle.putParcelableArrayList("AppList", apps);
    }

    public void reloadState(Bundle bundle) {
        loadedApps = bundle.getInt("LoadedApps");
        apps = bundle.getParcelableArrayList("AppList");
        if(apps != null && apps.size() < 1) {
            AppHandler.forceReloadApps();
        } else {
            loadApps(true);
        }
    }
}
