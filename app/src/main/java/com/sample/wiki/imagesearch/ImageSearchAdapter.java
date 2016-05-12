package com.sample.wiki.imagesearch;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**Adapter that binds the cursor contents to the views in the list.
 * @author Pratibha
 */
@SuppressWarnings("deprecation")
public class ImageSearchAdapter extends SimpleCursorAdapter {

    public ImageSearchAdapter(Context context, int layout, Cursor c,
                              String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView icon = (ImageView) view.findViewById(R.id.search_result_icon);
        icon.setTag(cursor.getString(2));
        Picasso.with(context).load(cursor.getString(1)).placeholder(R.mipmap.ic_launcher).into(icon);
        ((TextView) view.findViewById(R.id.search_title)).setText(cursor.getString(2));
        view.setTag(cursor.getString(1));
    }
}
