package com.rho.rhover.daemon;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.junit.Test;

public class RhoverDaemonApplicationTests {

	@Test
	public void contextLoads() {
		double[] d1 = {5.0, 4.0, 3.0, 2.0, 1.0};
		double[] d2 = {1.0, 2.0, 3.0, 4.0, 5.0};
		PearsonsCorrelation cor = new PearsonsCorrelation();
		System.out.println("Correlation: " + cor.correlation(d1, d2));
	}

}
