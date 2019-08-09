package com.core.utils.pictureselector;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.common.data.ListUtils;
import com.core.app.R;
import com.core.base.BaseActivity;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class ImgsActivity extends BaseActivity {

	Bundle bundle;
	FileTraversal fileTraversal;
	GridView imgGridView;
	ImgsAdapter imgsAdapter;
	LinearLayout select_layout;
	Util util;
	RelativeLayout relativeLayout2;
	HashMap<Integer, ImageView> hashImage;
	Button choise_button;
	ArrayList<String> filelist;
	private int max_size;
	private int current_size;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photogrally);

		imgGridView=(GridView) findViewById(R.id.gridView1);
		bundle= getIntent().getExtras();
		fileTraversal=bundle.getParcelable("data");
		max_size = bundle.getInt("MAX_SIZE");
		current_size = bundle.getInt("CURRENT_SIZE");
		imgsAdapter=new ImgsAdapter(this, fileTraversal.filecontent,onItemClickClass);
		imgGridView.setAdapter(imgsAdapter);
		select_layout=(LinearLayout) findViewById(R.id.selected_image_layout);
		relativeLayout2=(RelativeLayout) findViewById(R.id.relativeLayout2);
		choise_button=(Button) findViewById(R.id.button3);
		hashImage=new HashMap<Integer, ImageView>();
		filelist=new ArrayList<String>();
//		imgGridView.setOnItemClickListener(this);
		util=new Util(this);
	}

	class BottomImgIcon implements OnItemClickListener{

		int index;
		public BottomImgIcon(int index) {
			this.index=index;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

		}
	}

	@SuppressLint("NewApi")
	public ImageView iconImage(String filepath,int index,CheckBox checkBox) throws FileNotFoundException{
		LayoutParams params=new LayoutParams(relativeLayout2.getMeasuredHeight()-10, relativeLayout2.getMeasuredHeight()-10);
		ImageView imageView=new ImageView(this);
		imageView.setLayoutParams(params);
//		imageView.setBackgroundResource(R.drawable.imgbg);
		int alpha=100;
		imageView.setAlpha(alpha);
		util.imgExcute(imageView, imgCallBack, filepath);
		imageView.setOnClickListener(new ImgOnclick(filepath,checkBox));
		return imageView;
	}

	ImgCallBack imgCallBack=new ImgCallBack() {
		@Override
		public void resultImgCall(ImageView imageView, Bitmap bitmap) {
			imageView.setImageBitmap(bitmap);
		}
	};

	class ImgOnclick implements OnClickListener{
		String filepath;
		CheckBox checkBox;
		public ImgOnclick(String filepath,CheckBox checkBox) {
			this.filepath=filepath;
			this.checkBox=checkBox;
		}
		@Override
		public void onClick(View arg0) {
			checkBox.setChecked(false);
			select_layout.removeView(arg0);
			choise_button.setText("已选择("+select_layout.getChildCount()+")张");
			filelist.remove(filepath);
		}
	}

	ImgsAdapter.OnItemClickClass onItemClickClass=new ImgsAdapter.OnItemClickClass() {
		@Override
		public void OnItemClick(View v, int Position, CheckBox checkBox) {
			String filapath=fileTraversal.filecontent.get(Position);
			if (checkBox.isChecked()) {
				checkBox.setChecked(false);
				select_layout.removeView(hashImage.get(Position));
				filelist.remove(filapath);
				choise_button.setText("已选择("+select_layout.getChildCount()+")张");
			}else {
				if (select_layout.getChildCount() > max_size -1 || select_layout.getChildCount() > max_size - current_size -1){
					ToastMessage("您当前最多可选"+(max_size - current_size)+"张图片");
					return;
				}else {
					try {
						checkBox.setChecked(true);
						Log.i("img", "img choise position->"+Position);
						ImageView imageView=iconImage(filapath, Position,checkBox);
						if (imageView !=null) {
							hashImage.put(Position, imageView);
							filelist.add(filapath);
							select_layout.addView(imageView);
							choise_button.setText("已选择("+select_layout.getChildCount()+"张");
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}

			}
		}
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.back_sure, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() ==  R.id.sure){
			if (!ListUtils.isEmpty(filelist)){
				Intent intent = new Intent();
				intent.putExtra("files",filelist);
				Log.i("files0x03",filelist.toString());
				setResult(0x03,intent);
				finish();
			}else {
				ToastMessage(getString(R.string.please_add_image));
			}
		}else if (item.getItemId() ==  android.R.id.home){
			onBackPressed();

		}
		return true;
	}
}
