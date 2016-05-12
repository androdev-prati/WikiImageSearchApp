package com.sample.wiki.imagesearch;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

/**Adapter that binds the cursor contents to the views in the list.
 * @author Pratibha
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
        Picasso.with(context).load(cursor.getString(1)).placeholder(R.mipmap.ic_launcher).into(icon);
        ((TextView) view.findViewById(R.id.search_title)).setText(cursor.getString(2));
        view.setTag(cursor.getString(1));
    }

    /**
     * Creates a Volley ImageRequest to download image and load the thumbnail.
     * @param icon View which needs to be updated with the image
     * @param tag Tag to avoid reused views getting loaded with older thumbnails
     *            due to request time gap.
     * @param url Image url to load same.
     * @param context
     */
    private void loadImage(final ImageView icon, final String tag, final String url, final Context context) {
        ImageRequest imageRequest = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        if (icon.getTag().toString().equals(tag)) {
                            Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale_anim);
                            icon.setImageBitmap(bitmap);
                            icon.startAnimation(anim);
                        }
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale_anim);
                        icon.setImageResource(R.mipmap.ic_launcher);
                        icon.startAnimation(anim);
                    }
                });
        mVolleyRequestQueue.add(imageRequest);
    }
}
