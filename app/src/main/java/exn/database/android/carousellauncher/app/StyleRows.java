package exn.database.android.carousellauncher.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import exn.database.android.carousellauncher.handler.PhysicsHandler;
import exn.database.android.carousellauncher.handler.RenderHandler;
import exn.database.android.carousellauncher.handler.ViewHandler;
import exn.database.android.carousellauncher.settings.SettingsManager;
import exn.database.android.carousellauncher.settings.SubSettingDualInteger;

public class StyleRows extends AppStyleTemplate {
    public void findLocation(List<App2D> apps) {
        int appSize = ViewHandler.screenScale;
        SubSettingDualInteger space = SettingsManager.spacing.asDualInteger();

        double ratio = 8.0 / 7.0;

        int listSize = apps.size();
        int totalApps = (int)Math.round(Math.sqrt(listSize));

        List<int[]> placementList = new ArrayList<>();

        double x = 1, y = 0.5;
        int width = (int)Math.round(totalApps * ratio);
        int height = (int)Math.round(totalApps * (1.0 / ratio));
        int distX = (int)(appSize * (1 + space.getValue(true) * 0.01));
        int distY = (int)(appSize * (1 + space.getValue(false) * 0.01));
        boolean heightEven = (height%2) == 0;
        boolean change = true;

        for(int i = 0; i < apps.size(); i++) {
            int uw = change ? width + 1 : width;
            int appX = (int)((x - (uw * 0.5)) * distX - distX * 0.5);
            int appY = (int)((y - (height * 0.5)) * distY - (heightEven ? 0 : (distY * 0.5)));
            placementList.add(new int[]{appX, appY});
            PhysicsHandler.checkBounds(appX, appY);

            if(x < uw) {
                x++;
            }
            else {
                y++;
                x = 1;
                change = !change;
            }
        }

        int start = (int)Math.round(listSize * 0.5) - 1;
        int place = 0;
        for(App2D app : apps) {
            int[] placement = placementList.get(start + place);
            app.setPosition(placement[0], placement[1]);
            if(place == 0) {
                place += 1;
            } else if(place > 0) {
                place *= -1;
            } else if(place < 0) {
                place = (place * -1) + 1;
            }
        }
    }

    public void showBoundary(Canvas canvas) {
        Paint paint = RenderHandler.paint;
        float x1 = RenderHandler.centerX + PhysicsHandler.leftBound - ViewHandler.appSize + RenderHandler.scrollX;
        float y1 = RenderHandler.centerY - PhysicsHandler.topBound - ViewHandler.appSize + RenderHandler.scrollY;
        float x2 = RenderHandler.centerX + PhysicsHandler.rightBound + ViewHandler.appSize + RenderHandler.scrollX;
        float y2 = RenderHandler.centerY - PhysicsHandler.bottomBound + ViewHandler.appSize + RenderHandler.scrollY;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float r = ViewHandler.appSize;
            canvas.drawRoundRect(x1, y1, x2, y2, r, r, paint);
        }
        else {
            canvas.drawRect(x1, y1, x2, y2, paint);
        }
    }
}
