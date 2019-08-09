package com.uas.appworks.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.app.MyApplication;
import com.core.utils.CommonUtil;
import com.core.widget.ClearEditText;
import com.core.widget.listener.EditChangeListener;
import com.core.xmpp.utils.audio.voicerecognition.JsonParser;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.modular.apputils.activity.BaseNetActivity;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.uas.appworks.R;
import com.uas.appworks.datainquiry.Constants;
import com.uas.appworks.model.Schedule;

import java.util.ArrayList;
import java.util.List;

public class ScheduleSearchActivity extends BaseNetActivity {
    private final int LAST_KEY = 12;
    private RecyclerView mRecyclerView;
    private View emptyView;
    private String lastKey;//最后一次输入
    private ClearEditText mSearchEditText;
    private ScheduleAdapter mScheduleAdapter;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_schedule_search;
    }

    @Override
    protected void init() throws Exception {
        initView();

    }
    @Override
    public boolean needNavigation() {
        return false;
    }
    @Override
    protected String getBaseUrl() {
        return CommonUtil.getSchedulerBaseUrl();
    }

    private void initView() {
        setTitle("");
        View view = LayoutInflater.from(ct).inflate(R.layout.action_data_inquiry_list, null);
        view.findViewById(R.id.back).setOnClickListener(mOnClickListener);
        view.findViewById(R.id.data_inquiry_voice_iv).setOnClickListener(mOnClickListener);
        view.findViewById(R.id.data_inquiry_filter_iv).setVisibility(View.GONE);
        mSearchEditText = view.findViewById(R.id.data_inquiry_filter_et);
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_SEND
                        || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    searchEvent();
                    return true;
                }
                return false;
            }
        });
        mSearchEditText.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                searchEvent();
            }
        });
        ActionBar bar = this.getSupportActionBar();
        if (bar != null) {
            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            bar.setCustomView(view);
        }

        mRecyclerView = findViewById(R.id.mRecyclerView);
        emptyView = findViewById(R.id.emptyView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(ct, LinearLayout.VERTICAL));
    }

    private void getVoice() {
        RecognizerDialog dialog = new RecognizerDialog(this, null);
        dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        dialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        dialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String text = JsonParser.parseIatResult(recognizerResult.getResultString());
                String s = mSearchEditText.getText().toString() + CommonUtil.getPlaintext(text);
                mSearchEditText.setText(s);
                mSearchEditText.setSelection(s.length());
                if (b) {
                    searchEvent();
                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        });
        dialog.show();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (R.id.back == view.getId()) {
                onBackPressed();
            } else if (R.id.data_inquiry_voice_iv == view.getId()) {
                getVoice();
            }
        }
    };


    private void searchEvent() {
        String keyWord = TextUtils.isEmpty(mSearchEditText.getText()) ? "" : mSearchEditText.getText().toString();
        this.lastKey = keyWord;
        loadDataByKey();
    }

    private void loadDataByKey() {
        if (TextUtils.isEmpty(this.lastKey)){
            mRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            return;
        }
        requestHttp(new Parameter.Builder()
                        .record(11)
                        .addTag(LAST_KEY, this.lastKey)
                        .url("schedule/searchSchedule")
                        .addParams("keyword", this.lastKey)
                        .addParams("imid", MyApplication.getInstance().getLoginUserId())
                , mOnSmartHttpListener);
    }

    private OnSmartHttpListener mOnSmartHttpListener = new OnSmartHttpListener() {
        @Override
        public void onSuccess(int what, String message, Tags tag) throws Exception {
            if (tag != null & tag.get(LAST_KEY) != null && tag.get(LAST_KEY) instanceof String) {
                String keyword = (String) tag.get(LAST_KEY);
                if (keyword.equals(lastKey)) {
                    JSONObject jsonObject = JSON.parseObject(message);
                    if (JSONUtil.getBoolean(jsonObject, "success")) {
                        handlerData(JSONUtil.getJSONArray(jsonObject, "data"));
                    }

                }
            }

        }

        @Override
        public void onFailure(int what, String message, Tags tag) throws Exception {

        }
    };

    private void handlerData(JSONArray array) throws Exception {
        if (ListUtils.isEmpty(array)) {
            mRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            ArrayList<Schedule> schedules = new ArrayList<>();
            Schedule chche = null;
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = array.getJSONObject(i);
                chche = new Schedule(true);
                chche.setId(JSONUtil.getInt(object, "scheduleId"));
                chche.setType(JSONUtil.getText(object, "type"));
                chche.setAllDay(JSONUtil.getInt(object, "allDay"));
                chche.setRepeat(JSONUtil.getText(object, "repeat"));
                chche.setTitle(JSONUtil.getText(object, "title"));
                chche.setTag(JSONUtil.getText(object, "tag"));
                chche.setRemarks(JSONUtil.getText(object, "remarks"));
                chche.setStartTime(JSONUtil.getTime(object, "startTime"));
                chche.setEndTime(JSONUtil.getTime(object, "endTime"));
                chche.setWarnRealTime(JSONUtil.getTime(object, "warnRealTime"));
                chche.setWarnTime(JSONUtil.getInt(object, "warnTime"));
                chche.setAddress(JSONUtil.getText(object, "address"));
                chche.setStatus(JSONUtil.getText(object, "status"));
                schedules.add(chche);
            }
            setAdapter(schedules);
        }

    }


    private void setAdapter(List<Schedule> schedules) {
        if (mScheduleAdapter == null) {
            mScheduleAdapter = new ScheduleAdapter(schedules);
            mRecyclerView.setAdapter(mScheduleAdapter);
        } else {
            mScheduleAdapter.setSchedules(schedules);
            mScheduleAdapter.notifyDataSetChanged();
        }
    }

    private class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Schedule> mSchedules = null;

        public void setSchedules(List<Schedule> mSchedules) {
            this.mSchedules = mSchedules;
        }

        public ScheduleAdapter(List<Schedule> mSchedules) {
            this.mSchedules = mSchedules;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHoder(LayoutInflater.from(ct).inflate(R.layout.item_schedule_search, parent, false));

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (ListUtils.getSize(this.mSchedules) > position) {
                ViewHoder mViewHoder = (ViewHoder) holder;
                Schedule mSchedule = this.mSchedules.get(position);
                mViewHoder.titleTv.setText(TextUtils.isEmpty(mSchedule.getRemarks()) ? "" : mSchedule.getRemarks());
                mViewHoder.tagTv.setText(TextUtils.isEmpty(mSchedule.getTag()) ? "" : mSchedule.getTag());
                String startTime = DateFormatUtil.long2Str(mSchedule.getStartTime(), DateFormatUtil.YMD_HM);
                String endTime = DateFormatUtil.long2Str(mSchedule.getEndTime(), DateFormatUtil.YMD_HM);
                mViewHoder.timeTv.setText(startTime + " - " + endTime);
                mViewHoder.itemView.setTag(mSchedule);
                mViewHoder.itemView.setOnClickListener(mOnClickListener);
            }
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null && view.getTag() instanceof Schedule) {
                    Schedule mSchedule = (Schedule) view.getTag();
                    startActivity(new Intent(ct, SchedulerCreateActivity.class)
                            .putExtra(Constants.Intents.ENABLE, false)
                            .putExtra(Constants.Intents.MODEL, mSchedule));
                }
            }
        };


        @Override
        public int getItemCount() {
            return ListUtils.getSize(mSchedules);
        }

        class ViewHoder extends RecyclerView.ViewHolder {
            private TextView titleTv;
            private TextView tagTv;
            private TextView timeTv;

            public ViewHoder(View itemView) {
                super(itemView);
                titleTv = (TextView) itemView.findViewById(R.id.titleTv);
                tagTv = (TextView) itemView.findViewById(R.id.tagTv);
                timeTv = (TextView) itemView.findViewById(R.id.timeTv);

            }
        }

    }
}
