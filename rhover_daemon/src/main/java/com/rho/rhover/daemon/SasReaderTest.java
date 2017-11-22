package com.rho.rhover.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.epam.parso.Column;
import com.epam.parso.SasFileProperties;
import com.epam.parso.SasFileReader;
import com.epam.parso.impl.SasFileReaderImpl;

public class SasReaderTest {

	public static void main(String[] args) {
//		try {
//			Writer writer = new FileWriter("C:\\Temp\\sas.csv");
//			SasFileReader reader = new SasFileReaderImpl(new FileInputStream("S:\\RhoFED\\ICAC2\\PROSE\\Statistics\\Data\\Complete\\pdr.sas7bdat"));
//			List<Column> columns = reader.getColumns();
//			int count = 0;
//			for (Column column : columns) {
//				count++;
//				if (count > 1) {
//					writer.write(",");
//					System.out.print(",");
//				}
//				writer.write(column.getName());
//				//System.out.print(column.getName());
//			}
//			writer.write("\n");
//			//System.out.println();
//			
//			Object[][] data = reader.readAll();
//			for (int i = 0; i < data.length; i++) {
//				for (int j = 0; j < data[i].length; j++) {
//					if (j > 0) {
//						writer.write(",");
//						System.out.print(",");
//					}
//					if (data[i][j] != null) {
//						writer.write(data[i][j].toString());
//					}
//					//System.out.print(data[i][j]);
//				}
//				writer.write("\n");
//				//System.out.println();
//			}
//			writer.close();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		DataFrame df = DataFrame.extractSasData(new File("S:\\RhoFED\\ICAC2\\PROSE\\Statistics\\Data\\Complete\\pdr.sas7bdat"));
//		List<?> vals = df.getField("CMENDA_RAW");
//		for (Object val : vals) {
//			if (val == null) {
//				System.out.println("It is null");
//			}
//			else {
//				System.out.println(val);
//			}
//		}
		DataFrame df = DataFrame.extractSasData(new File("S:\\RhoFED\\ICAC2\\PROSE\\Statistics\\Data\\Complete\\pdr.sas7bdat"));
		List<String> colNames = df.getColNames();
		List<Class> dataTypes = df.getDataTypes();
		for (int i = 0; i < df.numCols(); i++) {
			System.out.println(colNames.get(i) + ": " + dataTypes.get(i).getName());
		}
		List<?> data = df.getField("PDR_q7a1c");
		//System.out.println("Num IDs: " + ids.size());
		for (Object datum : data) {
			System.out.println(datum);
		}
//		String s = "S:\\RhoFED\\CTOT";
//		System.out.println(s.replaceAll("\\", "/"));
//		Integer i = new Integer(5);
//		System.out.println(i.getClass().getSimpleName());
//		try {
//			Integer i = Integer.parseInt("fred");
//		}
//		catch (NumberFormatException e) {
//			StringWriter stringWriter = new StringWriter();
//			PrintWriter printWriter = new PrintWriter(stringWriter);
//			e.printStackTrace(printWriter);
//			System.out.println(stringWriter.toString());
//		}
	}

}
