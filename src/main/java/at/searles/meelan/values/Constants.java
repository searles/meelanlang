package at.searles.meelan.values;

import at.searles.commons.math.Cplx;
import at.searles.meelan.optree.Tree;

public enum Constants {
	PI {
		@Override
		public Tree get() {
			return new Real(Math.PI);
		}
	},
	TAU {
		@Override
		public Tree get() {
			return new Real(2 * Math.PI);
		}
	},
	E {
		@Override
		public Tree get() {
			return new Real(Math.E);
		}
	},
	I {
		@Override
		public Tree get() {
			return new CplxVal(new Cplx(0, 1));
		}
	};

	public abstract Tree get();

}
