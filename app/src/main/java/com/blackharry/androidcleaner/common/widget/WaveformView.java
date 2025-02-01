package com.blackharry.androidcleaner.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.blackharry.androidcleaner.R;

public class WaveformView extends View {
    private static final int MIN_BARS = 20;
    private static final int MAX_BARS = 100;
    private static final float BAR_WIDTH_DP = 2f;
    private static final float BAR_GAP_DP = 1f;
    private static final float MIN_BAR_HEIGHT_DP = 2f;

    private final Paint paint;
    private final RectF barRect;
    private float[] amplitudes;
    private int progress;
    private float density;
    private float barWidth;
    private float barGap;
    private float minBarHeight;
    private int barColor;
    private int progressColor;

    public WaveformView(Context context) {
        this(context, null);
    }

    public WaveformView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveformView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        density = context.getResources().getDisplayMetrics().density;
        barWidth = BAR_WIDTH_DP * density;
        barGap = BAR_GAP_DP * density;
        minBarHeight = MIN_BAR_HEIGHT_DP * density;
        
        barColor = ContextCompat.getColor(context, R.color.waveform_bar);
        progressColor = ContextCompat.getColor(context, R.color.waveform_progress);
        
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        
        barRect = new RectF();
        amplitudes = new float[0];
        progress = 0;
    }

    public void setAmplitudes(float[] amplitudes) {
        this.amplitudes = amplitudes;
        invalidate();
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (amplitudes.length == 0) {
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float centerY = height / 2;
        
        int barCount = Math.min(Math.max(MIN_BARS, (int) (width / (barWidth + barGap))), MAX_BARS);
        float totalBarWidth = barCount * (barWidth + barGap) - barGap;
        float startX = (width - totalBarWidth) / 2;
        
        for (int i = 0; i < barCount; i++) {
            float normalizedAmplitude = getNormalizedAmplitude(i, barCount);
            float barHeight = Math.max(normalizedAmplitude * height, minBarHeight);
            
            barRect.left = startX + i * (barWidth + barGap);
            barRect.right = barRect.left + barWidth;
            barRect.top = centerY - barHeight / 2;
            barRect.bottom = centerY + barHeight / 2;
            
            paint.setColor(i <= progress * barCount / 100 ? progressColor : barColor);
            canvas.drawRoundRect(barRect, barWidth / 2, barWidth / 2, paint);
        }
    }

    private float getNormalizedAmplitude(int index, int barCount) {
        if (amplitudes.length == 0) {
            return 0;
        }
        
        float samplesPerBar = (float) amplitudes.length / barCount;
        int startSample = (int) (index * samplesPerBar);
        int endSample = (int) ((index + 1) * samplesPerBar);
        
        float maxAmplitude = 0;
        for (int i = startSample; i < endSample && i < amplitudes.length; i++) {
            maxAmplitude = Math.max(maxAmplitude, amplitudes[i]);
        }
        
        return maxAmplitude;
    }
} 