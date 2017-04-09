/*
 * Copyright 2017 GcsSloop
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified 2017-04-09 05:15:40
 *
 * GitHub:  https://github.com/GcsSloop
 * Website: http://www.gcssloop.com
 * Weibo:   http://weibo.com/GcsSloop
 */

package com.gcssloop.diycode.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gcssloop.diycode.base.recyclerview.SpeedyLinearLayoutManager;
import com.gcssloop.diycode.fragment.provider.NewsProvider;
import com.gcssloop.diycode_sdk.api.news.bean.New;
import com.gcssloop.diycode_sdk.api.news.event.GetNewsListEvent;
import com.gcssloop.diycode_sdk.log.Logger;
import com.gcssloop.recyclerview.adapter.multitype.HeaderFooterAdapter;

import java.util.List;

public class NewsListFragment2 extends RefreshRecyclerFragment<New, GetNewsListEvent> {

    private boolean isFirstLaunch = true;

    public static NewsListFragment2 newInstance() {
        Bundle args = new Bundle();
        NewsListFragment2 fragment = new NewsListFragment2();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void initData(HeaderFooterAdapter adapter) {
        List<Object> news = mDataCache.getNewsListObj();
        if (null != news && news.size() > 0) {
            Logger.e("news : " + news.size());
            pageIndex = mConfig.getNewsListPageIndex();
            adapter.addDatas(news);
            if (isFirstLaunch) {
                int lastPosition = mConfig.getNewsListLastPosition();
                mRecyclerView.getLayoutManager().scrollToPosition(lastPosition);
                isFirstAddFooter = false;
                isFirstLaunch = false;
            }
        } else {
            loadMore();
        }
    }

    @Override protected void setRecyclerViewAdapter(Context context, RecyclerView recyclerView,
                                                    HeaderFooterAdapter adapter) {
        adapter.register(New.class, new NewsProvider(getContext()));
    }

    @NonNull @Override protected RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new SpeedyLinearLayoutManager(getContext());
    }

    @NonNull @Override protected String request(int offset, int limit) {
        return mDiycode.getNewsList(null, offset,limit);
    }

    @Override protected void onRefresh(GetNewsListEvent event, HeaderFooterAdapter adapter) {
        adapter.clearDatas();
        adapter.addDatas(event.getBean());
        toast("下拉刷新成功");
        mDataCache.saveNewsListObj(adapter.getDatas());
    }

    @Override protected void onLoadMore(GetNewsListEvent event, HeaderFooterAdapter adapter) {
        // TODO 排除重复数据
        adapter.addDatas(event.getBean());
        toast("加载更多成功");
        mDataCache.saveNewsListObj(adapter.getDatas());
    }

    @Override protected void onError(GetNewsListEvent event, String postType) {
        if (postType.equals(POST_LOAD_MORE)) {
            toast("加载更多失败");
        } else if (postType.equals(POST_REFRESH)) {
            toast("刷新数据失败");
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        // 存储 PageIndex
        mConfig.saveNewsListPageIndex(pageIndex);
        // 存储 RecyclerView 滚动位置
        View view = mRecyclerView.getLayoutManager().getChildAt(0);
        int lastPosition = mRecyclerView.getLayoutManager().getPosition(view);
        mConfig.saveNewsListPosition(lastPosition);
    }
}