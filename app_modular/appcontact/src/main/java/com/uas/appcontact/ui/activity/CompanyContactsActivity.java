package com.uas.appcontact.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.system.SystemUtil;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.model.Friend;
import com.core.model.Hrorgs;
import com.core.model.HrorgsEntity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.CustomerListView;
import com.core.widget.VoiceSearchView;
import com.core.xmpp.dao.FriendDao;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.uas.appcontact.R;
import com.uas.appcontact.db.TopContactsDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc:企业架构
 * @author：Arison on 2016/10/31
 */
public class CompanyContactsActivity extends BaseActivity {

    public CustomerListView listview;
    public CustomerListView mlistleft;
    public VoiceSearchView voiceSearchView;
    private LinearLayout ll_list;
    private HorizontalScrollView hscrollview;
    private RadioGroup rg_nav_content;
    private LinearLayout ll_content;
    private RelativeLayout rl_empty;
    private RefreshLayout mRefreshLayout;

//    private SuperSwipeRefreshLayout reScrollView;

    private Context ct;
    //关键变量
    public static ArrayList<String> tabTitle = new ArrayList<>(); // 标题
    public static Map<String, Object> keystore = new LinkedHashMap<>(); // 标题

    private LayoutInflater mInflater;
    private Hrorgs hrorgs_parent;
    private Hrorgs hrorgs_left;
    private int hrorgs_parent_position = 0;
    private View viewHead;
    private DBManager manager;
    private DetailItemAdapter adapter;// 左边
    private DetailItemAdapter madapter;// 右边
    private final int LOAD_SUCCESS_ALLDATA = 9;
    private final int UPDATE_CHANGE_MASTER = 12;
    private final int LOAD_SUCCESS_LEAFHRORG = 2;
    private final int LOAD_SUCCESS_LEAFHRORG_RIGHT = 5;
    private final int LOAD_SUCCESS_LEAFHRORG_RIGHTANDLEFT = 6;
    private final int LOAD_SUCCESS_EMPLOYEE = 4;
    private final int LOAD_SUCCESS_EMPLOYEEINFO = 3;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            HrorgsEntity hEntity;
            switch (msg.what) {
                case UPDATE_CHANGE_MASTER:
                    String master = CommonUtil.getSharedPreferences(ct, "erp_master");
                    String commpany = CommonUtil.getSharedPreferences(ct, "erp_commpany");
                    if (!TextUtils.isEmpty(master)) {
                        String date = manager.select_getCacheTime(
                                new String[]{commpany, master},
                                "ed_company=? and ed_whichsys=?");
                        LoadServerData(master, date);
                    } else {
//                        if (reScrollView.isRefreshing()) {
//                            reScrollView.setRefreshing(false);
//                        }
                        if (mRefreshLayout.isRefreshing()) {
                            mRefreshLayout.finishRefresh(0);
                        }
                        ll_content.setVisibility(View.GONE);
                        rl_empty.setVisibility(View.VISIBLE);
                    }
                    break;
                case LOAD_SUCCESS_ALLDATA:
                    String result = msg.getData().getString("result");
                    JSONObject jsonobject = JSON.parseObject(result);
                    String server_time = jsonobject.getString("sysdate");
                    List<HrorgsEntity> hrorgsEntities = JSON.parseArray(jsonobject.getString("hrorgs"), HrorgsEntity.class);
                    List<EmployeesEntity> employeesEntities = JSON.parseArray(jsonobject.getString("employees"), EmployeesEntity.class);
                    master = CommonUtil.getSharedPreferences(ct, "erp_master");
                    if (manager == null || !manager.getDb().isOpen()) return;
                    HrorgsEntity hrEntity = manager.select_getRootData(new String[]{master}, "whichsys=?");
                    boolean isFristLoad = true;//是否第一次加载
                    if (hrEntity != null) {
                        isFristLoad = false;
                    }
//                    reScrollView.setRefreshing(true);
                    if (!hrorgsEntities.isEmpty() || !employeesEntities.isEmpty()) {
                        insertDataSqlite(isFristLoad, hrorgsEntities, employeesEntities,
                                server_time);    //写入缓存
                        rl_empty.setVisibility(View.GONE);
                        ll_content.setVisibility(View.VISIBLE);
                        LoadRootHrorgFromServer();//取缓存
                    } else {
                        Log.i(TAG, "取缓存数据.....！");
                        LoadRootHrorgFromServer();//取缓存
                    }
//                    if (reScrollView.isRefreshing()) {
//                        reScrollView.setRefreshing(false);
//                    }
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(0);
                    }
                    progressDialog.dismiss();
                    break;
                case Constants.LOAD_SUCCESS://根节点
                    if (!keystore.isEmpty()) {
                        keystore.clear();
                    }
                    String jsonString = msg.getData().getString("result");
//				Log.i(TAG, "json:" + jsonString);
//				Hrorgs hrorgs = JSON.parseObject(jsonString, Hrorgs.class);
                    master = CommonUtil.getSharedPreferences(ct, "erp_master");
                    /**@注释：取缓存数据  */
                    hEntity = manager.select_getRootData(
                            new String[]{"0", master},
                            "or_subof=? and whichsys=?");
                    if (hEntity == null) {
                        //缓存数据为空
                        rl_empty.setVisibility(View.VISIBLE);
                        ll_content.setVisibility(View.GONE);
                        break;
                    }
                    rl_empty.setVisibility(View.GONE);
                    ll_content.setVisibility(View.VISIBLE);
                    master = CommonUtil.getSharedPreferences(ct, "erp_master");
                    String or_id_sqlite = String.valueOf("0");

