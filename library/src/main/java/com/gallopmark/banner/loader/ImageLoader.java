package com.gallopmark.banner.loader;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.ImageView;

public interface ImageLoader {
    @NonNull
    ImageView createImageView(Context context);

    void displayImage(Context context, String path, ImageView imageView);
}
