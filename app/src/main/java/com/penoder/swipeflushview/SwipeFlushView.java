package com.penoder.swipeflushview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
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

    private int mScaledTouchSlop;
    private View mFooterView;
    private ListView mListView;
    private OnLoadListener mOnLoadListener;
    private OnFlushListener mFlushListener;

    /**
     * 正在刷新状态
     */
    public boolean isFlushing;

    /**
     * 正在加载状态
     */
    public boolean isLoading;
    private boolean condition4 = false, condition5 = false;

    /**
     * 判断是否允许加载，默认允许
     */
    private boolean loadingEnabled = true;

    /**
     * 设置是否允许控件刷新，默认允许
     */
    private boolean flushingEnabled = true;

    /**
     * 判断加载的效果，为 true 表示内容滑到底部，自动弹出 FooterView 进行数据加载；
     * 为 false 表示内容滑到底部后，还需要手动上滑一次 才能显示 FooterView 加载数据
     */
    private boolean autoLoading;

    public SwipeFlushView(Context context) {
        this(context, null);
    }

    public SwipeFlushView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    /**
     * 初始化控件
     *
     * @param context
     * @param attrs
     */
    private void initView(Context context, AttributeSet attrs) {
        // 填充底部加载布局
        mFooterView = View.inflate(context, R.layout.view_footer, null);
        // 表示控件移动的最小距离，手移动的距离大于这个距离才能拖动控件
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeFlushView);
        if (typedArray != null) {
            boolean flushingEnabled = typedArray.getBoolean(R.styleable.SwipeFlushView_flushingEnable, true);
            loadingEnabled = typedArray.getBoolean(R.styleable.SwipeFlushView_loadingEnable, true);
            boolean autoFlushing = typedArray.getBoolean(R.styleable.SwipeFlushView_autoFlushing, false);
            autoLoading = typedArray.getBoolean(R.styleable.SwipeFlushView_autoLoading, false);
            setCanFlushing(flushingEnabled);
            setCanLoading(loadingEnabled);
            if (autoFlushing) {
                autoFlushing();
            }
        }
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
            // 移动过程中判断时候能下拉加载更多
            case MotionEvent.ACTION_MOVE:
                // 移动的终点
                mUpY = ev.getY();
                if (canLoadMore() && !autoLoading) {
                    // 手动滑出 FooterView 加载数据
                    loadData();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        // 如果不允许下拉刷新，根本就不要执行下面了
        if (flushingEnabled) {
            // 加载的时候，设置该控件不可用，则加载的时候不能刷新
            if (isLoading) {
                setEnabled(false);
            } else {
                setEnabled(true);
            }
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
        boolean condition3 = !isFlushing;

        return condition1 && condition2 && condition3 && condition4 && condition5 && loadingEnabled;
    }

    /**
     * 处理加载数据的逻辑
     */
    private void loadData() {
        // 设置加载状态，让布局显示出来
        setLoading(true);
        if (mOnLoadListener != null) {
            new Handler().postDelayed(() -> mOnLoadListener.onLoad(), 3000);
        } else {    // 未设置加载监听事件的话，实现转2秒后结束的效果
            new Handler().postDelayed(() -> setLoading(false), 2000);
        }

    }

    /**
     * 设置自动刷新，如果只是单纯的调用 setFlushing() 方法没有效果
     */
    public void autoFlushing() {
        new Handler().post(() -> setFlushing(true));
    }

    /**
     * 用于设置上拉加载的模式，是将内容滑动底部后自动滑出 FooterView 直接加载，
     * 还是先滑动到最底部，然后再手动的上拉一次才能加载？
     *
     * @param autoLoading
     */
    public void setAutoLoading(boolean autoLoading) {
        this.autoLoading = autoLoading;
    }

    /**
     * 设置刷新状态，是否刷新有参数 flushing 控制
     *
     * @param flushing
     */
    public void setFlushing(boolean flushing) {
        isFlushing = flushing;
        setRefreshing(flushing);
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
            if (mListView != null) {
                // 添加布局并且显示出来
                mListView.addFooterView(mFooterView);
                if (mListView.getAdapter() != null) {
                    // 用于上面添加完 FooterView 之后，将 其 滑动出屏幕
                    mListView.smoothScrollToPosition(mListView.getAdapter().getCount() - 1);
                }
            }
        } else {
            // 隐藏布局
            if (mListView != null && mListView.getFooterViewsCount() > 0) {
                mListView.removeFooterView(mFooterView);
            }

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
                if (canLoadMore() && autoLoading) {
                    // 自动滑出 FooterView 加载数据
                    loadData();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 每次滚动开始 重置 条件为 false
                condition4 = false;
                condition5 = false;

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
            setOnRefreshListener(() -> {
                setFlushing(true);
                if (mFlushListener != null) {
                    mFlushListener.onFlush();
                } else {    // 未设置刷新监听事件的话，实现转2秒后结束的效果
                    new Handler().postDelayed(() -> setFlushing(false), 2000);
                }
            });
        } else {
            // 加载的时候不让刷新
            setFlushing(false);
        }
    }

    /**
     * 设置是否可以下拉刷新
     *
     * @param enabled
     */
    public void setCanFlushing(boolean enabled) {
        flushingEnabled = enabled;
        setEnabled(enabled);
    }

    /**
     * 设置是否可以上拉加载
     *
     * @param enabled
     */
    public void setCanLoading(boolean enabled) {
        loadingEnabled = enabled;
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