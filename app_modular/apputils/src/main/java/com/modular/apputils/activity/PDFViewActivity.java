package com.modular.apputils.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.core.base.BaseActivity;
import com.core.utils.ToastUtil;
import com.modular.apputils.R;
import com.modular.apputils.utils.OpenFilesUtils;
import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

/**
 * Created by Bitlike on 2018/3/15.
 */

public class PDFViewActivity extends BaseActivity {
    private TbsReaderView mTbsReaderView;
    private String filepath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.openOther) {
            File file = new File(filepath);
            if (file != null && file.exists()) {
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromFile(file);
                intent.setDataAndType(uri, "application/pdf");
                startActivity(intent);
                finish();
            } else {
                ToastUtil.showToast(ct, "文件不存在或是已损坏");
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_other_open, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void initView() {
        mTbsReaderView = new TbsReaderView(this, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {

            }
        });
        RelativeLayout rootRl = findViewById(R.id.rl_root);
        rootRl.addView(mTbsReaderView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        Intent intent = getIntent();
        if (intent != null) {
            filepath = intent.getStringExtra("filepath");
        }
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filepath);
        bundle.putString("tempPath", Environment.getExternalStorageDirectory().getPath());
        boolean result = mTbsReaderView.preOpen("pdf", false);
        if (result) {
            mTbsReaderView.openFile(bundle);
        }
    }
}
