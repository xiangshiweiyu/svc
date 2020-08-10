package com.hxd.svc

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

/**
 * @CreateTime: 2020/7/23  10:22
 * @author:
 * @Content:
 * @UpdateTime:
 * @UpdateName;
 * @UpdateContent:
 */
object DensityUtils {

    fun samplingRateDensity(sampleSize: Int = 2, filePath: String, file: File) {

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false;
        //        options.inJustDecodeBounds = true;// 为true 的时候不进行加载，只是对图片宽高信息进行采样
        options.inSampleSize = sampleSize;
        val bitmap = BitmapFactory.decodeFile(filePath, options);
        val baos = ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        try {
            if (file.exists()) {
                file.delete()
            } else {
                file.createNewFile()
            }
            val fos = FileOutputStream(file)
            fos.apply {
                write(baos.toByteArray())
                flush()
                close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }
}