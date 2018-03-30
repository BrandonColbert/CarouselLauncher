package exn.database.android.carousellauncher.handler;

import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class SaveHandler {
    public static List<StateSaver> stateSavers = new ArrayList<>();
    public static SharedPreferences save;

    public static void saveData(String key, String value)
    {
        save.edit().putString(getDatabase()+"."+key, value).apply();
    }

    public static void saveData(String key, Object value)
    {
        saveData(key, value.toString());
    }

    public static String loadData(String key) {
        String fillKey = getDatabase()+"."+key;
        return save.contains(fillKey) ? save.getString(fillKey, "") : "";
    }

    public static int loadDataAsInteger(String key) {
        return loadData(key).isEmpty() ? 0 : Integer.valueOf(loadData(key));
    }

    public static long loadDataAsLong(String key) {
        return loadData(key).isEmpty() ? 0 : Long.valueOf(loadData(key));
    }

    public static void registerStateSaver(StateSaver stateSaver) {
        stateSavers.add(stateSaver);
    }

    public static boolean hasKey(String key)
    {
        return save.contains(getDatabase()+"."+key);
    }

    public static void handleState(Bundle bundle, boolean save) {
        if(save) {
            for(StateSaver stateSaver : stateSavers) {
                stateSaver.saveState(bundle);
            }
        } else {
            for(StateSaver stateSaver : stateSavers) {
                stateSaver.reloadState(bundle);
            }
        }
    }

    public static String getDatabase() {
        return "exn.database.android.carousellauncher";
    }

    public interface StateSaver {
        void saveState(Bundle bundle);
        void reloadState(Bundle bundle);
    }
}
