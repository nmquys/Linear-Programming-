package QHTT;

import javax.swing.*;
import java.awt.*;

public class Solve_Panel extends JFrame
{
    public Solve_Panel()
    {
        this.init();

        this.setTitle("Solving Linear Programming");
        this.setSize(500, 250);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void init()
    {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
    }


}


