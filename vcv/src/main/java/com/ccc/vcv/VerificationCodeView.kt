package com.ccc.vcv

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import kotlin.math.ceil
import kotlin.math.min

class VerificationCodeView : AppCompatEditText {

    private var mSpaceItem: Float = resources.getDimension(R.dimen.dp_20)
    private var mInputWidth: Float = resources.getDimension(R.dimen.dp_30)
    private var mInputHeight: Float = resources.getDimension(R.dimen.dp_56)
    private var mLineColor: Int = ContextCompat.getColor(context, android.R.color.black)
    private var mLineActiveColor: Int = ContextCompat.getColor(context, R.color.color_active)
    private var mTextColor: Int = ContextCompat.getColor(context, android.R.color.black)
    private var mCursorColor: Int = ContextCompat.getColor(context, android.R.color.black)
    private var mTextSize: Float = resources.getDimension(R.dimen.sp_18)
    private var mRadius: Float = resources.getDimension(R.dimen.dp_5)
    private var mInputCount: Int = 6
    private var mLinePaint = Paint()
    private var mLineFocusPaint = Paint()
    private var mTextPaint = Paint()
    private var mCursorPaint = Paint()
    private var mTextBound = Rect()
    private var mInputViewOffsets = ArrayList<InputOffset>()
    private var mTexts = ArrayList<Char>()
    private var mOldTextLength = 0
    private var mIsDrawCursor = true
    private var mDrawingCursor = false
    private var mStyle: Style = Style.BOX
    private var mHandler = Handler()
    private var mRunnable: Runnable? = null
    private var mOnInputVerificationCode: OnInputVerificationCodeListener? = null
    private var mClipBoard: ClipboardManager? = null
    private var mIsPaste = false
    private var mContentPaste = ""

    var style: Style = mStyle
        get() = mStyle
        set(value) {
            mStyle = value
            field = value
        }

