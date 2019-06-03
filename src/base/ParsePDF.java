package base;

import tool.PDF2Excel;

public class ParsePDF {
    public static void main(String[] args){
        //解析并提取PDF中的表格
        PDF2Excel.convert("新世界：2018年年度报告.PDF");
    }

}
