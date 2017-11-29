package com.rho.rhover.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.apache.bcel.classfile.Unknown;

import com.epam.parso.Column;
import com.epam.parso.SasFileReader;
import com.epam.parso.impl.SasFileReaderImpl;

public class DataFrame {

	private List<List<String>> data = new ArrayList<>();
	
	private List<String> colNames = new ArrayList<>();
	
	private List<String> colLabels = new ArrayList<>();
	
	private Map<String, Integer> colIndex = new HashMap<>();
	
	// Possible values: String, Integer, Double, Date, Mixed, Boolean, and Unknown.  Note that currently
	// date values are stored as strings as these values may be
	// formatted in many ways.
	private List<Class> dataTypes = new ArrayList<>();
	
	private int numRecords;
	
	private DataFrame() {
		
	}
	
	public static DataFrame extractSasData(File sasDataFile) {
		DataFrame df = new DataFrame();
		try {
			SasFileReader reader = new SasFileReaderImpl(new FileInputStream(sasDataFile));
			
			// Add columns
			List<Column> cols = reader.getColumns();
			int colNum = 0;
			for (Column col : cols) {
				df.colNames.add(col.getName());
				String label = col.getLabel();
				if (label == null) {
					label = col.getName();
				}
				df.colLabels.add(label);
				df.colIndex.put(col.getName(), colNum);
				colNum++;
			}
			
			// Infer data types
			Object[][] data = reader.readAll();
			df.numRecords = data.length;
			for (int j = 0; j < cols.size(); j++) {
				Set<String> values = new HashSet<>();
				int numericCount = 0;
				int continuousCount = 0;
				int dateCount = 0;
				int nullCount = 0;
				for (int i = 0; i < data.length; i++) {
					Object datum = data[i][j];
					if (datum == null) {
						nullCount++;
					}
					else {
						values.add(datum.toString());
						if (datum.toString().matches("^[0-9\\.]+$")) {
							numericCount++;
						}
						if (datum.toString().matches("^[0-9]+\\.[0-9]+$")) {
							continuousCount++;
						}
						if (datum.toString().toLowerCase().matches("^(mon|tue|wed|thu|fri|sat|sun).*[0-9]{4}$")) {
							dateCount++;
						}
					}
				}
				Class<?> type = UnknownType.class;
				if (nullCount < data.length) {
					if (values.size() == 2) {
						type = Boolean.class;
					}
					else {
						type = String.class;
						int nonNullCount = data.length - nullCount;
						if (numericCount == nonNullCount) {
							if (continuousCount > 0) {
								type = Double.class;
							}
							else {
								type = Integer.class;
							}
						}
						else if (dateCount == nonNullCount) {
							type = Date.class;
						}
						else if (numericCount > 0 || dateCount > 0) {
							type = MixedType.class;
						}
					}
				}
				df.dataTypes.add(type);
			}
			
			// Add columns
			for (int j = 0; j < cols.size(); j++) {
				
				// Create column list
				List<String> col = new ArrayList<>();
				df.data.add(col);
				
				// Populate column
				for (int i = 0; i < data.length; i++) {
					if (data[i][j] == null) {
						col.add(null);
					}
					else {
						col.add(data[i][j].toString());
					}
				}
			}
		}
		catch (Exception e) {
			String message = "Error reading SAS file " + sasDataFile.getName();
			throw new SourceDataException(message, e);
		}
		return df;
	}
	
	public int numCols() {
		return colNames.size();
	}
	
	public List<Class> getDataTypes() {
		return dataTypes;
	}
	
	public List<String> getColNames() {
		return colNames;
	}
	
	public List<String> getColLabels() {
		return colLabels;
	}

	public List<String> getField(String fieldName) {
		Integer idx = this.colIndex.get(fieldName);
		return this.data.get(idx);
	}
	
	public String getFieldAsCsv(String fieldName) {
		StringBuilder builder = new StringBuilder();
		int count = 0;
		List<String> field = getField(fieldName);
		for (String val : field) {
			count++;
			if (count > 1) {
				builder.append(",");
			}
			builder.append(val);
		}
		return builder.toString();
	}
	
	public Set<String> getUniqueValues(String fieldName) {
		Set s = new HashSet<>();
		s.addAll(getField(fieldName));
		return s;
	}
	
	public int numRecords() {
		return numRecords;
	}
	
	public static class UnknownType {
		
	}
	
	public static class MixedType {
		
	}
}
