package es.guiguegon.sineview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by guiguegon on 12/05/2016.
 */
public class SineView extends View {

    protected static final float STEP_X = 1f;
    //Default vars
    protected static final float DEFAULT_AMPLITUDE = 40f;
    protected static final float DEFAULT_ALPHA = 1f;
    // We use this offset to avoid a black line at the bottom of the view
    protected static final float DEFAULT_PHASE = 1f;
    protected static final float DEFAULT_FREQUENCY = 1f;
    protected static final int DEFAULT_ANIMATION_TIME_IN_MILLIS = 2000;
    protected static final float DEFAULT_PERIODS = 1f;
    protected static final boolean DEFAULT_FILL = true;
    protected static final int DEFAULT_COLOR = Color.BLACK;
    private static final String TAG = SineView.class.getSimpleName();
    private final static int Y_OFFSET = 0;
    protected Path mPath;
    protected Paint mPaint = new Paint();
    protected RectF mRectF = new RectF(0, 0, 100, 100);

    protected float paramSineAlpha = DEFAULT_ALPHA;
    protected float paramSinePhase = DEFAULT_PHASE;
    protected float paramSineAmplitude = DEFAULT_AMPLITUDE;
    protected int paramSineAnimTime = DEFAULT_ANIMATION_TIME_IN_MILLIS;
    protected float paramSinePeriod = DEFAULT_PERIODS;
    protected boolean paramSineFill = DEFAULT_FILL;
    protected int paramSineColor = DEFAULT_COLOR;

    protected float amplitude = DEFAULT_AMPLITUDE;
    protected float frequency = DEFAULT_FREQUENCY;
    protected float phase = DEFAULT_PHASE;
    protected float scaleY;

    protected float measuredHeight;
    protected float measuredWidth;

    protected float step = 0f;
    protected ValueAnimator progressValueAnimator;

    private boolean isAnimating;
    private long currentPlayTime;

    public SineView(Context context) {
        super(context);
        initialize();
    }

    public SineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SineView, 0, 0);
        try {
            getCustomAttributes(a);
        } finally {
            a.recycle();
        }
        initialize();
    }

    public SineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SineView, 0, 0);
        try {
            getCustomAttributes(a);
        } finally {
            a.recycle();
        }
        initialize();
    }

    protected void getCustomAttributes(TypedArray typedArray) {
        paramSineAlpha = typedArray.getFloat(R.styleable.SineView_sv_sine_alpha, DEFAULT_ALPHA);
        paramSinePhase = typedArray.getFloat(R.styleable.SineView_sv_sine_phase, DEFAULT_PHASE);
        paramSineAmplitude = typedArray.getDimension(R.styleable.SineView_sv_sine_amplitude, DEFAULT_AMPLITUDE);
        paramSineAnimTime =
                typedArray.getInt(R.styleable.SineView_sv_sine_animation_time_millis, DEFAULT_ANIMATION_TIME_IN_MILLIS);
        paramSinePeriod = typedArray.getFloat(R.styleable.SineView_sv_sine_periods_to_show, DEFAULT_PERIODS);
        paramSineColor = typedArray.getColor(R.styleable.SineView_sv_sine_color, DEFAULT_COLOR);
        paramSineFill = typedArray.getBoolean(R.styleable.SineView_sv_sine_fill, DEFAULT_FILL);
    }

    protected void initialize() {
        mPaint.setAntiAlias(true);
        mPaint.setColor(paramSineColor);
        if (paramSineFill) {
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
        }
        mPaint.setAlpha((int) (255 * paramSineAlpha));
        setLayerType(LAYER_TYPE_HARDWARE, mPaint);
        prepareWave();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        measuredHeight = h;
        measuredWidth = w;
        mRectF.set(0, 0, w, h);
        calculateFrequency();
        calculateAmplitude();
        calculateScaleY();
        calculatePhase();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable state = super.onSaveInstanceState();
        SavedState savedState = new SavedState(state);
        savedState.isAnimating = isAnimating;
        savedState.currentPlayTime = currentPlayTime;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.isAnimating = savedState.isAnimating;
        this.currentPlayTime = savedState.currentPlayTime;
        if (isAnimating) {
            if (currentPlayTime != 0) {
                resumeWave();
            } else {
                startWave();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        generatePath(canvas);
    }

    protected void generatePath(Canvas canvas) {
        mPath = new Path();
        float x = 0;
        float y = generateSinePoint(x);
        mPath.moveTo(x, y);
        for (; x < mRectF.width(); x += STEP_X) {
            y = generateSinePoint(x);
            mPath.lineTo(x + mRectF.left, y + mRectF.top + scaleY);
        }
        mPath.lineTo(measuredWidth, measuredHeight + Y_OFFSET);
        mPath.lineTo(0, measuredHeight + Y_OFFSET);
        mPath.close();
        canvas.drawPath(mPath, mPaint);
    }

    protected float generateSinePoint(float x) {
        return (float) (amplitude * Math.sin(x * frequency - phase));
    }

    protected void calculateAmplitude() {
        amplitude = paramSineAmplitude;
    }

    protected void calculateFrequency() {
        frequency = (float) (2 * Math.PI * paramSinePeriod / measuredWidth);
    }

    protected void calculateScaleY() {
        scaleY = amplitude;
    }

    protected void calculatePhase() {
        phase = (float) (paramSinePhase * (Math.PI / 2) + step);
    }

    protected void setValueAnimator() {
        progressValueAnimator = ValueAnimator.ofFloat(0, (float) (2 * Math.PI));
        progressValueAnimator.setDuration(paramSineAnimTime);
        progressValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        progressValueAnimator.setRepeatMode(ValueAnimator.RESTART);
        progressValueAnimator.setInterpolator(new LinearInterpolator());
    }

    public void prepareWave() {
        setValueAnimator();
        calculateFrequency();
        calculateAmplitude();
        progressValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                step = (float) animation.getAnimatedValue();
                calculatePhase();
                invalidate();
            }
        });
        progressValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setLayerType(View.LAYER_TYPE_NONE, mPaint);
            }
        });
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void startWave() {
        progressValueAnimator.start();
        isAnimating = true;
    }

    public void pauseWave() {
        if (isAnimating) {
            currentPlayTime = progressValueAnimator.getCurrentPlayTime();
            progressValueAnimator.cancel();
            isAnimating = false;
        }
    }

    public void resumeWave() {
        progressValueAnimator.start();
        progressValueAnimator.setCurrentPlayTime(currentPlayTime);
        isAnimating = true;
    }

    public void stopWave() {
        try {
            if (progressValueAnimator != null) {
                progressValueAnimator.end();
                isAnimating = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "[stopWave]", e);
        }
    }

    static class SavedState extends BaseSavedState {
        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean isAnimating;
        long currentPlayTime;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.isAnimating = in.readByte() != 0x00;
            this.currentPlayTime = in.readLong();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (isAnimating ? 0x01 : 0x00));
            out.writeLong(currentPlayTime);
        }
    }
}