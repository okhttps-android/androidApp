package com.modular.booking.activity.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.core.widget.CustomerListView;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.modular.booking.R;

import java.util.ArrayList;
import java.util.List;



public class BottomSheetActivity extends AppCompatActivity {
   
    BottomSheetLayout mBottomSheetLayout;
    Button button;
    ListPanel listPanel;
    CustomerListView lv_left;
    CustomerListView lv_right;
    List<String> data=new ArrayList<>();
    ItemTestDataAdapter itemTestDataAdapter;


     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test_bottomsheet);
         mBottomSheetLayout=findViewById(R.id.bottom_sheet_layout);
                 button=findViewById(R.id.bt_bottom);
         lv_left =findViewById(R.id.lv_left);
                 lv_right=findViewById(R.id.lv_right);
        for (int i = 0; i <15; i++) {
            data.add("姓名："+i);
        }
        itemTestDataAdapter=new ItemTestDataAdapter(this,data);
        lv_left.setAdapter(itemTestDataAdapter);
        
        lv_right.setAdapter(itemTestDataAdapter);
        
        
        listPanel=new ListPanel(this);
        mBottomSheetLayout.showWithSheetView(  listPanel);
        
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (! mBottomSheetLayout.isSheetShowing()){
                    listPanel.updateData(BottomSheetActivity.this);
                    mBottomSheetLayout.showWithSheetView(  listPanel);
                }else{
                    mBottomSheetLayout.dismissSheet();
                }
            }
        });
    }
}
