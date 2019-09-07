package org.denhac.kiosk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class Signature extends View {
    private static final float STROKE_WIDTH = 5f;
    private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

    enum Irrelevant { INSTANCE; }

    private Paint paint = new Paint();
    private Path path;
    private float lastTouchX;
    private float lastTouchY;
    private final RectF dirtyRect = new RectF();

    private PublishSubject<Irrelevant> pathChangedSubject;

    private String fieldName;

    public Signature(Context context) {
        super(context);

        init();
    }

    public Signature(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        pathChangedSubject = PublishSubject.create();

        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
        pathChangedSubject.onNext(Irrelevant.INSTANCE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY);
                lastTouchX = eventX;
                lastTouchY = eventY;
                return true;

            case MotionEvent.ACTION_MOVE:

            case MotionEvent.ACTION_UP:

                resetDirtyRect(eventX, eventY);
                int historySize = event.getHistorySize();
                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                    path.lineTo(historicalX, historicalY);
                }
                path.lineTo(eventX, eventY);
                break;

            default:
                return false;
        }

        invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

        lastTouchX = eventX;
        lastTouchY = eventY;

        return true;
    }

    private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX;
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX;
        }

        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY;
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY;
        }
    }

    private void resetDirtyRect(float eventX, float eventY) {
        dirtyRect.left = Math.min(lastTouchX, eventX);
        dirtyRect.right = Math.max(lastTouchX, eventX);
        dirtyRect.top = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }

    public Observable<Irrelevant> onPathChanged() {
        return pathChangedSubject.hide();
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
        invalidate();
    }

    public float getPathLength() {
        return new PathMeasure(path, false).getLength();
    }

    public boolean pathIsTooShort() {
        return getPathLength() < 300;
    }

    @SuppressLint("CheckResult")
    public Observable<String> getBase64() {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                if(pathIsTooShort()) {
                    return "";
                }

                Bitmap bitmap = Bitmap.createBitmap(
                        Signature.this.getWidth(),
                        Signature.this.getHeight(),
                        Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.TRANSPARENT);
                canvas.drawPath(path, paint);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                try {
                    baos.flush();
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] b = baos.toByteArray();

                return Base64.encodeToString(b, Base64.NO_WRAP);
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void clear() {
        path.reset();
        invalidate();
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
