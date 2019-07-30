package com.xcode.banner.loader;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.ImageView;

public abstract class ImageLoaderImpl implements ImageLoader {
    @NonNull
    @Override
    public ImageView createImageView(Context context) {
        return new ImageView(context);
    }
}
