package com.core.widget.view.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.R;
import com.core.base.BaseActivity;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用列表界面
 * 1.type 类型(1.网络请求  2.本地数据)(int)
 * 2.title 标题(String)
 * 3.reid  标题主题(int)
 * 4.isSingle  是否单选(默认单选)(boolean)  d
 * 多选 0x21  单选 0x20
 * <p/>
 * location
 * data   List<SelectBean> {@link SelectBean}  供选择的数组对象
 * <p/>
 * net
 * action 网址(String)
 * param  参数(map)
 * key 请求网络后取的数组key(String)
 * showKey 请求网络后显示选择索引的key,在key的数组的对象中(String)
 * <p/>
 * Created by pengminggong on 2016/9/29.
 */
public class SelectActivity extends BaseActivity {

    private ListView list;
    private ListAdapter adapter;
    private EmptyLayout emptyLayout;

    private int type = -1;//1 网络  2.本地 3 网络(两个网络请求)
    private String title = "";//标题
    private boolean isSingle = true;//是否单选
    private boolean isForm = true;//是否主表

    //location
    private List<SelectBean> formBeaan;//数据来源
    private Object object;
    //net
    private int id;//数字标识符 
    private String action;//网址
    private HashMap<String, Object> param;//参数
    private HashMap<String, Object> param1;//参数
    private String key;//从网络请求的json对象中获取显示列表的key
    private String showKey;//从列表对象中获取选择索引(name)的key

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            String message = msg.getData().getString("result");
            if (StringUtil.isEmpty(message)) return;
            if (!JSONUtil.validate(message)) return;
            JSONObject object = JSON.parseObject(message);
            if (object == null) return;
            if (msg.what == 0x11) {
                if ("mobile/common/getCombo.action".equals(action) || isSingle) {//下拉接口解析
                    if ("common/dbfind.action".equals(action)) {
                        LogUtil.d("单选模式和单选接口");
                        dispalayMuliSelect(message);
                    } else {
                        LogUtil.d("单选模式和单选接口");
                        if (!StringUtil.isEmpty(key)) {
                            if (StringUtil.isEmpty(showKey)) return;
                            if (object.containsKey(key)) {
                                JSONArray array = object.getJSONArray(key);
                                if (array == null || array.size() <= 0) {
                                    emptyLayout.showEmpty();
                                    return;
                                }
                                if (formBeaan == null) formBeaan = new ArrayList<>();
                                else formBeaan.clear();
                                SelectBean bean = null;
                                JSONObject chce = null;
                                for (int i = 0; i < array.size(); i++) {
                                    if (array.get(i) instanceof JSONObject) {
                                        chce = array.getJSONObject(i);
                                        if (chce == null) continue;
                                        bean = new SelectBean();
                                        if (chce.containsKey(showKey)) {
                                            bean.setName(chce.getString(showKey));
                                            bean.setObject(JSON.toJSONString(chce));//
                                            bean.setJson(chce.toString());
                                            formBeaan.add(bean);
                                        }
                                    }
                                    if (array.get(i) instanceof String) {
                                        bean = new SelectBean();
                                        bean.setName(array.get(i).toString());
                                        bean.setObject(JSON.toJSONString(array.get(i)));//
                                        bean.setJson(array.toJSONString());
                                        formBeaan.add(bean);
                                    }

                                }
                            }

                        }
                    }

                } else {
                    //多选解析
                    if (ApiUtils.getApiModel() instanceof ApiPlatform) {
                        parseDataMuliSelect(message);
                    } else {
                        dispalayMuliSelect(message);
                    }

                }
                setAdapter();

            } else if (msg.what == 0x15) {
                LogUtil.d("单选模式和单选接口");
                if (!StringUtil.isEmpty(key)) {
                    if (StringUtil.isEmpty(showKey)) return;
                    if (object.containsKey(key)) {
                        JSONArray array = object.getJSONArray(key);
                        if (array == null || array.size() <= 0) {
                            //emptyLayout.showEmpty();
                            //  return;
                        }
                        if (formBeaan == null) formBeaan = new ArrayList<>();
                        else formBeaan.clear();
                        SelectBean bean = null;
                        JSONObject chce = null;
                        for (int i = 0; i < array.size(); i++) {
                            if (array.get(i) instanceof JSONObject) {
                                chce = array.getJSONObject(i);
                                if (chce == null) continue;
                                bean = new SelectBean();
                                if (chce.containsKey(showKey)) {
                                    bean.setName(chce.getString(showKey));
                                    bean.setObject(JSON.toJSONString(chce));//
                                    bean.setJson(chce.toString());
                                    formBeaan.add(bean);
                                }
                            }
                            if (array.get(i) instanceof String) {
                                bean = new SelectBean();
                                bean.setName(array.get(i).toString());
                                bean.setObject("vr_nichestep");//
                                bean.setJson(array.toJSONString());
                                formBeaan.add(bean);
                            }

                        }
                    }

                }
                loadMutilDataByNet1();
            } else if (msg.what == 0x16) {
                LogUtil.d("单选模式和单选接口");
                if (!StringUtil.isEmpty(key)) {
                    if (StringUtil.isEmpty(showKey)) return;
                    if (object.containsKey(key)) {
                        JSONArray array = object.getJSONArray(key);
                        if (array == null || array.size() <= 0) {
//                            emptyLayout.showEmpty();
                            //return;
                        }
                        if (formBeaan == null) formBeaan = new ArrayList<>();
                        SelectBean bean = null;
                        JSONObject chce = null;
                        for (int i = 0; i < array.size(); i++) {
                            if (array.get(i) instanceof JSONObject) {
                                chce = array.getJSONObject(i);
                                if (chce == null) continue;
                                bean = new SelectBean();
                                if (chce.containsKey(showKey)) {
                                    bean.setName(chce.getString(showKey));
                                    bean.setObject(JSON.toJSONString(chce));//
                                    bean.setJson(chce.toString());
                                    formBeaan.add(bean);
                                }
                            }
                            if (array.get(i) instanceof String) {
                                bean = new SelectBean();
                                bean.setName(array.get(i).toString());
                                bean.setObject("vr_type");//
                                bean.setJson(array.toJSONString());
                                formBeaan.add(bean);
                            }

                        }
                    }

                }
                setAdapter();
            } else {
                emptyLayout.showEmpty();
            }

        }
    };
    private String method;

    private void setAdapter() {
        if (!ListUtils.isEmpty(formBeaan)) {
            //TODO 不为空的情况下提交给适配器
            if (adapter == null) {
                adapter = new ListAdapter(formBeaan);
                list.setAdapter(adapter);
            } else {
                adapter.setFormBeaan(formBeaan);
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * @desc:下拉联动接口
     * @author：Arison on 2017/4/10
     */
    private void dispalayMuliSelect(String message) {
        String dataStr = JSON.parseObject(message).getString("data");
        JSONArray datas = JSON.parseArray(dataStr);//values
        JSONArray dbfinds = JSON.parseObject(message).getJSONArray("dbfinds");
        if (datas != null) {
            if (datas.size() == 0) {
                return;
            }
        }
        if (dbfinds == null) return;
        String fieldkey = "";
        ArrayList<String> fieldKeys = new ArrayList<>();//联动数据
        for (int i = 0; i < dbfinds.size(); i++) {
            JSONObject item = dbfinds.getJSONObject(i);
            String key = item.getString("field");
            if (key.equals(showKey)) {
                fieldkey = item.getString("dbGridField");
            }

            if (fields != null) {//联动选择
                for (int j = 0; j < fields.length; j++) {
                    if (key.equals(fields[j])) {
                        fieldKeys.add(item.getString("dbGridField"));

                    }
                }
            }
        }
        if (formBeaan == null) formBeaan = new ArrayList<>();
        else formBeaan.clear();
        SelectBean bean = null;
        String dbfindsStr = JSON.toJSONString(dbfinds);
        if (fields != null) {
            ArrayList<String> values = new ArrayList<>();
            for (int i = 0; i < datas.size(); i++) {
                JSONObject item = datas.getJSONObject(i);
                bean = new SelectBean();
                bean.setId(id);
                String value2 = "";
                for (int j = 0; j < fieldKeys.size(); j++) {
                    if (j == fieldKeys.size() - 1) {
                        value2 = value2 + item.getString(fieldKeys.get(j));
                    } else {
                        value2 = value2 + item.getString(fieldKeys.get(j)) + "/";
                    }
                    values.add(item.getString(fieldKeys.get(j)));
                }
                if (StringUtil.isEmpty(item.getString(fieldkey))) {
                    bean.setName(item.getString(showKey));
                } else {
                    if (StringUtil.isEmpty(value2)) {
                        bean.setName(item.getString(fieldkey));
                    } else {
                        bean.setName(item.getString(fieldkey) + "(" + value2 + ")");
                    }
                }
                // 修改为显示所有的
                bean.setShowName(getName(item));
                bean.setDbfinds(dbfindsStr);
                bean.setFields(JSON.toJSONString(values));
                bean.setJson(JSON.toJSONString(datas.get(i)));
                formBeaan.add(bean);

            }
        } else {
            for (int i = 0; i < datas.size(); i++) {
                System.out.println("value" + i + ":" + datas.getJSONObject(i).getString(fieldkey));
                JSONObject item = datas.getJSONObject(i);
                bean = new SelectBean();
                bean.setId(id);
                if (StringUtil.isEmpty(item.getString(fieldkey))) {
                    bean.setName(item.getString(showKey));
                } else {
                    bean.setName(item.getString(fieldkey));
                }
                bean.setObject(JSON.toJSONString(datas.get(i)));
                bean.setJson(JSON.toJSONString(datas.get(i)));
                formBeaan.add(bean);
            }
        }

    }

    private String getName(JSONObject item) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> e : item.entrySet()) {
            if (e.getValue() != null && !e.getValue().equals("")) {
                builder.append(e.getValue() + ",");
            }
        }
        if (builder.length() > 1) {
            builder.delete(builder.length() - 1, builder.length());
        }
        return builder.toString();
    }

    public void parseDataMuliSelect(String message) {
        JSONObject object = JSON.parseObject(message);
        if (!StringUtil.isEmpty(key)) {//root
            if (StringUtil.isEmpty(showKey)) return;
            if (object.containsKey(key)) {
                JSONArray array = object.getJSONArray(key);
                if (array == null || array.size() <= 0) {
                    //return;
                }
                if (formBeaan == null) formBeaan = new ArrayList<>();
                SelectBean bean = null;
                JSONObject chce = null;
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i) instanceof JSONObject) {
                        chce = array.getJSONObject(i);
                        if (chce == null) continue;
                        bean = new SelectBean();
                        if (chce.containsKey(showKey)) {
                            bean.setName(chce.getString(showKey));
                            bean.setObject(JSON.toJSONString(chce));//
                            bean.setJson(chce.toString());
                            formBeaan.add(bean);
                        }
                    }
                    if (array.get(i) instanceof String) {
                        bean = new SelectBean();
                        bean.setName(array.get(i).toString());
                        bean.setObject("vr_type");//
                        bean.setJson(array.toJSONString());
                        formBeaan.add(bean);
                    }

                }
            }

        }

    }

    private VoiceSearchView search_edit;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            search_edit.setText("");
            selectOK();
            return true;
        } else {
            if (item.getItemId() == android.R.id.home) {
                putDownInput();
            }
            return super.onOptionsItemSelected(item);
        }
    }

    private void selectOK() {
        if (adapter == null || adapter.getFormBeaan() == null || adapter.getFormBeaan().size() <= 0) {
            Intent intent = new Intent();
            intent.putExtra("id", id);
            intent.putExtra("isForm", isForm);
            setResult(0x21, intent);
            finish();
            return;
        }
        ArrayList<SelectBean> formBeaan = (ArrayList) adapter.getFormBeaan();
        ArrayList<SelectBean> temps = new ArrayList<>();
        for (int i = 0; i < formBeaan.size(); i++) {
            if (formBeaan.get(i).isClick()) {
                temps.add(formBeaan.get(i));
            }
        }
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("data", temps);
        intent.putExtra("id", id);
        intent.putExtra("isForm", isForm);
        setResult(0x21, intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isSingle)
            getMenuInflater().inflate(R.menu.menu_visit_save, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        initView();
    }

    private void initView() {
        if (!StringUtil.isEmpty(title)) setTitle(title);
        list = (ListView) findViewById(R.id.listview);
        search_edit = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (ListUtils.isEmpty(formBeaan)) return;
                String str = editable.toString().trim();
                ArrayList<SelectBean> lists = new ArrayList<>();
                for (SelectBean e : formBeaan) {
                    if (!StringUtil.isEmpty(e.getName()) && StringUtil.isInclude(e.getName(), str)) {
                        lists.add(e);
                    }
                }
                if (adapter != null) {
                    adapter.setFormBeaan(lists);
                    adapter.notifyDataSetChanged();
                }
                if (ListUtils.isEmpty(lists))
                    emptyLayout.showEmpty();
                list.setSelection(0);
            }
        });
        emptyLayout = new EmptyLayout(ct, list);
        emptyLayout.setShowLoadingButton(false);
        emptyLayout.setShowEmptyButton(false);
        emptyLayout.setShowErrorButton(false);
        emptyLayout.setEmptyViewRes(R.layout.view_empty);
        adapter = new ListAdapter();
        if (!ListUtils.isEmpty(formBeaan))
            adapter.setFormBeaan(formBeaan);
        list.setAdapter(adapter);
        if (type == 1) {
            loadDateByNet();
        } else if (type == 3) {
            loadMutilDataByNet();
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                List<SelectBean> formBeaan = adapter.getFormBeaan();
                if (isSingle) {
                    putDownInput();
                    SelectBean bean = formBeaan.get(i);

//                    String names[]=bean.getName().split("\\(");
//                    bean.setName(names[0]);
                    bean.setName(bean.getName());
                    bean.setId(id);
                    //TODO 出错 先删除  如果要修改  bean.setJson 的返回  联系负责人 Bitliker
                    //bean.setJson(JSON.toJSONString(object));
                    bean.setObject(JSON.toJSONString(object));
                    Intent intent = new Intent();
                    LogUtil.d("ben" + JSON.toJSONString(bean));
                    intent.putExtra("data", bean);
                    intent.putExtra("id", id);
                    setResult(0x20, intent);
                    finish();
                } else {
                    if (formBeaan.get(i).isClick()) {
                        formBeaan.get(i).setId(id);
                        formBeaan.get(i).setClick(false);
                        adapter.notifyDataSetChanged();
                    } else {
                        formBeaan.get(i).setClick(true);
                        formBeaan.get(i).setId(id);
                        adapter.notifyDataSetChanged();
                    }

                }
            }
        });


    }

    String[] fields;

    private void initIntent() {
        Intent intent = getIntent();
        if (intent == null) return;
        type = intent.getIntExtra("type", -1);
        method = intent.getStringExtra("method");
        title = intent.getStringExtra("title");
        id = intent.getIntExtra("id", 0);
        object = intent.getParcelableExtra("object");
        fields = intent.getStringArrayExtra("fields");
        LogUtil.d(JSON.toJSONString(object));
        isSingle = intent.getBooleanExtra("isSingle", true);
        isForm = intent.getBooleanExtra("isFrom", true);
        int reid = intent.getIntExtra("reid", -1);
//        if (reid != -1)
//            setTheme(getSharedPreferences("cons", MODE_PRIVATE).getInt("theme", reid));
        if (type == 1) {//网络
            action = intent.getStringExtra("action");
            key = intent.getStringExtra("key");
            showKey = intent.getStringExtra("showKey");
            param = intent.getParcelableExtra("showKey");
            Bundle bundle = getIntent().getExtras();
            param = (HashMap<String, Object>) bundle.getSerializable("param");
        } else if (type == 2) {//本地
            formBeaan = intent.getParcelableArrayListExtra("data");
        } else if (type == 3) {
            //网络请求，两个相同格式体的接口数据合并操作
            action = intent.getStringExtra("action");
            key = intent.getStringExtra("key");//主数据集合key值
            showKey = intent.getStringExtra("showKey");
            Bundle bundle = getIntent().getExtras();
            param = (HashMap<String, Object>) bundle.getSerializable("param");
            param1 = (HashMap<String, Object>) bundle.getSerializable("param1");
        }

    }

    /*从服务器上获取数据*/
    private void loadDateByNet() {
        if (progressDialog != null)
            progressDialog.show();
        String url = "";
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        if (ApiUtils.getApiModel() instanceof ApiPlatform) {
            url = action;
            headers.put("Cookie", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        } else {
            url = CommonUtil.getAppBaseUrl(ct) + action;
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        }
        String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
        if (param != null) {
            param.put("sessionId", sessionId);
        } else {
            return;
        }


        if (StringUtil.isEmpty(method)) method = "post";
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x11, null, null, method);
    }


    private void loadMutilDataByNet() {
        if (progressDialog != null)
            progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + action;
        String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
        if (param != null) {
            param.put("sessionId", sessionId);
        } else {
            return;
        }
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x15, null, null, "post");
    }

    private void loadMutilDataByNet1() {
        if (progressDialog != null)
            progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + action;
        String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
        if (param1 != null) {
            param1.put("sessionId", sessionId);
        } else {
            return;
        }
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param1, handler, headers, 0x16, null, null, "post");
    }


    class ListAdapter extends BaseAdapter {
        private List<SelectBean> formBeaan;

        public ListAdapter() {
        }

        public ListAdapter(List<SelectBean> formBeaan) {
            this.formBeaan = formBeaan;
        }

        public void setFormBeaan(List<SelectBean> formBeaan) {
            this.formBeaan = formBeaan;
        }

        public List<SelectBean> getFormBeaan() {
            return formBeaan;
        }

        @Override
        public int getCount() {
            return formBeaan == null ? 0 : formBeaan.size();
        }

        @Override
        public Object getItem(int i) {
            return formBeaan.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        //临时变量
        SelectBean chche = null;

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHoler holer = null;
            if (view == null) {
                view = LayoutInflater.from(ct).inflate(R.layout.select_list_item, null);
                holer = new ViewHoler();
                holer.select_scb = (CheckBox) view.findViewById(R.id.select_scb);
                holer.name_tv = (TextView) view.findViewById(R.id.name_tv);
                view.setTag(holer);
            } else {
                holer = (ViewHoler) view.getTag();
            }
            chche = formBeaan.get(i);
            holer.name_tv.setText(StringUtil.isEmpty(chche.getShowName()) ? "" : chche.getShowName());
            holer.select_scb.setChecked(chche.isClick());
            if (isSingle) {
                holer.select_scb.setVisibility(View.GONE);
            } else {
                holer.select_scb.setFocusable(false);
                holer.select_scb.setClickable(false);
            }
            return view;
        }

        class ViewHoler {
            CheckBox select_scb;
            TextView name_tv;
        }
    }

    private void putDownInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_edit.getWindowToken(), 0);
    }
}
