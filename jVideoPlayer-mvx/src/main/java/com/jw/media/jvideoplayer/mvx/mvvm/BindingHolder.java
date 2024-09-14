package com.jw.media.jvideoplayer.mvx.mvvm;

import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

/**
 * Created by Joyce.wang on 2024/9/11 16:01
 *
 * @Description TODO
 */
public class BindingHolder<VDB extends ViewDataBinding> extends ViewHolder {

    VDB mBinding;

    public BindingHolder(View convertView) {
        super(convertView);
        mBinding = DataBindingUtil.bind(convertView);

    }

    public VDB getBinding(){
        return mBinding;
    }
}