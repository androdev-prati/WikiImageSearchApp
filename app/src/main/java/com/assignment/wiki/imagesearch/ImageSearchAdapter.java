package com.assignment.wiki.imagesearch;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;

/**
 * Created by Pratibha on 5/11/2016.
 */
@SuppressWarnings("deprecation")
public class ImageSearchAdapter extends SimpleCursorAdapter {

    RequestQueue mVolleyRequestQueue;
    public ImageSearchAdapter(Context context, int layout, Cursor c,
                              String[] from, int[] to, int flags, RequestQueue mVolleyRequestQueue) {
        super(context, layout, c, from, to, flags);
        this.mVolleyRequestQueue = mVolleyRequestQueue;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView icon = (ImageView) view.findViewById(R.id.search_result_icon);
        icon.setTag(cursor.getString(2));
        loadImage(icon, cursor.getString(2), cursor.getString(1));
        ((TextView) view.findViewById(R.id.search_title)).setText(cursor.getString(2));
    }

    private void loadImage(final ImageView icon, final String tag, String url) {
        ImageRequest imageRequest = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        if (icon.getTag().toString().equals(tag))
                            icon.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        icon.setImageResource(R.mipmap.ic_launcher);
                    }
                });
        mVolleyRequestQueue.add(imageRequest);
    }
}
