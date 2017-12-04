package com.rho.rhover.common.anomaly;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.rho.rhover.common.study.Field;

public class ObservationTest {

	public ObservationTest() {
		// TODO Auto-generated constructor stub
	}

	@Test
	public void testGenerateIdFieldValueHash() {
		Collection<IdFieldValue> values = new ArrayList<>();
		values.add(new IdFieldValue("123", new Field("abc", null, null, null)));
		values.add(new IdFieldValue("456", new Field("def", null, null, null)));
		String hashed = Observation.generateIdFieldValueHash(values);
		System.out.println(hashed);
	}
}
