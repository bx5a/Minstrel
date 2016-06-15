/*
 * Copyright Guillaume VINCKE 2016
 *
 * This file is part of Minstrel
 *
 * Minstrel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Minstrel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Minstrel.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bx5a.minstrel.widget;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.bx5a.minstrel.utils.SearchList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Widget that handles infinite scrolls.
 * An infinite scroll is a system that displays search results progressively when scroll reaches
 * bottom of list.
 * That class handles any adapter type and element
 */
abstract public class InfiniteScrollWidget<ElementType> {
    interface AdapterCreator<ElementType> {
        ArrayAdapter<ElementType> create(Context context, List<ElementType> elementList);
    }

    private AbsListView listView;
    private ProgressBar waitBar;
    private ArrayList<ElementType> elementList;
    private ArrayAdapter<ElementType> adapter;
    private AsyncTask updateTask;
    private SearchList<ElementType> searchList;

    public InfiniteScrollWidget(View parent,
                                int absListViewResourceId,
                                int waitBarResourceId,
                                AdapterCreator<ElementType> adapterCreator) {
        listView = (AbsListView) parent.findViewById(absListViewResourceId);
        waitBar = (ProgressBar) parent.findViewById(waitBarResourceId);
        elementList = new ArrayList<>();
        adapter = adapterCreator.create(parent.getContext(), elementList);
        listView.setAdapter(adapter);
        updateTask = null;

        initWaitBar();
        initListView();
    }

    /**
     * access the currently displayed element list
     * @return element list
     */
    protected List<ElementType> getElementList() {
        return elementList;
    }

    /**
     * that function is called when displayFirstPage is requested
     * @return the search list to be displayed
     */
    abstract protected SearchList<ElementType> getSearchList();

    /**
     * initialize and show the first few elements
     * that function get the search list from the abstract getSearchList function
     */
    protected void displayFirstPage() {
        if (isUpdating()) {
            updateTask.cancel(true);
        }
        InitTask task = new InitTask();
        task.execute();
        updateTask = task;
    }

    private void initWaitBar() {
        waitBar.setIndeterminate(true);
        waitBar.setVisibility(View.GONE);
    }

    private void initListView() {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // if view is empty, we don't even have the first page displayed. nothing to do
                if (totalItemCount == 0) {
                    return;
                }
                // if every item are shown and we are not already updating the list, load more
                if (firstVisibleItem + visibleItemCount == totalItemCount && !isUpdating()) {
                    displayNextPage();
                }
            }
        });
    }

    private boolean isUpdating() {
        return updateTask != null && updateTask.getStatus() != AsyncTask.Status.FINISHED;
    }

    private void displayNextPage() {
        if (isUpdating()) {
            updateTask.cancel(true);
        }
        if (!searchList.hasNextPage()) {
            return;
        }
        DisplayNextPageTask task = new DisplayNextPageTask();
        task.execute();
        updateTask = task;
    }

    // Async tasks
    private class InitTask extends AsyncTask<Void, Void, SearchList<ElementType>> {
        private boolean isCancelled;
        public InitTask() {
            isCancelled = false;
        }
        @Override
        protected SearchList<ElementType> doInBackground(Void... params) {
            if (isCancelled) {
                return null;
            }
            return getSearchList();
        }

        @Override
        protected void onPreExecute() {
            elementList.clear();
            adapter.notifyDataSetChanged();
            waitBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(SearchList<ElementType> result) {
            if (!isCancelled && result != null) {
                searchList = result;
                displayNextPage();
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            isCancelled = true;
            super.onCancelled();
        }
    }

    private class DisplayNextPageTask extends AsyncTask<Void, Void, List<ElementType>> {
        private boolean isCancelled;
        public DisplayNextPageTask() {
            isCancelled = false;
        }
        @Override
        protected List<ElementType> doInBackground(Void... params) {
            if (isCancelled) {
                return null;
            }
            try {
                return searchList.getNextPage();
            } catch (IOException e) {
                Timber.e(e, "Can't get next page");
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            waitBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<ElementType> elementTypes) {
            if (!isCancelled && elementTypes != null) {
                elementList.addAll(elementTypes);
                adapter.notifyDataSetChanged();
                waitBar.setVisibility(View.GONE);
            }
            super.onPostExecute(elementTypes);
        }

        @Override
        protected void onCancelled() {
            isCancelled = true;
            super.onCancelled();
        }
    }
}
