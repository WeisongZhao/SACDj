package signalSACD.factory;

import java.util.Random;

import signalSACD.RealSignal;

public class RandomLines extends SignalFactory {

	private double number = 100.0;

	public RandomLines(double number) {
		super(new double[] {number});
		setParameters(new double[] {number});
	}

	@Override
	public String getName() {
		return "RandomLines";
	}

	@Override
	public String[] getParametersName() {
		return new String[] { "Number of lines" };
	}

	@Override
	public void setParameters(double[] parameters) {
		if (parameters.length >= 1) this.number = parameters[0];
	}

	@Override
	public double[] getParameters() {
		return new double[] { number };
	}

	@Override
	public void fill(RealSignal signal) {
		Random rand = new Random(12345);
		double Q = Math.sqrt(3)*1.5;
		for (int index = 0; index < number; index++) {
			double x1 = -rand.nextDouble() * nx;
			double x2 = nx + rand.nextDouble() * nx;
			double y1 = (-0.1 + rand.nextDouble() * 1.2) * ny;
			double y2 = (-0.1 + rand.nextDouble() * 1.2) * ny;
			double z1 = (-0.1 + rand.nextDouble() * 1.2) * nz;
			double z2 = (-0.1 + rand.nextDouble() * 1.2) * nz;
			double d = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);
			d = Math.sqrt(d);
			int n = (int) (d / 0.3);
			double dx = (x2 - x1) / d;
			double dy = (y2 - y1) / d;
			double dz = (z2 - z1) / d;
			for (int s = 0; s < n; s++) {
				double x = x1 + s * dx;
				int i = (int) Math.round(x);
				if (i >= 1 && i < nx-1) {
					double y = y1 + s * dy;
					int j = (int) Math.round(y);
					if (j >= 1 && j < ny-1) {
						double z = z1 + s * dz;
						int k = (int) Math.round(z);
						if (k >= 1 && k < nz-1) {
							for(int ii=i-1; ii<=i+1; ii++)
							for(int jj=j-1; jj<=j+1; jj++)
							for(int kk=k-1; kk<=k+1; kk++) {
								double p = 1.0 - Math.sqrt((x - i) * (x - i) + (y - j) * (y - j) + (z - k) * (z - k))/Q;
								signal.data[k][i + nx * j] =  Math.max(signal.data[k][i + nx * j], (float)(p*amplitude));
							}
						}
					}
				}
			}
		}
	}

}
