package com.core.dao;

import android.util.Log;

import com.core.app.MyApplication;
import com.core.db.SQLiteHelper;
import com.core.model.Company;
import com.core.model.User;
import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.sql.SQLException;

/**
 * 访问用户数据的Dao，包括访问两个实体，User和UserDetail
 * 
 * 
 */
public class UserDao {
	private static UserDao instance = null;

	public static final UserDao getInstance() {
		if (instance == null) {
			synchronized (UserDao.class) {
				if (instance == null) {
					instance = new UserDao();
				}
			}
		}
		return instance;
	}

	public Dao<User, String> userDao;
	public Dao<Company, Integer> companyDao;

	private UserDao() {
		try {
			OrmLiteSqliteOpenHelper helper = OpenHelperManager.getHelper(MyApplication.getInstance(), SQLiteHelper.class);
			userDao = DaoManager.createDao(helper.getConnectionSource(), User.class);
			companyDao = DaoManager.createDao(helper.getConnectionSource(), Company.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		OpenHelperManager.releaseHelper();
	}

	/**
	 * 保存当前登陆的用户（基本信息）,并将Id赋值给参数user
	 * 
	 * @param user
	 * @return
	 */
	public boolean saveUserLogin(User user) {
		try {
			User existUser = userDao.queryForId(user.getUserId());
			if (existUser != null) {// 用户已经存在,只更新2个字段
				UpdateBuilder<User, String> builder = userDao.updateBuilder();
				builder.updateColumnValue("telephone", user.getTelephone());
				builder.updateColumnValue("password", user.getPassword());
				// builder.updateColumnValue("userId", user.getUserId());
				builder.where().idEq(user.getUserId());
				userDao.update(builder.prepare());
			} else {
				userDao.create(user);
			}
			userDao.refresh(user);// 加载其他信息到user中
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public User getUserByUserId(String userId) {
		try {
			return userDao.queryForId(userId);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void updateCountryId(String userId, int value) {
		updateColumnValue(userId, "countryId", value + "");
	}

	public void updateProvinceId(String userId, int value) {
		updateColumnValue(userId, "provinceId", value + "");
	}

	public void updateCityId(String userId, int value) {
		updateColumnValue(userId, "cityId", value + "");
	}

	public void updateAreaId(String userId, int value) {
		updateColumnValue(userId, "areaId", value + "");
	}

	public void updateNickName(String userId, String value) {
		updateColumnValue(userId, "nickName", value);
	}

	public void updateSex(String userId, String value) {
		updateColumnValue(userId, "sex", value);
	}

	public void updateBirthday(String userId, String value) {
		updateColumnValue(userId, "birthday", value);
	}

	public void updateDescription(String userId, String value) {
		updateColumnValue(userId, "description", value);
	}
	public void updateUnLineTime(String userId, long value) {
		updateColumnValue(userId, "offlineTime", value+"");
	}
	public void updateColumnValue(String userId, String columnName, String value) {
		UpdateBuilder<User, String> builder = userDao.updateBuilder();
		try {
			builder.updateColumnValue(columnName, value);
			builder.where().idEq(userId);
			userDao.update(builder.prepare());
			
			User user = userDao.queryForId(userId);
			
			Log.d("roamer", "user.sex:" + user.getSex());
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			closeCursor();
			userDao.closeableIterator().closeQuietly();
		}
	}

	private void closeCursor() {
		CloseableIterator<User> iterator =userDao.iterator();
		AndroidDatabaseResults results= (AndroidDatabaseResults) iterator.getRawResults();
		results.close();
	}

	/**
	 * 该方法只适用于调用了获取用户资料接口，得到User对象，来更新本地数据库。<br/>
	 * 在其他地方使用会造成不准确的后果<br/>
	 * 用UserBean来更新
	 */
	public boolean updateByUser(User user) {
		if (user == null) {
			return false;
		}
		try {
			User existUser = userDao.queryForId(user.getUserId());
			if (existUser != null) {// 用户已经存在,保留数据库的某些字段
				user.setPassword(existUser.getPassword());// 密码
			} else {// 用户不存在，这是个错误的情况
				return false;
			}
			if (user.getCompany() != null) {
				companyDao.createOrUpdate(user.getCompany());
			}
			userDao.update(user);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	public void updateUserCompany(Company company) {
		if (company != null) {
			try {
				companyDao.createOrUpdate(company);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// public User getUserByTelephone(String telephone) {
	// try {
	// List<User> users = userDao.queryForEq("telephone", telephone);
	// if (users != null && users.size() > 0) {
	// return users.get(0);
	// }
	// } catch (SQLException e) {
	// e.printStackTrace();
	// } catch (NullPointerException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

}
