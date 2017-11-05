package com.penoder.swipeflushview;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * 自定义View继承SwipeRefreshLayout，添加上拉加载更多的布局属性
 * 修改自 http://www.jianshu.com/u/64f479a1cef7
 *
 * @author Penoder
 * @date 2017/04/30
 */

public class SwipeFlushView extends SwipeRefreshLayout {

    private final int mScaledTouchSlop;
    private final View mFooterView;
    private ListView mListView;
    private OnLoadListener mOnLoadListener;
    private OnFlushListener mFlushListener;

    /**
     * 正在加载状态
     */
    public boolean isLoading;
    private boolean condition4 = false, condition5 = false;

    public SwipeFlushView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 填充底部加载布局
        mFooterView = View.inflate(context, R.layout.view_footer, null);

        // 表示控件移动的最小距离，手移动的距离大于这个距离才能拖动控件
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 获取ListView,设置ListView的布局位置
        if (mListView == null) {
            // 判断容器有多少个孩子
            if (getChildCount() > 0) {
                // 判断第一个孩子是不是ListView
                if (getChildAt(0) instanceof ListView) {
                    // 创建ListView对象
                    mListView = (ListView) getChildAt(0);

                    // 设置ListView的滑动监听
                    setListViewOnScroll();
                }
            }
            setOnRefresh();
        }
    }

    /**
     * 在分发事件的时候处理子控件的触摸事件
     *
     * @param ev
     * @return
     */
    private float mDownY, mUpY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 移动的起点
                mDownY = ev.getY();
                break;
            // 用滑动事件来判断是否可以加载不妥，松手后因为惯性继续滑动是不再执行该事件的ACTION_MOVE的
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                // 移动的终点
                mUpY = getY();
                break;
            default:
                break;
        }
        // 加载的时候，设置该控件不可用，则加载的时候不能刷新
        if (isLoading) {
            setEnabled(false);
        } else {
            setEnabled(true);
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判断是否满足加载更多条件
     *
     * @return
     */
    private boolean canLoadMore() {
        // 1. 是上拉状态
        boolean condition1 = (mDownY - mUpY) >= mScaledTouchSlop;

        // 2. 正在加载时不能继续上拉加载
        boolean condition2 = !isLoading;

        // 3，正在刷新时不能加载
        boolean condition3 = false;
        if (!isRefreshing()) {
            condition3 = true;
        }
        return condition1 && condition2 && condition3 && condition4 && condition5;
    }

    /**
     * 处理加载数据的逻辑
     */
    private void loadData() {
        if (mOnLoadListener != null) {
            // 设置加载状态，让布局显示出来
            setLoading(true);
            // 如果要看清楚效果可以加上延时
            mOnLoadListener.onLoad();
        }

    }

    /**
     * 设置加载状态，是否加载传入boolean值进行判断
     *
     * @param loading
     */
    public void setLoading(boolean loading) {
        // 修改当前的状态
        isLoading = loading;
        if (isLoading) {
            // 添加布局并且显示出来
            mListView.addFooterView(mFooterView);
            if (mListView.getAdapter() != null) {
                // 用于上面添加完 FooterView 之后，将 其 滑动出屏幕
                mListView.smoothScrollToPosition(mListView.getAdapter().getCount() - 1);
            }
        } else {
            // 隐藏布局
            mListView.removeFooterView(mFooterView);

            // 重置滑动的坐标
            mDownY = 0;
            mUpY = 0;
        }
    }

    /**
     * 设置ListView的滑动监听
     */
    private void setListViewOnScroll() {

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 移动过程中判断时候能下拉加载更多
                if (canLoadMore()) {
                    // 加载数据
                    loadData();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 每次滚动开始 重置 条件为 false
                condition4 = false;
                condition5 = false;
                /*
                用于 判断 ListView 滑动到 最后一条 的 底部,
                http://blog.csdn.net/wangbaochu/article/details/50630371
                 */
                if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
                    View lastVisibleItemView = mListView.getChildAt(mListView.getChildCount() - 1);
                    if (lastVisibleItemView != null && lastVisibleItemView.getBottom() == mListView.getHeight()) {
                        condition4 = true;
                    }
                }

                if (totalItemCount > visibleItemCount) {
                    // 是否可以加载 条件5，ListView的数据量超过一屏幕
                    condition5 = true;
                }
            }
        });
    }

    /**
     * 设置刷新事件
     */
    private void setOnRefresh() {
        if (!isLoading) {
            setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (mFlushListener != null) {
                        // 如果要看清楚效果可以加上延时
                        mFlushListener.onFlush();
                    }
                }
            });
        } else {
            // 加载的时候不让刷新
            setRefreshing(false);
        }
    }

    /**
     * 上拉加载的接口回调
     */
    public interface OnLoadListener {
        void onLoad();
    }

    /**
     * 下拉刷新的接口回调
     */
    public interface OnFlushListener {
        void onFlush();
    }

    /**
     * 设置 加载事件监听器
     *
     * @param listener
     */
    public void setOnLoadListener(OnLoadListener listener) {
        this.mOnLoadListener = listener;
    }

    /**
     * 设置 刷新事件监听器
     *
     * @param listener
     */
    public void setOnFlushListener(OnFlushListener listener) {
        mFlushListener = listener;
    }
}