package com.rho.rhover.common.check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rho.rhover.common.study.FieldInstance;

@Service
public class CorrelationServiceImpl implements CorrelationService {
	
	@Autowired
	private CorrelationRepository correlationRepository;

	@Override
	public Correlation getCorrelationWithAnyFieldOrder(FieldInstance fieldInstance1, FieldInstance fieldInstance2) {
		Correlation correlation = correlationRepository.findByFieldInstance1AndFieldInstance2(fieldInstance1, fieldInstance2);
		if (correlation == null) {
			correlation = correlationRepository.findByFieldInstance1AndFieldInstance2(fieldInstance2, fieldInstance1);
		}
		return correlation;
	}

	@Override
	public void save(Correlation correlation) {
		correlationRepository.save(correlation);
	}

}
