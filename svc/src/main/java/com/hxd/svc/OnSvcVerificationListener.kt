package com.hxd.svc

/**
 * CreateTime: 2020/7/13  8:08
 * Author: hxd
 * Content:
 * UpdateTime:
 * UpdateName;
 * UpdateContent:
 */
public interface OnSvcVerificationListener {
    /**
     * 验证成功
     */
    fun onSuccess(svcView: SvcView)

    /**
     * 验证失败
     */
    fun onFailed(svcView: SvcView)
}