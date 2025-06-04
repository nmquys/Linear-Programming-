package QHTT;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class SimplexSolver extends JFrame
{
    private JTextArea solutionTextArea;
    private JScrollPane scrollPane;
    private StandardFormTransformer.StandardLPModel model;
    private Simplex simplex;
    private boolean needsTwoPhase;

    public SimplexSolver(StandardFormTransformer.StandardLPModel inputModel)
    {
        this.model = inputModel;
        this.needsTwoPhase = checkForNegativeRHS();
        this.init();
        this.setupUI();
        this.solveProblem();
    }

    private void init()
    {
        solutionTextArea = new JTextArea();
        solutionTextArea.setEditable(false);
        solutionTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        scrollPane = new JScrollPane(solutionTextArea);
    }

    private void setupUI()
    {
        this.setTitle("Simplex Solver - Solution Steps");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.add(scrollPane, BorderLayout.CENTER);
        this.setVisible(true);
    }

    private boolean checkForNegativeRHS()
    {
        for (Double rhs : model.rhsValues)
        {
            if (rhs < 0)
            {
                return true;
            }
        }
        return false;
    }

    private void solveProblem()
    {
        if (needsTwoPhase)
        {
            solutionTextArea.append("=== TWO-PHASE METHOD(Negative RHS values found) ===\n");
            solveWithTwoPhaseMethod();
        }
        else
        {
            solutionTextArea.append("=== STANDARD SIMPLEX METHOD ===\n");
            solveWithStandardMethod();
        }
    }

    private void solveWithStandardMethod()
    {
        double[][] tableData = TableDataExtractor.extractTableData(model);
        int numConstraints = model.constraintCoeffs.size();
        int numVariables = model.objectiveCoeffs.length;

        simplex = new Simplex(numConstraints, numVariables);
        simplex.fillTable(tableData);
        String[] variable_list = new String[model.numOriginalVars + model.numSlackVars];
        for (int i = 0; i < model.numOriginalVars; i++) {
        	variable_list[i] = "x" + (i+1);
        }
        for (int i = 0; i < model.numSlackVars; i++) {
        	variable_list[i+model.numOriginalVars] = "w" + (i+1);
        }
        simplex.set_variable_list(variable_list);

        solutionTextArea.append("\nINITIAL SIMPLEX DICTIONARY:\n");
        //displayTableBland(simplex.getTable());
        displayDictionary(simplex);
        runSimplexIterations();
    }

    private void solveWithTwoPhaseMethod()
    {
        solutionTextArea.append("\n=== PHASE 1: FINDING FEASIBLE SOLUTION ===\n");
        double[][] tableData = TableDataExtractor.extractTableData(model);
        int numConstraints = model.constraintCoeffs.size();
        int numVariables = model.objectiveCoeffs.length;

        // Prepare phase 1 table with artificial variables
        double[][] phase1Table = TableDataExtractor.extractTableData(model);


        // Create phase 1 simplex with extra column for artificial variables
        Simplex simplex = new Simplex(numConstraints, numVariables);
        simplex.fillTable_phase1(phase1Table);
        String[] variable_list = new String[model.numOriginalVars + model.numSlackVars];
        for (int i = 0; i < model.numOriginalVars; i++) {
        	variable_list[i] = "x" + (i+1);
        }
        for (int i = 0; i < model.numSlackVars; i++) {
        	variable_list[i+model.numOriginalVars] = "w" + (i+1);
        }
        simplex.set_variable_list_phase1(variable_list);
        
        
        solutionTextArea.append("\nPHASE 1 INITIAL DICTIONARY:\n");
        solutionTextArea.append("(let z = xi)\n");
        //simplex.print();
        displayDictionary(simplex);

        // Run phase 1
        if (runPhase1(simplex))
        {
            solutionTextArea.append("\n=== PHASE 2: OPTIMIZING ORIGINAL PROBLEM ===\n");

            // Prepare phase 2 table

            simplex.fillTable_phase2(phase1Table[phase1Table.length -1]);

            this.simplex = simplex;

            solutionTextArea.append("\nPHASE 2 INITIAL DICTIONARY:\n");
            solutionTextArea.append("(z is optimal value)\n");
            //displayPhase2Table(simplex.getTable());
            displayDictionary(simplex);
            // Continue with phase 2
            runSimplexIterationsPhase2();
        }
    }

    private boolean runPhase1(Simplex simplex)
    {
        simplex.compute(3);
        solutionTextArea.append("\nPHASE 1 \nITERATION 1 :\n");
        solutionTextArea.append("Variable in: " + simplex.getVariableIn() + "\n");
        solutionTextArea.append("Variable out: " + simplex.getVariableOut() + "\n");
        solutionTextArea.append("Dictionary:\n");
        //displayPhase1Table(simplex.getTable());
        displayDictionary(simplex);
        
        int iteration = 2;
        boolean phase1Complete = false;
        boolean feasible = false;

        while (!phase1Complete && iteration <= 50)
        {
            Simplex.ERROR err = simplex.compute(2);

            if(err == Simplex.ERROR.NOT_OPTIMAL)
            {
                solutionTextArea.append("\nITERATION " + iteration + ":\n");
                solutionTextArea.append("Variable in: " + simplex.getVariableIn() + "\n");
                solutionTextArea.append("Variable out: " + simplex.getVariableOut() + "\n");
                solutionTextArea.append("Dictionary:\n");
                //displayPhase1Table(simplex.getTable());
                displayDictionary(simplex);
            }
            iteration++;

            if(err == Simplex.ERROR.IS_OPTIMAL)
            {
                phase1Complete = true;
                // Check if all artificial variables are 0
                double objectiveValue = simplex.getTable()[simplex.getTable().length-1][simplex.getTable()[0].length-1];
                solutionTextArea.append("\n---SOLUTION IS OPTIMAL---\n");
                if (Math.abs(objectiveValue) < 1e-10)
                {
                    feasible = true;
                    solutionTextArea.append("\nFEASIBLE SOLUTION FOUND IN PHASE 1\n");
                }
                else
                {
                    solutionTextArea.append("\nNO FEASIBLE SOLUTION (Artificial variables remain in basis)\n");
                }
                break;
            }
            else if (err == Simplex.ERROR.UNBOUNDED)
            {
                phase1Complete = true;
                solutionTextArea.append("\nPHASE 1 UNBOUNDED - NO FEASIBLE SOLUTION\n");
                break;
            }
            else if(err == Simplex.ERROR.NO_SOLUTION)
            {
                solutionTextArea.append("\n---NO SOLUTION---\n");
                phase1Complete = true;
            }
        }

        if (!phase1Complete)
        {
            solutionTextArea.append("\nPHASE 1 ITERATION LIMIT REACHED\n");
        }

        return feasible;
    }

    private void runSimplexIterations()
    {
        int iteration = 1;
        boolean quit = false;

        while (!quit && iteration <= 50)
        {
            Simplex.ERROR err = simplex.compute(2); // Using Bland

            solutionTextArea.append("\nITERATION " + iteration + ":\n");
            //displayTableBland(simplex.getTable());
            solutionTextArea.append("Variable in: " + simplex.getVariableIn() + "\n");
            solutionTextArea.append("Variable out: " + simplex.getVariableOut() + "\n"); 
            solutionTextArea.append("Dictionary:\n");
            displayDictionary(simplex);
            
            iteration++;

            if (err == Simplex.ERROR.IS_OPTIMAL)
            {
                solutionTextArea.append("\nOPTIMAL SOLUTION FOUND\n");
                displaySolution();
                quit = true;
            }
            else if (err == Simplex.ERROR.UNBOUNDED)
            {
                solutionTextArea.append("\nPROBLEM IS UNBOUNDED\n");
                quit = true;
            }
        }

        if (!quit)
        {
            solutionTextArea.append("\nITERATION LIMIT REACHED\n");
        }
    }

    private void runSimplexIterationsPhase2()
    {
        int iteration = 1;
        boolean quit = false;

        while (!quit && iteration <= 50)
        {
            Simplex.ERROR err = simplex.compute(2); // Using Bland

            solutionTextArea.append("\nITERATION " + iteration + ":\n");
            solutionTextArea.append("Variable in: " + simplex.getVariableIn() + "\n");
            solutionTextArea.append("Variable out: " + simplex.getVariableOut() + "\n");
            solutionTextArea.append("Dictionary:\n");
            
            //displayPhase2Table(simplex.getTable());
            displayDictionary(simplex);

            iteration++;

            if (err == Simplex.ERROR.IS_OPTIMAL)
            {
                solutionTextArea.append("\nOPTIMAL SOLUTION FOUND\n");
                displaySolutionPhase2();
                quit = true;
            } else if (err == Simplex.ERROR.UNBOUNDED)
            {
                solutionTextArea.append("\nPROBLEM IS UNBOUNDED\n");
                quit = true;
            }
        }

        if (!quit)
        {
            solutionTextArea.append("\nITERATION LIMIT REACHED\n");
        }
    }

    private void displayTableBland(float[][] fs)
    {
        DecimalFormat df = new DecimalFormat("0.00");

        // Print column headers
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < model.numOriginalVars; i++)
        {
            header.append(String.format("%8s", "x" + (i + 1)));
        }
        for (int i = 0; i < model.numSlackVars; i++)
        {
            header.append(String.format("%8s", "w" + (i + 1)));
        }
        header.append(String.format("%8s", "b"));
        solutionTextArea.append(header.toString() + "\n");

        // Print table rows
        for (int i = 0; i < fs.length; i++)
        {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < fs[i].length; j++)
            {
                row.append(String.format("%8s", df.format(fs[i][j])));
            }
            solutionTextArea.append(row.toString() + "\n");
        }
    }

    private void displayPhase1Table(float[][] fs)
    {
        DecimalFormat df = new DecimalFormat("0.00");

        // Print column headers
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < model.numOriginalVars + 1; i++)
        {
            header.append(String.format("%8s", "x" + (i)));
        }
        for (int i = 0; i < model.numSlackVars; i++)
        {
            header.append(String.format("%8s", "w" + (i + 1)));
        }

        header.append(String.format("%8s", "b"));
        solutionTextArea.append(header.toString() + "\n");

        // Print table rows
        for (int i = 0; i < fs.length; i++)
        {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < fs[i].length; j++)
            {
                row.append(String.format("%8s", df.format(fs[i][j])));
            }
            solutionTextArea.append(row.toString() + "\n");
        }
    }

    private void displayPhase2Table(float[][] fs)
    {
        DecimalFormat df = new DecimalFormat("0.00");

        // Print column headers
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < model.numOriginalVars; i++)
        {
            header.append(String.format("%8s", "x" + (i + 1)));
        }
        for (int i = 0; i < model.numSlackVars; i++)
        {
            header.append(String.format("%8s", "w" + (i + 1)));
        }
        header.append(String.format("%8s", "b"));
        solutionTextArea.append(header.toString() + "\n");

        // Print table rows
        for (int i = 0; i < fs.length ; i++)
        {
            StringBuilder row = new StringBuilder();
            
            for (int j = 0; j < fs[i].length - 1; j++)
            {
                row.append(String.format("%8s", df.format(fs[i][j])));
            }
            solutionTextArea.append(row.toString() + "\n");
        }
    }
    
    public void displayDictionary(Simplex simplex) {
    	
    	StringBuilder sb = new StringBuilder();
    	
    	 
    	String[][] ans = simplex.print_dictionary();
    	
    	for (int j = 0; j < ans[0].length; j++) {
    		sb.append(ans[0][j]);
    	}
    	solutionTextArea.append(sb.toString() + "\n");
    	sb = new StringBuilder();
    	for (int j = 0; j < ans[0].length; j++) {
    		sb.append("------");
    	}
    	solutionTextArea.append(sb.toString() + "\n");
    	for(int i = 1; i < ans.length; i++) {
    		sb = new StringBuilder();
    		for (int j = 0; j < ans[0].length; j++) {
        		sb.append(ans[i][j]);
        	}
    		solutionTextArea.append(sb.toString() + "\n");
    	}
    }

    private void displaySolution()
    {
    	DecimalFormat df = new DecimalFormat("0.00");
        float[][] table = simplex.getTable();
        int lastRow = simplex.get_rows() - 1; // Last row is the objective row;
        int rhsCol = table[0].length -1;

        // Display objective value
        double objectiveValue = table[lastRow][rhsCol - 1];
        if (model.isMax)
        {
            objectiveValue *= -1; // Convert back to maximization
        }
        solutionTextArea.append("\n--->OBJECTIVE VALUE: " + df.format(objectiveValue) + "\n");

        // Display variable values
        solutionTextArea.append("\n--->VARIABLE VALUES:\n");
        
        ArrayList<Integer> basis = simplex.get_basis();
        
		//simplex.print();
        //for (int i : basis) {
		//	System.out.println("Basis variable col: " + i);
		//}
        
        for (int i = 0; i < model.numOriginalVars; i++)
        {
        	solutionTextArea.append("x" + (i + 1) + " = ");//+ "\n"
        	if(!basis.contains(i)) {
        		if(-0.000001f < table[lastRow][i] && table[lastRow][i] < 0.000001f) {
        			// If the value is close to zero, treat it as zero
        			// If x has coefficient 0 in z then is in interval [0,r]
        			solutionTextArea.append(" some value greater than 0.00 \n");
        			continue;
        		}
				solutionTextArea.append(" 0.00\n");
				continue;
        	}
            String all_sol = simplex.find_all_solutions(basis.indexOf(i));
			if (all_sol != "") {
				solutionTextArea.append(all_sol + "\n");
			}
			else {
				solutionTextArea.append("\n");
			}
        }
        //simplex.print();
    }

    private void displaySolutionPhase2()
    {
    	DecimalFormat df = new DecimalFormat("0.00");
        float[][] table = simplex.getTable();
        int lastRow = simplex.get_rows() - 1; // Last row is the objective row;
        int rhsCol = table[0].length -1;

        // Display objective value
        double objectiveValue = table[lastRow][rhsCol - 1];
        if (model.isMax)
        {
            objectiveValue *= -1; // Convert back to maximization
        }
        solutionTextArea.append("\n--->OBJECTIVE VALUE: " + df.format(objectiveValue) + "\n");

        // Display variable values
        solutionTextArea.append("\n--->VARIABLE VALUES:\n");
        
        ArrayList<Integer> basis = simplex.get_basis();
        
		//simplex.print();
        //for (int i : basis) {
		//	System.out.println("Basis variable col: " + i);
		//}
        
        for (int i = 0; i < model.numOriginalVars; i++)
        {
        	solutionTextArea.append("x" + (i + 1) + " = ");//+ "\n"
        	if(!basis.contains(i)) {
        		if(-0.000001f < table[lastRow][i] && table[lastRow][i] < 0.000001f) {
        			// If the value is close to zero, treat it as zero
        			// If x has coefficient 0 in z then is in interval [0,r]
        			solutionTextArea.append(" some value greater than 0.00 \n");
        			continue;
        		}
				solutionTextArea.append(" 0.00\n");
				continue;
        	}
            String all_sol = simplex.find_all_solutions(basis.indexOf(i));
			if (all_sol != "") {
				solutionTextArea.append(all_sol + "\n");
			}
			else {
				solutionTextArea.append("\n");
			}
        }
        //simplex.print();
    }

    private String findVariableValue(int varIndex)
    {
        DecimalFormat df = new DecimalFormat("0.00");
        float[][] table = simplex.getTable();
        int rhsCol = simplex.get_cols() - 1; // Last column is RHS;
        int lastRow = table.length - 1;

        // Check if this variable is basic
        for (int i = 0; i < table.length - 1; i++)
        {
            boolean isBasic = true;
            for (int j = 0; j < rhsCol; j++)
            {

                if (table[i][lastRow] != 0.0 && j == varIndex)
                {
                    if (table[i][j] != 1.0)
                    {
                        isBasic = false;
                        break;
                    }
                }

            }

            if (isBasic)
            {
                return df.format(table[i][rhsCol]);
            }
        }

        // If not basic, value is 0
        return "0.00";
    }

    public static void solveAndDisplay(StandardFormTransformer.StandardLPModel model)
    {
        SwingUtilities.invokeLater(() -> new SimplexSolver(model));
    }
}