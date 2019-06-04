package com.gallopmark.banner.transformer;

import android.view.View;

public class AccordionTransformer extends ABaseTransformer {

    @Override
    protected void onTransform(View view, float position) {
        float scale = position < 0 ? 1f + position : 1f - position;
        if(scale <= 0.5f) scale = 1f;
        view.setPivotX(position < 0 ? 0 : view.getWidth());
        view.setScaleX(scale);
    }
}
