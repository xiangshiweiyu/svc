package com.hxd.svc

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import java.util.*
import kotlin.math.abs

/**
 * CreateTime: 2020/7/11  9:43
 * Author: hxd
 * Content:自定义 ImageView 实现滑块操作
 * UpdateTime:
 * UpdateName;
 * UpdateContent:
 */

@SuppressLint("AppCompatCustomView")
public class SvcView @JvmOverloads constructor(context: Context? = null,
                                               attrs: AttributeSet? = null,
                                               defStyleAttr: Int = 0,
                                               defStyleRes: Int = 0) :
        ImageView(context, attrs, defStyleAttr, defStyleRes) {

    private var mWidth = 0
    private var mHeight = 0

    /**
     * 验证码尺寸
     * mCaptchaWidth ：宽度
     *mCaptchaHeight : 高度
     */
    private var mSvcWidth = 0
    private var mSvcHeight = 0

    /**
     * 验证码左上角位置
     * mCaptchaX ： x 轴坐标
     * mCaptchaY : y 轴坐标
     */
    private var mCaptchaX = 0
    private var mCaptchaY = 0

    private lateinit var mRandom: Random
    private lateinit var mPaint: Paint

    /**
     * 验证码 阴影、抠图的Path
     */
    private lateinit var mCaptchaPath: Path
    private lateinit var mPorterDuffXmlFerMode: PorterDuffXfermode

    /**
     * 是否绘制失败（验证失败闪烁动画用）
     */
    private var isDrawMask = false

    /**
     * 绘制滑块
     */
    private var mMaskPaint: Paint
    private lateinit var mMaskBitmap: Bitmap

    /**
     * 绘制阴影
     */
    private var mMaskShadowPaint: Paint
    private lateinit var mMaskShadowBitmap: Bitmap

    /**
     * 滑块位移
     */
    private var mSliderOffset: Int = 0

    /**
     * 是否处于验证状态
     */
    private var isMatchMode = false

    /**
     * 滑块允许误差范围
     */
    private var mErrorValue = 0f

    /**
     * 验证失败闪烁动画
     */
    private lateinit var mFailAnim: ValueAnimator

    /**
     * 验证成功闪烁动画
     */
    private lateinit var mSuccessAnim: ValueAnimator
    private var isSuccessShow = false

    /**
     * 成功动画 画笔
     */
    private lateinit var mSuccessPaint: Paint

    /**
     * 动画坐标位置
     */
    private var mSuccessAnimOffset = 0

    /**
     * 动画形状
     */
    private lateinit var mSuccessPath: Path

    /**
     * 初始化必要数据
     */
    init {
        //配置滑块 大小  长宽为 50 dp
        val defaultSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                50f,
                resources.displayMetrics).toInt()
        mSvcHeight = defaultSize
        mSvcWidth = defaultSize

        // 配置 误差值大小 3 dp
        mErrorValue =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics)
        val ta = context?.theme?.obtainStyledAttributes(attrs, R.styleable.SvcView, defStyleAttr, 0)

        val n = ta?.indexCount

        n?.let {
            for (i in 0 until it) {
                when (val attr = ta.getIndex(i)) {
                    R.styleable.SvcView_svcHeight -> mSvcHeight =
                            ta.getDimension(attr, defaultSize.toFloat()).toInt()

                    R.styleable.SvcView_svcWidth  -> mSvcWidth =
                            ta.getDimension(attr, defaultSize.toFloat()).toInt()

                    R.styleable.SvcView_errorSize -> mErrorValue =
                            ta.getDimension(attr, mErrorValue)
                }
            }
        }

        ta?.recycle()

        mRandom = Random(System.nanoTime())
        //kotlin 内 or 代替 Java 内 |
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mPaint.color = 0x77000000
        //设置残缺区域遮罩滤镜
        mPaint.maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.SOLID)

        //滑块区域
        mPorterDuffXmlFerMode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        mMaskPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)

        //阴影画笔
        mMaskShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mMaskShadowPaint.color = Color.BLACK
        mMaskShadowPaint.maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.SOLID)

        mCaptchaPath = Path()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mWidth = w
        mHeight = h

        createAnim()

        post {
            checkCaptcha()
        }
    }

    /**
     * 初始化动画区域
     */
    private fun createAnim() {
        mFailAnim = ValueAnimator.ofFloat(0f, 1f)
        mFailAnim.apply {
            duration = 100
            repeatCount = 4
            repeatMode = ValueAnimator.REVERSE

            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                    super.onAnimationEnd(animation, isReverse)
                    onSvcVerificationListener?.onFailed(this@SvcView)
                }
            })

            addUpdateListener {
                //强转为 float 类型数据
                val animatedValue = it.animatedValue as Float
                isDrawMask = animatedValue >= 0.5f
                invalidate()
            }
        }

        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                100f,
                resources.displayMetrics).toInt()

        mSuccessAnim = ValueAnimator.ofInt(mWidth + width, 0)
        mSuccessAnim.apply {
            duration = 500
            interpolator = FastOutLinearInInterpolator()

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationStart(animation)
                    isSuccessShow = true
                }

                override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                    super.onAnimationEnd(animation, isReverse)
                    onSvcVerificationListener?.onSuccess(this@SvcView)
                    isSuccessShow = false
                    isMatchMode = false
                }
            })

            addUpdateListener {
                mSuccessAnimOffset = it.animatedValue as Int
                invalidate()
            }
        }

        mSuccessPaint = Paint()
        mSuccessPaint.shader = LinearGradient(0f,
                0f,
                width / 2 * 3f,
                mHeight.toFloat(),
                intArrayOf(0x00ffffff, -0x77000001),
                floatArrayOf(0f, 0.5f),
                Shader.TileMode.MIRROR)


        //模仿斗鱼 是一个平行四边形滚动过去
        mSuccessPath = Path()
        mSuccessPath.apply {
            moveTo(0f, 0f)
            rLineTo(width.toFloat(), 0f)
            rLineTo(width / 2f, mHeight.toFloat())
            rLineTo(-width.toFloat(), 0f)
            close()
        }
    }

    /**
     * 初始化验证码区域
     */
    public fun createCaptcha() {
        drawable?.let {
            resetFlags()
            createCaptchaPath()
            createMask()
            invalidate()
        }
    }


    /**
     *重置标志位  开启验证
     */
    private fun resetFlags() {
        isMatchMode = true
    }

    /**
     * 生成验证码 滑块
     */
    private fun createCaptchaPath() {
        //随机生成 gap
        var gap = mRandom.nextInt(mSvcWidth / 2)
        // 宽度/3  获取更好展示效果
        gap = mSvcWidth / 3
        //随机产生 缺口部分左上角 x，y 坐标
        mCaptchaX = mRandom.nextInt(mWidth - mSvcWidth - gap)
        mCaptchaY = mRandom.nextInt(mHeight - mSvcHeight - gap)

        mCaptchaPath.apply {
            reset()
            lineTo(0f, 0f)
            //左上角开始 绘制一个不规则的阴影
            moveTo(mCaptchaX.toFloat(), mCaptchaY.toFloat())
            lineTo((mCaptchaX + gap).toFloat(), mCaptchaY.toFloat())
            //draw一个随机凹凸的圆
            SvcSizeUtils.drawPartCircle(PointF((mCaptchaX + gap).toFloat(), mCaptchaY.toFloat()),
                    PointF((mCaptchaX + gap * 2).toFloat(), mCaptchaY.toFloat()),
                    mCaptchaPath,
                    mRandom.nextBoolean())
            //右上角
            lineTo((mCaptchaX + mSvcWidth).toFloat(), mCaptchaY.toFloat())
            lineTo((mCaptchaX + mSvcWidth).toFloat(), (mCaptchaY + gap).toFloat())
            //draw一个随机凹凸的圆
            SvcSizeUtils.drawPartCircle(PointF((mCaptchaX + mSvcWidth).toFloat(),
                    (mCaptchaY + gap).toFloat()),
                    PointF((mCaptchaX + mSvcWidth).toFloat(), (mCaptchaY + gap * 2).toFloat()),
                    mCaptchaPath,
                    mRandom.nextBoolean())
            //右下角
            lineTo((mCaptchaX + mSvcWidth).toFloat(), (mCaptchaY + mSvcHeight).toFloat())
            lineTo((mCaptchaX + mSvcWidth - gap).toFloat(), (mCaptchaY + mSvcHeight).toFloat())
            //draw 一个随机的 凹凸圆
            SvcSizeUtils.drawPartCircle(PointF((mCaptchaX + mSvcWidth - gap).toFloat(),
                    (mCaptchaY + mSvcHeight).toFloat()),
                    PointF((mCaptchaX + mSvcWidth - gap * 2).toFloat(),
                            (mCaptchaY + mSvcHeight).toFloat()),
                    mCaptchaPath,
                    mRandom.nextBoolean())
            //左下角
            lineTo(mCaptchaX.toFloat(), (mCaptchaY + mSvcHeight).toFloat())
            lineTo(mCaptchaX.toFloat(), (mCaptchaY + mSvcHeight - gap).toFloat())
            //draw 一个随机的 凹凸圆
            SvcSizeUtils.drawPartCircle(PointF(mCaptchaX.toFloat(),
                    (mCaptchaY + mSvcHeight - gap).toFloat()),
                    PointF(mCaptchaX.toFloat(), (mCaptchaY + mSvcHeight - gap * 2).toFloat()),
                    mCaptchaPath,
                    mRandom.nextBoolean())
            close()
        }
    }

    /**
     * 生成滑块
     */
    private fun createMask() {
        mMaskBitmap = getMaskBitmap(drawable.toBitmap(), mCaptchaPath)
        //滑块阴影
        mMaskShadowBitmap = mMaskBitmap.extractAlpha()
        //重置拖动位移
        mSliderOffset = 0
        //绘制失败 标志闪烁动画
        isDrawMask = true
    }

    /**
     * 抠图操作
     */
    private fun getMaskBitmap(toBitmap: Bitmap, mCaptchaPath: Path): Bitmap {

        //按照控件的宽高 创建一个 bitmap
        val tempBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        //将 创建 bitmap 作为画板
        val c = Canvas(tempBitmap)
        //抗锯齿
        c.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        //绘制用于遮罩的圆形
        c.drawPath(mCaptchaPath, mMaskPaint)
        //设置遮罩模式
        mMaskPaint.xfermode = mPorterDuffXmlFerMode
        //考虑 scaleType 等因素 ，要用 Matrix 对 Bitmap 进行缩放
        c.drawBitmap(toBitmap, imageMatrix, mMaskPaint)
        mMaskPaint.xfermode = null
        return tempBitmap
    }


    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        //在 ImageView 上绘制验证码相关部分
        if (isMatchMode) {
            //绘制阴影部分
            canvas?.drawPath(mCaptchaPath, mPaint)
            //绘制滑块
            // isDrawMask  绘制失败闪烁动画用
            if (isDrawMask) {
                canvas?.apply {
                    // 先绘制阴影
                    drawBitmap(mMaskShadowBitmap,
                            (-mCaptchaX + mSliderOffset).toFloat(),
                            0f,
                            mMaskShadowPaint)

                    drawBitmap(mMaskBitmap, (-mCaptchaX + mSliderOffset).toFloat(), 0f, null)
                }
            }

            //验证成功 白光闪烁
            if (isSuccessShow) {
                canvas?.apply {
                    translate(mSuccessAnimOffset.toFloat(), 0f)
                    drawPath(mSuccessPath, mSuccessPaint)
                }
            }

        }
    }

    /**
     * 校验 缺块是否 滑入阴影内
     */
    public fun checkCaptcha() {

        if (null != onSvcVerificationListener && isMatchMode) {
            //判断 滑块与缺口重合度 误差默认为 3dp
            if (abs(mSliderOffset - mCaptchaX) < mErrorValue) {
                //成功动画
                mSuccessAnim.start()
            } else {
                //失败动画
                mFailAnim.start()
            }
        }
    }

    /**
     * 重置滑块
     */
    public fun reSliderSize() {
        mSliderOffset = 0
        invalidate()
    }

    /**
     * 可滑动最大距离
     */
    public fun maxSliderSize(): Int {
        return mWidth - mSvcWidth
    }


    /**
     * 配置滑动距离
     * @value
     */
    public fun setSliderSize(value: Int) {
        mSliderOffset = value
        invalidate()
    }

    /**
     * 验证码验证的回调
     */
    private var onSvcVerificationListener: OnSvcVerificationListener? = null

    /**
     * 获取 监听接口
     */
    public fun getOnSvcVerificationListener(): OnSvcVerificationListener? {
        return onSvcVerificationListener
    }

    /**
     * 设置 验证码验证回调
     */
    public fun setOnSvcVerificationListener(onSvcVerificationListener: OnSvcVerificationListener): SvcView {
        this.onSvcVerificationListener = onSvcVerificationListener
        return this
    }
}



