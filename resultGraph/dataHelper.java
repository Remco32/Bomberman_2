package resultGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * Created by Joseph on 6/14/2017.
 */
public class dataHelper {
    Color color;
    String text;
    ArrayList<Double> datalist;

    dataHelper(ArrayList<Double> data,DefaultListModel<dataHelper> listModel){
        datalist = data;
        popup(listModel);
    }

    dataHelper(ArrayList<Double> data,DefaultListModel<dataHelper> listModel,String name){
        datalist = data;
        text=name;
    }

    void popup(final DefaultListModel<dataHelper> listModel){
        final JFrame popup = new JFrame("what is the name of this list?");
        JButton save = new JButton("save");



        final JTextField itext = new JTextField();
        itext.setText("");
        itext.setEditable(true);

        popup.setLayout(new GridLayout(1,2));
        popup.add(itext);
        popup.add(save);



        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!itext.getText().equals("")) {
                    text = itext.getText();
                    popup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    popup.dispatchEvent(new WindowEvent(popup, WindowEvent.WINDOW_CLOSING));
                    listModel.addElement(null);
                    listModel.removeElement(null);
                } else {
                    System.out.println("empty");
                }
            }
        });

        popup.pack();
        popup.setVisible(true);
        popup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    }



    @Override
    public String toString() {
        return text;
    }
}
