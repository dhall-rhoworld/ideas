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

import com.epam.parso.Column;
import com.epam.parso.SasFileReader;
import com.epam.parso.impl.SasFileReaderImpl;

public class DataFrame {

	private List<List<?>> data = new ArrayList<>();
	
	private List<String> colNames = new ArrayList<>();
	
	private Map<String, Integer> colIndex = new HashMap<>();
	
	// Possible values: String, Integer, Double, Date.  Note that currently
	// data values are stored as strings as these values may be
	// formatted in many ways.
	private List<Class> dataTypes = new ArrayList<>();
	
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
				df.colIndex.put(col.getName(), colNum);
				colNum++;
			}
			
			// Infer data types
			Object[][] data = reader.readAll();
			for (int j = 0; j < cols.size(); j++) {
				boolean isNumeric = true;
				boolean isContinuous = false;
				boolean isDate = true;
				boolean allNull = true;
				for (int i = 0; i < data.length; i++) {
					Object datum = data[i][j];
					if (datum != null) {
						allNull = false;
						isNumeric = isNumeric && datum.toString().matches("^[0-9\\.]+$");
						isContinuous = isContinuous || datum.toString().matches("^[0-9]+\\.[0-9]+$");
						isDate = isDate && datum.toString().toLowerCase().matches("^(mon|tue|wed|thu|fri|sat|sun).*[0-9]{4}$");
					}
				}
				Class<?> type = String.class;
				if (!allNull) {
					if (isNumeric) {
						if (isContinuous) {
							type = Double.class;
						}
						else {
							type = Integer.class;
						}
					}
					else if (isDate) {
						type = Date.class;
					}
				}
				df.dataTypes.add(type);
			}
			
			// Add columns
			for (int j = 0; j < cols.size(); j++) {
				
				// Create column list
				List<?> col = null;
				Class<?> type = df.dataTypes.get(j);
				if (type.equals(String.class) || type.equals(Date.class)) {
					col = new ArrayList<String>();
				}
				else if (type.equals(Integer.class)) {
					col = new ArrayList<Integer>();
				}
				else if (type.equals(Double.class)) {
					col = new ArrayList<Double>();
				}
				df.data.add(col);
				
				// Populate column
				for (int i = 0; i < data.length; i++) {
					if (data[i][j] == null) {
						col.add(null);
					}
					else {
						if (type.equals(String.class) || type.equals(Date.class)) {
							((ArrayList<String>)col).add(data[i][j].toString());
						}
						else if (type.equals(Integer.class)) {
							((ArrayList<Integer>)col).add(Integer.parseInt(data[i][j].toString()));
						}
						else if (type.equals(Double.class)) {
							((ArrayList<Double>)col).add(Double.parseDouble(data[i][j].toString()));
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
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
	
	public List<?> getField(String fieldName) {
		Integer idx = this.colIndex.get(fieldName);
		return this.data.get(idx);
	}
	
	public Set<String> getUniqueValues(String fieldName) {
		Set s = new HashSet<>();
		s.addAll(getField(fieldName));
		return s;
	}
}
