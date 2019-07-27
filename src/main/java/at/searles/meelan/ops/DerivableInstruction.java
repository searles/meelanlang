package at.searles.meelan.ops;

import at.searles.meelan.optree.Tree;
import at.searles.meelan.optree.inlined.Var;
import at.searles.parsing.utils.ast.SourceInfo;

import java.util.List;

public interface DerivableInstruction {
    Tree derive(SourceInfo sourceInfo, Var var, List<Tree> args, List<Tree> dargs);
}
