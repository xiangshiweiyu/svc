### 引入操作
这是我第一次生成轮子，各位如果有需要可以引用下哈。
###### 1、依赖引入
```
   implementation 'com.github.xiangshiweiyu:svc:1.0.3'
```
###### 2、xml 配置
```
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.hxd.svc.SvcView
        android:id="@+id/sv_main"
        android:layout_width="match_parent"
        android:layout_height="140dp"
        android:layout_marginStart="20dp"
        android:layout_marginTop="100dp"
        android:layout_marginEnd="20dp"
        app:layout_constraintTop_toTopOf="parent" />

    <SeekBar
        android:id="@+id/sb_main"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginStart="20dp"
        android:layout_marginTop="50dp"
        android:layout_marginEnd="20dp"
        android:thumb="@drawable/selector_button"
        app:layout_constraintTop_toBottomOf="@+id/sv_main" />
</androidx.constraintlayout.widget.ConstraintLayout>
```
###### 3、kt 文件编写
```
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        sv_main.setOnSvcVerificationListener(object : OnSvcVerificationListener {
            override fun onSuccess(svcView: SvcView) {
                sb_main.isEnabled = false
            }

            override fun onFailed(svcView: SvcView) {
                svcView.reSliderSize()
                sb_main.progress = 0
            }
        })


        sb_main.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                sv_main.setSliderSize(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                sb_main.max = sv_main.maxSliderSize()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                sv_main.checkCaptcha()
            }
        })

        Glide.with(this).asBitmap().load(R.mipmap.ic_bg)
                .into(object : SimpleTarget<Bitmap>() {

                    override fun onResourceReady(resource: Bitmap,
                                                 transition: Transition<in Bitmap>?) {
                        sv_main.setImageBitmap(resource)
                        sv_main.createCaptcha()
                    }
                })
    }
}
```
欢迎关注 码虫韩小呆
![欢迎关注 码虫韩小呆](https://upload-images.jianshu.io/upload_images/6433394-69832c80bdc58b5d.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
