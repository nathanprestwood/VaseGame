package ufpe.cin.nmf2.vasegame.ngsi;

import java.util.Date;


public class Occurrence {

	  private Long mOccurrenceId;

	  private String title;

	  private Date mOccurrenceDate;

	  private User user;

	  private Integer occurrenceCode;

	public Long getOccurrenceId() {
		return mOccurrenceId;
	}

	public void setOccurrenceId(Long occurrenceId) {
		this.mOccurrenceId = occurrenceId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getOccurrenceDate() {
		return mOccurrenceDate;
	}

	public void setOccurrenceDate(Date occurrenceDate) {
		this.mOccurrenceDate = occurrenceDate;
	}

	public void setOccurenceCode(Integer occurenceCode) {
		this.occurrenceCode = occurenceCode;
	}

	public Integer getOccurenceCode() {
		return occurrenceCode;
	}
}