                    Hrorgs hrorgs = manager.select_getLeafData(
                            new String[]{or_id_sqlite, master},
                            "or_subof=? and whichsys=?", or_id_sqlite, master);
                    hrorgs.setMaster(hEntity.getOr_name());//数据库查询的数据没有master
                    hrorgs.getEmployees().clear();
                    /** @注释：省略根节点 */
                    adapter = new DetailItemAdapter(ct, hrorgs);
                    listview.setAdapter(adapter);
//				ViewUtil.setListViewHeightBasedOnChildren(listview);

                    if (hrorgs.getHrorgs().size() != 0) {
                        // 还需要加载一次!!! 默认选中第一项
                        if (hrorgs.getHrorgs().size() + hrorgs.getEmployees().size() == 1) {
                            String or_id = String.valueOf(hrorgs.getHrorgs().get(0).getOr_id());
                            String or_name = hrorgs.getHrorgs().get(0).getOr_name();
                            keystore.put(or_name, or_id);
                            tabTitle = parseHashMap(keystore, or_name);
                            LoadLeafHrorgFromServer(or_id, LOAD_SUCCESS_LEAFHRORG);
                        } else {
                            keystore.put(CommonUtil.getSharedPreferences(ct, "erp_commpany"), "0");
                            //tabTitle = parseHashMap(keystore, hrorgs.getMaster());
                            tabTitle = parseHashMap(keystore, CommonUtil.getSharedPreferences(ct, "erp_commpany"));
                            initNavigationHSV(tabTitle.size());
                            adapter.setMode(LIST_LEFT_MODE);
                            String or_id = String.valueOf(hrorgs.getHrorgs().get(0).getOr_id());
                            String or_name = hrorgs.getHrorgs().get(0).getOr_name();
                            keystore.put(or_name, or_id);
                            tabTitle = parseHashMap(keystore, or_name);
                            LoadLeafHrorgFromServer(or_id, LOAD_SUCCESS_LEAFHRORG_RIGHT);
                        }
                    }

                    progressDialog.dismiss();
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(0);
                    }
                    //reScrollView.onRefreshComplete();
                    break;
                /** @注释：左边栏接收数据 */
                case LOAD_SUCCESS_LEAFHRORG:
                    jsonString = msg.getData().getString("result");
                    initNavigationHSV(tabTitle.size());
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            hscrollview.smoothScrollTo(5500, 0);
                        }
                    }, 10);
                    // 解析同时有联系人和部门的数据
                    //hrorgs_left = JSON.parseObject(jsonString, Hrorgs.class);
                    master = CommonUtil.getSharedPreferences(ct, "erp_master");
                    or_id_sqlite = msg.getData().getString("or_id");
                    hrorgs_left = manager.select_getLeafData(
                            new String[]{or_id_sqlite, master},
                            "or_subof=? and whichsys=?", or_id_sqlite, master);
                    //hrorgs_left = LoadGroupData(hrorgs_left);//常用组
                    adapter = new DetailItemAdapter(ct, hrorgs_left);
                    if (!StringUtil.isEmpty(next_or_id)) {
                        adapter.setSelectOrId(Integer.valueOf(next_or_id));
                        adapter.setMode(LIST_TOP_MODE);
                    } else {
                        adapter.setMode(LIST_LEFT_MODE);
                    }

                    listview.setAdapter(adapter);
//				ViewUtil.setListViewHeightBasedOnChildren(listview);
                    // 得到左边栏数据之后，左边栏第一项，默认加载右边栏数据；
                    if (hrorgs_left.getHrorgs().size() > 0) {
                        if (!StringUtil.isEmpty(next_or_id)) {
                            keystore.put(next_or_name, next_or_id);
                            tabTitle = parseHashMap(keystore, next_or_name);
                            LoadLeafHrorgFromServer(next_or_id, LOAD_SUCCESS_LEAFHRORG_RIGHT);
                        } else {
                            //默认加载第一条数据
                            String or_id = String.valueOf(hrorgs_left.getHrorgs().get(0).getOr_id());
                            String or_name = hrorgs_left.getHrorgs().get(0).getOr_name();
                            keystore.put(or_name, or_id);
                            tabTitle = parseHashMap(keystore, or_name);
                            LoadLeafHrorgFromServer(or_id, LOAD_SUCCESS_LEAFHRORG_RIGHT);
                        }
                    } else {
                        if (madapter != null) {
                            madapter.getEmployees().clear();
                            madapter.notifyDataSetChanged();
                        }
                    }

                    // mlistleft.setAdapter(adapter);
                    if (adapter.getCount() == 0) {
//                        ViewUtil.ShowMessageTitle(ct, "暂时没有数据！");
                    }
//                    progressDialog.dismiss();
                    // listview.onRefreshComplete();
                    break;
                case LOAD_SUCCESS_LEAFHRORG_RIGHT:
//				jsonString = msg.getData().getString("result");// 需要记住父数据
//				Log.i(TAG, "json:" + jsonString);
                    initNavigationHSV(tabTitle.size());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hscrollview.smoothScrollTo(5500, 0);
                        }
                    }, 10);
                    //Log.i(TAG,"tabtitle:"+tabTitle.toString());
                    //hrorgs = JSON.parseObject(jsonString, Hrorgs.class);
                    master = CommonUtil.getSharedPreferences(ct, "erp_master");
                    or_id_sqlite = msg.getData().getString("or_id");
                    hrorgs = manager.select_getLeafData(
                            new String[]{or_id_sqlite, master},
                            "or_subof=? and whichsys=?", or_id_sqlite, master);// 解析同时有联系人和部门的数据

                    madapter = new DetailItemAdapter(ct, hrorgs);
                    mlistleft.setAdapter(madapter);
