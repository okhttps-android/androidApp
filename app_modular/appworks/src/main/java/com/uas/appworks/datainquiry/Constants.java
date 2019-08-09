package com.uas.appworks.datainquiry;

import android.os.Environment;

/**
 * Created by RaoMeng on 2017/8/16.
 */
public interface Constants {
    interface Intents {
        String MODEL = "model";
        String ENABLE = "isEnable";
    }

    interface CONSTANT {
        String PDF_FILE_NAME = "statement.pdf";
        String PDF_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/UU/statementFile";

        String DATA_INQUIRY_MENU_CACHE = "data_inquiry_menu_cache";
        String REPORT_QUERY_MENU_CACHE = "report_query_menu_cache";

        String DATA_INQUIRY_MENU_RECENT_CACHE = "data_inquiry_menu_recent_cache";
        String REPORT_QUERY_MENU_RECENT_CACHE = "report_query_menu_recent_cache";

        //打印成功
        int DOWNLOAD_SUCCESS = 1;
        //打印失败
        int DOWNLOAD_FAILED = 2;
        //打印过载
        int PDF_OVERLOAD = 3;
        //打印进度
        int DOWNLOAD_PROGRESS = 4;

    }
}
