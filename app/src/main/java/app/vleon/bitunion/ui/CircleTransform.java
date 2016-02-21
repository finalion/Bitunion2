package app.vleon.bitunion.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

public class CircleTransform extends BitmapTransformation {
    int mBorderWidth, mBorderColor;

    public CircleTransform(Context context) {
        super(context);
    }

    public CircleTransform(Context context, int borderWidth, int borderColor) {
        super(context);
        mBorderColor = borderColor;
        mBorderWidth = borderWidth;
    }

    private Bitmap circleCrop(BitmapPool pool, Bitmap source) {
        if (source == null) return null;

        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        // TODO this could be acquired from the pool too
        Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

        Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }

        int borderColor = Color.RED;
        float borderWidth = 2f;
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        Paint stroke = new Paint();
        paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        //draw ring
//        stroke.setAntiAlias(true);
//        stroke.setStyle(Paint.Style.STROKE);
//        stroke.setColor(mBorderColor);
//        stroke.setStrokeWidth(mBorderWidth);
//        canvas.drawCircle(r, r, r - stroke.getStrokeWidth() / 2 + 0.1f, stroke);
        return result;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return circleCrop(pool, toTransform);
    }

    @Override
    public String getId() {
        return getClass().getName();
    }
}