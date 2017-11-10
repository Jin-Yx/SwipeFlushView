package com.penoder.swipeflushview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Penoder
 * @date 2017/00/00
 */
public class MainActivity extends AppCompatActivity {

    private SwipeFlushView swipeFlushView;
    private MyAdapter adapter;

    private MyHandler mHandler;

    private List<EntityInfo.ResultBean.ListBean> dataList = new ArrayList<>();

    private int pageNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new MyHandler(this);

        swipeFlushView = (SwipeFlushView) findViewById(R.id.swipeFlushView);
        ListView listView = (ListView) findViewById(R.id.listView);
        adapter = new MyAdapter(dataList);
        listView.setAdapter(adapter);

//        swipeFlushView.autoFlushing();
//        swipeFlushView.setAutoLoading(false);
//        swipeFlushView.setCanFlushing(false);
//        swipeFlushView.setCanLoading(true);

        // 刷新事件
        swipeFlushView.setOnFlushListener(() -> {
            pageNum = 1;
            getDataList();
        });

        // 加载事件
        swipeFlushView.setOnLoadListener(() -> {
            pageNum++;
            getDataList();
        });

        pageNum = 1;
        getDataList();
    }

    private void getDataList() {
        OkHttpClient client = new OkHttpClient();
        // 聚合数据
        String apiUrl = "http://v.juhe.cn/weixin/query?ps=10&dtype=&key=bf1eeb6862649b12c07feca7436adcf9&pno=";
        Request request = new Request.Builder().url(apiUrl + pageNum).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.sendEmptyMessage(-1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                if (!TextUtils.isEmpty(result)) {
                    Gson gson = new Gson();
                    EntityInfo entityInfo = gson.fromJson(result, EntityInfo.class);
                    if (entityInfo != null && entityInfo.getError_code() == 0 && entityInfo.getResult() != null) {
                        EntityInfo.ResultBean resultBean = entityInfo.getResult();
                        if (resultBean.getList() != null && resultBean.getList().size() > 0) {
                            if (pageNum == 1) {
                                dataList.clear();
                            }
                            dataList.addAll(resultBean.getList());
                            if (pageNum == 1) {
                                mHandler.sendEmptyMessage(1);
                            } else {
                                mHandler.sendEmptyMessage(2);
                            }

                        } else {
                            mHandler.sendEmptyMessage(0);
                        }
                    } else {
                        mHandler.sendEmptyMessage(3);
                    }
                } else {
                    mHandler.sendEmptyMessage(3);
                }
            }
        });
    }

    static class MyHandler extends Handler {
        private WeakReference<MainActivity> weakReference;

        MyHandler(MainActivity activity) {
            weakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case -1:
                    weakReference.get().swipeFlushView.setFlushing(false);
                    weakReference.get().swipeFlushView.setLoading(false);
                    Toast.makeText(weakReference.get(), "获取数据失败！", Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    weakReference.get().swipeFlushView.setFlushing(false);
                    weakReference.get().swipeFlushView.setLoading(false);
                    Toast.makeText(weakReference.get(), "没有更多的数据！", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    weakReference.get().adapter.notifyDataSetChanged();
                    weakReference.get().swipeFlushView.setFlushing(false);
                    break;
                case 2:
                    weakReference.get().adapter.notifyDataSetChanged();
                    weakReference.get().swipeFlushView.setLoading(false);
                    break;
                case 3:
                    if (weakReference.get().pageNum == 1) {
                        weakReference.get().swipeFlushView.setFlushing(false);
                        Toast.makeText(weakReference.get(), "刷新失败！", Toast.LENGTH_SHORT).show();
                    } else if (weakReference.get().pageNum > 1) {
                        weakReference.get().swipeFlushView.setLoading(false);
                        Toast.makeText(weakReference.get(), "加载失败！", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    class MyAdapter extends BaseAdapter {

        private List<EntityInfo.ResultBean.ListBean> dataList = new ArrayList();

        MyAdapter(List<EntityInfo.ResultBean.ListBean> dataList) {
            this.dataList = dataList;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int i) {
            return dataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                holder = new ViewHolder();
                view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_view, viewGroup, false);

                holder.txtTitle = (TextView) view.findViewById(R.id.txt_title);
                holder.imgPic = (ImageView) view.findViewById(R.id.img_Pic);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.txtTitle.setText(dataList.get(i).getTitle());
            // 给 ImageView 打标记，避免图片复用问题
            String imgUrl = TextUtils.isEmpty(dataList.get(i).getFirstImg()) ? "http://img5.imgtn.bdimg.com/it/u=4004044022,1540768321&fm=27&gp=0.jpg" : dataList.get(i).getFirstImg();
            if (!(dataList.get(i).getUrl() + i).equals(holder.imgPic.getTag(R.id.img_Pic))) {
                Picasso.with(MainActivity.this).load(imgUrl).into(holder.imgPic);
                holder.imgPic.setTag(R.id.img_Pic, dataList.get(i).getUrl() + i);
            }

            return view;
        }

        class ViewHolder {
            private TextView txtTitle;
            private ImageView imgPic;
        }
    }
}
