package tool;

import java.awt.image.BufferedImage;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;

public class IcePDF {
	public static BufferedImage page2Image(String filePath, int pageNum) {
		Document document = new Document();
		try {
			document.setFile(filePath);
		} catch (Exception ex) {
		}

		// save page caputres to file.
		float scale = 2f;
		float rotation = 0f;
		BufferedImage image = (BufferedImage) document.getPageImage(pageNum, GraphicsRenderingHints.SCREEN,
				Page.BOUNDARY_CROPBOX, rotation, scale);

		image.flush();

		// clean up resources
		document.dispose();
		return image;
	}
}
