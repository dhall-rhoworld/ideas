package com.rho.rhover.web.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rho.rhover.common.study.Field;

public class FieldDtoGroup implements Comparable<FieldDtoGroup> {
	
	private static final Map<String, Integer> DATA_TYPE_ORDINALS = new HashMap<>();
	
	static {
		DATA_TYPE_ORDINALS.put("Double", 1);
		DATA_TYPE_ORDINALS.put("Integer", 2);
		DATA_TYPE_ORDINALS.put("String", 3);
		DATA_TYPE_ORDINALS.put("Date", 4);
		DATA_TYPE_ORDINALS.put("MixedType", 5);
		DATA_TYPE_ORDINALS.put("UnknownType", 6);
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
		Integer thisOrdinal = DATA_TYPE_ORDINALS.get(this.getDataType());
		Integer otherOrdinal = DATA_TYPE_ORDINALS.get(other.getDataType());
		return thisOrdinal.compareTo(otherOrdinal);
	}
	
	public static List<FieldDtoGroup> toDtoGroups(Iterable<Field> fields) {
		Map<String, FieldDtoGroup> groupMap = new HashMap<>();
		for (Field field : fields) {
			FieldDtoGroup group = groupMap.get(field.getDataType());
			if (group == null) {
				group = new FieldDtoGroup();
				group.setDataType(field.getDataType());
				groupMap.put(field.getDataType(), group);
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
