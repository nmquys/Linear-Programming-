package QHTT;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;


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

        solutionTextArea.append("\nINITIAL SIMPLEX TABLE:\n");
        displayTableBland(simplex.getTable());

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
        Simplex simplex = new Simplex(numConstraints, numVariables + 1);
        simplex.fillTable_phase1(phase1Table);

        solutionTextArea.append("\nPHASE 1 INITIAL TABLE:\n");
        displayPhase1Table(simplex.getTable());

        // Run phase 1
        if (runPhase1(simplex))
        {
            solutionTextArea.append("\n=== PHASE 2: OPTIMIZING ORIGINAL PROBLEM ===\n");

            // Prepare phase 2 table

            simplex.fillTable_phase2(phase1Table[phase1Table.length -1]);

            this.simplex = simplex;

            solutionTextArea.append("\nPHASE 2 INITIAL TABLE:\n");
            displayPhase2Table(simplex.getTable());

            // Continue with phase 2
            runSimplexIterationsPhase2();
        }
    }

    private boolean runPhase1(Simplex simplex)
    {
        simplex.compute(3);
        solutionTextArea.append("\nPHASE 1 ITERATION 1 :\n");
        displayPhase1Table(simplex.getTable());

        int iteration = 2;
        boolean phase1Complete = false;
        boolean feasible = false;

        while (!phase1Complete && iteration <= 50)
        {
            Simplex.ERROR err = simplex.compute(1);

            if(err == Simplex.ERROR.NOT_OPTIMAL)
            {
                solutionTextArea.append("\nITERATION " + iteration + ":\n");
                displayPhase1Table(simplex.getTable());
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
            Simplex.ERROR err = simplex.compute(1); // Using Bland

            solutionTextArea.append("\nITERATION " + iteration + ":\n");
            displayTableBland(simplex.getTable());

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
            Simplex.ERROR err = simplex.compute(1); // Using Bland

            solutionTextArea.append("\nITERATION " + iteration + ":\n");
            displayPhase2Table(simplex.getTable());

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

    private void displayTableBland(double[][] table)
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
        for (int i = 0; i < table.length; i++)
        {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < table[i].length; j++)
            {
                row.append(String.format("%8s", df.format(table[i][j])));
            }
            solutionTextArea.append(row.toString() + "\n");
        }
    }

    private void displayPhase1Table(double[][] table)
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
        for (int i = 0; i < table.length; i++)
        {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < table[i].length; j++)
            {
                row.append(String.format("%8s", df.format(table[i][j])));
            }
            solutionTextArea.append(row.toString() + "\n");
        }
    }

    private void displayPhase2Table(double[][] table)
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
        for (int i = 0; i < table.length ; i++)
        {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < table[i].length - 1; j++)
            {
                row.append(String.format("%8s", df.format(table[i][j])));
            }
            solutionTextArea.append(row.toString() + "\n");
        }
    }

    private void displaySolution()
    {
        DecimalFormat df = new DecimalFormat("0.00");
        double[][] table = simplex.getTable();
        int lastRow = table.length - 1;
        int rhsCol = table[0].length -1;

        // Display objective value
        double objectiveValue = table[lastRow][rhsCol];
        if (model.isMax)
        {
            objectiveValue *= -1; // Convert back to maximization
        }
        solutionTextArea.append("\n--->OBJECTIVE VALUE: " + df.format(objectiveValue) + "\n");

        // Display variable values
        solutionTextArea.append("\n--->VARIABLE VALUES:\n");
        for (int i = 0; i < model.numOriginalVars; i++)
        {
            solutionTextArea.append("x" + (i + 1) + " = " + findVariableValue(i) + "\n");
        }
    }

    private void displaySolutionPhase2()
    {
        DecimalFormat df = new DecimalFormat("0.00");
        double[][] table = simplex.getTable();
        int lastRow = table.length - 1;
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
        for (int i = 0; i < model.numOriginalVars; i++)
        {
            solutionTextArea.append("x" + (i + 1) + " = " + findVariableValue(i) + "\n");
        }
    }

    private String findVariableValue(int varIndex)
    {
        DecimalFormat df = new DecimalFormat("0.00");
        double[][] table = simplex.getTable();
        int rhsCol = table[0].length - 1;
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