package com.foodfriend.selecta;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Dan on 06/04/2018.
 */

public class MessageListAdapter extends ArrayAdapter<Message> {

    ArrayList<Message> messages;
    Context context;
    int resource;

    public MessageListAdapter(Context context, int resource, ArrayList<Message> messages) {
        super(context, resource, messages);

        this.messages = messages;
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            convertView = layoutInflater.inflate(R.layout.list_layout, null, true);

        }
        Message message = getItem(position);

        //set list item image by getting image url from Match object, using Picasso
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageUser);

        if(!TextUtils.isEmpty(message.getTime()))
        {
            Picasso.with(context).load(message.getTime()).into(imageView);
        }
        if(TextUtils.isEmpty(message.getTime()))
        {
            //use default image if user has no image
            Picasso.with(context).load(R.drawable.profileimage).into(imageView);
        }


        //set list item name text from Message
        TextView name = (TextView) convertView.findViewById(R.id.textName);
        name.setText(message.getUsername());

        //set list item place of interest text from Message
        TextView content = (TextView) convertView.findViewById(R.id.textPOI);
        content.setText(message.getContent());

        TextView empty = (TextView) convertView.findViewById(R.id.textTime);
        empty.setText("");



        return convertView;
    }
}
