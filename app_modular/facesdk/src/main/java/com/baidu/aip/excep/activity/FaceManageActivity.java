package com.baidu.aip.excep.activity;

import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.aip.excep.utils.FaceConfig;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.MyApplication;
import com.core.model.EmployeesEntity;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.core.widget.listener.EditChangeListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.activity.BaseNetActivity;
import com.modular.apputils.manager.ContactsManager;
import com.modular.apputils.widget.VeriftyDialog;

import java.util.ArrayList;
import java.util.List;

import demo.face.aip.baidu.com.facesdk.R;

/**
 * 管理人脸数据列表
 */
public class FaceManageActivity extends BaseNetActivity {
    private List<EmployeesEntity> allDatas = null;
    private FaceAdapter mFaceAdapter = null;
    private PullToRefreshListView refreshListView;
    private String groupId;
    private EmptyLayout mEmptyLayout;
    private VoiceSearchView voiceSearchView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_face_manage;
    }

    @Override
    protected void init() throws Exception {
        initView();

    }

    @Override
    protected String getBaseUrl() {
        return "https://aip.baidubce.com/";
    }

    private void initView() {
        refreshListView = (PullToRefreshListView) findViewById(com.modular.apputils.R.id.refreshListView);
        mEmptyLayout = new EmptyLayout(this, refreshListView.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        voiceSearchView = (VoiceSearchView) findViewById(com.modular.apputils.R.id.voiceSearchView);
        voiceSearchView.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                search(TextUtils.isEmpty(s) ? "" : s.toString());
            }
        });
        voiceSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                search(TextUtils.isEmpty(v.getText()) ? "" : v.getText().toString());
                return false;
            }
        });

        groupId = CommonUtil.getEnuu(ct);
        loadFaceList();
    }

    private void search(String keyWord) {
        if (!ListUtils.isEmpty(allDatas)) {
            if (TextUtils.isEmpty(keyWord)) {
                setAdapter(allDatas);
            } else {
                List<EmployeesEntity> showDatas = new ArrayList<>();
                for (EmployeesEntity e : allDatas) {
                    String word = e.getEM_MOBILE() + "" + e.getEM_NAME();
                    if (word.contains(keyWord)) {
                        showDatas.add(e);
                    }
                }
                setAdapter(showDatas);
            }
        } else {
            setAdapter(allDatas);
        }
    }


    private void loadFaceList() {
        showProgress();
        FaceConfig.loadToken(new FaceConfig.FaceTokenListener() {
            @Override
            public void callBack(String accessToken) {
                LogUtil.i("gong", "accessToken=" + accessToken);
                httpClient.Api().send(new HttpClient.Builder()
                        .url("rest/2.0/face/v3/faceset/group/getusers")
                        .add("access_token", accessToken)
                        .header("Content-Type", "application/json")
                        .add("group_id", groupId)
                        .add("start", 0)
                        .add("length", 1000)
                        .isDebug(true)
                        .method(Method.POST).build(), new ResultSubscriber<>(new Result2Listener<Object>() {
                    @Override
                    public void onResponse(Object o) {
                        try {
                            LogUtil.i("gong", "o==" + o.toString());
                            JSONObject object = JSON.parseObject(o.toString());
                            handlerUserList(JSONUtil.getJSONArray(JSONUtil.getJSONObject(object, "result"), "user_id_list"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Object t) {
                        LogUtil.i("gong", "onFailure=" + t.toString());
                        dismissProgress();
                    }
                }));
            }
        });
    }

    private void deleteUserFace(final EmployeesEntity model) {
        showProgress();
        FaceConfig.loadToken(new FaceConfig.FaceTokenListener() {
            @Override
            public void callBack(String accessToken) {
                LogUtil.i("gong", "accessToken=" + accessToken);
                httpClient.Api().send(new HttpClient.Builder()
                        .url("rest/2.0/face/v3/faceset/user/delete")
                        .add("access_token", accessToken)
                        .header("Content-Type", "application/json")
                        .add("group_id", groupId)
                        .add("user_id", String.valueOf(model.getEm_IMID()))
                        .isDebug(true)
                        .method(Method.POST).build(), new ResultSubscriber<>(new Result2Listener<Object>() {
                    @Override
                    public void onResponse(Object o) {
                        try {
                            LogUtil.i("gong", "o==" + o.toString());
                            JSONObject object = JSON.parseObject(o.toString());
                            handlerDelete(JSONUtil.getInt(object, "error_code") == 0, model);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dismissProgress();
                    }

                    @Override
                    public void onFailure(Object t) {
                        LogUtil.i("gong", "onFailure=" + t.toString());
                        dismissProgress();
                    }
                }));
            }
        });
    }

    private void handlerDelete(boolean deleteOk, EmployeesEntity model) {
        if (deleteOk) {
            ToastUtil.showToast(ct, R.string.delete_succeed_notice1);
            allDatas.remove(model);
            search(TextUtils.isEmpty(voiceSearchView.getText()) ? "" : voiceSearchView.getText().toString());
            faceDeleteErp(model);
        } else {
            ToastUtil.showToast(ct, R.string.delete_failed);
        }
    }

    /**
     * 向erp删除人脸
     */
    private void faceDeleteErp(EmployeesEntity model) {
        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(this),
                new HttpParams.Builder()
                        .url("mobile/updateUploadPictureSign.action")
                        .method(Method.POST)
                        .addParam("master", CommonUtil.getMaster())
                        .addParam("em_imid", model.getEm_IMID())
                        .addParam("emcode", model.getEM_CODE())
                        .addParam("em_uploadsign", 0)
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {

                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {

                    }
                });
    }

    private void handlerUserList(final JSONArray userListArray) throws Exception {
        if (ListUtils.isEmpty(userListArray)) {
            mEmptyLayout.showEmpty();
            dismissProgress();
        } else {
            ContactsManager.getInstance().loadContact(new ContactsManager.OnEmployListener() {
                @Override
                public void callback(List<EmployeesEntity> employees) {

                    if (ListUtils.isEmpty(employees)) {
                        ToastUtil.showToast(ct, R.string.not_load_ok_fefresh);
                    } else {
                        List<EmployeesEntity> hasFaceModels = new ArrayList<>();
                        List<String> notHasUser = new ArrayList<>();

                        for (int i = 0; i < userListArray.size(); i++) {
                            String userId = userListArray.getString(i);
                            boolean hasUser = false;
                            for (EmployeesEntity e : employees) {
                                String imid = String.valueOf(e.getEm_IMID());
                                if (e != null && !TextUtils.isEmpty(imid) && !TextUtils.isEmpty(userId) &&
                                        userId.equals(imid)) {
                                    hasFaceModels.add(e);
                                    hasUser = true;
                                    break;
                                }
                            }
                            if (!hasUser) {
                                notHasUser.add(userId);
                            }
                        }
                        LogUtil.i("gong", "notHasUser=" + JSON.toJSONString(notHasUser));
                        if (ListUtils.isEmpty(hasFaceModels)) {
                            ToastUtil.showToast(ct, R.string.not_load_ok_fefresh);
                        } else {
                            allDatas = hasFaceModels;
                            setAdapter(hasFaceModels);
                        }
                    }
                    dismissProgress();
                }
            });
        }
    }

    private void setAdapter(final List<EmployeesEntity> faceModels) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ListUtils.isEmpty(faceModels)) {
                    mEmptyLayout.showEmpty();
                } else {
                    if (mFaceAdapter == null) {
                        mFaceAdapter = new FaceAdapter(faceModels);
                        refreshListView.setAdapter(mFaceAdapter);
                    } else {
                        mFaceAdapter.setFaceModels(faceModels);
                        mFaceAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        if (!ListUtils.isEmpty(faceModels)) {
            LogUtil.i("gong", "faceModels=" + faceModels.size());
        }
    }

    private class FaceAdapter extends BaseAdapter {
        private List<EmployeesEntity> faceModels;

        public FaceAdapter(List<EmployeesEntity> faceModels) {
            this.faceModels = faceModels;
        }

        public List<EmployeesEntity> getFaceModels() {
            return faceModels;
        }

        public void setFaceModels(List<EmployeesEntity> faceModels) {
            this.faceModels = faceModels;
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(faceModels);
        }

        @Override
        public Object getItem(int position) {
            return faceModels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mViewHolder = null;
            if (convertView == null) {
                mViewHolder = new ViewHolder();
                convertView = LayoutInflater.from(ct).inflate(R.layout.item_face_list, null);
                mViewHolder.deleteTv = (TextView) convertView.findViewById(R.id.deleteTv);
                mViewHolder.nameTv = (TextView) convertView.findViewById(R.id.nameTv);
                mViewHolder.phoneTv = (TextView) convertView.findViewById(R.id.phoneTv);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }
            EmployeesEntity model = faceModels.get(position);
            mViewHolder.nameTv.setText(TextUtils.isEmpty(model.getEM_NAME()) ? "" : model.getEM_NAME());
            mViewHolder.phoneTv.setText(TextUtils.isEmpty(model.getEM_MOBILE()) ? "" : model.getEM_MOBILE());
            mViewHolder.deleteTv.setTag(model);
            mViewHolder.deleteTv.setOnClickListener(mOnClickListener);
            return convertView;
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null && v.getTag() != null && v.getTag() instanceof EmployeesEntity) {
                    EmployeesEntity model = (EmployeesEntity) v.getTag();
                    showDeleteDialog(model);
                }
            }
        };

        private class ViewHolder {
            private TextView deleteTv;
            private TextView nameTv;
            private TextView phoneTv;
        }
    }


    private void showDeleteDialog(final EmployeesEntity model) {
        new VeriftyDialog.Builder(ct)
                .setTitle(getString(R.string.app_name))
                .setContent("确定删除" + model.getEM_NAME() + "的脸照？")
                .setShowCancel(true)
                .build(new VeriftyDialog.OnDialogClickListener() {
                    @Override
                    public void result(boolean clickSure) {
                        if (clickSure) {
                            deleteUserFace(model);
                        }
                    }


                });
    }


}
