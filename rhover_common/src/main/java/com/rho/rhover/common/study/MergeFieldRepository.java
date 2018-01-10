package com.rho.rhover.common.study;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface MergeFieldRepository extends CrudRepository<MergeField, Long> {

	@Query("select mf from MergeField mf where mf.fieldInstance1.dataset = ?1 and mf.fieldInstance2.dataset = ?2")
	List<MergeField> findByDataset1AndDataset2(Dataset dataset1, Dataset dataset2);
}
