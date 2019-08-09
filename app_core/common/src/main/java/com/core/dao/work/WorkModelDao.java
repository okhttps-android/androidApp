package com.core.dao.work;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.core.db.DatabaseManager;
import com.core.model.WorkModel;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitliker on 2016/12/12.
 */

public class WorkModelDao {

	private static WorkModelDao instance;
	private final String TIBLE_NAME = "workdata";


	public static WorkModelDao getInstance() {
		if (instance == null) {
			synchronized (WorkModelDao.class) {
				instance = new WorkModelDao();
			}
		}
		return instance;
	}

	private WorkModelDao() {

	}


	//TODO 修改待完成
	public boolean createOrUpdata(List<WorkModel> models, long time) {
		SQLiteDatabase db = null;
		try {
			db = DatabaseManager.getInstance().openDatabase();
			String emCode = CommonUtil.getEmcode();
			String master = CommonUtil.getMaster();
			db.beginTransaction();
			//插入或是更新
			for (int i = 0; i < models.size(); i++) {
				WorkModel e = models.get(i);
				ContentValues values = getValues(time, i, e);
				if (values == null) continue;
				long item = db.insert(TIBLE_NAME, null, values);
				if (item < 0) {
					String where = "id=?";
					String[] whereArgs = {String.valueOf(i)};
					db.update(TIBLE_NAME, values, where, whereArgs);
				}
			}
			//删除多余班次
			String where = "emCode=? and master=?  and id >=?";
			String[] whereArgs = {emCode, master, String.valueOf(models.size())};
			db.delete(TIBLE_NAME, where, whereArgs);
			db.setTransactionSuccessful();
			db.endTransaction();
			DatabaseManager.getInstance().closeDatabase();
			return true;
		} catch (Exception e) {
			return false;
		}
	}


