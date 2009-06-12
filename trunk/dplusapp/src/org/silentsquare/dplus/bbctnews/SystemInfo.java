package org.silentsquare.dplus.bbctnews;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class SystemInfo {
	
	@PrimaryKey
    private String id;
	
	private long startUpTime;
	
	private int updateNum;

	public final String getId() {
		return id;
	}

	public final void setId(String id) {
		this.id = id;
	}

	public final long getStartUpTime() {
		return startUpTime;
	}

	public final void setStartUpTime(long startUpTime) {
		this.startUpTime = startUpTime;
	}

	public final int getUpdateNum() {
		return updateNum;
	}

	public final void setUpdateNum(int updateNum) {
		this.updateNum = updateNum;
	}

}
