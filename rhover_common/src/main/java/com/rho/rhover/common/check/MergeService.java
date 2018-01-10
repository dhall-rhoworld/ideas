package com.rho.rhover.common.check;

import java.util.List;

import com.rho.rhover.common.study.FieldInstance;
import com.rho.rhover.common.study.MergeField;

public interface MergeService {

	String mergeToCsv(List<MergeField> mergeFields, List<FieldInstance> dataFields);
}