//				    ViewUtil.setListViewHeightBasedOnChildren(mlistleft);
                    hrorgs_parent = hrorgs;
                    if (adapter.getCount() == 0) {
//                        ViewUtil.ShowMessageTitle(ct, "暂时没有数据！");
                    }
//                    progressDialog.dismiss();
                    //reScrollView.onRefreshComplete();
                    break;
                case LOAD_SUCCESS_LEAFHRORG_RIGHTANDLEFT:
                    // 改变右边列表，改变左边列表 联动改变
                    jsonString = msg.getData().getString("result");
                    initNavigationHSV(tabTitle.size());
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            hscrollview.smoothScrollTo(5500, 0);
                        }
                    }, 10);
                    master = CommonUtil.getSharedPreferences(ct, "erp_master");
                    or_id_sqlite = msg.getData().getString("or_id");
                    hrorgs = manager.select_getLeafData(
                            new String[]{or_id_sqlite, master},
                            "or_subof=? and whichsys=?", or_id_sqlite, master);
                    adapter = new DetailItemAdapter(ct, hrorgs);
                    mlistleft.setAdapter(adapter);
                    if (hrorgs_parent != null) {
                        adapter = new DetailItemAdapter(ct, hrorgs_parent);
                        adapter.setMode(LIST_LEFT_MODE);
                        adapter.setSelectPosition(hrorgs_parent_position);
                        listview.setAdapter(adapter);
                    }
                    // 再次替换父类数据源
                    hrorgs_parent = hrorgs;

                    if (adapter.getCount() == 0) {
//                        ViewUtil.ShowMessageTitle(ct, "暂时没有数据！");
                    }
//                    progressDialog.dismiss();
                    break;
                case LOAD_SUCCESS_EMPLOYEE://查询员工
                    String key = msg.getData().getString("or_id");
                    master = CommonUtil.getSharedPreferences(ct, "erp_master");
                    List<EmployeesEntity> emList = manager.select_getEmployee(
                            new String[]{"%" + key + "%", master},
                            "em_name like ? and whichsys=?");
                    List<Hrorgs.Employee> employees_query = new ArrayList<>();
                    for (int i = 0; i < emList.size(); i++) {
                        Hrorgs.Employee em = new Hrorgs().new Employee();
                        em.setEm_id(emList.get(i).getEM_ID());
                        em.setEm_code(emList.get(i).getEM_CODE());
                        em.setEm_name(emList.get(i).getEM_NAME());
                        employees_query.add(em);
                    }
                    Hrorgs hrorgs2 = new Hrorgs();
                    hrorgs2.setEmployees(employees_query);
                    hrorgs2.setSuccess(true);
                    adapter = new DetailItemAdapter(ct, hrorgs2);
                    mlistleft.setAdapter(adapter);
                    if (adapter.getCount() == 0) {
                        //ViewUtil.ShowMessageTitle(ct, "暂时没有数据！");
                    }
                    break;
                case LOAD_SUCCESS_EMPLOYEEINFO:
                    master = CommonUtil.getSharedPreferences(ct, "erp_master");
                    List<EmployeesEntity> eList = manager.select_getEmployee(
                            new String[]{msg.getData().getString("or_id"), master},
                            "em_id=? and whichsys=?");
                    EmployeesEntity object = eList.get(0);
                    Map<String, Object> employee = new HashMap<>();
                    employee.put("em_name", object.getEM_NAME());
                    employee.put("em_mobile",
                            object.getEM_MOBILE() == null ? MyApplication.getInstance().getString(R.string.common_noinput) : object.getEM_MOBILE());
                    employee.put("em_email", object.getEM_EMAIL() == null ? MyApplication.getInstance().getString(R.string.common_noinput) : object.getEM_EMAIL());
                    employee.put("em_position",
                            object.getEM_POSITION() == null ? MyApplication.getInstance().getString(R.string.common_noinput) : object.getEM_POSITION());
                    employee.put("em_defaultorname",
                            object.getEM_DEFAULTORNAME() == null ? MyApplication.getInstance().getString(R.string.common_noinput) : object.getEM_DEFAULTORNAME());
                /*showDialogEmployee(employee);*/
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    String exception = msg.getData().getString("result");
                    com.core.net.http.ViewUtil.ToastMessage(ct, exception);
                    com.core.net.http.ViewUtil.AutoLoginErp(ct);
//                    reScrollView.onRefreshComplete();