	public List<WorkModel> query(boolean allSignin, long time) {
		List<WorkModel> models = new ArrayList<>();
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			String date = TimeUtils.s_long_2_str(time);
			String emCode = CommonUtil.getEmcode();
			String master = CommonUtil.getMaster();
			if (StringUtil.isEmpty(emCode) || StringUtil.isEmpty(master)) return null;
			db = DatabaseManager.getInstance().openDatabase();
			String[] columns = getColumns();
			String selection = "emCode=? and master=? and date=?";
			String[] selectionArg = {emCode, master, date};
			c = db.query(TIBLE_NAME, columns, selection, selectionArg, null, null, null);
			WorkModel model = null;
			while (c.moveToNext()) {
				model = getWorkModel(c, allSignin);
				if (model != null)
					models.add(model);
			}
		} catch (Exception e) {
			if (e != null) LogUtil.i("query Exception" + e.getMessage());
		} finally {
			if (c != null)
				c.close();
			DatabaseManager.getInstance().closeDatabase();
			return models;
		}
	}

	/**
	 * 获取数据库中全部数据
	 *
	 * @param allSignin 是否包含迟到和早退情况
	 * @return
	 */
	public List<WorkModel> query(boolean allSignin) {
		return query(allSignin, System.currentTimeMillis());
	}

	public List<WorkModel> queryAuto(){
		return query(false, System.currentTimeMillis());
	}


	/**
	 * 清除当天以前的数据
	 *
	 * @param date
	 * @return
	 */
	public long clearByDate(long date) {
		if (date <= 0) return -1;
		SQLiteDatabase db = null;
		long i = 0;
		try {
			String emCode = CommonUtil.getEmcode();
			String master = CommonUtil.getMaster();
			if (StringUtil.isEmpty(emCode) || StringUtil.isEmpty(master)) return 0;
			db = DatabaseManager.getInstance().openDatabase();
			String where = "emCode=? and master=?  and date=?";
			String[] whereArg = {emCode, master, TimeUtils.s_long_2_str(date)};
			i = db.delete(TIBLE_NAME, where, whereArg);
		} catch (Exception e) {
		} finally {
			DatabaseManager.getInstance().closeDatabase();
			return i;
		}
	}

	/**
	 * 更新一行数据   签到后使用
	 *
	 * @param models
	 * @return
	 */
	public long update(WorkModel models) {
		if (models == null) return 0;
		SQLiteDatabase db = null;
		long i = 0;
		try {
			String emCode = CommonUtil.getEmcode();
			String master = CommonUtil.getMaster();
			if (StringUtil.isEmpty(emCode) || StringUtil.isEmpty(master)) return 0;
			ContentValues values = getValues(System.currentTimeMillis(), -1, models);
			if (values == null) return -1;
			db = DatabaseManager.getInstance().openDatabase();
			String where = "emCode=? and master=?  and workStart=? and workTime=?" +
					" and workend=? and offStart=? and offTime=? and offend=? and date=?";
			String[] whereArg = {emCode, master, models.getWorkStart(),
					models.getWorkTime(), models.getWorkend()
					, models.getOffStart(), models.getOffTime(),
					models.getOffend(), DateFormatUtil.long2Str(DateFormatUtil.YMD)};
			i = db.update(TIBLE_NAME, values, where, whereArg);
		} catch (Exception e) {
		} finally {
			DatabaseManager.getInstance().closeDatabase();
			return i;
		}
	}


	private ContentValues getValues(long time, int id, WorkModel e) {
		String emCode = CommonUtil.getEmcode();
		String master = CommonUtil.getMaster();
		if (StringUtil.isEmpty(emCode) || StringUtil.isEmpty(master)) return null;
		if (isEmptyByOne(e.getWorkStart(), e.getWorkTime(), e.getWorkend(), e.getOffStart(), e.getOffTime(), e.getOffend()))
			return null;
		ContentValues values = new ContentValues();
		if (id != -1)
			values.put("id", id);
		values.put("emCode", emCode);
		values.put("master", master);
		values.put("date", TimeUtils.s_long_2_str(time));

		values.put("workStart", e.getWorkStart());
		values.put("workTime", e.getWorkTime());
		values.put("workend", e.getWorkend());
		values.put("workAllegedly", e.getWorkAllegedly());

		values.put("offStart", e.getOffStart());
		values.put("offTime", e.getOffTime());
		values.put("offend", e.getOffend());
		values.put("offAllegedly", e.getOffAllegedly());

		values.put("leaveAlarm", e.isLeaveAlarm() ? 1 : 0);
		values.put("workAlarm", e.isWorkAlarm() ? 1 : 0);
		values.put("offAlarm", e.isLeaveAlarm() ? 1 : 0);
		values.put("isNextDay", e.isNextDay() ? 1 : 0);

		if (!StringUtil.isEmpty(e.getWorkSignin())
				&& (e.getWorkSignin().compareTo(e.getWorkStart()) < 0 ||
				e.getWorkSignin().compareTo(e.getWorkend()) > 0))
			values.put("workSignin", "");
		else values.put("workSignin", e.getWorkSignin());
		if (!StringUtil.isEmpty(e.getOffSignin()) && (e.getOffSignin().compareTo(e.getOffStart()) < 0 ||
				e.getOffSignin().compareTo(e.getOffend()) > 0))
			values.put("offSignin", "");
		else values.put("offSignin", e.getOffSignin());
		return values;
	}

	private WorkModel getWorkModel(Cursor c, boolean showAllSignin) {
		if (c == null)
			return null;
		int id = c.getInt(c.getColumnIndex("id"));//编号
		String workStart = c.getString(c.getColumnIndex("workStart"));//上班开始时间
		String workTime = c.getString(c.getColumnIndex("workTime"));//上班时间
		String workend = c.getString(c.getColumnIndex("workend")); //上班计算结束时间
		String workSignin = c.getString(c.getColumnIndex("workSignin")); //上班签到时间
		String workAllegedly = c.getString(c.getColumnIndex("workAllegedly"));//下班计算开始时间
		boolean workAlarm = c.getInt(c.getColumnIndex("workAlarm")) > 0;//下班时间
		String offStart = c.getString(c.getColumnIndex("offStart"));//下班结束时间
		String offTime = c.getString(c.getColumnIndex("offTime"));//下班签到时间
		String offend = c.getString(c.getColumnIndex("offend"));//下班签到时间
		String offSignin = c.getString(c.getColumnIndex("offSignin"));//下班签到时间
		String offAllegedly = c.getString(c.getColumnIndex("offAllegedly"));//下班签到时间
		boolean isNextDay = c.getInt(c.getColumnIndex("isNextDay")) > 0;//是否是跨天
		boolean offAlarm = c.getInt(c.getColumnIndex("offAlarm")) > 0;//下班提醒
		boolean leaveAlarm = c.getInt(c.getColumnIndex("leaveAlarm")) > 0;//离开提醒
		if (StringUtil.isEmpty(workStart) || StringUtil.isEmpty(workTime) || StringUtil.isEmpty(workend) ||
				StringUtil.isEmpty(offStart) || StringUtil.isEmpty(offTime) || StringUtil.isEmpty(offend))
			return null;

		if (!showAllSignin) {
			if (!StringUtil.isEmpty(workSignin) && workSignin.compareTo(workTime) > 0)//迟到  打卡时间>上班时间
				workSignin = "";
			if (!StringUtil.isEmpty(offSignin) && offSignin.compareTo(offTime) < 0)//早退  打卡时间<下班时间
				offSignin = "";
		}
		return new WorkModel(id, workStart, workTime,
				workend, workSignin,
				workAllegedly,
				workAlarm,
				offStart,
				offTime,
				offend,
				offSignin,
				offAllegedly,
				offAlarm,
				leaveAlarm,
				isNextDay
		);
	}

	private String[] getColumns() {
		return new String[]{"id", "workStart", "workTime", "workend", "workSignin", "workAllegedly", "workAlarm", "offStart"
				, "offTime", "offend", "offSignin", "offAllegedly", "offAlarm", "leaveAlarm", "isNextDay"};
	}

	private boolean isEmptyByOne(String... args) {
		for (String e : args)
			if (StringUtil.isEmpty(e)) return true;
		return false;
	}
}
