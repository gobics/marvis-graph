/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.utils.compoundrules;

/**
 *
 * @author manuel
 */
public class NOPS extends Rule {

	private int idx_N = -1;
	private int idx_O = -1;
	private int idx_P = -1;
	private int idx_S = -1;

	@Override
	public String getName() {
		return "NOPS Ratio (Fiehn 6)";
	}

	@Override
	public String getDescription() {
		return "Bounds the ratio between N, O, P and S (Fiehn 6)";
	}

	@Override
	public int check(int[] formula) {
		
		int cN = idx_N >= 0 ? formula[idx_N] : 0;
		int cO = idx_O >= 0 ? formula[idx_O] : 0;
		int cP = idx_P >= 0 ? formula[idx_P] : 0;
		int cS = idx_S >= 0 ? formula[idx_S] : 0;

		if (checkNops(cN, cO, cP, cS) == OK
				&& checkNop(cN, cO, cP) == OK
				&& checkOps(cO, cP, cS) == OK
				&& checkPsn(cP, cS, cN) == OK
				&& checkNos(cN, cO, cS) == OK) {
			return OK;
		}
		return UPPER_BOUND;
	}

	@Override
	public void prepare(double desiredMass, String[] elements) {
		idx_N = idx_O = idx_P = idx_S = -1;
		
		for( int i = 0; i < elements.length ; i++) {
			if( elements[i].equals("N"))
				idx_N = i;
			if( elements[i].equals("O"))
				idx_O = i;
			if( elements[i].equals("P"))
				idx_P = i;
			if( elements[i].equals("S"))
				idx_S = i;
		}
	}

	private int checkNops(int N, int O, int P, int S) {
		if (N <= 1 || O <= 1 || P <= 1 || S <= 1) {
			return OK;
		}
		if (N >= 10 || O >= 20 || P >= 4 || S >= 3) {
			return UPPER_BOUND;
		}
		return OK;
	}

	private int checkNop(int cN, int cO, int cP) {
		if (cN <= 3 || cO <= 3 || cP <= 3) {
			return OK;
		}
		if (cN >= 11 || cO >= 22 || cP >= 6) {
			return UPPER_BOUND;
		}
		return OK;
	}

	private int checkOps(int O, int P, int S) {
		if (O <= 1 || P <= 1 || S <= 1) {
			return OK;
		}
		if (O >= 14 || P >= 3 || S >= 3) {
			return UPPER_BOUND;
		}
		return OK;
	}

	private int checkPsn(int P, int S, int N) {
		if (N <= 1 || P <= 1 || S <= 1) {
			return OK;
		}
		if (P >= 3 || S >= 3 || N >= 4) {
			return UPPER_BOUND;
		}
		return OK;
	}

	private int checkNos(int N, int O, int S) {
		if (N <= 6 || O <= 6 || S <= 6) {
			return OK;
		}

		if (N >= 19 || O >= 14 || S >= 8) {
			return UPPER_BOUND;
		}

		return OK;
	}
}
