package com.xzjmyk.pm.activity.ui.erp.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.StringUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.core.app.Constants;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NewsDetailActivity extends BaseActivity implements OnClickListener {


	@ViewInject(R.id.tv_log_handler)
	private TextView tv_pre;
	@ViewInject(R.id.tv_log_approve)
	private TextView tv_next;
	
	@ViewInject(R.id.tv_content)
	private TextView tv_content;
	@ViewInject(R.id.bottom_layout)
	private LinearLayout  linear_bottom;
	private Context ct;
	
	private int preId;
	private int nextId;
	
	private static final int LOAD_SUCESS_NOTICE=2;
	int type;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}

	public void initView() {
        setContentView(R.layout.act_news_detail);
        ViewUtils.inject(this);
		ct=this;
        TAG="NewsDetailActivity";
		setTitle("新闻详情");
        tv_next.setText("下一条");
        tv_pre.setText("上一条");
        
        tv_pre.setOnClickListener(this);
        tv_next.setOnClickListener(this);
	}

	public void initData() {
	    type=getIntent().getIntExtra("type",0);
		if (type!=0) {
			loadNoticeData(getIntent().getIntExtra("id",0));
			linear_bottom.setVisibility(View.GONE);
		}else{
		   loadNewsData(getIntent().getIntExtra("id",0));
		}
	}

	private void loadNewsData(int id) {
		String url= CommonUtil.getAppBaseUrl(ct)+"oa/news/getNews.action";
        final Map<String,Object> param=new HashMap<>();
        param.put("caller", "News");
        param.put("id", id);
        LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID="+ CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, Constants.LOAD_SUCCESS, null, null, "get");
	}
	
	private void loadNoticeData(int id) {
		String url=CommonUtil.getAppBaseUrl(ct)+"/oa/note/getNote.action";
        final Map<String,Object> param=new HashMap<>();
        param.put("caller", "Note");
        param.put("id", id);
        LinkedHashMap<String , Object> headers=new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID="+CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, LOAD_SUCESS_NOTICE, null, null, "get");
	}
	
	
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constants.LOAD_SUCCESS:
				String result=msg.getData().getString("result");
				if(StringUtil.isEmpty(result))return;
				if(JSON.parseObject(result).getJSONObject("news")==null)return;
			    String html=JSON.parseObject(result).getJSONObject("news").getString("ne_content");
				if (!StringUtil.isEmpty(html)) {
					tv_content.setText(Html.fromHtml(html));
				}
			    JSONObject preObject=	JSON.parseObject(result).getJSONObject("news").getJSONObject("prevNews");
			    JSONObject nextObject=	JSON.parseObject(result).getJSONObject("news").getJSONObject("nextNews");
			    if (preObject!=null) {
					 tv_pre.setText(preObject.getString("ne_theme"));
					 preId= preObject.getIntValue("ne_id");
					 tv_pre.setEnabled(true);
				}else{
				     tv_pre.setEnabled(false);
				}
			    if (nextObject!=null) {
					tv_next.setText(nextObject.getString("ne_theme"));
					nextId= nextObject.getIntValue("ne_id");
					tv_next.setEnabled(true);
				}else{
					tv_next.setEnabled(false);
				}
			    
			    Log.i(TAG, result);
				break;
			case LOAD_SUCESS_NOTICE:
				result=msg.getData().getString("result");
				if(StringUtil.isEmpty(result))return;
			    String content=	JSON.parseObject(result).getString("content");
				tv_content.setText(Html.fromHtml(content));
				if (type==1) {
					setTitle("通知");
				}
				if (type==2) {
					setTitle("公告");
				}
				tv_next.setVisibility(View.GONE);
				tv_pre.setVisibility(View.GONE);
				break;
			case Constants.APP_SOCKETIMEOUTEXCEPTION:
			    result=msg.getData().getString("result");
			   	Log.i(TAG, result);
			   	ViewUtil.ToastMessage(ct, result);
			default :
				break;
			  }
			}
		};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_log_handler:
			if (preId!=0) {
				loadNewsData(preId);
			}
			ViewUtil.ToastMessage(ct, "上一条");
			break;
		case R.id.tv_log_approve:
			if (nextId!=0) {
				loadNewsData(nextId);
			}
			ViewUtil.ToastMessage(ct, "下一条");
			break;
		default:
			break;
		}
	}

}
