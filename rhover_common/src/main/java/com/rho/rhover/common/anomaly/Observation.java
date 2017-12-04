package com.rho.rhover.common.anomaly;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.Site;
import com.rho.rhover.common.study.Subject;

@Entity
public class Observation {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="observation_id")
	private Long observationId;
	
	@ManyToOne
	@JoinColumn(name="dataset_id")
	private Dataset dataset;
	
	@Column(name="id_field_value_hash")
	private String idFieldValueHash;

	public Observation() {
		
	}

	public Observation(Dataset dataset, String idFieldValueHash) {
		super();
		this.dataset = dataset;
		this.idFieldValueHash = idFieldValueHash;
	}

	public Long getObservationId() {
		return observationId;
	}

	public void setObservationId(Long observationId) {
		this.observationId = observationId;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public String getIdFieldValueHash() {
		return idFieldValueHash;
	}

	public void setIdFieldValueHash(String idFieldValueHash) {
		this.idFieldValueHash = idFieldValueHash;
	}

	public static String generateIdFieldValueHash(Collection<IdFieldValue> idFieldValues) {
		List<IdFieldValue> list = new ArrayList<>();
		list.addAll(idFieldValues);
		Collections.sort(list, new IdFieldValueComparator());
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (IdFieldValue val : list) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			builder.append(val.getField().getFieldName() + ":" + val.getValue());
		}
		String digested = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(builder.toString().getBytes());
			byte[] bytes = digest.digest();
			builder = new StringBuilder();
			for (byte b : bytes) {
				//builder.append(String.format("02x", b & 0xff));
				builder.append(Integer.toHexString(0xFF & b));
			}
			digested = builder.toString();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return digested;
	}
	
	private static final class IdFieldValueComparator implements Comparator<IdFieldValue> {

		@Override
		public int compare(IdFieldValue f1, IdFieldValue f2) {
			return f1.getField().getFieldName().compareTo(f2.getField().getFieldName());
		}
		
	}
}
