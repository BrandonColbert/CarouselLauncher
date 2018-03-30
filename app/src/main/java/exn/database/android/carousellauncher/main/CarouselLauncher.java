package exn.database.android.carousellauncher.main;

import android.app.AppOpsManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import exn.database.android.carousellauncher.R;
import exn.database.android.carousellauncher.handler.AppHandler;
import exn.database.android.carousellauncher.handler.InteractionHandler;
import exn.database.android.carousellauncher.handler.RenderHandler;
import exn.database.android.carousellauncher.handler.SaveHandler;
import exn.database.android.carousellauncher.handler.ViewHandler;
import exn.database.android.carousellauncher.settings.SettingsManager;

public class CarouselLauncher extends AppCompatActivity {
    private static CarouselLauncher instance;
    public static CarouselView cHome;
    private static final int permission_code = 0x1;
    private static PermissionRequester permissionResult;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        boolean restoreStateExists = savedInstanceState != null;
        cHome = new CarouselView(this, restoreStateExists);
        cHome.setBackground(ActivityCompat.getDrawable(this, R.drawable.splash_screen));
        SaveHandler.save = getSharedPreferences(SaveHandler.getDatabase(), Context.MODE_PRIVATE);
        SaveHandler.registerStateSaver(new AppHandler());
        SaveHandler.registerStateSaver(new RenderHandler());
        SaveHandler.registerStateSaver(new InteractionHandler());
        ViewHandler.fullscreen(true);
        SettingsManager.init();
        setContentView(cHome);
        if(!restoreStateExists) {
            AppHandler.loadApps(true);
        }
        cHome.setBackground(WallpaperManager.getInstance(this).getDrawable());
    }

    public void showSettings(View view) {
        InteractionHandler.toggleSettings(true, true);
    }

    @Override
    public void onBackPressed() {
        InteractionHandler.onBackPressed();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasCategory(Intent.CATEGORY_HOME)) {
            InteractionHandler.onHomePressed();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SettingsManager.saveSettings();
        SaveHandler.handleState(outState, true);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SaveHandler.handleState(savedInstanceState, false);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK && AppHandler.appToBeUninstalled != null) {
                String appLabel = AppHandler.appToBeUninstalled.label;
                AppHandler.removeApp(AppHandler.appToBeUninstalled);
                AppHandler.appToBeUninstalled = null;
                showMessage("Uninstalled " + appLabel);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if(requestCode == permission_code && permissionResult != null) {
            permissionResult.permissionResult(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    public static void showMessage(String msg) {
        Toast.makeText(instance, msg, Toast.LENGTH_SHORT).show();
    }

    public static boolean hasPermission(String permission, boolean systemPermission) {
        if(systemPermission) {
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                AppOpsManager appOps = (AppOpsManager) getLauncher().getSystemService(Context.APP_OPS_SERVICE);
                int mode = appOps.checkOpNoThrow(permission, android.os.Process.myUid(), getLauncher().getPackageName());
                return mode == AppOpsManager.MODE_ALLOWED;
            }
            return true;
        }
        else {
            return ContextCompat.checkSelfPermission(CarouselLauncher.getLauncher(), permission) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestPermission(String permission, boolean systemPermission, PermissionRequester resultReceiver) {
        permissionResult = resultReceiver;
        if(systemPermission) {
            getLauncher().startActivity(new Intent(permission));
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getLauncher(), permission)) {
                ActivityCompat.requestPermissions(getLauncher(), new String[]{permission}, permission_code);
            }
        }
    }

    public static CarouselLauncher getLauncher() {
        return instance;
    }

    public interface PermissionRequester {
        void permissionResult(boolean granted);
    }
}