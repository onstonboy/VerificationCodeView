package com.ccc.vcv

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Handler
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat

class VerificationCodeView : EditText {

    private var mSpaceItem: Float = DimensionUtils.getDimensionWithDensity(context, R.dimen.dp_20)
    private var mInputWidth: Float = DimensionUtils.getDimensionWithDensity(context, R.dimen.dp_30)
    private var mInputHeight: Float = DimensionUtils.getDimensionWithDensity(context, R.dimen.dp_5)
    private var mLineColor: Int = ContextCompat.getColor(context, android.R.color.black)
    private var mTextColor: Int = ContextCompat.getColor(context, android.R.color.black)
    private var mCursorColor: Int = ContextCompat.getColor(context, android.R.color.black)
    private var mTextSize: Float =
        DimensionUtils.getDimensionWithScaledDensity(context, R.dimen.sp_18)
    private var mTextStyle: Int = Typeface.BOLD
    private var mRadius: Float = DimensionUtils.getDimensionWithDensity(context, R.dimen.dp_5)
    private var mInputCount: Int = 6
    private var mLinePaint = Paint()
    private var mTextPaint = Paint()
    private var mCursorPaint = Paint()
    private var mTextBound = Rect()
    private var mInputViewOffsets = ArrayList<InputOffset>()
    private var mTexts = ArrayList<Char>()
    private var mOldTextLength = 0
    private var mIsDrawCursor = true
    private var mDrawingCursor = false
    private var mHandler = Handler()
    private var mRunnable: Runnable? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val typeArray =
            context.obtainStyledAttributes(attrs, R.styleable.VerificationCodeView, 0, 0)
        try {
            mSpaceItem = typeArray.getDimension(
                R.styleable.VerificationCodeView_spacingItem,
                DimensionUtils.getDimensionWithDensity(context, R.dimen.dp_20)
            )
            mInputWidth = typeArray.getDimension(
                R.styleable.VerificationCodeView_inputWidth,
                DimensionUtils.getDimensionWithDensity(context, R.dimen.dp_30)
            )
            mInputHeight = typeArray.getDimension(
                R.styleable.VerificationCodeView_inputHeight,
                DimensionUtils.getDimensionWithDensity(context, R.dimen.dp_5)
            )
            mRadius = typeArray.getDimension(
                R.styleable.VerificationCodeView_radius,
                DimensionUtils.getDimensionWithDensity(context, R.dimen.dp_5)
            )
            mInputCount = typeArray.getInt(
                R.styleable.VerificationCodeView_inputCount, 6
            )
            mTextSize = typeArray.getDimension(
                R.styleable.VerificationCodeView_textSize,
                DimensionUtils.getDimensionWithScaledDensity(context, R.dimen.sp_18)
            )
            mTextStyle = typeArray.getInt(
                R.styleable.VerificationCodeView_textStyle, Typeface.BOLD
            )
            mLineColor = typeArray.getColor(
                R.styleable.VerificationCodeView_lineColor,
                ContextCompat.getColor(context, android.R.color.black)
            )
            mTextColor = typeArray.getColor(
                R.styleable.VerificationCodeView_textColor,
                ContextCompat.getColor(context, android.R.color.black)
            )
            mCursorColor = typeArray.getColor(
                R.styleable.VerificationCodeView_cursorColor,
                ContextCompat.getColor(context, android.R.color.black)
            )
        } finally {
            typeArray.recycle()
        }
        initViews()
        runDrawCursor()
    }

    override fun onDetachedFromWindow() {
        mHandler.removeCallbacks(mRunnable)
        mDrawingCursor = false
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mInputViewOffsets.isEmpty()) return
        if (isFocused) {
            if (!mDrawingCursor) runDrawCursor()
        } else {
            mDrawingCursor = false
            mHandler.removeCallbacks(mRunnable)
        }
        var cursorLeftOffset = 0f
        for ((index, text) in mTexts.withIndex()) {
            mTextPaint.getTextBounds(text.toString(), 0, 1, mTextBound)
            val centerPosition =
                (mInputViewOffsets[index].left + mInputViewOffsets[index].right) / 2
            val nextCenterPosition = if (index < mInputViewOffsets.lastIndex) {
                (mInputViewOffsets[index + 1].left + mInputViewOffsets[index + 1].right) / 2
            } else {
                centerPosition
            }
            cursorLeftOffset = if (index >= mInputViewOffsets.lastIndex) {
                nextCenterPosition + mTextBound.width() / 2
            } else {
                nextCenterPosition - CURSOR_WIDTH
            }
            val textLeftOffset = if (text.toString() == "1") {
                centerPosition - mTextBound.width()
            } else {
                centerPosition - (mTextBound.width() / 2)
            }
            canvas.drawText(
                text.toString(),
                textLeftOffset,
                height - mInputHeight - (mTextBound.height() / 2),
                mTextPaint
            )
        }

        if (mIsDrawCursor && mInputViewOffsets.isNotEmpty()) {
            if (cursorLeftOffset == 0f) {
                val centerPosition =
                    (mInputViewOffsets[0].left + mInputViewOffsets[0].right) / 2
                cursorLeftOffset = centerPosition - CURSOR_WIDTH
            }
            canvas.drawRect(
                cursorLeftOffset + 10f,
                height - mInputHeight - mTextBound.height() - paddingBottom,
                cursorLeftOffset + 10f + CURSOR_WIDTH,
                height.toFloat() - paddingBottom,
                mCursorPaint
            )
        }
        mIsDrawCursor = !mIsDrawCursor
        mInputViewOffsets.forEach { inputOffset ->
            canvas.drawRoundRect(
                inputOffset.left,
                inputOffset.top,
                inputOffset.right,
                inputOffset.bottom,
                mRadius,
                mRadius,
                mLinePaint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mTextPaint.getTextBounds("1", 0, 1, mTextBound)
        val height = mTextBound.height() + paddingTop + paddingBottom + mInputHeight
        var width = 0f
        for (index in 0 until mInputCount) {
            width += mSpaceItem + mInputWidth
        }
        width += PADDING_DEFAULT * 2 + paddingRight + paddingLeft - mSpaceItem
        setMeasuredDimension(width.toInt(), height.toInt())
        initData(height.toInt())
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }

        if (result < desiredSize) {
            Log.e("View", "The view is too small, the content might get cut")
        }
        return result
    }

    private fun initViews() {
        mLinePaint.color = mLineColor
        mTextPaint.apply {
            color = mTextColor
            textSize = mTextSize
            isAntiAlias = true
            style = Paint.Style.FILL
            setTypeface(Typeface.DEFAULT, mTextStyle)
        }
        mCursorPaint.apply {
            color = mTextColor
            style = Paint.Style.FILL
        }
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        setTextColor(ContextCompat.getColor(context, android.R.color.transparent))
        inputType = InputType.TYPE_CLASS_NUMBER
        isCursorVisible = false
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(mInputCount))

        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length > mInputCount) return
                if (mOldTextLength > s.length) {
                    mTexts.removeAt(mTexts.lastIndex)
                    invalidate()
                } else {
                    mTexts.add(s[s.length - 1])
                    invalidate()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                mOldTextLength = s.length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun initData(height: Int) {
        mInputViewOffsets.clear()
        val viewBottom = height - mInputHeight
        val viewLeft = PADDING_DEFAULT
        for (index in 0 until mInputCount) {
            if (mInputViewOffsets.isEmpty()) {
                mInputViewOffsets.add(
                    InputOffset(
                        viewLeft,
                        viewBottom,
                        mInputWidth + viewLeft,
                        viewBottom + mInputHeight
                    )
                )
            } else {
                if (index - 1 == -1) continue
                mInputViewOffsets.add(
                    InputOffset(
                        mInputViewOffsets[index - 1].right + mSpaceItem,
                        viewBottom,
                        mInputViewOffsets[index - 1].right + mSpaceItem + mInputWidth,
                        viewBottom + mInputHeight
                    )
                )
            }
        }
    }

    private fun runDrawCursor() {
        mDrawingCursor = true
        mRunnable = Runnable {
            setSelection(text.length)
            invalidate()
            mHandler.postDelayed(mRunnable, 500)
        }
        mHandler.postDelayed(mRunnable, 500)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    companion object {
        private const val PADDING_DEFAULT = 30f
        private const val CURSOR_WIDTH = 5f
    }
}