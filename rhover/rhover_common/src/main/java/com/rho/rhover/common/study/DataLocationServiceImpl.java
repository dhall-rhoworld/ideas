package com.rho.rhover.common.study;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataLocationServiceImpl implements DataLocationService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DataLocationRepository dataLocationRepository;

	@Override
	public DataLocation findByDirectory(File directory) {
		String path = directory.getAbsolutePath();
		logger.debug("Looking for data loaction with path = " + path);
		DataLocation loc = dataLocationRepository.findByFolderPath(path);
		if (loc == null) {
			if (path.indexOf("\\") >= 0) {
				path = path.replaceAll("\\\\", "/");
				logger.debug("Trying path = " + path);
				loc = dataLocationRepository.findByFolderPath(path);
			}
		}
		return loc;
	}

}
