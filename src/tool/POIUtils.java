package tool;


import java.io.FileOutputStream;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

public class POIUtils {
	private HSSFWorkbook wb;
	private HSSFCellStyle style;
	public POIUtils(){
		// 创建Excel的工作书册 Workbook,对应到一个excel文档
		wb = new HSSFWorkbook();

		// 创建字体样式
        HSSFFont font = wb.createFont();
        font.setFontName("宋体");
        font.setBoldweight((short) 100);
        font.setFontHeight((short) 300);

        // 创建单元格样式
        style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

        // 设置边框
        style.setBorderBottom(HSSFCellStyle.BORDER_THICK);
        style.setBorderLeft(HSSFCellStyle.BORDER_THICK);
        style.setBorderRight(HSSFCellStyle.BORDER_THICK);
        style.setBorderTop(HSSFCellStyle.BORDER_THICK);

        style.setFont(font);// 设置字体
	}
	
	public void writeExcel(int sheetNum, int rowNum, int colNum, int mergeRow, int mergeCol,String content){
		HSSFSheet sheet;
		// 创建Excel的工作sheet,对应到一个excel文档的tab
		try {
			sheet = wb.createSheet("sheet" + sheetNum);
		} catch (IllegalArgumentException e) {
			sheet = wb.getSheet("sheet" + sheetNum);
		}
        

        // ����Excel��sheet��һ��
		HSSFRow row;
		if (sheet.getRow(rowNum) == null) {
			row = sheet.createRow(rowNum);
		}else {
			row = sheet.getRow(rowNum);
		}
        
        row.setHeight((short) 500);// �趨�еĸ߶�
        
        // ����һ��Excel�ĵ�Ԫ��
        while(row.getCell(colNum) != null) {
			colNum ++;
		}
        HSSFCell cell = row.createCell(colNum);
        sheet.autoSizeColumn(colNum, true);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum + mergeRow, colNum, colNum + mergeCol));
        // ��Excel�ĵ�Ԫ��������ʽ�͸�ֵ
        for(int i = rowNum; i <= rowNum + mergeRow; i++){  
            HSSFRow row1 = sheet.getRow(i);  
            if (row1 == null) {
				row1 = sheet.createRow(i);
			}
            for(int j = colNum; j <= colNum + mergeCol;j++){  
                HSSFCell cell1 = row1.getCell(j);  
                if( cell1 == null){  
                    cell1 = row1.createCell(j);  
                    cell1.setCellValue("");
                }  
                 cell1.setCellStyle(style);  
            }  
        }
        cell.setCellValue(content);
	}
	
	public void save(){
		 FileOutputStream os;
			try {
				os = new FileOutputStream("pdf.xls");
				wb.write(os);
		        os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
