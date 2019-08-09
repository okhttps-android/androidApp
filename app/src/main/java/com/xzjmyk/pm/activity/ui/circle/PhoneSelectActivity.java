package com.xzjmyk.pm.activity.ui.circle;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.xzjmyk.pm.activity.R;

import java.util.ArrayList;

public class PhoneSelectActivity extends AppCompatActivity {
    private ArrayList<Photo> photos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_select);
        init();
        initView();
    }

    private void initView() {


    }

    private void init() {


    }


    class GridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public Object getItem(int i) {
            return photos.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            return null;
        }

        class VoiewHolder {


        }
    }

    class Photo {
        public String path;
        public Uri uri;
        public String smilPath;
    }
}
