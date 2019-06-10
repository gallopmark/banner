package com.holike.banner;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gallopmark.banner.loader.ImageLoaderImpl;
import com.gallopmark.banner.widget.Banner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<String> list = new ArrayList<>();
    private MyImageAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Banner banner = findViewById(R.id.banner);
        Banner mBanner2 = findViewById(R.id.mBanner2);
        list.add("https://file.holike.com/929461a5-72ea-4fbd-8085-973c34f0a9db.jpg");
        list.add("https://file.holike.com/1aa19351-88ce-479a-9fd6-42444e8366db.jpg");
        list.add("https://file.holike.com/301f1b8c-f63b-4796-ad80-9cbc6a9241fe.jpg");
        list.add("https://file.holike.com/e6be1812-cdb3-4b94-bfb1-85763946a843.jpg");
        adapter = new MyImageAdapter(this, list);
        mBanner2.setAdapter(adapter);
//        adapter = new MyAdapter(this, list);
        List<String> images = new ArrayList<>();
        images.add("https://file.holike.com/929461a5-72ea-4fbd-8085-973c34f0a9db.jpg");
        banner.initializer()
                .withImages(images)
                .withImageLoader(new MyImageLoader())
                .withBannerClickListener(new Banner.OnBannerClickListener() {
                    @Override
                    public void onBannerClick(@NonNull View view, int position) {
                        Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_LONG).show();
                    }
                })
                .startup();
        mBanner2.postDelayed(new Runnable() {
            @Override
            public void run() {
                startUpdate();
            }
        }, 3000);

    }

    private void startUpdate() {
        list.add("https://file.holike.com/e6be1812-cdb3-4b94-bfb1-85763946a843.jpg");
        list.add("https://file.holike.com/301f1b8c-f63b-4796-ad80-9cbc6a9241fe.jpg");
        list.add("https://file.holike.com/1aa19351-88ce-479a-9fd6-42444e8366db.jpg");
        list.add("https://file.holike.com/929461a5-72ea-4fbd-8085-973c34f0a9db.jpg");
        adapter.notifyDataSetChanged();
    }

    private class MyImageLoader extends ImageLoaderImpl {

        @Override
        public void displayImage(Context context, String path, ImageView imageView) {
            Glide.with(context).load(path).into(imageView);
        }
    }

    private class MyImageAdapter extends PagerAdapter {
        private Context context;
        private List<String> images;

        MyImageAdapter(Context context, List<String> images) {
            this.context = context;
            this.images = images;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_banner, new LinearLayout(MainActivity.this), false);
            ImageView imageView = view.findViewById(R.id.imageView);
            Glide.with(context).load(images.get(position)).into(imageView);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }
}
