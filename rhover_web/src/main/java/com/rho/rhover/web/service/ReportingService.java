package com.rho.rhover.web.service;

import java.util.List;

import com.rho.rhover.web.reporting.DataLoadOverview;

public interface ReportingService {

	List<DataLoadOverview> getDataLoadOverviews();
}
