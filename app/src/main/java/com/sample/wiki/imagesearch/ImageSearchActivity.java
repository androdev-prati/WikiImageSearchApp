package com.sample.wiki.imagesearch;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

/** The class that handles creation of android SearchView. We use the CursorAdapter and
 * extend it to bindviews as per our requirement. The Cursor currently handles only few
 * details such as search result item's title and thumbnail image.
 *
 * We have used android's Volley library to perform network operation. Volley comes with
 * internal request queue handler and image loader with caching. Volley in this sample
 * is used for two basic operation: 1. Fetch search results and 2. load thumbnail images.
 * @author Pratibha
 */
public class ImageSearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        SearchView.OnCloseListener, AdapterView.OnItemClickListener {

    private static final int MIN_SEARCH_CHAR = 1;
    private static final String TAG_QUERY = "query";
    private static final String TAG_PAGES = "pages";
    private static final String TAG_TITLE = "title";
    private static final String TAG_THUMB = "thumbnail";
    private static final String TAG_SRC = "source";

    private static final String BASE_URL = "https://en.wikipedia.org/w/api.php?%20" +
            "action=query&prop=pageimages&format=json&piprop=thumbnail&pithumbsize=300" +
            "&%20pilimit=50&generator=prefixsearch&gpslimit=50&gpssearch=";
    private static final String[] CURSON_COLUMNS = new String[]{"_id", "icon_url", "title"};

    private SearchView mSearchView;
    private ListView mSearchResultList;
    private TextView mListEmptyView;
    private ImageSearchAdapter mSearchAdapter;
    private RequestQueue mVolleyRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_search);
        mVolleyRequestQueue = Volley.newRequestQueue(this);

        mSearchView = (SearchView) findViewById(R.id.search_view);
        mSearchResultList = (ListView) findViewById(R.id.result_list);
        mSearchAdapter = new ImageSearchAdapter(this, R.layout.search_result_item,
                null, CURSON_COLUMNS, null, 0, mVolleyRequestQueue);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setIconified(false);
        mSearchView.setIconifiedByDefault(false);
        mSearchResultList.setAdapter(mSearchAdapter);
        mListEmptyView = (TextView) findViewById(R.id.empty_view);
        mSearchResultList.setEmptyView(mListEmptyView);
        mSearchResultList.setOnItemClickListener(this);
    }

    /**
     * Listener to observe the searchview's input submission.
     *
     * @param query The string in the text view on submit of search request
     * @return true as the event is handled by us.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!query.isEmpty() && query.length() > MIN_SEARCH_CHAR) {
            searchQuery(query);
        } else {
            mSearchAdapter.changeCursor(null);
            mSearchAdapter.notifyDataSetChanged();
            mListEmptyView.setText("");
        }
        return true;
    }

    /**
     * Listener that gets triggered on the searchview's input text changes.
     *
     * @param newText The string in the text view at the instance of change event triggered
     * @return true as the event is handled by us.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if (!newText.isEmpty() && newText.length() > MIN_SEARCH_CHAR) {
            searchQuery(newText);
        } else {
            mSearchAdapter.changeCursor(null);
            mSearchAdapter.notifyDataSetChanged();
            mListEmptyView.setText("");
        }
        return true;
    }

    @Override
    public boolean onClose() {
        mSearchResultList.removeAllViews();
        mListEmptyView.setText("");
        return true;
    }

    /**
     * searchQuery makes a call to the required API to get the result list for the
     * given query.
     * We create a volley JsonObjectRequest with the API url. Success and Error
     * listeners are registered to receive the response of the network call.
     * OnSuccess we parse the json response received to form our data models and
     * refresh the result list.
     * OnError we display approriate message to user.
     *
     * @param queryText the query string to search in wikipedia.
     *
     */
    private void searchQuery(String queryText) {
        String requestUrl = BASE_URL + queryText;
        JsonObjectRequest searchQueryReuest = new JsonObjectRequest(Request.Method.GET,
                requestUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Cursor responseCursor = parseResponse(response);
                if (responseCursor != null && responseCursor.getCount() > 0) {
                    mSearchAdapter.changeCursor(responseCursor);
                    mSearchAdapter.notifyDataSetChanged();
                } else {
                    mSearchAdapter.changeCursor(null);
                    mSearchAdapter.notifyDataSetChanged();
                    mListEmptyView.setText(R.string.empty_result);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSearchAdapter.changeCursor(null);
                mSearchAdapter.notifyDataSetChanged();
                mListEmptyView.setText(R.string.empty_result);
            }
        });
        mVolleyRequestQueue.add(searchQueryReuest);
    }

    /**
     * A json parser to parse the response received and generate a cursor.
     *
     * Note: The method can be furthur optimized by removing the data model and
     * creating the directly our of the json data being parsed.
     * @param response Json response received from the API call.
     * @return cursor that contains the data to be populated on the list.
     */
    private Cursor parseResponse(JSONObject response) {
        ArrayList<SearchItem> resultList = null;
        try {
            JSONObject query;
            if (response.has(TAG_QUERY)) {
                query = response.getJSONObject(TAG_QUERY);
                if (query.has(TAG_PAGES)) {
                    resultList = new ArrayList<>();
                    JSONObject pages = query.getJSONObject(TAG_PAGES);
                    Iterator<String> ids = pages.keys();
                    while (ids.hasNext()) {
                        String pageId = ids.next();
                        JSONObject searchObj = pages.getJSONObject(pageId);
                        SearchItem item = new SearchItem();
                        item.setPageID(pageId);
                        if (searchObj.has(TAG_TITLE))
                            item.setTitle(searchObj.getString(TAG_TITLE));
                        if (searchObj.has(TAG_THUMB) && searchObj.getJSONObject(TAG_THUMB).has(TAG_SRC))
                            item.setUrl(searchObj.getJSONObject(TAG_THUMB).getString(TAG_SRC));
                        resultList.add(item);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            resultList = null; // make the list null to avoid cursor creation.
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (view != null) {
            //String title = ((TextView) view.findViewById(R.id.search_title)).getText().toString();
            Intent intent = new Intent(ImageSearchActivity.this, ResultPageActivity.class);
            if (view.getTag() != null && !view.getTag().toString().isEmpty()) {
                intent.putExtra(TAG_TITLE, view.getTag().toString());
                startActivity(intent);
            } else {
                Toast.makeText(ImageSearchActivity.this, getResources().getString(R.string.no_image),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
