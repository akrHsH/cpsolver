package org.cpsolver.studentsct.heuristics.selection;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.Neighbour;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Student;

/**
 * This selection is very much like {@link BranchBoundSelection}, but only enough
 * courses to a student is assigned to reach the min credit (see {@link Student#getMinCredit()}).
 * Students that do not have the min credit set or have it set to zero are skipped.
 * 
 * <br>
 * <br>
 * Parameters: <br>
 * <table border='1' summary='Related Solver Parameters'>
 * <tr>
 * <th>Parameter</th>
 * <th>Type</th>
 * <th>Comment</th>
 * </tr>
 * <tr>
 * <td>Neighbour.BranchAndBoundTimeout</td>
 * <td>{@link Integer}</td>
 * <td>Timeout for each neighbour selection (in milliseconds).</td>
 * </tr>
 * <tr>
 * <td>Neighbour.BranchAndBoundMinimizePenalty</td>
 * <td>{@link Boolean}</td>
 * <td>If true, section penalties (instead of section values) are minimized:
 * overall penalty is minimized together with the maximization of the number of
 * assigned requests and minimization of distance conflicts -- this variant is
 * to better mimic the case when students can choose their sections (section
 * times).</td>
 * </tr>
 * </table>
 * <br>
 * <br>
 * 
 * @version StudentSct 1.3 (Student Sectioning)<br>
 *          Copyright (C) 2007 - 2014 Tomas Muller<br>
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
 *          License along with this library; if not see
 *          <a href='http://www.gnu.org/licenses/'>http://www.gnu.org/licenses/</a>.
 */
public class MinCreditBranchAndBoundSelection extends BranchBoundSelection {
    
    public MinCreditBranchAndBoundSelection(DataProperties properties) {
        super(properties);
    }
    
    @Override
    public void init(Solver<Request, Enrollment> solver) {
        init(solver, "Min Credit B&B...");
    }
    
    @Override
    public Neighbour<Request, Enrollment> selectNeighbour(Solution<Request, Enrollment> solution) {
        Student student = null;
        while ((student = nextStudent()) != null) {
            Progress.getInstance(solution.getModel()).incProgress();
            if (student.getMinCredit() <= 0) continue; // skip students that have no min credit
            Neighbour<Request, Enrollment> neighbour = getSelection(solution.getAssignment(), student).select();
            if (neighbour != null)
                return neighbour;
        }
        return null;
    }
    
    @Override
    public Selection getSelection(Assignment<Request, Enrollment> assignment, Student student) {
        return new MinCreditSelection(student, assignment);
    }
    
    public class MinCreditSelection extends Selection {
        
        public MinCreditSelection(Student student, Assignment<Request, Enrollment> assignment) {
            super(student, assignment);
        }
        
        public double getCredit(int idx) {
            float credit = 0f;
            for (int i = 0; i < idx; i++)
                if (iAssignment[i] != null)
                    credit += iAssignment[i].getCredit();
            return credit;
        }
        
        @Override
        public void backTrack(int idx) {
            if (getCredit(idx) >= iStudent.getMinCredit()) {
                if (iMinimizePenalty) {
                    if (getBestAssignment() == null || (getNrAssigned() > getBestNrAssigned() || (getNrAssigned() == getBestNrAssigned() && getPenalty() < getBestValue())))
                        saveBest();
                } else {
                    if (getBestAssignment() == null || getValue() < getBestValue())
                        saveBest();
                }
                return;
            }
            super.backTrack(idx);
        }
    }
}
