package job;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.study.Study;

@Entity
public class DataLoadJob {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="field_id")
	private Long dataLoadJobId;
	
	@ManyToOne
	@JoinColumn(name="study_id")
	private Study study;
	
	@Column(name="status")
	private String status;
	
	@Column(name="created_on")
	private Date date;
	
	@Column(name="created_by")
	private String userName;
	
	public DataLoadJob() {
		
	}

	public Long getDataLoadJobId() {
		return dataLoadJobId;
	}

	public void setDataLoadJobId(Long dataLoadJobId) {
		this.dataLoadJobId = dataLoadJobId;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
