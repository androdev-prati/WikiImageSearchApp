package com.assignment.wiki.imagesearch;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ImageSearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final int MIN_SEARCH_CHAR = 2;
    private static final String BASE_URL = "https://en.wikipedia.org/w/api.php?%20" +
            "action=query&prop=pageimages&format=json&piprop=thumbnail&pithumbsize=50" +
            "&%20pilimit=50&generator=prefixsearch&gpssearch=";
    private static final String[] CURSON_COLUMNS = new String[]{"_id", "icon_url", "title"};
    private SearchView mSearchView;
    private ListView mSearchResultList;
    ImageSearchAdapter mSearchAdapter;
    private RequestQueue mVolleyRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_search);
        mVolleyRequestQueue = Volley.newRequestQueue(this);

        mSearchView = (SearchView) findViewById(R.id.search_view);
        mSearchResultList = (ListView) findViewById(R.id.result_list);
        mSearchAdapter = new ImageSearchAdapter(this, R.layout.search_result_item,
                null, CURSON_COLUMNS,null,-1000, mVolleyRequestQueue);
        mSearchView.setOnQueryTextListener(this);
        mSearchResultList.setAdapter(mSearchAdapter);


    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!query.isEmpty() && query.length() > MIN_SEARCH_CHAR) {
            searchQuery(query);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!newText.isEmpty() && newText.length() > MIN_SEARCH_CHAR) {
            searchQuery(newText);
        }
        return true;
    }

    private void searchQuery(String queryText) {

        String requestUrl = BASE_URL + queryText;

        JsonObjectRequest searchQueryReuest = new JsonObjectRequest(Request.Method.GET,
                requestUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Cursor responseCursor = parseResponse(response);
                mSearchAdapter.changeCursor(responseCursor);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mVolleyRequestQueue.add(searchQueryReuest);
    }

    private Cursor parseResponse(JSONObject response) {
        ArrayList<SearchItem> resultList = null;
        try {
            JSONObject query;
            if (response.has("query")) {
                query = response.getJSONObject("query");
                if (query.has("pages")) {
                    resultList = new ArrayList<>();
                    JSONObject pages = query.getJSONObject("pages");
                    Iterator<String> ids = pages.keys();
                    while (ids.hasNext()) {
                        String pageId = ids.next();
                        JSONObject searchObj = pages.getJSONObject(pageId);
                        SearchItem item = new SearchItem();
                        item.setPageID(pageId);
                        if (searchObj.has("title"))
                            item.setTitle(searchObj.getString("title"));
                        if (searchObj.has("thumbnail") && searchObj.getJSONObject("thumbnail").has("source"))
                            item.setUrl(searchObj.getJSONObject("thumbnail").getString("source"));
                        resultList.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (resultList != null) {
            return convertToCursor(resultList);
        } else
        return null;
    }

    private MatrixCursor convertToCursor(ArrayList<SearchItem> resultList) {
        MatrixCursor cursor = new MatrixCursor(CURSON_COLUMNS);
        for (SearchItem item : resultList) {
            String[] rowItem = new String[3];
            rowItem[0] = item.getPageID();
            rowItem[1] = item.getUrl();
            rowItem[2] = item.getTitle();
            cursor.addRow(rowItem);
        }
        return cursor;
    }
}
