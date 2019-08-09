package com.core.xmpp.utils.audio;

public interface RecordListener {
	public void onRecordStart();
	public void onRecordCancel();
	public void onRecordSuccess(String filePath, int timeLen);
}
