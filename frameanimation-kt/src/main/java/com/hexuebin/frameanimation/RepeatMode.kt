package com.hexuebin.frameanimation

import androidx.annotation.IntDef

/**
 * @author HeXuebin on 2021/6/26.
 */
@IntDef(RESTART, REVERSE)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class RepeatMode

const val RESTART = 1
const val REVERSE = 2
const val INFINITE = -1
