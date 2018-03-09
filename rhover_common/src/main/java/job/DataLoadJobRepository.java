package job;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.rho.rhover.common.study.Study;

public interface DataLoadJobRepository extends CrudRepository<DataLoadJob, Long> {

	List<DataLoadJob> findByStudyAndStatus(Study study, String status);
}
