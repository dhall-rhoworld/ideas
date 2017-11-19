package com.rho.rhover.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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
//			SasFileReader reader = new SasFileReaderImpl(new FileInputStream("\\S:\\RhoFED\\CTOT-SACCC\\CTOT\\CTOT-08-Abecassis\\Stats\\Data\\Clinical\\vitlmstr.sas7bdat"));
//			List<Column> columns = reader.getColumns();
//			for (Column column : columns) {
//				System.out.println("name: " + column.getName() + ", label: " + column.getLabel() + ", format: " + column.getFormat() +
//						", type: " + column.getType().getName());
//			}
//			
//			Object[][] data = reader.readAll();
//			for (int i = 0; i < data.length; i++) {
//				for (int j = 0; j < data[i].length; j++) {
//					if (j > 0) {
//						System.out.print(",");
//					}
//					System.out.print(data[i][j]);
//				}
//				System.out.println();
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		DataFrame df = DataFrame.extractSasData(new File("\\S:\\RhoFED\\CTOT-SACCC\\CTOT\\CTOT-08-Abecassis\\Stats\\Data\\Clinical\\itrtmstr.sas7bdat"));
//		List<?> vals = df.getField("CMENDA_RAW");
//		for (Object val : vals) {
//			if (val == null) {
//				System.out.println("It is null");
//			}
//			else {
//				System.out.println(val);
//			}
//		}
//		//DataFrame df = DataFrame.extractSasData(new File("S:/RhoFED/ICAC2/PROSE/Statistics/Data/Complete/vsgp.sas7bdat"));
//		List<String> colNames = df.getColNames();
//		List<Class> dataTypes = df.getDataTypes();
//		for (int i = 0; i < df.numCols(); i++) {
//			System.out.println(colNames.get(i) + ": " + dataTypes.get(i).getName());
//		}
//		List<Integer> ids = df.getIntegerField("ID");
//		System.out.println("Num IDs: " + ids.size());
//		for (Integer id : ids) {
//			System.out.println(id);
//		}
//		String s = "S:\\RhoFED\\CTOT";
//		System.out.println(s.replaceAll("\\", "/"));
//		Integer i = new Integer(5);
//		System.out.println(i.getClass().getSimpleName());
		try {
			Integer i = Integer.parseInt("fred");
		}
		catch (NumberFormatException e) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			System.out.println(stringWriter.toString());
		}
	}

}
