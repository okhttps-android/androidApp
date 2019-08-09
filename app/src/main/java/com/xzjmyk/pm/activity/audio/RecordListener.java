package com.xzjmyk.pm.activity.audio;

public interface RecordListener {
	public void onRecordStart();
	public void onRecordCancel();
	public void onRecordSuccess(String filePath, int timeLen);
}
