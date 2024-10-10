package org.cpsolver.ifs.heuristics;

import java.util.Collection;
import java.util.Map;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.context.AssignmentContext;
import org.cpsolver.ifs.assignment.context.NeighbourSelectionWithContext;
import org.cpsolver.ifs.model.Neighbour;
import org.cpsolver.ifs.model.Value;
import org.cpsolver.ifs.model.Variable;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solution.SolutionListener;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;

/**
 * Simple extension of a provided {@link NeighbourSelection} that halts the construction
 * heuristic or the IFS search when the underlying heuristic is unable to improve the
 * number of assigned variables. When a given number of non-improving iterations is reached,
 * the {@link MaxIdleNeighbourSelection} extension starts returning null. The counter gets
 * automatically reset every time a solution with more variables assigned is stored as best
 * solution.

 * @see NeighbourSelection
 * 
 * @version IFS 1.4 (Iterative Forward Search)<br>
 *          Copyright (C) 2024 Tomas Muller<br>
 *          <a href="mailto:muller@unitime.org">muller@unitime.org</a><br>
 *          <a href="http://muller.unitime.org">http://muller.unitime.org</a><br>
 * <br>
 *          This library is free software; you can redistribute it and/or modify
 *          it under the terms of the GNU Lesser General Public License as
 *          published by the Free Software Foundation; either version 3 of the
 *          License, or (at your option) any later version. <br>
 * <br>
 *          This library is distributed in the hope that it will be useful, but
 *          WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *          Lesser General Public License for more details. <br>
 * <br>
 *          You should have received a copy of the GNU Lesser General Public
 *          License along with this library; if not see <a href='http://www.gnu.org/licenses'>http://www.gnu.org/licenses</a>.
 *
 * @param <V> Variable
 * @param <T> Value
 **/
public class MaxIdleNeighbourSelection<V extends Variable<V, T>, T extends Value<V, T>> extends NeighbourSelectionWithContext<V, T, MaxIdleNeighbourSelection<V, T>.MaxIdleContext> implements SolutionListener<V, T> {
    protected NeighbourSelection<V, T> iParent = null;
    protected int iMaxIdle = 1000;
    protected int iBestAssigned = 0;

    public MaxIdleNeighbourSelection(DataProperties properties, NeighbourSelection<V, T> parent, int maxIdle) {
        iParent = parent;
        iMaxIdle = maxIdle;
    }
    
    @Override
    public void init(Solver<V, T> solver) {
        super.init(solver);
        iParent.init(solver);
        solver.currentSolution().addSolutionListener(this);
    }


    @Override
    public void solutionUpdated(Solution<V, T> solution) {
    }

    @Override
    public void getInfo(Solution<V, T> solution, Map<String, String> info) {
    }

    @Override
    public void getInfo(Solution<V, T> solution, Map<String, String> info, Collection<V> variables) {
    }

    @Override
    public void bestCleared(Solution<V, T> solution) {
    }

    @Override
    public void bestSaved(Solution<V, T> solution) {
        if (solution.getAssignment().nrAssignedVariables() > iBestAssigned)
            getContext(solution.getAssignment()).reset();
        iBestAssigned = solution.getAssignment().nrAssignedVariables();
    }

    @Override
    public void bestRestored(Solution<V, T> solution) {
    }

    @Override
    public Neighbour<V, T> selectNeighbour(Solution<V, T> solution) {
        MaxIdleContext context = getContext(solution.getAssignment());
        if (context.inc() >= iMaxIdle)
            return null;
        return iParent.selectNeighbour(solution);
    }

    @Override
    public MaxIdleContext createAssignmentContext(Assignment<V, T> assignment) {
        return new MaxIdleContext(assignment);
    }

    public class MaxIdleContext implements AssignmentContext {
        private int iCounter = 0;
        
        public MaxIdleContext(Assignment<V, T> assignment) {
        }
        
        public int inc() { return iCounter++; }
        public void reset() { iCounter = 0; }
        
    }
}
