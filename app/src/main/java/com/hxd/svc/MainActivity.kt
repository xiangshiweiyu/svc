package com.hxd.svc

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.activity_main.*

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