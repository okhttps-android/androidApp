package com.uas.appme.settings.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.modular.apputils.widget.VeriftyDialog;
import com.uas.appme.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择ip界面
 */
public class SelectIpActivity extends BaseActivity {
    private final String IP_TAG = "当前IP地址为：";
    private TextView currentIpTv;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_select);
        initView();
    }

    private void initView() {
        currentIpTv = (TextView) findViewById(R.id.currentIpTv);
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(ct, LinearLayout.VERTICAL));
        mRecyclerView.setAdapter(new ListAdapter());
        currentIpTv.setText(IP_TAG + CommonUtil.getAppBaseUrl(ct));
        currentIpTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new VeriftyDialog.Builder(ct)
                        .setTitle(getString(R.string.app_name))
                        .setContent("是否需要复制当前的IP地址 ?")
                        .build(new VeriftyDialog.OnDialogClickListener() {
                            @Override
                            public void result(boolean clickSure) {
                                if (clickSure) {
                                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    // 将文本内容放到系统剪贴板里。
                                    ClipData clipData = ClipData.newPlainText("ERP Ip  地址", CommonUtil.getAppBaseUrl(ct));
                                    cm.setPrimaryClip(clipData);
                                }
                            }
                        });
                return false;
            }
        });
    }


    private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        private List<SelectBean> selectBeans;

        public ListAdapter() {
            selectBeans = new ArrayList<>();
            selectBeans.add(new SelectBean("陈金兴", "http://192.168.253.52:8080/ERP/"));
            selectBeans.add(new SelectBean("黄信", "http://192.168.253.35:8080/ERP/"));
            selectBeans.add(new SelectBean("刘鹏", "http://192.168.253.243:8080/ERP/"));
            selectBeans.add(new SelectBean("UAS研发系统（测试）", "https://218.17.158.219:9443/uas_dev/"));
            selectBeans.add(new SelectBean("周鼎文", "http://192.168.253.52:8080/ERP/"));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(ct).inflate(R.layout.item_select_list, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            SelectBean bean = selectBeans.get(i);
            viewHolder.name_tv.setText(bean.getName() + ":" + bean.getIp());
            viewHolder.itemView.setTag(bean);
            viewHolder.itemView.setOnClickListener(mOnClickListener);
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() != null && view.getTag() instanceof SelectBean) {
                    final SelectBean bean = (SelectBean) view.getTag();
                    new VeriftyDialog.Builder(ct)
                            .setTitle(getString(R.string.app_name))
                            .setContent("是否确定将当前erp的Ip地址变成\n" +
                                    bean.getName() + ":" + bean.getIp() + " ?")
                            .build(new VeriftyDialog.OnDialogClickListener() {
                                @Override
                                public void result(boolean clickSure) {
                                    if (clickSure) {
                                        CommonUtil.setSharedPreferences(ct, "erp_baseurl", bean.getIp());
                                        currentIpTv.setText(IP_TAG + CommonUtil.getAppBaseUrl(ct));
                                    }
                                }
                            });
                }
            }
        };

        @Override
        public int getItemCount() {
            return ListUtils.getSize(selectBeans);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name_tv;

            public ViewHolder(View itemView) {
                super(itemView);
                name_tv = itemView.findViewById(R.id.name_tv);
            }
        }
    }

    private class SelectBean {
        String name;
        String ip;

        public SelectBean(String name, String ip) {
            this.name = name;
            this.ip = ip;
        }

        public String getIp() {
            return ip;
        }

        public String getName() {
            return name;
        }
    }
}
