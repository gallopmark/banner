package com.gallopmark.banner.loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;

public abstract class ImageLoaderImpl implements ImageLoader {
    @NonNull
    @Override
    public ImageView createImageView(Context context) {
        return new ImageView(context);
    }
}
