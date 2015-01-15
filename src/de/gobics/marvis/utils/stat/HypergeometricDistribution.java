package de.gobics.marvis.utils.stat;

/**
 * The awesome new HypergeometricDistribution
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class HypergeometricDistribution {

	private static final double[] cof = new double[]{
		76.18009172947146, -86.50532032941677,
		24.01409824083091, -1.231739572450155,
		0.12086509738661e-2, -0.000005395239384953
	};
	private final int overall_good;
	private final int overall_bad;
	private final double logfact_good;
	private final double logfact_bad;
	private final double logfact_ovall_size;
	private final int overall_size;

	public HypergeometricDistribution(int overall_size, int overall_good) {
		this.overall_size = overall_size;
		this.overall_good = overall_good;
		this.overall_bad = overall_size - overall_good;

		logfact_good = logfact(overall_good);
		logfact_bad = logfact(overall_bad);
		logfact_ovall_size = logfact(overall_size);
	}

	/**
	 * Calculates the probability that, given an urn with {@code good} "good" of {@code overall_size}
	 * balls, while drawing {@code set_size} ball to fetch
	 * {@code i} or more "good" balls.
	 *
	 * @param good
	 * @param set_size
	 * @param overall_size
	 * @param i
	 * @return
	 */
	public double hypergeom(int set_size, int i) {
		double loghyp1 = logfact_bad + logfact_good + logfact(set_size) + logfact(overall_bad + overall_good - set_size);
		double loghyp2 = logfact(i) + logfact(overall_good - i) + logfact(overall_bad + i - set_size) + logfact(set_size - i) + logfact_ovall_size;
		double pv = Math.exp(loghyp1 - loghyp2);
		if (Double.isNaN(pv)) {
			throw new RuntimeException("Can not calculate: hypergeom(" + overall_size + ", " + overall_good + ", " + set_size + ", " + i);
		}
		return pv;
	}

	public static double gamma(int xx) {
		int y = xx;
		int x = xx;
		double tmp = x + 5.5;
		//System.out.println("tmp1 = " + tmp);
		tmp -= (x + 0.5) * Math.log(tmp);
		//System.out.println("tmp2 = " + tmp);
		double ser = 1.000000000190015;
		//System.out.println("ser = " + ser);

		for (int idx = 0; idx < cof.length; idx++) {
			y++;
			//System.out.println(ser + " += " + cof[idx] + " / " + y);
			ser += cof[idx] / y;
			//System.out.println("ser" + idx + " = " + ser);
		}
		//System.out.println(2.5066282746310005 * ser / xx);
		return -1 * tmp + Math.log(2.5066282746310005 * ser / x);
	}

	public static double logfact(int n) {
		return gamma(n + 1);
	}

	/**
	 * Calculates the likelihood that, given an urn with {@code good} "good" of {@code overall_size}
	 * balls, while drawing {@code set_size} ball to fetch
	 * {@code i} or more "good" balls.
	 *
	 * @param good
	 * @param set_size
	 * @param overall_size
	 * @param i
	 * @return
	 */
	public static double hypergeom(int good, int set_size, int overall_size, int i) {
		int bad = overall_size - good;
		double loghyp1 = logfact(bad) + logfact(good) + logfact(set_size) + logfact(bad + good - set_size);
		double loghyp2 = logfact(i) + logfact(good - i) + logfact(bad + i - set_size) + logfact(set_size - i) + logfact(bad + good);
		return Math.exp(loghyp1 - loghyp2);
	}
}
