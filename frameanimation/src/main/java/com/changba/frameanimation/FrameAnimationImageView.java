package com.changba.frameanimation;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.lang.reflect.Field;

/**
 * @author HeXuebin on 3/5/21.
 */
public class FrameAnimationImageView extends AppCompatImageView {
    private static final String TAG = FrameAnimationImageView.class.getSimpleName();

    public FrameAnimationImageView(@NonNull Context context) {
        this(context, null);
    }

    public FrameAnimationImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FrameAnimationImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        try {
            Class RClazz = Class.forName("com.android.internal.R$styleable");
            Field imageViewField = RClazz.getField("ImageView");
            imageViewField.setAccessible(true);
            Field imageViewFieldSrc = RClazz.getField("ImageView_src");
            imageViewFieldSrc.setAccessible(true);
            int[] styleableImageViewInts = (int[]) imageViewField.get(RClazz);
            int styleableImageViewSrcInt = imageViewFieldSrc.getInt(RClazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
//        final TypedArray a = context.obtainStyledAttributes(attrs, android.R.styleable.ImageView, defStyleAttr, 0);
//
//        a.recycle();

    }
}
