package org.silentsquare.dplus.bbctnews;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UpdateStat {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	/**
	 * System.currentTimeMillis() when the corresponding update process starts.
	 */
	private long startTime;
	
	/**
	 * System.currentTimeMillis() when the corresponding update process finishes.
	 */
	private long finishTime;
	
	/**
	 * Total news retrieved.
	 */
	private int totalNum; 
	
	/**
	 * The number of news that are new since the last update.
	 */
	private int newNum;
	
	/**
	 * The number of news that are new since the last update, but whose coordinates 
	 * are located in the previous updates. This indicates the effect of coordinate caching.
	 */
	private int coordinateHitNum;

	public final Long getId() {
		return id;
	}

	public final void setId(Long id) {
		this.id = id;
	}

	public final long getStartTime() {
		return startTime;
	}

	public final void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public final long getFinishTime() {
		return finishTime;
	}

	public final void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	public final int getTotalNum() {
		return totalNum;
	}

	public final void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}

	public final int getNewNum() {
		return newNum;
	}

	public final void setNewNum(int newNum) {
		this.newNum = newNum;
	}

	public final int getCoordinateHitNum() {
		return coordinateHitNum;
	}

	public final void setCoordinateHitNum(int coordinateHitNum) {
		this.coordinateHitNum = coordinateHitNum;
	}
	
}
