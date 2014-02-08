package com.github.unispeech.recognition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.unispeech.R;

public abstract class BaseLanguageSelectActivity extends Activity {

    private ListView mListView;

    protected abstract String getPrompt();

    protected abstract void onLanguageClicked(SupportedSttLanguage language);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_select);

        ((TextView) findViewById(R.id.lbl_prompt)).setText(getPrompt());

        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(new LanguagesAdapter(this, SupportedSttLanguage.values()));
        mListView.setOnItemClickListener(mOnItemClickListener);
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

            SupportedSttLanguage language = getItem(position);

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.language.setText(language.getLabel());
            if (!TextUtils.isEmpty(language.getSpecifier())) {
                viewHolder.specifier.setText("(" +language.getSpecifier() + ")");
                viewHolder.specifier.setVisibility(View.VISIBLE);
            } else {
                viewHolder.specifier.setVisibility(View.GONE);
            }

            return convertView;
        }
    }

    public static final class ViewHolder {
        public final TextView language;
        public final TextView specifier;

        public ViewHolder(View view) {
            language = (TextView) view.findViewById(R.id.lbl_language);
            specifier = (TextView) view.findViewById(R.id.lbl_language_specifier);
        }
    }
}