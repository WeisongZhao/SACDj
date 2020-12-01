package signalSACD.range;

import deconvolutionSACDlab.monitor.Monitors;
import signalSACD.RealSignal;

public class IdentityRange extends AbstractRange {

	protected Monitors monitors;
	
	public IdentityRange(Monitors monitors) {
		super(monitors);
	}
	
	public void apply(RealSignal x) {

	}
}
