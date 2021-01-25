package site.doramusic.app.widget;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Checkable;

import dora.util.DensityUtils;
import dora.util.ViewUtils;

public class DoraToggleButton extends View implements Checkable {

    /**
     * 阴影半径
     */
    private int shadowRadius;
    /**
     * 阴影Y偏移px
     */
    private int shadowOffset;
    /**
     * 阴影颜色
     */
    private int shadowColor;

    /**
     * 背景半径
     */
    private float viewRadius;
    /**
     * 按钮半径
     */
    private float buttonRadius;

    /**
     * 背景高
     */
    private float height;
    /**
     * 背景宽
     */
    private float width;
    /**
     * 背景位置
     */
    private float left;
    private float top;
    private float right;
    private float bottom;
    private float centerX;
    private float centerY;

    /**
     * 背景底色
     */
    private int background;
    /**
     * 背景关闭颜色
     */
    private int uncheckColor;
    /**
     * 背景打开颜色
     */
    private int checkedColor;
    /**
     * 边框宽度px
     */
    private int borderWidth;

    /**
     * 打开指示线颜色
     */
    private int checkLineColor;
    /**
     * 打开指示线宽
     */
    private int checkLineWidth;
    /**
     * 打开指示线长
     */
    private float checkLineLength;
    /**
     * 关闭圆圈颜色
     */
    private int uncheckCircleColor;
    /**
     * 关闭圆圈线宽
     */
    private int uncheckCircleWidth;
    /**
     * 关闭圆圈位移X
     */
    private float uncheckCircleOffsetX;
    /**
     * 关闭圆圈半径
     */
    private float uncheckCircleRadius;
    /**
     * 打开指示线位移X
     */
    private float checkedLineOffsetX;
    /**
     * 打开指示线位移Y
     */
    private float checkedLineOffsetY;


    /**
     * 按钮最左边
     */
    private float buttonMinX;
    /**
     * 按钮最右边
     */
    private float buttonMaxX;

    /**
     * 按钮画笔
     */
    private Paint mButtonPaint;
    /**
     * 背景画笔
     */
    private Paint mBgPaint;

    /**
     * 手势按下的时刻
     */
    private long touchDownTime;

    /**
     * 当前状态
     */
    private ViewState viewState;
    private ViewState beforeState;
    private ViewState afterState;
    /**
     * 动画状态
     */
    private int animateState = ANIMATE_STATE_NONE;

    /**
     * 是否选中
     */
    private boolean isChecked;
    /**
     * 是否启用动画
     */
    private boolean enableEffect;
    /**
     * 是否启用阴影效果
     */
    private boolean shadowEffect;
    /**
     * 是否显示指示器
     */
    private boolean showIndicator;
    /**
     * 收拾是否按下
     */
    private boolean isTouchingDown = false;
    /**
     *
     */
    private boolean isFirstLoaded = false;
    /**
     *
     */
    private boolean isCheckedChanging = false;

    private static final int DEFAULT_WIDTH = DensityUtils.dp2px(58);
    private static final int DEFAULT_HEIGHT = DensityUtils.dp2px(36);

    /**
     * 动画状态：
     * 1.静止
     * 2.进入拖动
     * 3.处于拖动
     * 4.拖动-复位
     * 5.拖动-切换
     * 6.点击切换
     */
    private static final int ANIMATE_STATE_NONE = 0;
    private static final int ANIMATE_STATE_PENDING_DRAG = 1;
    private static final int ANIMATE_STATE_DRAGGING = 2;
    private static final int ANIMATE_STATE_PENDING_RESET = 3;
    private static final int ANIMATE_STATE_PENDING_SETTLE = 4;
    private static final int ANIMATE_STATE_SWITCH = 5;
    private ValueAnimator mValueAnimator;
    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private int buttonColor;
    private int effectDuration;

    public DoraToggleButton(Context context) {
        this(context, null);
    }

