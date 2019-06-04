package com.holike.banner;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gallopmark.banner.loader.ImageLoaderImpl;
import com.gallopmark.banner.widget.Banner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Banner banner = findViewById(R.id.banner);
        list.add("https://file.holike.com/929461a5-72ea-4fbd-8085-973c34f0a9db.jpg");
        list.add("https://file.holike.com/1aa19351-88ce-479a-9fd6-42444e8366db.jpg");
        list.add("https://file.holike.com/301f1b8c-f63b-4796-ad80-9cbc6a9241fe.jpg");
        list.add("https://file.holike.com/e6be1812-cdb3-4b94-bfb1-85763946a843.jpg");
//        adapter = new MyAdapter(this, list);
        banner.initializer()
                .withImages(list)
                .withImageLoader(new MyImageLoader())
                .withBannerClickListener(new Banner.OnBannerClickListener() {
                    @Override
                    public void onBannerClick(View view, int position) {
                        Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_LONG).show();
                    }
                })
                .startup();
        banner.postDelayed(new Runnable() {
            @Override
            public void run() {
//                startUpdate();
            }
        }, 3000);
    }

    private void startUpdate() {
        list.add("https://file.holike.com/e6be1812-cdb3-4b94-bfb1-85763946a843.jpg");
        list.add("https://file.holike.com/301f1b8c-f63b-4796-ad80-9cbc6a9241fe.jpg");
        list.add("https://file.holike.com/1aa19351-88ce-479a-9fd6-42444e8366db.jpg");
        list.add("https://file.holike.com/929461a5-72ea-4fbd-8085-973c34f0a9db.jpg");
    }

    private class MyImageLoader extends ImageLoaderImpl {

        @Override
        public void displayImage(Context context, String path, ImageView imageView) {
            Glide.with(context).load(path).into(imageView);
        }
    }
}
