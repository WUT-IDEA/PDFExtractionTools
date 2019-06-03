package tool;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripperByArea;


public class PDFUtils {
	@SuppressWarnings("unchecked")
	public static void pdf2Png(File file, int pageNum){
		PDDocument doc = null;
		try {
			doc = PDDocument.load(file);
			List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
			PDPage page = (PDPage)pages.get(pageNum);
			BufferedImage img = page.convertToImage();
			File outFile = new File("pages/" + pageNum + ".png");
			ImageIO.write(img, "png", outFile);
			doc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static BufferedImage PDFPage2Img(PDPage page){
		BufferedImage img = null;
		try {
			img = page.convertToImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return img;
	}
	
	public static String getRectTxT(PDPage page, Point upLeft, Point downRight){
		int w = downRight.x - upLeft.x;
		int h = downRight.y - upLeft.y;

		PDFTextStripperByArea stripper = null;
		try {
			stripper = new PDFTextStripperByArea();
		} catch (IOException e) {
			e.printStackTrace();
		}
		stripper.setSortByPosition( true );

        Rectangle rect = new Rectangle(upLeft.x, upLeft.y, w, h);
        stripper.addRegion( "class1", rect);
        try {
			stripper.extractRegions(page);
		} catch (IOException e) {
			e.printStackTrace();
		}
        String content=stripper.getTextForRegion( "class1" );
        return content;

	}
}
		
	

