package com.core.widget.view.Activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.common.system.PermissionUtil;
import com.core.app.R;
import com.core.base.BaseActivity;
import com.core.utils.pictureselector.FileTraversal;
import com.core.utils.pictureselector.ImgsActivity;
import com.core.utils.pictureselector.Util;
import com.core.widget.view.adapter.ImgFileListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImgFileListActivity extends BaseActivity implements OnItemClickListener{

	ListView listView;
	Util util;
	ImgFileListAdapter listAdapter;
	List<FileTraversal> locallist;
	private int max_size;
	private int current_size;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imgfilelist);
		listView=(ListView) findViewById(R.id.listView1);
		initView();
		initData();
	}

	private void initData() {
		Intent intent = getIntent();
		max_size = intent.getIntExtra("MAX_SIZE",9);
		current_size = intent.getIntExtra("CURRENT_SIZE",0);
	}

	private void initView() {
		util=new Util(this);
		locallist=util.LocalImgFileList();
		List<HashMap<String, String>> listdata=new ArrayList<HashMap<String,String>>();
		Bitmap bitmap[] = null;
		if (locallist!=null) {
			bitmap=new Bitmap[locallist.size()];
			for (int i = 0; i < locallist.size(); i++) {
				HashMap<String, String> map=new HashMap<String, String>();
				map.put("filecount", locallist.get(i).filecontent.size()+"张");
				map.put("imgpath", locallist.get(i).filecontent.get(0)==null?null:(locallist.get(i).filecontent.get(0)));
				map.put("filename", locallist.get(i).filename);
				listdata.add(map);
			}
		}
		listAdapter=new ImgFileListAdapter(this, listdata);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent intent=new Intent(this,ImgsActivity.class);
		Bundle bundle=new Bundle();
		bundle.putInt("MAX_SIZE",max_size);
		bundle.putInt("CURRENT_SIZE",current_size);
		bundle.putParcelable("data", locallist.get(arg2));
		intent.putExtras(bundle);
		startActivityForResult(intent,0x06);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0x06 && resultCode == 0x03 && data != null){
			Intent intent = new Intent();
			intent.putExtra("files",data.getStringArrayListExtra("files"));
			Log.i("files0x02",data.getStringArrayListExtra("files").toString());
			setResult(0x02,intent);
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
		for (String permission : permissions) {
			if (PermissionUtil.lacksPermissions(ct, permission)) {
				PermissionUtil.requestPermission(this, PermissionUtil.DEFAULT_REQUEST, permission);
			}
		}
	}
}
