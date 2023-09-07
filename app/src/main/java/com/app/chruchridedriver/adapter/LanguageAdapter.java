package com.app.chruchridedriver.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.chruchridedriver.R;
import com.app.chruchridedriver.data.model.LanguageCode;
import com.app.chruchridedriver.interfaces.ClickedAdapterInterface;

import java.util.List;

public class LanguageAdapter extends BaseAdapter {
    private final Context context;
    private final List<LanguageCode> codeList;
    ClickedAdapterInterface onLanguageChangeListener;

    public LanguageAdapter(Context context, List<LanguageCode> languageCodes, ClickedAdapterInterface onLanguageChangeListener) {
        this.context = context;
        this.codeList = languageCodes;
        this.onLanguageChangeListener = onLanguageChangeListener;
    }

    @Override
    public int getCount() {
        return codeList != null ? codeList.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        @SuppressLint("ViewHolder") View rootView = LayoutInflater.from(context).inflate(R.layout.item_language, viewGroup, false);

        TextView txtName = rootView.findViewById(R.id.name);
        ImageView image = rootView.findViewById(R.id.image);

        txtName.setText(codeList.get(i).getName());
        image.setImageResource(codeList.get(i).getImage());

        txtName.setOnClickListener(v -> onLanguageChangeListener.selectedValue(codeList.get(i).getName()));

        return rootView;
    }
}