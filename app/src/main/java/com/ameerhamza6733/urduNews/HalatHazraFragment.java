package com.ameerhamza6733.urduNews;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by DELL 3542 on 7/19/2016.
 */
public class HalatHazraFragment extends Fragment {

    private static final String TAG = "HalatHazraFragment";
    private static final String TAG_LIFE = "RecyclerViewLifeCycle";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RecyclerView mRecyclerView;
    protected RecyclerAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected ArrayList<RssItem> mDataset = new ArrayList<>();
    protected Activity activity;

    private Elements metalinks;
    private String mImageURL;
    private Document documentImage;
    public View view;
    public static boolean isHalatHazraFragmentVisible;
    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LoadRssFeedsItems mLoadRssFeedsItems;


    private boolean IsTaskRun=true;
    private int indexofThePost;
    private String CurrentDay;
    private String mMints;
    private DateFormat df;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.my_recycler_view_fragment, container, false);


        // rootView.setTag(TAG);

        view = rootView;
        activity=getActivity();
        Log.i(TAG, "Inside onCreateView");

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.Progressbar);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });


        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());


        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        } else {
            mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
        }
        if (savedInstanceState != null) {
            mDataset = savedInstanceState.getParcelableArrayList(Constants.MY_DATA_SET_KEY);
            mAdapter = new RecyclerAdapter(mDataset);
            // Log.i(TAG, "m data set size in current affairs: " + mDataset.size());
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();

        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);


        return rootView;
    }


    private void refreshItems() {

        mDataset.clear();
        new LoadRssFeedsItems().execute(Constants.PAKISTAN);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);



        if (isVisibleToUser && mDataset.isEmpty()) {

                Log.i(TAG, "setUserVisibleHint");

            if(IsTaskRun)
                {
                    IsTaskRun=false;
                    mLoadRssFeedsItems= new LoadRssFeedsItems();
                    mLoadRssFeedsItems .execute(Constants.PAKISTAN);

                    Log.i(TAG, "start task setUserVisibleHint");

                }


        }else {
            if(mProgressBar!=null && !mDataset.isEmpty())
            {

                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }


    }

    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;


        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        savedInstanceState.putParcelableArrayList(Constants.MY_DATA_SET_KEY, mDataset);
        super.onSaveInstanceState(savedInstanceState);
    }


    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */


    public class LoadRssFeedsItems extends AsyncTask<String, Void, String> {
        private String mTitle, mDescription, mLink, mPubDate;

        private String mCategory;


        protected String doInBackground(String... urls) {
            try {
                Document rssDocument = Jsoup.connect(urls[0]).timeout(Constants.BIG_TIME_OUT).ignoreContentType(true).parser(Parser.xmlParser()).get();

                Elements mItems = rssDocument.select(Constants.ITEM);
                RssItem rssItem;
                int i = 0;
                for (Element element : mItems) {
                    i++;
                    mTitle = element.select("title").first().text();
                    mDescription = element.select("description").first().text();
                    mLink = element.select("link").first().text();
                    mPubDate = element.select("pubDate").first().text();
                    mCategory = element.select("Category").first().text();
                    mImageURL = element.select("media|content").attr("url").toString();



                    CurrentDay = mPubDate.substring(0, 3);
                    mMints = mPubDate.substring(16,22);

                    df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                    String date = df.format(Calendar.getInstance().getTime());
                    if(date.substring(0,3).equals(CurrentDay)) {
                        mPubDate=mPubDate.replace(mPubDate, "آج").concat(mMints);
                    }else {
                        mPubDate=mPubDate.substring(0,3).concat(mMints);
                    }

                    mDescription=Jsoup.parse(mDescription).text();
                    indexofThePost = mDescription.indexOf(Constants.THE_psot);

                    String mysubstring = mDescription.substring(indexofThePost, mDescription.length());
                    mDescription = mDescription.replace(mysubstring, "");
                    Log.i(TAG, "Item title in current affairs: " + (mTitle == null ? "N/A" : mTitle));
                    Log.i(TAG, "Item Description in current affairs: " + (mDescription == null ? "N/A" : mDescription));
                    Log.i(TAG, "Item link in  current affairs: " + (mLink == null ? "N/A" : mLink));
                    Log.i(TAG, "Item data in current affairs: " + (mPubDate == null ? "N/A" : mPubDate));
                    Log.i(TAG, "Item image link in current affairs: " + (mImageURL == null ? "N/A" : mImageURL));
                    Log.i(TAG, "int i : " + i);
                    rssItem = new RssItem(mTitle, mDescription, mPubDate, mLink, mCategory, i, mImageURL, getActivity(),activity);
                    mDataset.add(rssItem);

                }

                //            }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "d";
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            mProgressBar.setProgress(1);
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(String result) {
            if (mDataset.isEmpty()) try {


                IsTaskRun=true;

                Snackbar mSnackbar = Snackbar.make(view, "Unable to connect Current Affairs", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
//                                Snackbar.make(getView(), "CheckIn Cancelled", Snackbar.LENGTH_LONG).show();
                                new LoadRssFeedsItems().execute(Constants.PAKISTAN);


                            }
                        });
                mSnackbar.show();
            } catch (NullPointerException n) {
                n.printStackTrace();
            }
            else {
                mProgressBar.setVisibility(View.INVISIBLE);
                mSwipeRefreshLayout.setRefreshing(false);
                mAdapter = new RecyclerAdapter(mDataset);
                Log.i(TAG, "m data set size in current affairs: " + mDataset.size());
//        // Set CustomAdapter as the adapter for RecyclerView.
                mRecyclerView.setAdapter(mAdapter);


            }


        }
    }

}
