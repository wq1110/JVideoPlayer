package com.jw.media.jvideoplayer.lib.ass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class AssView extends View implements IAssView {
    Paint paint = new Paint();
    AssImage[] assImage;

    public AssView(Context context) {
        super(context);
        init();
    }

    public AssView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        setFocusableInTouchMode(false);
        setBackground(null);
//        Bitmap bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bm);
//        canvas.drawColor(0xffffb6c1);
//        canvas.toString();
        //paint.setShader(new BitmapShader(bm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        //      paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (assImage != null && assImage.length > 0) {
            for (AssImage image : assImage) {
                canvas.drawBitmap(image.bitmap, image.x, image.y, paint);
            }
        }
    }

    @Override
    public void render(AssImage[] assImage) {
        if (this.assImage != null && this.assImage.length > 0) {
            for (AssImage ai : this.assImage) {
                AssBitmapPool.recycle(ai.bitmap);
            }
        }

        this.assImage = assImage;
        invalidate();

    }

}
