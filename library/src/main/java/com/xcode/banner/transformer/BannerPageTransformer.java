package com.xcode.banner.transformer;

import android.view.View;

public class BannerPageTransformer extends ABaseTransformer {
    private float scaleMin;
    private float alphaMin;

    public BannerPageTransformer(float scaleMin, float alphaMin) {
        this.scaleMin = scaleMin;
        this.alphaMin = alphaMin;
    }

    @Override
    protected void onTransform(View page, float position) {
        //            // 不同位置的缩放和透明度
        final float scaleFactor = (position < 0) ? ((1 - scaleMin) * position + 1) : ((scaleMin - 1) * position + 1);
        float scale = scaleFactor <= 0.5f ? 1f : scaleFactor;
        float alpha = (position < 0) ? ((1 - alphaMin) * position + 1) : ((alphaMin - 1) * position + 1);
        // 保持左右两边的图片位置中心
        page.setPivotX(position < 0 ? page.getWidth() : 0f);
        page.setPivotY(page.getHeight() / 2f);
        if (scale > 0 && scale <= 1f) {
            if (scale <= 0.5f) scale = 1f;
            page.setScaleX(scale);
            page.setScaleY(scale);
        }
        page.setAlpha(Math.abs(alpha));
    }

    @Override
    protected boolean isPagingEnabled() {
        return true;
    }
}
