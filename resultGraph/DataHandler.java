package resultGraph;

import sun.plugin.com.Utils;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by Joseph on 6/14/2017.
 */
public class DataHandler extends JPanel{

    JButton selectData = new JButton("SelectData");
    JButton drawGraph  = new JButton("Draw the graph");
    ResultGraph resultGraph = new ResultGraph();

    DefaultListModel<dataHelper> dataListModel = new DefaultListModel<>();
    JList inputDataList = new JList(dataListModel);


    DataHandler(){
       handleGui();
    }



    void handleGui(){
        this.add(inputDataList);
        final JTextField title = new JTextField("currently selected Data");
        this.add(title);
        title.setBounds(150,0,150,20);
        inputDataList.setBounds(150,20,150,280);



        setLayout(null);
        this.add(selectData);
        selectData.setBounds(0,0,150,20);
        selectData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                final JFileChooser chooser=new JFileChooser(System.getProperty("user.dir")+"\\..\\results\\images");
                chooser.setMultiSelectionEnabled(true);
                chooser.setPreferredSize(new Dimension(1000,500));
                final int returnVal = chooser.showOpenDialog(chooser);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    ExcelPrinter excel=null;
                    try {
                      //  System.out.println(chooser.getSelectedFile().getParentFile().getName().substring(20,30));
                         excel = new ExcelPrinter(chooser.getSelectedFile().getParentFile().getName().substring(20,21));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for(File file:chooser.getSelectedFiles()) {
                        load(file.getAbsolutePath(),file.getName(),excel);
                    }
                    excel.Safe();
                }
            }
        });



        this.add(drawGraph);
        drawGraph.setBounds(0,20,150,20);
        drawGraph.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultGraph.Draw(dataListModel);
            }
        });
        this.setBackground(Color.black);

    }

    //accept filter
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        int extensionIdx= f.getName().lastIndexOf(".");
        String extension = f.getName().substring(extensionIdx);
        System.out.println(extension);
        if (extension == "data") {
                return true;
        } else {
            return false;
        }
    }

    void load(String adress,String name,ExcelPrinter printer){
        FileInputStream fin = null;
        ArrayList<Double> winrate;
        try {
            fin = new FileInputStream(adress);
            ObjectInputStream ois = new ObjectInputStream(fin);
            winrate = (ArrayList<Double>) ois.readObject();
              //error
           // winrate = (ArrayList<Double>) ois.readObject();
            //points
            //winrate = (ArrayList<Double>) ois.readObject();

            printer.write(winrate,name);
           dataListModel.addElement(new dataHelper(winrate,dataListModel,name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }


}
