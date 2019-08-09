package com.xzjmyk.pm.activity.bean.circle;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * 
 * @项目名称: SkWeiChat-Baidu
 * @包名: com.xzjmyk.pm.activity.bean.circle
 * @作者:王阳
 * @创建时间: 2015年10月15日 下午5:02:11
 * @描述: 赞的实体
 * @SVN版本号: $Rev$
 * @修改人: $Author$
 * @修改时间: $Date$
 * @修改的内容: TODO
 */
public class Praise implements Serializable{
	private static final long serialVersionUID = -7269886584765396602L;
	
	private String praiseId;
	private String userId;//赞的人的id
	@JSONField(name="nickname")
	private String nickName;//赞的人的昵称
	public String getPraiseId() {
		return praiseId;
	}
	public void setPraiseId(String praiseId) {
		this.praiseId = praiseId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickname) {
		this.nickName = nickname;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Praise praise = (Praise) o;

		if (!userId.equals(praise.userId)) return false;
		return !(nickName != null ? !nickName.equals(praise.nickName) : praise.nickName != null);

	}

	@Override
	public int hashCode() {
		int result = userId.hashCode();
		result = 31 * result + (nickName != null ? nickName.hashCode() : 0);
		return result;
	}
}
