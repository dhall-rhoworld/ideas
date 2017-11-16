package com.rho.rhover.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.parso.Column;
import com.epam.parso.SasFileReader;
import com.epam.parso.impl.SasFileReaderImpl;
import com.rho.rhover.common.study.DataLocation;
import com.rho.rhover.common.study.Dataset;
import com.rho.rhover.common.study.DatasetRepository;
import com.rho.rhover.common.study.DatasetVersion;
import com.rho.rhover.common.study.DatasetVersionRepository;
import com.rho.rhover.common.study.Study;

@Service
public class StudyDbService {
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;
	
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	public StudyDbService() {
		
	}

	public File[] getDataFiles(DataLocation dataLocation) {
		File folder = new File(dataLocation.getFolderPath());
		return folder.listFiles(new DataFileNameFilter());
	}
	
	public Collection<File> getModifiedDataFiles(DataLocation dataLocation) {
		Collection<File> mods = new ArrayList<>();
		File[] files = getDataFiles(dataLocation);
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			Dataset dataset = datasetRepository.findByFilePath(file.getAbsolutePath());
			if (dataset != null) {
				String versionName = generateDatasetVersionName(file);
				DatasetVersion datasetVersion = datasetVersionRepository.findByDatasetAndIsCurrent(dataset, Boolean.TRUE);
				if (!versionName.equals(datasetVersion.getDatasetVersionName())) {
					mods.add(file);
				}
			}
		}
		return mods;
	}
	
	public Collection<File> getModifiedDataFiles(Study study) {
		Collection<File> mods = new ArrayList<>();
		for (DataLocation location : study.getDataLocations()) {
			mods.addAll(getModifiedDataFiles(location));
		}
		return mods;
	}
	
	public Collection<File> getNewDataFiles(DataLocation dataLocation) {
		Collection<File> mods = new ArrayList<>();
		File[] files = getDataFiles(dataLocation);
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			Dataset dataset = datasetRepository.findByFilePath(file.getAbsolutePath());
			if (dataset == null) {
				mods.add(file);
			}
		}
		return mods;
	}
	
	public Collection<File> getNewDataFiles(Study study) {
		Collection<File> mods = new ArrayList<>();
		for (DataLocation location : study.getDataLocations()) {
			mods.addAll(getNewDataFiles(location));
		}
		return mods;
	}
	
	public List<FieldInfo> getFieldInfo(File file) {
		List<FieldInfo> info = new ArrayList<>();
		try {
			SasFileReader reader = new SasFileReaderImpl(new FileInputStream(file));
			List<Column> cols = reader.getColumns();
			for (Column col : cols) {
				FieldInfo fieldInfo = new FieldInfo();
				fieldInfo.setFieldName(col.getName());
				String label = col.getLabel();
				if (label == null || label.length() == 0) {
					fieldInfo.setFieldLabel(null);
				}
				else {
					fieldInfo.setFieldLabel(label);
				}
				info.add(fieldInfo);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return info;
	}
	
	public List<Class> inferDataTypes(File file) {
		return null;
	}
	
	public List<String> getStringFieldValues(File file, String colName) {
		return null;
	}
	
	private String generateDatasetVersionName(File file) {
		return formatter.format(new Date(file.lastModified()));
	}
	
	private static class DataFileNameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".sas7bdat");
		}
		
	}
}