//                    if (reScrollView.isRefreshing()) {
//                        reScrollView.setRefreshing(false);
//                    }
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(0);
                    }
                    break;
                default:
                    break;
            }
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_staff_query_list);
        setTitle(getString(R.string.contact_company));
        ct = this;
        manager = new DBManager(ct);
        mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(101/*MainActivity.UPDATA_LIST*/);
        closePoppupWindow();
        manager.closeDB();
    }

    private void initView() {
        listview = (CustomerListView) findViewById(R.id.lv_saff_list);
        mlistleft = (CustomerListView) findViewById(R.id.lv_left_list);
        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        ll_list = (LinearLayout) findViewById(R.id.ll_list);
        hscrollview = (HorizontalScrollView) findViewById(R.id.hv_head_view);
        rg_nav_content = (RadioGroup) findViewById(R.id.rg_nav_content);
        ll_content = (LinearLayout) findViewById(R.id.ll_content);
        rl_empty = (RelativeLayout) findViewById(R.id.rl_empty);
        rl_empty.setVisibility(View.GONE);
//        reScrollView = (SuperSwipeRefreshLayout) findViewById(R.id.refresh_top);
        mRefreshLayout = (RefreshLayout) findViewById(R.id.act_staff_query_refresh_layout);
        initListener();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(UPDATE_CHANGE_MASTER);
            }
        }, 300);
    }

    private String next_or_id;//记录下级标题栏的or_id   解决顶部导航栏点击选择问题
    private String next_or_name;//记录下级标题栏的or_id   解决顶部导航栏点击选择问题

    public void initListener() {

        rg_nav_content.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                try {
                    RadioButton button = (RadioButton) rg_nav_content.getChildAt(checkedId);
                    String key = button.getText().toString();
                    String or_id = keystore.get(key).toString();
                    next_or_id = or_id;
                    next_or_name = key;
                    /**@注释：取父一级菜单  */
                    int parent = 0;
                    for (int i = 0; i < tabTitle.size(); i++) {
                        if (key.equals(tabTitle.get(i))) {
                            parent = i - 1;
                        }
                    }
                    if (parent < 0) {
                        parent = 0;
                        next_or_id = null;
                        next_or_name = null;
                    }
                    key = tabTitle.get(parent);
                    or_id = keystore.get(key).toString();
                    /**@注释：取父一级菜单  */

                    Log.i(TAG, "key:" + key);
                    Log.i(TAG, "or_id:" + or_id);
                    tabTitle = parseHashMap(keystore, key);
                    if ("root".equals(or_id)) {
                        LoadRootHrorgFromServer();
                    } else {
                        LoadLeafHrorgFromServer(or_id, LOAD_SUCCESS_LEAFHRORG);
                    }
                    hscrollview.smoothScrollTo(
                            (checkedId > 1 ? ((RadioButton) rg_nav_content.getChildAt(checkedId)).getLeft() : 0)
                                    - ((RadioButton) rg_nav_content.getChildAt(2)).getLeft(),
                            0);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        /**@注释：左边栏点击事件  */
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                if (adapter != null) {
                    adapter.setMode(LIST_LEFT_MODE);
                }
                DetailItemAdapter.ModelItem item = (DetailItemAdapter.ModelItem) view.getTag();
                String or_id = String.valueOf(item.or_id);    // 是叶子节点的给予提示
                String or_name = item.tv_name.getText().toString();
                if ("部门".equals(item.tv_type.getText().toString())) {
                    String lastTitle = tabTitle.get(tabTitle.size() - 1);
                    keystore.remove(lastTitle);
                    keystore.put(or_name, or_id);
                    tabTitle = parseHashMap(keystore, or_name);
                    if (adapter.getCount() != 0) {
                        adapter.setSelectPosition(position);/** @注释：选中行数 */
                        adapter.notifyDataSetChanged();
                    }
                    LoadLeafHrorgFromServer(or_id, LOAD_SUCCESS_LEAFHRORG_RIGHT);
                } else {
                    enterToBasicInfo(item);
//                    EnterChatAction(item);
                }
            }
        });

        /** @注释：右边列表的点击事件 */
        mlistleft.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final DetailItemAdapter.ModelItem item = (DetailItemAdapter.ModelItem) view.getTag();
                if ("部门".equals(item.tv_type.getText().toString())) {// 是叶子节点的给予提示
                    String or_id = String.valueOf(item.or_id);
                    String or_name = item.tv_name.getText().toString();
                    keystore.put(or_name, or_id);
                    tabTitle = parseHashMap(keystore, or_name);
                    hrorgs_parent_position = position;// 记住父级菜单的位置
                    LoadLeafHrorgFromServer(or_id, LOAD_SUCCESS_LEAFHRORG_RIGHTANDLEFT);/** @注释：还需要改变左侧列表 */
                } else {
                    enterToBasicInfo(item);
//                    EnterChatAction(item);
                }
            }
        });
        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                if (firstVisibleItem == 0)
//                    reScrollView.setEnabled(true);
//                else
//                    reScrollView.setEnabled(false);
                if (firstVisibleItem == 0) {
                    mRefreshLayout.setEnableRefresh(true);
                } else {
                    mRefreshLayout.setEnableRefresh(false);
                }
            }
        });
        mlistleft.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                if (firstVisibleItem == 0)
//                    reScrollView.setEnabled(true);
//                else
//                    reScrollView.setEnabled(false);
                if (firstVisibleItem == 0) {
                    mRefreshLayout.setEnableRefresh(true);
                } else {
                    mRefreshLayout.setEnableRefresh(false);
                }
            }
        });
        viewHead = LayoutInflater.from(this).inflate(R.layout.head_view, null);
