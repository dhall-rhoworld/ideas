package com.rho.rhover.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.rho.rhover.common.study.StudyRepository;

@Service
public class StudyDbService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private DatasetRepository datasetRepository;
	
	@Autowired
	private DatasetVersionRepository datasetVersionRepository;
	
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	public StudyDbService() {
		
	}

	public File[] getDataFiles(DataLocation dataLocation) {
		File folder = new File(dataLocation.getFolderPath());
		return folder.listFiles(new DataFileNameFilter(dataLocation));
	}
	
	public Set<File> getDataFiles(Study study) {
		Set<File> files = new HashSet<>();
		for (DataLocation location : study.getDataLocations()) {
			File[] fileArray = getDataFiles(location);
			for (int i = 0; i < fileArray.length; i++) {
				files.add(fileArray[i]);
			}
		}
		return files;
	}
	
	public Collection<File> getModifiedDataFiles(DataLocation dataLocation) {
		Collection<File> mods = new ArrayList<>();
		File[] files = getDataFiles(dataLocation);
		if (files == null) {
			logger.warn("No data files found in " + dataLocation.getFolderPath());
			return mods;
		}
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String path = file.getAbsolutePath().replace("\\", "/");
			if (path.equals(dataLocation.getStudy().getQueryFilePath())) {
				continue;
			}
			Dataset dataset = datasetRepository.findByFilePath(path);
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
		if (files == null) {
			logger.warn("No data files found in " + dataLocation.getFolderPath());
			return mods;
		}
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String path = file.getAbsolutePath().replace("\\", "/");
			if (path.equals(dataLocation.getStudy().getQueryFilePath())) {
				continue;
			}
			Dataset dataset = datasetRepository.findByFilePath(path);
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
	
	public String generateDatasetVersionName(File file) {
		return formatter.format(new Date(file.lastModified()));
	}
	
	private static class DataFileNameFilter implements FilenameFilter {
		
		private final DataLocation dataLocation;
		
		private DataFileNameFilter(DataLocation dataLocation) {
			this.dataLocation = dataLocation;
		}

		@Override
		public boolean accept(File dir, String name) {
			return (dataLocation.getIncludeSasFiles() && name.endsWith(".sas7bdat")
					|| (dataLocation.getIncludeCsvFiles() && name.endsWith(".csv")));
		}
		
	}
}
