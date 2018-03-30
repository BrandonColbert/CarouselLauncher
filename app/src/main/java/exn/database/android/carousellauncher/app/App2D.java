package exn.database.android.carousellauncher.app;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import exn.database.android.carousellauncher.handler.ViewHandler;

public class App2D extends AppUsage implements Parcelable {
    private boolean locked, unused;
    private double scale, inflate;
    private int x, y;
    private int moveX, moveY;

    public App2D(Parcel in) {
        super(in);
        scale = in.readDouble();
        x = in.readInt();
        y = in.readInt();
        setup();
    }

    public App2D(String name, String label, Drawable icon) {
        this(0, 0, name, label, icon);
    }

    public App2D(int x, int y, String name, String label, Drawable icon) {
        super(name, label, icon);
        setPosition(x, y);
        setup();
    }

    private void setup() {
        moveX = 0;
        moveY = 0;
        inflate = 1;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(scale);
        dest.writeInt(x);
        dest.writeInt(y);
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void setPosition(int x, int y) {
        if(!locked) {
            this.x = x;
            this.y = y;
        }
    }

    public void translate(int x, int y) {
        moveX += x;
        moveY += y;
    }

    public void inflate(int amount) {
        double inflation = inflate + amount * 0.01;
        int max = 2;
        int min = 0;
        if(min <= inflation) {
            inflate = (inflation <= max) ? inflation : max;
        }
    }

    public int getInflation() {
        return (int)(inflate * 100);
    }

    public int getX() {
        return x + moveX;
    }

    public int getY() {
        return y + moveY;
    }

    public int getStaticX() {
        return x;
    }

    public int getStaticY() {
        return y;
    }

    public int getSize() {
        return (int)(ViewHandler.appSize * scale * inflate);
    }

    public int getRelativeSize() {
        return (int)(ViewHandler.screenScale * scale * inflate);
    }

    public void recenter(int speed) {
        int margin = 0;
        if(moveX > margin) {
            moveX -= speed;
            if(moveX < margin) {
                moveX = margin;
            }
        }
        else if(moveX < margin) {
            moveX += speed;
            if(moveX > margin) {
                moveX = margin;
            }
        }

        if(moveY > margin) {
            moveY -= speed;
            if(moveY < margin) {
                moveY = margin;
            }
        }
        else if(moveY < margin) {
            moveY += speed;
            if(moveY > margin) {
                moveY = margin;
            }
        }
    }

    public void deflate(int speed) {
        int margin = 1;
        double useSpeed = speed * 0.01;
        if(inflate > margin) {
            inflate -= useSpeed;
            if(inflate < margin) {
                inflate = margin;
            }
        }
        else if(inflate < margin) {
            inflate += useSpeed;
            if(inflate > margin) {
                inflate = margin;
            }
        }
    }

    public void deflate() {
        inflate = 1;
    }

    public void recenter() {
        moveX = 0;
        moveY = 0;
    }

    public static final Parcelable.Creator<App2D> CREATOR = new Parcelable.Creator<App2D>() {
        public App2D createFromParcel(Parcel in) {
            return new App2D(in);
        }
        public App2D[] newArray(int size) {
            return new App2D[size];
        }
    };

    public void lockPos(int x, int y) {
        locked = true;
        this.x = x;
        this.y = y;
    }
}