//        reScrollView.setHeaderView(viewHead);
//        reScrollView.setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {
//            @Override
//            public void onRefresh() {
//                manager.deleteData(CommonUtil.getSharedPreferences(ct, "erp_master"));
//                LoadServerData(CommonUtil.getSharedPreferences(ct, "erp_master"), null);
//            }
//
//            @Override
//            public void onPullDistance(int distance) {
//                //TODO 下拉距离
//            }
//
//            @Override
//            public void onPullEnable(boolean enable) {
//                //TODO 下拉过程中，下拉的距离是否足够出发刷新
//            }
//        });
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                LogUtil.d("RefreshLayout","---------onRefresh()-----------");
                manager.deleteData(CommonUtil.getSharedPreferences(ct, "erp_master"));
                LoadServerData(CommonUtil.getSharedPreferences(ct, "erp_master"), null);
            }
        });

        voiceSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!StringUtil.isEmpty(s.toString())) {
                    searchData = searchEmployee(s.toString());
                    if (!ListUtils.isEmpty(searchData)) {
                        popupWindow = null;
                        showPopupWindow(voiceSearchView);
                        //隐藏
                        mRefreshLayout.getLayout().setVisibility(View.INVISIBLE);
                    }else{
                        //显示
                        mRefreshLayout.getLayout().setVisibility(View.VISIBLE);
                    }
                }else{
                    //显示
                    mRefreshLayout.getLayout().setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    //create by bitliker
    private void enterToBasicInfo(DetailItemAdapter.ModelItem item) {
        Friend friend = new Friend();
        friend.setTimeCreate((int) (System.currentTimeMillis() / 1000));
        friend.setOwnerId(MyApplication.getInstance().mLoginUser.getUserId());
        friend.setUserId(String.valueOf(item.imid));
        friend.setNickName(item.tv_name.getText().toString());
        friend.setPhone(item.mobile);
        friend.setDepart("");
        friend.setPosition(item.position);
        friend.setEmCode(item.em_code);
        friend.setPrivacy(item.email);
        friend.setCompanyId(0);
        friend.setRoomFlag(0);// 0朋友 1群组
        friend.setStatus(Friend.STATUS_UNKNOW);
        Intent intent = new Intent("com.modular.basic.BasicInfoActivity");
        intent.putExtra(AppConstant.EXTRA_NICK_CODE, friend.getPhone());
        intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
        intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
        intent.putExtra("friend", friend);
        startActivity(intent);
    }

    private void EnterChatAction(DetailItemAdapter.ModelItem item) {
        Friend friendOne = FriendDao.getInstance().getFriend(MyApplication.getInstance().mLoginUser.getUserId(),
                String.valueOf(item.imid));
        Friend friend = new Friend();
        friend.setUserId(String.valueOf(item.imid));
        friend.setOwnerId(MyApplication.getInstance().mLoginUser.getUserId());
        friend.setRemarkName(item.tv_name.getText().toString());//备注
        friend.setNickName(item.tv_name.getText().toString());//昵称
        friend.setDescription("企业通讯录人员");
        // timeSend作为取群聊离线消息的标志，所以要在这里设置一个初始值
        friend.setTimeSend(CalendarUtil.getSecondMillion());
        friend.setStatus(Friend.STATUS_FRIEND);
        if (friendOne == null) {
            FriendDao.getInstance().createOrUpdateFriend(friend);
        }
        Intent intent = new Intent("com.modular.message.ChatActivity");
        intent.putExtra("friend", friend);
        startActivity(intent);
    }

    public ArrayList<String> parseHashMap(Map<String, Object> vMap, String falgValue) {
        ArrayList<String> templist = new ArrayList<>();
        Map<String, Object> tempMap = new LinkedHashMap<String, Object>();
        Iterator<Map.Entry<String, Object>> iter = vMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            templist.add(key.toString());
            tempMap.put(key.toString(), val);
            if (key.toString().equals(falgValue))
                break;
        }
        keystore = tempMap;// 不加这句，会产生bug;a
        return templist;
    }

    /**
     * @注释：查询根节点
     */
    private void LoadRootHrorgFromServer() {
        mHandler.sendEmptyMessage(Constants.LOAD_SUCCESS);
    }

    /**
     * @注释：查询叶子节点
     */
    private void LoadLeafHrorgFromServer(String or_id, int handler_what) {
        Bundle bundle = new Bundle();
        bundle.putString("or_id", or_id);
        Message message = new Message();
        message.setData(bundle);
        message.what = handler_what;
        mHandler.sendMessage(message);
    }


    /**
     * @注释：关键字查询
     */
  /*  private void LoadEmployeeFromServer(String em_name) {
        Bundle bundle = new Bundle();
        bundle.putString("or_id", em_name);
        Message message = new Message();
        message.setData(bundle);
        message.what = LOAD_SUCCESS_EMPLOYEE;
        mHandler.sendMessage(message);
    }*/

    /**
     * @注释：详细信息
     */
  /*  private void LoadEmployeeInfoFromServer(String em_id) {
        Bundle bundle = new Bundle();
        bundle.putString("or_id", em_id);
        Message message = new Message();
        message.setData(bundle);
        message.what = LOAD_SUCCESS_EMPLOYEEINFO;
        mHandler.sendMessage(message);
    }*/

    /**
     * @author Administrator
     * @功能:初始化标题栏
     */
    private void initNavigationHSV(int num) {
        rg_nav_content.removeAllViews();
        for (int i = 0; i < num; i++) {
            RadioButton rb = (RadioButton) mInflater.inflate(R.layout.radio_staff_button, null);
            rb.setId(i);
            rb.setText(tabTitle.get(i));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            rb.setLayoutParams(params);
            if (num == (i + 1)) {
                rb.setCompoundDrawables(null, null, null, null);
            }
            rg_nav_content.addView(rb);
        }
    }


    /**
     * @author Administrator
     * @功能:加载通讯录数据
     */
    public void LoadServerData(String master, String lastdate) {
        if (StringUtil.isEmpty(master)) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(0);
            }
            ll_content.setVisibility(View.GONE);
            rl_empty.setVisibility(View.VISIBLE);
            return;
        }
        String url = CommonUtil.getAppBaseUrl(this) + "mobile/getAllHrorgEmps.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("master", master);
        if (lastdate == null) {
            lastdate = "";
        }
        param.put("lastdate", lastdate);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, LOAD_SUCCESS_ALLDATA, null, null, "get");
    }

    /**
     * @author Administrator
     * @功能:写入数据库 isFristLoad 标志是否是第一次加载(数据库是否有缓存);
     * 因为第一次，flag字段不管是更新或者是插入，都执行插入操作;
     */
    public void insertDataSqlite(boolean isFristLoad,
                                 List<HrorgsEntity> hrorgsEntities,
                                 List<EmployeesEntity> employeesEntities
            , String servertime) {
        if (isFristLoad) {
            manager.saveHrogrs(hrorgsEntities);
            manager.saveEmployees(employeesEntities);
        } else {
            synSqliteDataforServer(hrorgsEntities, employeesEntities);
        }
        manager.deleteHrogrsAndEmployees();
        Map<String, Object> dateCaches = new HashMap<String, Object>();
        String time = dateMinute(servertime);
        Log.i(TAG, "缓存时间相减后：" + time);
        dateCaches.put("ed_lastdate", time);
        dateCaches.put("ed_kind", "通讯录");
        dateCaches.put("ed_company", CommonUtil.getSharedPreferences(ct, "erp_commpany"));
        dateCaches.put("ed_whichsys", CommonUtil.getSharedPreferences(ct, "erp_master"));
        String db_time = manager.select_getCacheTime(new String[]{}, "ed_company=? and ed_whichsys=?");
        if (db_time != null) {
            manager.updateCacheTime(dateCaches);
        } else {
            manager.saveCacheTime(dateCaches);
        }
    }

    /**
     * @author Administrator
     * @功能:时间减法
     */
    private String dateMinute(String datetime) {
        String str = datetime;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 2);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }

    /**
     * @author Administrator
     * @功能:同步数据
     */
    private void synSqliteDataforServer(List<HrorgsEntity> hrorgsEntities, List<EmployeesEntity> employeesEntities) {
        if (!hrorgsEntities.isEmpty()) {
            List<HrorgsEntity> insertHrorgsList = new ArrayList<HrorgsEntity>();
            List<HrorgsEntity> updateHrorgsList = new ArrayList<HrorgsEntity>();
            for (int i = 0; i < hrorgsEntities.size(); i++) {
                HrorgsEntity entity = hrorgsEntities.get(i);
                String or_code = entity.getOr_code();
                String whichsys = entity.getWhichsys();
                if ("UPDATE".equals(entity.getFlag())) {
                    HrorgsEntity hentity = manager.select_getRootData(new String[]{or_code, whichsys}, "or_code=? and whichsys=?");
                    if (hentity != null) {
                        updateHrorgsList.add(entity);
                    } else {
                        insertHrorgsList.add(entity);
                    }
                } else if ("INSERT".equals(entity.getFlag())) {
                    insertHrorgsList.add(entity);
                }
            }
            manager.saveHrogrs(insertHrorgsList);
            manager.updateHrogrs(updateHrorgsList);
        }
        if (!employeesEntities.isEmpty()) {
            List<EmployeesEntity> insertEmployeesList = new ArrayList<EmployeesEntity>();
            List<EmployeesEntity> updateEmployeesList = new ArrayList<EmployeesEntity>();
            for (int i = 0; i < employeesEntities.size(); i++) {
                EmployeesEntity eEntity = employeesEntities.get(i);
                String em_code = eEntity.getEM_CODE();
                String whichsys = eEntity.getWHICHSYS();
                if (StringUtil.isEmpty(em_code)) return;
                if ("UPDATE".equals(eEntity.getFLAG())) {
                    List<EmployeesEntity> tempEntity = manager.select_getEmployee(new String[]{em_code, whichsys}, "em_code=? and whichsys=?");
                    if (tempEntity.isEmpty()) {
                        insertEmployeesList.add(eEntity);
                    } else {
                        updateEmployeesList.add(eEntity);
                    }
                } else if ("INSERT".equals(eEntity.getFLAG())) {
                    insertEmployeesList.add(eEntity);
                }
            }
            manager.saveEmployees(insertEmployeesList);
            manager.updateEmployees(updateEmployeesList);
        }
    }


    private PopupWindow popupWindow = null;

    @SuppressLint("WrongConstant")
    public void showPopupWindow(View parent) {
        View view = null;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pop_crm_list, null);
            final ListView plist =  view.findViewById(R.id.mList);
            final MySimpleAdapter adapter = new MySimpleAdapter(
                    this,
                    searchData,
                    R.layout.item_pop_employee,
                    new String[]{"item_name", "item_sub"}, new int[]{R.id.name_tv, R.id.sub_tv});
            plist.setAdapter(adapter);

            plist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, Object> mMap = (Map<String, Object>) adapter.getItem(position);
                    String name = mMap.get("item_name").toString();
                    String sub = mMap.get("item_sub").toString();
                    String depart = mMap.get("item_depart").toString();
                    String pos = mMap.get("item_position").toString();
                    String imId = mMap.get("item_imId").toString();
                    final String phone = mMap.get("item_phone").toString();


                    Friend friend = new Friend();
                    friend.setNickName(name);
                    friend.setPhone(phone);
                    friend.setDepart(depart);
                    friend.setPosition(pos);
                    friend.setUserId(imId);
                    Intent intent = new Intent("com.modular.basic.BasicInfoActivity");
                    intent.putExtra(AppConstant.EXTRA_NICK_CODE, friend.getPhone());
                    intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                    intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
                    intent.putExtra("friend", friend);
                    startActivity(intent);
                    closePoppupWindow();
                }
            });
            popupWindow = new PopupWindow(view, windowManager.getDefaultDisplay().getWidth(), windowManager.getDefaultDisplay().getHeight() / 3);

        }
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(false);
//        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                DisplayUtil.backgroundAlpha(ct, 1f);
//                //显示
//                mRefreshLayout.getLayout().setVisibility(View.VISIBLE);
//            }
//        });
//        DisplayUtil.backgroundAlpha(this, 1f);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        popupWindow.showAsDropDown(parent, windowManager.getDefaultDisplay().getWidth(), 0);
    }

    private void closePoppupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    List<Map<String, Object>> searchData;

    public List<Map<String, Object>> searchEmployee(String key) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = null;
        String master = CommonUtil.getSharedPreferences(ct, "erp_master");
        //select * from employees where em_name like '%优软移动终端部%' or em_position like '%优软移动终端部%' or em_defaultorname like '%优软移动终端部%'

        List<EmployeesEntity> eList = manager.select_getEmployee(
                new String[]{"%" + key + "%", "%" + key + "%", "%" + key + "%", master},
                "(em_name like ?  or em_position like ? or em_defaultorname like ? )and whichsys=?");
        LogUtil.prinlnLongMsg(TAG, JSON.toJSONString(eList));
        if (!ListUtils.isEmpty(eList)) {
            for (int i = 0; i < eList.size(); i++) {
                map = new HashMap<String, Object>();
                map.put("item_name", eList.get(i).getEM_NAME());
                map.put("item_sub", eList.get(i).getEM_DEPART() + ">" + eList.get(i).getEM_POSITION());
                map.put("item_depart", eList.get(i).getEM_DEPART());
                map.put("item_position", eList.get(i).getEM_POSITION());
                map.put("item_imId", eList.get(i).getEm_IMID());
                map.put("item_phone", eList.get(i).getEM_MOBILE());
                list.add(map);
            }
        }
        return list;
    }


    public static final int LIST_LEFT_MODE = 0;
    public static final int LIST_TOP_MODE = 2;
    public static final int LIST_RIGHT_MODE = 1;

    /**
     * @注释：右边列表
     */
    public class DetailItemAdapter extends BaseAdapter {
        private Context ct;
        private LayoutInflater inflater;
        private List<Hrorgs.HrorgItem> lists;
        private List<Hrorgs.Employee> employees;
        private int selectPosition;
        private int selectOrId;
        private int typeMode = LIST_RIGHT_MODE;

/*
        public DetailItemAdapter(Context ct, List<Hrorgs.HrorgItem> items) {
            this.ct = ct;
            this.inflater = LayoutInflater.from(ct);
            this.lists = items;
        }*/

        public DetailItemAdapter(Context ct, Hrorgs object) {
            this.ct = ct;
            this.inflater = LayoutInflater.from(ct);
            this.employees = object.getEmployees();
            this.lists = object.getHrorgs();
        }

        public void setMode(int typeMode) {
            this.typeMode = typeMode;
        }

        @Override
        public int getCount() {
            if (lists != null && employees != null) {
                return lists.size() + employees.size();
            } else if (lists != null) {
                return lists.size();
            } else if (employees != null) {
                return employees.size();
            }
            return 0;
        }

        public List<Hrorgs.Employee> getEmployees() {
            return employees;
        }

        public void setEmployees(List<Hrorgs.Employee> employees) {
            this.employees = employees;
        }

        @Override
        public Object getItem(int position) {
            if (lists != null && employees != null) {
                if (position < lists.size()) {
                    return lists.get(position);
                } else {
                    return employees.get(position);
                }
            } else if (lists != null) {
                return lists.get(position);
            } else if (employees != null) {
                return employees.get(position);
            }
            return null;
        }

        public int getSelectOrId() {
            return selectOrId;
        }

        public void setSelectOrId(int selectOrId) {
            this.selectOrId = selectOrId;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setSelectPosition(int position) {
            this.selectPosition = position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ModelItem item = null;
            if (convertView == null) {
                item = new ModelItem();
                convertView = inflater.inflate(R.layout.item_staff_tree, parent, false);
                item.iv_falg = (ImageView) convertView.findViewById(R.id.iv_item_falg);
                item.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                item.tv_type = (TextView) convertView.findViewById(R.id.tv_name_type);
                item.iv_enter = (ImageView) convertView.findViewById(R.id.iv_item_into);
                item.add_friend = (ImageView) convertView.findViewById(R.id.add_friend);
                item.phone = (ImageView) convertView.findViewById(R.id.phone);
                convertView.setTag(item);
            } else {
                item = (ModelItem) convertView.getTag();
            }
            // 三种情况
            if (lists != null && employees != null) {
                if (position < lists.size()) {
                    int count = lists.get(position).getOr_emcount();
                    if (count == 0) {
                        item.tv_name.setText(lists.get(position).getOr_name());
                    } else {
                        item.tv_name.setText(lists.get(position).getOr_name() + "(" + lists.get(position).getOr_emcount() + ")");
                    }
                    item.or_id = lists.get(position).getOr_id();
                    item.isleaf = lists.get(position).getOr_isleaf();
                    item.tv_type.setText("部门");
                    item.tv_type.setVisibility(View.INVISIBLE);
                    item.add_friend.setVisibility(View.GONE);
                    item.phone.setVisibility(View.GONE);
                    item.iv_enter.setImageResource(R.drawable.iconfont_jinru);
                } else {
                    //gongpengming
                    item.mobile = employees.get(position - lists.size()).em_mobile;
                    item.position = employees.get(position - lists.size()).em_position;
                    item.em_code = employees.get(position - lists.size()).em_code;
                    item.email = employees.get(position - lists.size()).em_email;

                    item.tv_name.setText(employees.get(position - lists.size()).em_name);
                    item.or_id = employees.get(position - lists.size()).getEm_id();
                    item.isleaf = 1;
                    item.imid = employees.get(position - lists.size()).getEm_imid();
                    item.tv_type.setText("联系人");
                    item.tv_type.setVisibility(View.INVISIBLE);
                    item.add_friend.setVisibility(View.GONE);
                    item.phone.setVisibility(View.GONE);
                    item.iv_enter.setImageResource(R.drawable.phone);
                    item.iv_enter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SystemUtil.phoneAction(ct, employees.get(position - lists.size()).getEm_mobile());
                            TopContactsDao.api().addGoodFriend( employees.get(position - lists.size()));
                        }
                    });
                }
            } else if (lists != null) {
                int count = lists.get(position).getOr_emcount();
                if (count == 0) {
                    item.tv_name.setText(lists.get(position).getOr_name());
                } else {
                    item.tv_name.setText(lists.get(position).getOr_name() + "(" + lists.get(position).getOr_emcount() + ")");
                }
                item.or_id = lists.get(position).getOr_id();
                item.isleaf = lists.get(position).getOr_isleaf();
                item.tv_type.setText("部门");
                item.tv_type.setVisibility(View.INVISIBLE);
                item.add_friend.setVisibility(View.GONE);
                item.phone.setVisibility(View.GONE);
            } else if (employees != null) {
                //gongpengming
                item.mobile = employees.get(position - lists.size()).em_mobile;
                item.position = employees.get(position - lists.size()).em_position;
                item.em_code = employees.get(position - lists.size()).em_code;
                item.email = employees.get(position - lists.size()).em_email;

                item.add_friend.setVisibility(View.VISIBLE);
                item.phone.setVisibility(View.VISIBLE);
                item.tv_name.setText(employees.get(position).em_name);
                item.or_id = employees.get(position).getEm_id();
                item.isleaf = 1;
                item.tv_type.setText("联系人");
                item.imid = employees.get(position).getEm_imid();
                item.tv_type.setVisibility(View.INVISIBLE);
                item.iv_enter.setImageResource(R.drawable.phone);
                item.iv_enter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SystemUtil.phoneAction(ct, employees.get(position - lists.size()).getEm_mobile());
                        TopContactsDao.api().addGoodFriend( employees.get(position - lists.size()));
                    }
                });
            }
            item.iv_falg.setVisibility(View.GONE);
            if (typeMode == LIST_LEFT_MODE) {
                if (position == selectPosition) {
                    convertView.setBackgroundColor(Color.parseColor("#f3f3f3"));
                } else {
                    convertView.setBackgroundColor(Color.parseColor("#ffffff"));
                }
                item.iv_enter.setVisibility(View.GONE);
            } else {
                if (typeMode == LIST_TOP_MODE) {
                    if (item.or_id == selectOrId) {
//                        Log.i(TAG, "or_id相等。。。。");
                        // convertView.setBackgroundResource(R.drawable.tongcheng_all_bg01);
                        convertView.setBackgroundColor(Color.parseColor("#f3f3f3"));
                    } else {
                        convertView.setBackgroundColor(Color.parseColor("#ffffff"));
                    }
                    item.iv_enter.setVisibility(View.GONE);
                } else {
                    convertView.setBackgroundColor(Color.parseColor("#f3f3f3"));
                }
            }
            return convertView;
        }

        class ModelItem {
            public int imid;
            public int or_id;
            public int isleaf;

            public String mobile;
            public String position;//职位
            public String em_code;
            public String email;


            public TextView tv_type;
            public ImageView iv_falg;
            public TextView tv_name;
            public ImageView iv_enter;
            public ImageView add_friend;
            public ImageView phone;
        }
    }

    /**
     * @desc:查询列表
     * @author：Arison on 2016/11/7
     */
    private class MySimpleAdapter extends SimpleAdapter {

        /**
         * Constructor
         *
         * @param context  The context where the View associated with this SimpleAdapter is running
         * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
         *                 Maps contain the data for each row, and should include all the entries specified in
         *                 "from"
         * @param resource Resource identifier of a view layout that defines the views for this list
         *                 item. The layout file should include at least those named views defined in "to"
         * @param from     A list of column names that will be added to the Map associated with each
         *                 item.
         * @param to       The views that should display column in the "from" parameter. These should all be
         *                 TextViews. The first N views in this list are given the values of the first N columns
         */
        public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            ImageView phone_img =  convertView.findViewById(R.id.phone_img);
            ImageView header_img =  convertView.findViewById(R.id.header_img);
            final Map<String, Object> mMap = (Map<String, Object>) getItem(position);
            phone_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SystemUtil.phoneAction(CompanyContactsActivity.this, mMap.get("item_phone").toString());
                    LogUtil.i("企业架构界面 拨打电话"+JSON.toJSONString(mMap));
                }
            });

            AvatarHelper.getInstance().display(mMap.get("item_imId").toString(), header_img, true, true);
            return convertView;
        }
    }
}
