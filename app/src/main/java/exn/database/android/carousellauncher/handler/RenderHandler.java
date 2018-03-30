package exn.database.android.carousellauncher.handler;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.util.ArrayList;

import exn.database.android.carousellauncher.app.App2D;
import exn.database.android.carousellauncher.app.AppContainer;
import exn.database.android.carousellauncher.app.AppStyleTemplate;
import exn.database.android.carousellauncher.main.CarouselLauncher;
import exn.database.android.carousellauncher.settings.SettingsManager;

public class RenderHandler implements SaveHandler.StateSaver {
    public static Canvas canvas;
    public static Paint paint = new Paint();
    public static float zoom = 1.0F;
    public static int centerX, centerY, scrollX, scrollY;
    public static int maxTransparentPixels = -1;

    public static void calcZoom() {
        if (zoom != 1.0F) {
            canvas.translate(-(zoom * centerX - centerX), -(zoom * centerY - centerY));
            canvas.scale(zoom, zoom);
        }
    }

    public static void renderBoundaries(int color) {
        paint.setColor(color);
        paint.setStrokeWidth(ViewHandler.appSize * 0.05f);
        paint.setStyle(Paint.Style.STROKE);
        ((AppStyleTemplate)SettingsManager.layoutStyle.asEnum().getObjectValue()).showBoundary(canvas);
    }

    public static void renderApps(ArrayList<App2D> apps, boolean appLabels, int labelColor, double labelSize) {
        for(App2D app : apps) {
            renderApp(app, appLabels, labelColor, labelSize);
        }
    }

    public static float getScrollX(float x) {
        return x - centerX - scrollX  * zoom;
    }

    public static float getScrollY(float y) {
        return y - centerY - scrollY  * zoom;
    }

    public static float editCircleSize;
    private static void renderApp(App2D app, boolean appLabels, int labelColor, double labelSize) {
        ((AppStyleTemplate)SettingsManager.layoutStyle.asEnum().getObjectValue()).edgeAction(app);

        int growSpeed = 15;
        if(app.hidden) {
            app.inflate(-growSpeed);
        }
        else {

            app.deflate(app.getInflation() >= 100 ? 5 : growSpeed);
        }
        if(app.getSize() > 0) {
            int loc = (int)Math.round(app.getSize() * 0.5D);
            int cx = centerX + scrollX;
            int cy = centerY + scrollY;
            app.icon.setBounds(cx + (app
                    .getX() - loc), cy + (app
                    .getY() - loc), cx + (app
                    .getX() + loc), cy + (app
                    .getY() + loc));
            app.icon.draw(canvas);

            if(InteractionHandler.editActive && app.deletable) {
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(labelColor);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize((int)(app.getSize() * labelSize * editCircleSize));
                int delX = cx + app.getX() + loc;
                int delY = cy + app.getY() - loc;
                canvas.drawCircle(delX, delY, loc * 0.35f * editCircleSize, paint);
                paint.setColor(Color.BLACK);
                canvas.drawText("x", delX, (int)(delY + paint.getTextSize() * 0.3), paint);
            }

            if(appLabels) {
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(labelColor);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize((int)(app.getRelativeSize() * labelSize));
                canvas.drawText(app.label, cx + app.getX(), (int)(cy + app.getY() + app.getSize() * 0.8D), paint);
            }
        }
    }

    public static BitmapDrawable makeIconCircular(AppContainer app) {
        BitmapDrawable icon = (BitmapDrawable)app.icon;
        if(maxTransparentPixels < 0) {
            PackageManager pm = CarouselLauncher.getLauncher().getPackageManager();
            Drawable launcherIcon;

            try {
                launcherIcon = pm.getApplicationInfo(CarouselLauncher.getLauncher().getPackageName(), PackageManager.GET_META_DATA).loadIcon(pm);
            } catch(PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            if(launcherIcon instanceof BitmapDrawable) {
                Bitmap launcherBitmap = ((BitmapDrawable)launcherIcon).getBitmap();
                int launcherWidth = launcherBitmap.getWidth();
                int launcherHeight = launcherBitmap.getHeight();
                int pixels[] = new int[launcherWidth * launcherHeight];
                int transparentPixels = 0;

                launcherBitmap.getPixels(pixels, 0, launcherWidth, 0, 0, launcherWidth, launcherHeight);
                for(int pixel : pixels) {
                    if(pixel == Color.TRANSPARENT) {
                        transparentPixels++;
                    }
                }

                maxTransparentPixels = (int)(transparentPixels * 8000.0 / 14364.0);
            }
        }

        Bitmap iconBitmap = icon.getBitmap();
        BitmapShader iconTexture = new BitmapShader(iconBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        int iconWidth = iconBitmap.getWidth();
        int iconHeight = iconBitmap.getHeight();
        float iconRadius = (iconWidth < iconHeight ? iconWidth : iconHeight) * 0.5f;

        Bitmap newBitmap = Bitmap.createBitmap(iconWidth, iconHeight, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        paint.setShader(iconTexture);
        cv.drawCircle(iconRadius, iconRadius, iconRadius, paint);

        int totalTransparentPixels = 0;
        int[] pixels = new int[iconWidth * iconHeight];
        newBitmap.getPixels(pixels, 0, iconWidth, 0, 0, iconWidth, iconHeight);
        for(int pixel : pixels) {
            if(pixel == Color.TRANSPARENT) {
                totalTransparentPixels++;
            }
        }

        if(totalTransparentPixels > maxTransparentPixels && !app.name.equalsIgnoreCase(CarouselLauncher.getLauncher().getPackageName())) {
            newBitmap = Bitmap.createBitmap(iconWidth, iconHeight, Bitmap.Config.ARGB_8888);
            cv = new Canvas(newBitmap);
            paint.setColor(Color.WHITE);
            paint.setShader(null);
            float scale = 0.7f;
            cv.drawCircle(iconRadius, iconRadius, iconRadius, paint);
            Matrix matrix = new Matrix();
            matrix.setScale(scale , scale);
            matrix.postTranslate(0.5f * iconWidth * (1.0f - scale), 0.5f * iconHeight * (1.0f - scale));
            cv.drawBitmap(iconBitmap, matrix, paint);
        }

        return new BitmapDrawable(CarouselLauncher.getLauncher().getResources(), newBitmap);
    }

    public static void zoomToOne(float speed) {
        if(zoom < 1) {
            zoom += speed;
            if(zoom > 1) {
                zoom = 1;
            }
        }
        else if(zoom > 1) {
            zoom -= speed;
            if(zoom < 1) {
                zoom = 1;
            }
        }
    }

    public static void moveToHome(int speed) {
        if (scrollX < 0) {
            scrollX += speed;
            if (scrollX > 0) {
                scrollX = 0;
            }
        }
        else if (scrollX > 0) {
            scrollX -= speed;
            if (scrollX < 0) {
                scrollX = 0;
            }
        }
        if (scrollY < 0) {
            scrollY += speed;
            if (scrollY > 0) {
                scrollY = 0;
            }
        }
        else if (scrollY > 0) {
            scrollY -= speed;
            if (scrollY < 0) {
                scrollY = 0;
            }
        }
    }

    public void saveState(Bundle bundle) {
        bundle.putFloat("Zoom", zoom);
        bundle.putInt("ScrollX", scrollX);
        bundle.putInt("ScrollY", scrollY);
    }

    public void reloadState(Bundle bundle) {
        zoom = bundle.getFloat("Zoom");
        scrollX = bundle.getInt("ScrollX");
        scrollY = bundle.getInt("ScrollY");
    }
}