    public DoraToggleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoraToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    public final void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(0, 0, 0, 0);
    }

    /**
     * 初始化参数
     */
    private void init(Context context, AttributeSet attrs) {
        initAttrs(context, attrs);
        initPaints();
        if (shadowEffect) {
            mButtonPaint.setShadowLayer(
                    shadowRadius,
                    0, shadowOffset,
                    shadowColor);
        }
        viewState = new ViewState();
        beforeState = new ViewState();
        afterState = new ViewState();
        mValueAnimator = ViewUtils.createValueAnimator(effectDuration, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                switch (animateState) {
                    case ANIMATE_STATE_PENDING_SETTLE: {
                    }
                    case ANIMATE_STATE_PENDING_RESET: {
                    }
                    case ANIMATE_STATE_PENDING_DRAG: {
                        viewState.checkedLineColor = (int) argbEvaluator.evaluate(
                                value,
                                beforeState.checkedLineColor,
                                afterState.checkedLineColor
                        );
                        viewState.radius = beforeState.radius
                                + (afterState.radius - beforeState.radius) * value;

                        if (animateState != ANIMATE_STATE_PENDING_DRAG) {
                            viewState.buttonX = beforeState.buttonX
                                    + (afterState.buttonX - beforeState.buttonX) * value;
                        }
                        viewState.checkStateColor = (int) argbEvaluator.evaluate(
                                value,
                                beforeState.checkStateColor,
                                afterState.checkStateColor
                        );
                        break;
                    }
                    case ANIMATE_STATE_SWITCH: {
                        viewState.buttonX = beforeState.buttonX
                                + (afterState.buttonX - beforeState.buttonX) * value;

                        float fraction = (viewState.buttonX - buttonMinX) / (buttonMaxX - buttonMinX);

                        viewState.checkStateColor = (int) argbEvaluator.evaluate(
                                fraction,
                                uncheckColor,
                                checkedColor
                        );
                        viewState.radius = fraction * viewRadius;
                        viewState.checkedLineColor = (int) argbEvaluator.evaluate(
                                fraction,
                                Color.TRANSPARENT,
                                checkLineColor
                        );
                        break;
                    }
                    default:
                    case ANIMATE_STATE_DRAGGING: {
                    }
                    case ANIMATE_STATE_NONE: {
                        break;
                    }
                }
                postInvalidate();
            }
        }, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                switch (animateState) {
//                case ANIMATE_STATE_DRAGGING: {
//                    break;
//                }
                    case ANIMATE_STATE_PENDING_DRAG: {
                        animateState = ANIMATE_STATE_DRAGGING;
                        viewState.checkedLineColor = Color.TRANSPARENT;
                        viewState.radius = viewRadius;

                        postInvalidate();
                        break;
                    }
                    case ANIMATE_STATE_PENDING_RESET: {
                        animateState = ANIMATE_STATE_NONE;
                        postInvalidate();
                        break;
                    }
                    case ANIMATE_STATE_PENDING_SETTLE: {
                        animateState = ANIMATE_STATE_NONE;
                        postInvalidate();
                        notifyCheckedChanged();
                        break;
                    }
                    case ANIMATE_STATE_SWITCH: {
                        isChecked = !isChecked;
                        animateState = ANIMATE_STATE_NONE;
                        postInvalidate();
                        notifyCheckedChanged();
                        break;
                    }
                    default:
                    case ANIMATE_STATE_NONE: {
                        break;
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        }, null, 0, 1);
        this.setClickable(true);
        this.setPadding(0, 0, 0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(ViewUtils.applyWrapContentSize(widthMeasureSpec, DEFAULT_WIDTH),
                ViewUtils.applyWrapContentSize(heightMeasureSpec, DEFAULT_HEIGHT));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float viewPadding = Math.max(shadowRadius + shadowOffset, borderWidth);
        height = h - viewPadding - viewPadding;
        width = w - viewPadding - viewPadding;
        viewRadius = height * .5f;
        buttonRadius = viewRadius - borderWidth;
        left = viewPadding;
        top = viewPadding;
        right = w - viewPadding;
        bottom = h - viewPadding;
        centerX = (left + right) * .5f;
        centerY = (top + bottom) * .5f;
        buttonMinX = left + viewRadius;
        buttonMaxX = right - viewRadius;
        if (isChecked()) {
            setCheckedViewState(viewState);
        } else {
            setUncheckViewState(viewState);
        }
        isFirstLoaded = true;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mBgPaint.setStrokeWidth(borderWidth);
        mBgPaint.setStyle(Paint.Style.FILL);
        //绘制白色背景
        mBgPaint.setColor(background);
        ViewUtils.drawRoundRect(canvas,
                left, top, right, bottom,
                viewRadius, mBgPaint);
        //绘制关闭状态的边框
        mBgPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setColor(uncheckColor);
        ViewUtils.drawRoundRect(canvas,
                left, top, right, bottom,
                viewRadius, mBgPaint);
        //绘制小圆圈
        if (showIndicator) {
            drawUncheckIndicator(canvas);
        }
        //绘制开启背景色
        float des = viewState.radius * .5f;//[0-backgroundRadius*0.5f]
        mBgPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setColor(viewState.checkStateColor);
        mBgPaint.setStrokeWidth(borderWidth + des * 2f);
        ViewUtils.drawRoundRect(canvas,
                left + des, top + des, right - des, bottom - des,
                viewRadius, mBgPaint);
        //绘制按钮左边绿色长条遮挡
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setStrokeWidth(1);
        ViewUtils.drawArc(canvas,
                left, top,
                left + 2 * viewRadius, top + 2 * viewRadius,
                90, 180, mBgPaint);
        canvas.drawRect(
                left + viewRadius, top,
                viewState.buttonX, top + 2 * viewRadius,
                mBgPaint);
        //绘制小线条
        if (showIndicator) {
            drawCheckedIndicator(canvas);
        }
        //绘制按钮
        drawButton(canvas, viewState.buttonX, centerY);
    }
    /**
     * 绘制选中状态指示器
     */
    protected void drawCheckedIndicator(Canvas canvas) {
        drawCheckedIndicator(canvas,
                viewState.checkedLineColor,
                checkLineWidth,
                left + viewRadius - checkedLineOffsetX, centerY - checkLineLength,
                left + viewRadius - checkedLineOffsetY, centerY + checkLineLength,
                mBgPaint);
    }

    /**
     * 绘制选中状态指示器
     */
    protected void drawCheckedIndicator(Canvas canvas,
                                        int color,
                                        float lineWidth,
                                        float sx, float sy, float ex, float ey,
                                        Paint paint) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(lineWidth);
        canvas.drawLine(
                sx, sy, ex, ey,
                paint);
    }

    /**
     * 绘制关闭状态指示器
     *
     * @param canvas
     */
    private void drawUncheckIndicator(Canvas canvas) {
        drawUncheckIndicator(canvas,
                uncheckCircleColor,
                uncheckCircleWidth,
                right - uncheckCircleOffsetX, centerY,
                uncheckCircleRadius,
                mBgPaint);
    }

    /**
     * 绘制关闭状态指示器
     *
     * @param canvas
     * @param color
     * @param lineWidth
     * @param centerX
     * @param centerY
     * @param radius
     * @param paint
     */
    protected void drawUncheckIndicator(Canvas canvas,
                                        int color,
                                        float lineWidth,
                                        float centerX, float centerY,
                                        float radius,
                                        Paint paint) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(lineWidth);
        canvas.drawCircle(centerX, centerY, radius, paint);
    }

    private void drawButton(Canvas canvas, float x, float y) {
        canvas.drawCircle(x, y, buttonRadius, mButtonPaint);
        mBgPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setStrokeWidth(1);
        mBgPaint.setColor(0XffDDDDDD);
        canvas.drawCircle(x, y, buttonRadius, mBgPaint);
    }

    @Override
    public void setChecked(boolean checked) {
        if (checked == isChecked()) {
            postInvalidate();
            return;
        }
        toggle(enableEffect, false);
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        toggle(true);
    }

    public void toggle(boolean animate) {
        toggle(animate, true);
    }

    private void toggle(boolean animate, boolean broadcast) {
        if (!isEnabled()) {
            return;
        }
        if (isCheckedChanging) {
            throw new RuntimeException("should NOT switch the state in method: [onCheckedChanged]!");
        }
        if (!isFirstLoaded) {
            isChecked = !isChecked;
            if (broadcast) {
                notifyCheckedChanged();
            }
            return;
        }
        if (mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
        if (!enableEffect || !animate) {
            isChecked = !isChecked;
            if (isChecked()) {
                setCheckedViewState(viewState);
            } else {
                setUncheckViewState(viewState);
            }
            postInvalidate();
            if (broadcast) {
                notifyCheckedChanged();
            }
            return;
        }
        animateState = ANIMATE_STATE_SWITCH;
        beforeState.copy(viewState);
        if (isChecked()) {
            //切换到unchecked
            setUncheckViewState(afterState);
        } else {
            setCheckedViewState(afterState);
        }
        mValueAnimator.start();
    }

    private void notifyCheckedChanged() {
        if (mOnCheckedChangeListener != null) {
            isCheckedChanging = true;
            mOnCheckedChangeListener.onCheckedChanged(this, isChecked());
        }
        isCheckedChanging = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        int actionMasked = event.getActionMasked();

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                isTouchingDown = true;
                touchDownTime = System.currentTimeMillis();
                //取消准备进入拖动状态
                removeCallbacks(postPendingDrag);
                //预设100ms进入拖动状态
                postDelayed(postPendingDrag, 100);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float eventX = event.getX();
                if (isPendingDragState()) {
                    //在准备进入拖动状态过程中，可以拖动按钮位置
                    float fraction = eventX / getWidth();
                    fraction = Math.max(0f, Math.min(1f, fraction));

                    viewState.buttonX = buttonMinX
                            + (buttonMaxX - buttonMinX)
                            * fraction;

                } else if (isDragState()) {
                    //拖动按钮位置，同时改变对应的背景颜色
                    float fraction = eventX / getWidth();
                    fraction = Math.max(0f, Math.min(1f, fraction));

                    viewState.buttonX = buttonMinX
                            + (buttonMaxX - buttonMinX)
                            * fraction;

                    viewState.checkStateColor = (int) argbEvaluator.evaluate(
                            fraction,
                            uncheckColor,
                            checkedColor
                    );
                    postInvalidate();

                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                isTouchingDown = false;
                //取消准备进入拖动状态
                removeCallbacks(postPendingDrag);
                if (System.currentTimeMillis() - touchDownTime <= 300) {
                    //点击时间小于300ms，认为是点击操作
                    toggle();
                } else if (isDragState()) {
                    //在拖动状态，计算按钮位置，设置是否切换状态
                    float eventX = event.getX();
                    float fraction = eventX / getWidth();
                    fraction = Math.max(0f, Math.min(1f, fraction));
                    boolean newCheck = fraction > .5f;
                    if (newCheck == isChecked()) {
                        pendingCancelDragState();
                    } else {
                        isChecked = newCheck;
                        pendingSettleState();
                    }
                } else if (isPendingDragState()) {
                    //在准备进入拖动状态过程中，取消之，复位
                    pendingCancelDragState();
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                isTouchingDown = false;
                removeCallbacks(postPendingDrag);
                if (isPendingDragState()
                        || isDragState()) {
                    //复位
                    pendingCancelDragState();
                }
                break;
            }
        }
        return true;
    }

    /**
     * 是否在动画状态
     *
     * @return
     */
    private boolean isInAnimating() {
        return animateState != ANIMATE_STATE_NONE;
    }

    /**
     * 是否在进入拖动或离开拖动状态
     *
     * @return
     */
    private boolean isPendingDragState() {
        return animateState == ANIMATE_STATE_PENDING_DRAG
                || animateState == ANIMATE_STATE_PENDING_RESET;
    }

    /**
     * 是否在手指拖动状态
     *
     * @return
     */
    private boolean isDragState() {
        return animateState == ANIMATE_STATE_DRAGGING;
    }

    /**
     * 设置是否启用阴影效果
     *
     * @param shadowEffect true.启用
     */
    public void setShadowEffect(boolean shadowEffect) {
        if (this.shadowEffect == shadowEffect) {
            return;
        }
        this.shadowEffect = shadowEffect;
        if (this.shadowEffect) {
            mButtonPaint.setShadowLayer(
                    shadowRadius,
                    0, shadowOffset,
                    shadowColor);
        } else {
            mButtonPaint.setShadowLayer(
                    0,
                    0, 0,
                    0);
        }
    }

    public void setEnableEffect(boolean enable) {
        this.enableEffect = enable;
    }

    /**
     * 开始进入拖动状态
     */
    private void pendingDragState() {
        if (isInAnimating()) {
            return;
        }
        if (!isTouchingDown) {
            return;
        }
        if (mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
        animateState = ANIMATE_STATE_PENDING_DRAG;
        beforeState.copy(viewState);
        afterState.copy(viewState);
        if (isChecked()) {
            afterState.checkStateColor = checkedColor;
            afterState.buttonX = buttonMaxX;
            afterState.checkedLineColor = checkedColor;
        } else {
            afterState.checkStateColor = uncheckColor;
            afterState.buttonX = buttonMinX;
            afterState.radius = viewRadius;
        }
        mValueAnimator.start();
    }

    /**
     * 取消拖动状态
     */
    private void pendingCancelDragState() {
        if (isDragState() || isPendingDragState()) {
            if (mValueAnimator.isRunning()) {
                mValueAnimator.cancel();
            }
            animateState = ANIMATE_STATE_PENDING_RESET;
            beforeState.copy(viewState);
            if (isChecked()) {
                setCheckedViewState(afterState);
            } else {
                setUncheckViewState(afterState);
            }
            mValueAnimator.start();
        }
    }

    /**
     * 动画-设置新的状态
     */
    private void pendingSettleState() {
        if (mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
        animateState = ANIMATE_STATE_PENDING_SETTLE;
        beforeState.copy(viewState);
        if (isChecked()) {
            setCheckedViewState(afterState);
        } else {
            setUncheckViewState(afterState);
        }
        mValueAnimator.start();
    }

    @Override
    public final void setOnClickListener(OnClickListener l) {
    }

    @Override
    public final void setOnLongClickListener(OnLongClickListener l) {
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
        this.mOnCheckedChangeListener = l;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(DoraToggleButton view, boolean isChecked);
    }

    private OnCheckedChangeListener mOnCheckedChangeListener;

    private Runnable postPendingDrag = new Runnable() {
        @Override
        public void run() {
            if (!isInAnimating()) {
                pendingDragState();
            }
        }
    };

    private static class ViewState {
        /**
         * 按钮x位置[buttonMinX-buttonMaxX]
         */
        float buttonX;
        /**
         * 状态背景颜色
         */
        int checkStateColor;
        /**
         * 选中线的颜色
         */
        int checkedLineColor;
        /**
         * 状态背景的半径
         */
        float radius;

        ViewState() {
        }

        private void copy(ViewState source) {
            this.buttonX = source.buttonX;
            this.checkStateColor = source.checkStateColor;
            this.checkedLineColor = source.checkedLineColor;
            this.radius = source.radius;
        }
    }

    private void setUncheckViewState(ViewState viewState) {
        viewState.radius = 0;
        viewState.checkStateColor = uncheckColor;
        viewState.checkedLineColor = Color.TRANSPARENT;
        viewState.buttonX = buttonMinX;
    }

    private void setCheckedViewState(ViewState viewState) {
        viewState.radius = viewRadius;
        viewState.checkStateColor = checkedColor;
        viewState.checkedLineColor = checkLineColor;
        viewState.buttonX = buttonMaxX;
    }

    private void initPaints() {
        mBgPaint = ViewUtils.getPaint(background);
        mButtonPaint = ViewUtils.getPaint(buttonColor);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DoraToggleButton);
        shadowEffect = a.getBoolean(R.styleable.DoraToggleButton_dora_shadow_effect,
                true);
        uncheckCircleColor = a.getColor(R.styleable.DoraToggleButton_dora_uncheckcircle_color,
                0XffAAAAAA);
        uncheckCircleWidth = a.getDimensionPixelSize(R.styleable.DoraToggleButton_dora_uncheckcircle_width,
                DensityUtils.dp2px(1.5f));
        uncheckCircleOffsetX = DensityUtils.dp2px(10);

        uncheckCircleRadius = a.getDimensionPixelSize(
                R.styleable.DoraToggleButton_dora_uncheckcircle_radius,
                DensityUtils.dp2px(4));
        checkedLineOffsetX = DensityUtils.dp2px(4);
        checkedLineOffsetY = DensityUtils.dp2px(4);
        shadowRadius = a.getDimensionPixelSize(
                R.styleable.DoraToggleButton_dora_shadow_radius,
                DensityUtils.dp2px(2.5f));
        shadowOffset = a.getDimensionPixelSize(
                R.styleable.DoraToggleButton_dora_shadow_offset,
                DensityUtils.dp2px(1.5f));
        shadowColor = a.getColor(
                R.styleable.DoraToggleButton_dora_shadow_color,
                0X33000000);
        uncheckColor = a.getColor(
                R.styleable.DoraToggleButton_dora_uncheck_color,
                0XffDDDDDD);
        checkedColor = a.getColor(
                R.styleable.DoraToggleButton_dora_checked_color,
                0Xff51d367);
        borderWidth = a.getDimensionPixelSize(
                R.styleable.DoraToggleButton_dora_border_width,
                DensityUtils.dp2px(1));
        checkLineColor = a.getColor(
                R.styleable.DoraToggleButton_dora_checkline_color,
                Color.WHITE);
        checkLineWidth = a.getDimensionPixelSize(
                R.styleable.DoraToggleButton_dora_checkline_width,
                DensityUtils.dp2px(1f));
        checkLineLength = DensityUtils.dp2px(6);
        buttonColor = a.getColor(
                R.styleable.DoraToggleButton_dora_button_color,
                Color.WHITE);
        effectDuration = a.getInt(
                R.styleable.DoraToggleButton_dora_effect_duration,
                300);
        isChecked = a.getBoolean(
                R.styleable.DoraToggleButton_dora_checked,
                false);
        showIndicator = a.getBoolean(
                R.styleable.DoraToggleButton_dora_show_indicator,
                true);
        background = a.getColor(
                R.styleable.DoraToggleButton_dora_background,
                Color.WHITE);
        enableEffect = a.getBoolean(
                R.styleable.DoraToggleButton_dora_enable_effect,
                true);
        a.recycle();
    }
}
