package com.starmedia.leisure_support_java;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.starmedia.pojos.ContentImage;
import com.starmedia.pojos.ContentItem;
import com.starmedia.pojos.ContentNews;
import com.starmedia.pojos.ContentVideo;
import com.starmedia.tinysdk.IContent;
import com.starmedia.tinysdk.StarMedia;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InfoFlowActivity extends AppCompatActivity {

    ArrayList<DataWrapper> dataList = new ArrayList();
    RecyclerView.Adapter adapter = new RecyclerView.Adapter<Holder>() {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new Holder(new FrameLayout(InfoFlowActivity.this));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {

            holder.bind(dataList.get(position));
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }
    };
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    int page = 1;
    Boolean isLoading = false;
    Button btnReload;
    ProgressBar loading;
    //每隔多少条插入广告
    private int AD_INTERVAL = 2;
    private int lastLeftItemCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_flow);


        HashMap<Long, ArrayList<Integer>> params = new HashMap<Long, ArrayList<Integer>>();
        params.put(0L, new ArrayList<Integer>());
        params.put(1L, new ArrayList<Integer>());
        params.put(2L, new ArrayList<Integer>());

        RecyclerView infoflow = findViewById(R.id.rcy_infoflow);
        btnReload = findViewById(R.id.btn_reload);
        loading = findViewById(R.id.pb_loading);

        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestContentData(params);
            }
        });


        infoflow.setLayoutManager(layoutManager);
        infoflow.setAdapter(adapter);

        infoflow.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                if (visibleItemCount > 0 && !isLoading && visibleItemCount + firstVisiblePosition >= totalItemCount - 5) {
                    requestContentData(params);
                }
            }
        });

        if (dataList.isEmpty()) {
            requestContentData(params);
        }
    }


    private void requestContentData(HashMap<Long, ArrayList<Integer>> params) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading.setVisibility(View.VISIBLE);
                btnReload.setVisibility(View.GONE);
            }
        });
        isLoading = true;
        findViewById(R.id.btn_reload).setVisibility(View.GONE);

        StarMedia.loadContent(this, params, page, new IContent.Listener() {
            @Override
            public void onError(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading.setVisibility(View.GONE);
                        btnReload.setVisibility(View.VISIBLE);
                    }
                });
                Toast.makeText(InfoFlowActivity.this, message, Toast.LENGTH_LONG).show();


                if (page == 1) {
                    findViewById(R.id.btn_reload).setVisibility(View.VISIBLE);
                    findViewById(R.id.btn_reload).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    requestContentData(params);
                                }
                            });
                        }
                    });
                }

                isLoading = false;
            }

            @Override
            public void onSuccess(List<ContentItem> list) {
                page++;

                fillItems(list);
                isLoading = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading.setVisibility(View.GONE);
                    }
                });

            }

        });
    }

    private void fillItems(
            List<ContentItem> list
    ) {
        ArrayList tempList = new ArrayList();

        for (ContentItem item :
                list) {
            tempList.add(new DataWrapper(item, false));
        }

        int firstInterval = Math.max(0, AD_INTERVAL - lastLeftItemCount);

        int adCount = (tempList.size() - firstInterval) / AD_INTERVAL + 1;

        lastLeftItemCount = (tempList.size() - firstInterval) % AD_INTERVAL;

        tempList.add(firstInterval, new DataWrapper(null, true));

        for (int i = 2; i <= adCount; i++) {
            tempList.add(
                    i * (AD_INTERVAL + 1) - 1 - (AD_INTERVAL - firstInterval),
                    new DataWrapper(null, true)
            );
        }

        int star = dataList.size();
        dataList.addAll(tempList);
        adapter.notifyItemRangeInserted(star, dataList.size());
    }

    class DataWrapper {
        ContentItem data;
        Boolean isAd;
//        15910825306

        DataWrapper(ContentItem data, Boolean isAd) {
            this.data = data;
            this.isAd = isAd;
        }
    }

    class Holder extends RecyclerView.ViewHolder {

        public Holder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(DataWrapper data) {
            FrameLayout container = (FrameLayout) itemView;
            container.removeAllViews();
            if (data.isAd) {
                getLayoutInflater().inflate(R.layout.item_ad, container, true);
                container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                //上报条目曝光
                data.data.onShow();
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        data.data.onClick(InfoFlowActivity.this);
                    }
                });
                if (data.data.getType().equals(ContentItem.Type.NEWS)) {
                    ContentNews contentNews = data.data.getContentNews();
                    getLayoutInflater().inflate(R.layout.item_content_text, container, true);
                    ((TextView) container.findViewById(R.id.tv_content_title)).setText(contentNews.getTitle());
                    ((TextView) container.findViewById(R.id.tv_content_source)).setText(contentNews.getSource());
                    ((TextView) container.findViewById(R.id.tv_content_read)).setText(
                            MessageFormat.format("{0}阅读", contentNews.getReadCounts()));

                    ((TextView) container.findViewById(R.id.tv_content_time)).setText(contentNews.getUpdateTime());
                } else if (data.data.getType().equals(ContentItem.Type.IMAGE)) {
                    ContentImage contentImage = data.data.getContentImage();
                    getLayoutInflater().inflate(R.layout.item_content_image, container, true);
                    ((TextView) container.findViewById(R.id.tv_content_title)).setText(contentImage.getTitle());
                    ((TextView) container.findViewById(R.id.tv_content_image_count)).setText(
                            contentImage.getColImageCount() + "");
                    ((TextView) container.findViewById(R.id.tv_content_source)).setText(contentImage.getSource().getName());
                    ((TextView) container.findViewById(R.id.tv_content_read)).setText(
                            MessageFormat.format("{0}阅读", contentImage.getReadCounts()));
                    ((TextView) container.findViewById(R.id.tv_content_time)).setText(contentImage.getUpdateTime());

                    Glide.with(InfoFlowActivity.this).load(contentImage.loadImage())
                            .into((ImageView) container.findViewById(R.id.iv_content_image));
                } else {
                    ContentVideo contentVideo = data.data.getContentVideo();
                    getLayoutInflater().inflate(R.layout.item_content_video, container, true);

                    ((TextView) container.findViewById(R.id.tv_content_title)).setText(contentVideo.getTitle());
                    ((TextView) container.findViewById(R.id.tv_video_duration)).setText(contentVideo.getDuration().toString());
                    ((TextView) container.findViewById(R.id.tv_content_source)).setText(contentVideo.getSource().getName());
                    ((TextView) container.findViewById(R.id.tv_content_time)).setText(contentVideo.getUpdateTime());
                    ((TextView) container.findViewById(R.id.tv_content_video_play_count)).setText(
                            MessageFormat.format("{0}次播放", contentVideo.getPlayCounts()));

                    Glide.with(InfoFlowActivity.this).load(contentVideo.loadImage())
                            .into((ImageView) container.findViewById(R.id.iv_content_video_picture));
                }
            }
        }
    }
}