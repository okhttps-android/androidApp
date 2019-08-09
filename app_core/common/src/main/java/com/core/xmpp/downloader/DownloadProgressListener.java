package com.core.xmpp.downloader;

import android.view.View;

public interface DownloadProgressListener {
	void onProgressUpdate(String imageUri, View view, int current, int total);
}
