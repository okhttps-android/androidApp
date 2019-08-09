package com.uas.appworks.OA.platform.model;

import java.util.List;

/**
 * Created by Bitlike on 2017/11/15.
 */

public class CharitActModel {

	private String actImg;
	private int id;
	private String name;
	private String stage;
	private String subTitle;
	private List<AwardsBean> awards;

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public String getActImg() {
		return actImg;
	}

	public void setActImg(String actImg) {
		this.actImg = actImg;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}



	public List<AwardsBean> getAwards() {
		return awards;
	}

	public void setAwards(List<AwardsBean> awards) {
		this.awards = awards;
	}

	public static class AwardsBean {

		private String awardLevel;
		private String awardName;
		private long endTime;
		private int id;
		private long startTime;



		public String getAwardLevel() {
			return awardLevel;
		}

		public void setAwardLevel(String awardLevel) {
			this.awardLevel = awardLevel;
		}

		public String getAwardName() {
			return awardName;
		}

		public void setAwardName(String awardName) {
			this.awardName = awardName;
		}

		public long getEndTime() {
			return endTime;
		}

		public void setEndTime(long endTime) {
			this.endTime = endTime;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}
	}
}
