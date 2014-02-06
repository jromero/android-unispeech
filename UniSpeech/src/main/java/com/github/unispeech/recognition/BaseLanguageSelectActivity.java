package com.github.unispeech.recognition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.unispeech.R;

import me.jromero.accessability.widget.GravityListView;

public abstract class BaseLanguageSelectActivity extends Activity {

    private GravityListView mListView;

    protected abstract String getPrompt();

    protected abstract void onLanguageClicked(SupportedSttLanguage language);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_select);

        ((TextView) findViewById(R.id.lbl_prompt)).setText(getPrompt());

        mListView = (GravityListView) findViewById(R.id.list);
        mListView.setAdapter(new LanguagesAdapter(this, SupportedSttLanguage.values()));
        mListView.setOnItemClickListener(mOnItemClickListener);

        mListView.onCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mListView.onResume();
    }

    @Override
    protected void onPause() {
        mListView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mListView.onDestroy();
        super.onDestroy();
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SupportedSttLanguage language = ((LanguagesAdapter) mListView.getAdapter()).getItem(position);
            onLanguageClicked(language);
        }
    };

    private static final class LanguagesAdapter extends ArrayAdapter<SupportedSttLanguage> {

        private final LayoutInflater mLayoutInflater;

        public LanguagesAdapter(Context context, SupportedSttLanguage[] languages) {
            super(context, R.layout.item_language, languages);
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_language, null);
                convertView.setTag(new ViewHolder(convertView));
            }

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.language.setText(getItem(position).getLabel());

            return convertView;
        }
    }

    public static final class ViewHolder {
        public final TextView language;

        public ViewHolder(View view) {
            language = (TextView) view.findViewById(R.id.lbl_language);
        }
    }
}