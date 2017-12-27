package resultGraph;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by joseph on 6-7-2017.
 */
public class ExcelPrinter {


    HSSFSheet sheet;
    HSSFWorkbook wb;
    int rowIndex=0;


    public ExcelPrinter(String sheetname) throws IOException {

         wb = new HSSFWorkbook();
        sheet = wb.createSheet(sheetname);

    }

    public void write(ArrayList<Double> valueList,String name) {


        HSSFRow row = sheet.createRow(rowIndex);
        HSSFCell cell = row.createCell(rowIndex);
        cell.setCellValue(name);
        for (int RowNum = 0; RowNum < 100; RowNum++) {
            cell = row.createCell(RowNum+1);
            cell.setCellValue(valueList.get(RowNum));
        }
        rowIndex++;

    }
    public void Safe(){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(System.getProperty("user.dir") + "\\..\\results\\excelData.xls"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            wb.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
