package com.rho.rhover.common.study;

import java.io.File;

public interface DataLocationService {

	DataLocation findByDirectory(File directory);
}
