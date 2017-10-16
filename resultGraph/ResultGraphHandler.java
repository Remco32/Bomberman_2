package resultGraph;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Joseph on 6/14/2017.
 */
public class ResultGraphHandler {
    JFrame jframe = new JFrame("dataHelper");
    DataHandler datahandler = new DataHandler();
    public static void main(String[] args) {
        new ResultGraphHandler();
    }


    ResultGraphHandler(){
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setLayout(new BorderLayout());

        JPanel container = new JPanel();
        container.setLayout(new GridLayout(1,2));
        container.add(datahandler);
        jframe.add(container);
        jframe.setPreferredSize(new Dimension(300,300));
        jframe.pack();
        jframe.setVisible(true);

    }

}
