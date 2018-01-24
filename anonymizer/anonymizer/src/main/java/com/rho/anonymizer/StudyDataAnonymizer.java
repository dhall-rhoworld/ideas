package com.rho.anonymizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.epam.parso.Column;
import com.epam.parso.SasFileReader;
import com.epam.parso.impl.SasFileReaderImpl;
import com.rho.rhover.daemon.DataFrame;

public class StudyDataAnonymizer {
	
	private final AnonymizationHelper anonymizationHelper;
	
	private int nextRecordId = 1;
	
	public StudyDataAnonymizer(AnonymizationHelper anonymizationHelper) {
		this.anonymizationHelper = anonymizationHelper;
	}

	public static void main(String[] args) {
		//File inputDir = new File("H:\\Data\\RhoVer\\MUPPITS\\Clinical");
		//File outputDir = new File("S:\\RhoFED\\NIAID\\DAIT\\General\\Bioinformatics\\Projects\\RhoVer\\Data\\AREPA");
		//StudyDataAnonymizer anonymizer = new StudyDataAnonymizer(new MuppitsAnonymizationHelper());
		
		File inputDir = new File("S:\\RhoFED\\ICAC2\\PROSE\\Statistics\\Data\\Complete");
		File outputDir = new File("S:\\RhoFED\\NIAID\\DAIT\\General\\Bioinformatics\\Projects\\RhoVer\\Data\\SNIFFLES");
		StudyDataAnonymizer anonymizer = new StudyDataAnonymizer(new ProseAnonymizationHelper());
		anonymizer.anonymizeFiles(inputDir, outputDir);
	}
	
	public void anonymizeFiles(File inputDir, File outputDir) {
		String[] fileNames = inputDir.list(new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				return name.endsWith(".sas7bdat");
			}
		});
		// 1D keys: Field name
		// 2D keys: Original field value
		// 2D values: Generated field value
		Map<String, Map<String, String>> generatedIdMap = new HashMap<String, Map<String, String>>();
		for (String inFileName : fileNames) {
			File inFile = new File(inputDir, inFileName);
			String outFileName = inFileName.substring(0, inFileName.lastIndexOf(".")) + ".csv";
			File outFile = new File(outputDir, outFileName);
			anonymizeFile(inFile, outFile, generatedIdMap);
//			if (true) {
//				break;
//			}
		}
	}
	
	public void anonymizeFile(File inFile, File outFile, Map<String, Map<String, String>> generatedIdMap) {
		System.out.println("Anonymizing file: " + inFile.getAbsolutePath());
		
		FileInputStream inStream = null;
		BufferedWriter writer = null;
		try {
			inStream = new FileInputStream(inFile);
			writer = new BufferedWriter(new FileWriter(outFile));
			SasFileReader reader = new SasFileReaderImpl(inStream);
			List<Column> cols = reader.getColumns();
			
			// Write field names to output
			int count = 0;
			for (Column col : cols) {
				String colName = col.getName();
				if (!anonymizationHelper.dropField(colName)) {
					count++;
					if (count > 1) {
						writer.write(",");
					}
					writer.write(colName);
				}
			}
			if (anonymizationHelper.generateRecordId()) {
				writer.write(",RECORDID");
			}
			writer.write("\n");
			
			// Write field labels to output
			count = 0;
			for (Column col : cols) {
				String colName = col.getName();
				if (!anonymizationHelper.dropField(colName)) {
					count++;
					if (count > 1) {
						writer.write(",");
					}
					writer.write(col.getLabel().replaceAll(",", " "));
				}
			}
			if (anonymizationHelper.generateRecordId()) {
				writer.write(",Internal Record ID");
			}
			writer.write("\n");
			
			// Write data to output
			Object[] data = reader.readNext();
			while (data != null) {
				count = 0;
				int i = 0;
				for (Column col : cols) {
					
					String colName = col.getName();
					if (!anonymizationHelper.dropField(colName)) {
						count++;
						if (count > 1) {
							writer.write(",");
						}
						if (data[i] == null) {
							writer.write(".");
						}
						else {
							String datum = data[i].toString();
							if (anonymizationHelper.fieldMustBeGenerated(colName)) {
								Map<String, String> subMap = generatedIdMap.get(colName);
								if (subMap == null) {
									subMap = new HashMap<String, String>();
									generatedIdMap.put(colName, subMap);
								}
								String newDatum = subMap.get(datum);
								if (newDatum == null) {
									newDatum = anonymizationHelper.generateValue(colName);
									subMap.put(datum, newDatum);
								}
								datum = newDatum;
							}
							datum = datum.replaceAll("[,\\n\\r]", " ");
							writer.write(datum);
						}
					}
					i++;
				}
				if (anonymizationHelper.generateRecordId()) {
					writer.write("," + nextRecordId);
					nextRecordId++;
				}
				writer.write("\n");
				data = reader.readNext();
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (Exception e) {
			System.err.println("Error processing file");
			e.printStackTrace();
		}
		finally {
			IOUtils.close(inStream);
			IOUtils.close(writer);
		}
	}
}
