package com.rho.rhover.web.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rho.rhover.common.study.Field;

public class FieldDtoGroup implements Comparable<FieldDtoGroup> {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final Map<String, Integer> DATA_TYPE_ORDINALS = new HashMap<>();
	
	static {
		DATA_TYPE_ORDINALS.put("CONTINUOUS", 1);
		DATA_TYPE_ORDINALS.put("INTEGER", 2);
		DATA_TYPE_ORDINALS.put("CHARACTER", 3);
		DATA_TYPE_ORDINALS.put("DATE", 4);
		DATA_TYPE_ORDINALS.put("YES/NO", 5);
		DATA_TYPE_ORDINALS.put("MIXED", 6);
		DATA_TYPE_ORDINALS.put("UNKNOWN", 7);
	}

	private String dataType;
	
	private List<FieldDto> fieldDtos = new ArrayList<>();

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public List<FieldDto> getFieldDtos() {
		return fieldDtos;
	}

	public void setFieldDtos(List<FieldDto> fieldDtos) {
		this.fieldDtos = fieldDtos;
	}
	
	public void addFieldDto(FieldDto fieldDto) {
		fieldDtos.add(fieldDto);
	}

	@Override
	public int compareTo(FieldDtoGroup other) {
		logger.debug(this.dataType + " " + other.dataType);
		Integer thisOrdinal = DATA_TYPE_ORDINALS.get(this.getDataType());
		Integer otherOrdinal = DATA_TYPE_ORDINALS.get(other.getDataType());
		return thisOrdinal.compareTo(otherOrdinal);
	}
	
	public static List<FieldDtoGroup> toDtoGroups(Iterable<Field> fields) {
		Map<String, FieldDtoGroup> groupMap = new HashMap<>();
		for (Field field : fields) {
			FieldDtoGroup group = groupMap.get(field.getDisplayDataType());
			if (group == null) {
				group = new FieldDtoGroup();
				group.setDataType(field.getDisplayDataType());
				groupMap.put(field.getDisplayDataType(), group);
			}
			group.addFieldDto(new FieldDto(field));
		}
		List<FieldDtoGroup> groups = new ArrayList<>();
		for (String dataType : groupMap.keySet()) {
			groups.add(groupMap.get(dataType));
		}
		Collections.sort(groups);
		for (FieldDtoGroup group : groups) {
			Collections.sort(group.getFieldDtos());
		}
		return groups;
	}
}
