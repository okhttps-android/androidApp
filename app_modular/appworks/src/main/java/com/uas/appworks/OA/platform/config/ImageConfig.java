package com.uas.appworks.OA.platform.config;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.uas.appworks.R;

/**
 * Created by Bitlike on 2017/12/4.
 */

public class ImageConfig {
private static DisplayImageOptions charitableImageOptions;

    public static DisplayImageOptions getCharitableImageOptions() {
        if (charitableImageOptions==null){
            charitableImageOptions = new DisplayImageOptions.Builder()
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .cacheInMemory(true).cacheOnDisc(true)
//                    .displayer(new RoundedBitmapDisplayer(10))
                    .resetViewBeforeLoading(false)
                    .showImageForEmptyUri(R.drawable.charitable_def_image)
                    .showImageOnFail(R.drawable.charitable_def_image)
                    .build();
        }
        return charitableImageOptions;
    }


}