    constructor(context: Context) : super(context) {
        onCreate()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        initAttributeSet(attrs)
        onCreate()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttributeSet(attrs)
        onCreate()
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
            mIsDrawCursor = !mIsDrawCursor
            if (!mDrawingCursor) runDrawCursor()
        } else {
            mIsDrawCursor = false
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
                nextCenterPosition + mTextBound.width() / 2 + 10f
            } else {
                nextCenterPosition - CURSOR_WIDTH_DEFAULT / 2
            }
            val textLeftOffset = if (text.toString() == "1") {
                centerPosition - mTextBound.width()
            } else {
                centerPosition - (mTextBound.width() / 2)
            }
            canvas.drawText(
                text.toString(),
                textLeftOffset,
                mInputHeight / 2f + (mTextBound.height() / 2f),
                mTextPaint
            )
        }

        if (mIsDrawCursor && mInputViewOffsets.isNotEmpty()) {
            if (cursorLeftOffset == 0f) {
                cursorLeftOffset = (mInputViewOffsets[0].left + mInputViewOffsets[0].right) / 2
            }
            canvas.drawRect(
                cursorLeftOffset,
                mInputHeight / 2f - (mTextBound.height() / 2f) - context.resources.getDimension(R.dimen.dp_2),
                cursorLeftOffset + CURSOR_WIDTH_DEFAULT,
                mInputHeight / 2f + (mTextBound.height() / 2f) + context.resources.getDimension(R.dimen.dp_2),
                mCursorPaint
            )
        }
        mInputViewOffsets.forEachIndexed { index, inputOffset ->
            val paint = when {
                isFocused && mTexts.size == mInputCount -> {
                    if (index == mTexts.lastIndex) {
                        mLineFocusPaint
                    } else {
                        mLinePaint
                    }
                }
                isFocused && mTexts.size < mInputCount -> {
                    if (index == mTexts.size) {
                        mLineFocusPaint
                    } else {
                        mLinePaint
                    }
                }
                else -> mLinePaint
            }
            canvas.drawRoundRect(
                inputOffset.left,
                inputOffset.top,
                inputOffset.right,
                inputOffset.bottom,
                mRadius,
                mRadius,
                paint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mTextPaint.getTextBounds("1", 0, 1, mTextBound)
        val height = mInputHeight
        var width = 0f
        for (index in 0 until mInputCount) {
            width += mSpaceItem + mInputWidth
        }
        width += PADDING_LEFT_DEFAULT * 2 + paddingRight + paddingLeft - mSpaceItem
        setMeasuredDimension(ceil(width).toInt(), ceil(height).toInt())
        updateInputViewOffsets()
    }

    fun setOnInputVerificationCodeListener(onInputVerificationCode: OnInputVerificationCodeListener?) {
        mOnInputVerificationCode = onInputVerificationCode
    }

    fun setHexColorCursor(@ColorInt color: Int) {
        mCursorColor = color
    }

    fun setColorCursor(@ColorRes colorId: Int) {
        mCursorColor = ContextCompat.getColor(context, colorId)
    }

    fun setHexColorText(@ColorInt color: Int) {
        mTextColor = color
    }

    fun setColorText(@ColorRes colorId: Int) {
        mTextColor = ContextCompat.getColor(context, colorId)
    }

    fun setHexColorLine(@ColorInt color: Int) {
        mLineColor = color
    }

    fun setColorLine(@ColorRes colorId: Int) {
        mLineColor = ContextCompat.getColor(context, colorId)
    }

    fun setHexColorLineActive(@ColorInt color: Int) {
        mLineActiveColor = color
    }

    fun setColorLineActive(@ColorRes colorId: Int) {
        mLineActiveColor = ContextCompat.getColor(context, colorId)
    }

    fun setHeightInput(@DimenRes dimenId: Int) {
        mInputHeight = resources.getDimension(dimenId)
    }

    fun setWidthInput(@DimenRes dimenId: Int) {
        mInputWidth = resources.getDimension(dimenId)
    }

    fun setRadius(@DimenRes dimenId: Int) {
        mRadius = resources.getDimension(dimenId)
    }

    fun setTextSize(@DimenRes dimenId: Int) {
        mTextSize = resources.getDimension(dimenId)
    }

    fun setSpacingItem(@DimenRes dimenId: Int) {
        mSpaceItem = resources.getDimension(dimenId)
    }

    fun refresh() {
        initPaints()
        updateInputViewOffsets()
        invalidate()
    }

    private fun initAttributeSet(attrs: AttributeSet) {
        val typeArray =
            context.obtainStyledAttributes(attrs, R.styleable.VerificationCodeView, 0, 0)
        try {
            mSpaceItem = typeArray.getDimension(
                R.styleable.VerificationCodeView_vcv_spacingItem,
                resources.getDimension(R.dimen.dp_20)
            )
            mInputWidth = typeArray.getDimension(
                R.styleable.VerificationCodeView_vcv_inputWidth,
                resources.getDimension(R.dimen.dp_30)
            )
            mInputHeight = typeArray.getDimension(
                R.styleable.VerificationCodeView_vcv_inputHeight,
                resources.getDimension(R.dimen.dp_56)
            )
            mRadius = typeArray.getDimension(
                R.styleable.VerificationCodeView_vcv_radius,
                resources.getDimension(R.dimen.dp_5)
            )
            mInputCount = typeArray.getInt(
                R.styleable.VerificationCodeView_vcv_inputCount, 6
            )
            mTextSize = typeArray.getDimension(
                R.styleable.VerificationCodeView_vcv_textSize,
                resources.getDimension(R.dimen.sp_18)
            )
            mStyle = Style.fromValue(
                typeArray.getInt(
                    R.styleable.VerificationCodeView_vcv_style, Style.BOX.value()
                )
            )
            mLineColor = typeArray.getColor(
                R.styleable.VerificationCodeView_vcv_lineColor,
                ContextCompat.getColor(context, android.R.color.black)
            )
            mLineActiveColor = typeArray.getColor(
                R.styleable.VerificationCodeView_vcv_lineActiveColor,
                ContextCompat.getColor(context, R.color.color_active)
            )
            mTextColor = typeArray.getColor(
                R.styleable.VerificationCodeView_vcv_textColor,
                ContextCompat.getColor(context, android.R.color.black)
            )
            mCursorColor = typeArray.getColor(
                R.styleable.VerificationCodeView_vcv_cursorColor,
                ContextCompat.getColor(context, android.R.color.black)
            )
        } finally {
            typeArray.recycle()
        }
    }

    private fun onCreate() {
        initData()
        initViews()
        initPaints()
        handleEvents()
        runDrawCursor()
    }

    private fun initData() {
        mClipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private fun initPaints() {
        updateLinePaint()
        updateTextPaint()
        updateCursorPaint()
    }

    private fun handleEvents() {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length > mInputCount) return
                if (mOldTextLength > s.length) {
                    if (mTexts.lastIndex > -1) {
                        mTexts.removeAt(mTexts.lastIndex)
                        invalidate()
                    }
                } else {
                    if (mIsPaste) {
                        for (index in mContentPaste.indices) {
                            if (mTexts.size >= mInputCount) return
                            mTexts.add(mContentPaste[index])
                        }
                    } else {
                        if (s.length - 1 >= 0) {
                            mTexts.add(s[s.length - 1])
                        }
                    }
                    mContentPaste = ""
                    invalidate()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                mOldTextLength = s.length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mIsPaste = count != 1
                if (s.length >= mInputCount) {
                    mOnInputVerificationCode?.onInputVerificationCodeComplete()
                } else {
                    mOnInputVerificationCode?.onInputVerificationCodeUnComplete()
                }
            }
        })
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }

        if (result < desiredSize) {
            Log.e("View", "The view is too small, the content might get cut")
        }
        return result
    }

    private fun initViews() {
        setOnLongClickListener {
            mContentPaste = getContentClipBoard()
            if (mContentPaste.isNotBlank()) {
                this@VerificationCodeView.append(mContentPaste)
            }
            true
        }
        height = ceil(mInputHeight).toInt()
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        setTextColor(ContextCompat.getColor(context, android.R.color.transparent))
        inputType = InputType.TYPE_CLASS_NUMBER
        isCursorVisible = false
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(mInputCount))
    }

    private fun updateCursorPaint() {
        mCursorPaint.apply {
            color = mTextColor
            style = Paint.Style.FILL
        }
    }

    private fun updateTextPaint() {
        mTextPaint.apply {
            color = mTextColor
            textSize = mTextSize
            style = Paint.Style.FILL
            typeface = this@VerificationCodeView.typeface
        }
    }

    private fun updateLinePaint() {
        when (mStyle) {
            Style.UNDERLINE -> {
                mLinePaint = Paint().apply {
                    color = mLineColor
                }
                mLineFocusPaint = Paint().apply {
                    color = mLineActiveColor
                }
            }
            else -> {
                mLinePaint = Paint().apply {
                    style = Paint.Style.STROKE
                    strokeWidth = context.resources.getDimension(R.dimen.dp_1_5)
                    color = mLineColor
                }
                mLineFocusPaint = Paint().apply {
                    style = Paint.Style.STROKE
                    strokeWidth = context.resources.getDimension(R.dimen.dp_1_5)
                    color = mLineActiveColor
                }
            }
        }
    }

    private fun updateInputViewOffsets() {
        mInputViewOffsets.clear()
        val viewLeft = PADDING_LEFT_DEFAULT
        for (index in 0 until mInputCount) {
            val viewBottom = when (mStyle) {
                Style.UNDERLINE -> mInputHeight
                else -> mInputHeight - PADDING_TOP_DEFAULT
            }
            val top = when (mStyle) {
                Style.UNDERLINE -> viewBottom - UNDERLINE_INPUT_HEIGHT_DEFAULT
                else -> PADDING_TOP_DEFAULT
            }
            if (mInputViewOffsets.isEmpty()) {
                mInputViewOffsets.add(
                    InputOffset(
                        viewLeft,
                        top,
                        mInputWidth + viewLeft,
                        viewBottom
                    )
                )
            } else {
                if (index - 1 == -1) continue
                mInputViewOffsets.add(
                    InputOffset(
                        mInputViewOffsets[index - 1].right + mSpaceItem,
                        top,
                        mInputViewOffsets[index - 1].right + mSpaceItem + mInputWidth,
                        viewBottom
                    )
                )
            }
        }
    }

    private fun runDrawCursor() {
        mDrawingCursor = true
        mRunnable = Runnable {
            setSelection(text?.length ?: 0)
            invalidate()
            mHandler.postDelayed(mRunnable, DELAY_CURSOR)
        }
        mHandler.postDelayed(mRunnable, DELAY_CURSOR)
    }

    private fun getContentClipBoard(): String {
        val clipboard = mClipBoard ?: return ""
        return if (clipboard.hasPrimaryClip() &&
            clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true
        ) {
            val content = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
            if (content.isDigitsOnly()) {
                if (mTexts.size + content.length > mInputCount) {
                    content.substring(0, mInputCount - mTexts.size)
                } else {
                    content
                }
            } else {
                ""
            }
        } else {
            ""
        }
    }

    enum class Style {
        UNDERLINE {
            override fun value(): Int = 0
        },
        BOX {
            override fun value(): Int = 1
        };

        abstract fun value(): Int

        companion object {
            fun fromValue(value: Int) = values().first { it.value() == value }
        }
    }

    companion object {
        private const val UNDERLINE_INPUT_HEIGHT_DEFAULT = 10f
        private const val PADDING_LEFT_DEFAULT = 30f
        private const val PADDING_TOP_DEFAULT = 9f
        private const val CURSOR_WIDTH_DEFAULT = 5f
        private const val DELAY_CURSOR = 500L
    }

    interface OnInputVerificationCodeListener {
        fun onInputVerificationCodeComplete()
        fun onInputVerificationCodeUnComplete() = Unit
    }
}
