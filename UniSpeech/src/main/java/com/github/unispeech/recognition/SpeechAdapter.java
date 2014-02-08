package com.github.unispeech.recognition;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.unispeech.R;

import java.util.List;

/**
* Created by javier.romero on 2/8/14.
*/
public class SpeechAdapter extends ArrayAdapter<SpeechData> {

    private final LayoutInflater mLayoutInflater;

    public SpeechAdapter(Context context, List<SpeechData> speechDataList) {
        super(context, 0, speechDataList);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SpeechData data = getItem(position);

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_speech, null);
            convertView.setTag(new ViewHolder(convertView));
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (data.isTranslated()) {
            holder.text.setText(data.getTranslatedText());
            holder.text.setTextColor(getContext().getResources().getColor(R.color.white));
            holder.text.setTypeface(null, Typeface.NORMAL);
        } else {
            holder.text.setText(data.getOriginalText());
            holder.text.setTextColor(getContext().getResources().getColor(R.color.darkest_grey));
            holder.text.setTypeface(null, Typeface.ITALIC);
        }

        return convertView;
    }

    private static class ViewHolder {
        public final TextView text;

        public ViewHolder(View view) {
            text = (TextView) view.findViewById(R.id.lbl_speech);
        }
    }
}
