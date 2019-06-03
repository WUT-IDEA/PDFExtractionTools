package tool;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;


public class PDF2Excel {
    private POIUtils poiUtils;

    PDF2Excel() {
        poiUtils = new POIUtils();
    }

	public void form2TXT(PDPage page, int sheetNum, byte[][] pixel, int yOffset) {
		int h = pixel.length;
		int w = pixel[0].length;
		int row = -1;
		int col = -1;
		int mergeCol = 0;
		int mergeRow = 0;
		boolean isFirst = true;
		boolean isCell = false;
		byte[][] pixels = ImageUtils.getSkeleton(pixel);
		String content;
		
		for (int y = 0; y < h; y++) {
			isFirst = true;
			for (int x = 0; x < w; x++) {
				isCell = false;
				// find the upleft point of the form
				if (pixels[y][x] == 2) {
					mergeCol = 0;
					mergeRow = 0;
					if (isFirst) {
						row++;
						col = -1;
					}
					isFirst = false;

					for (int i = x + 1; i < w; i++) {
						// find the upright point of the cell
						if (pixels[y][i] == 2 && pixels[y][x + 5] == 1) {
							for (int j = y + 1; j < h; j++) {
								// find the bottom line of the cell
								if (pixels[j][x] == 2 && pixels[j][i] == 2 && pixels[j][x + 5] == 1
										&& pixels[y + 5][i] == 1 && pixels[y + 5][x] == 1) {
									col++;
									for (int y0 = 0; y0 < h; y0++) {
										int temp_mergeCol = 0;
										for (int x0 = x + 1; x0 < i; x0++) {
											if (pixels[y0][x0] == 2) {
												temp_mergeCol++;
											}
										}
										mergeCol = mergeCol > temp_mergeCol ? mergeCol : temp_mergeCol;
									}
	                                //获取高度在y 到  j，宽度为0-w,即现在cell所在的这一条状区域，纵向最多的像素点为2的个数，即在这一宽度合并最多的行数
									for (int x2 = 0; x2 < w; x2++) {
										int temp = 0;
										for (int j2 = y + 1; j2 < j; j2++) {
											if (pixels[j2][x2] == 2) {
												temp++;
											}
										}
										mergeRow = mergeRow > temp ? mergeRow : temp;
									}

									content = PDFUtils.getRectTxT(page, new Point(x / 2, (y + yOffset) / 2),
											new Point(i / 2, (j + yOffset) / 2));
	                                   poiUtils.writeExcel(sheetNum, row, col, mergeRow, mergeCol, content);
									x = i - 1;
									isCell = true;
									break;
								}
							}

						}
						if (isCell)
							break;
					}
				} // if pixels[y][x] == 2 end
			}
		    poiUtils.save();
		}
		System.out.println("已提取并写入第" + (sheetNum + 1) + "个表格^_^");
	}

	@SuppressWarnings("unchecked")
	public static void convert(String filePath) {
		PDF2Excel pdf2Excel = new PDF2Excel();
		int sheetNum = 0;
		PDDocument doc = null;
		try {
			doc = PDDocument.load(new File(filePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
		File textFile = new File("content.txt");
		if (textFile.exists()) {
			textFile.delete();
		}
		//作为测试样例，解析PDF第7页
		int testPageNum = 6;
		for (int pageNum = 0; pageNum < doc.getNumberOfPages(); pageNum++) {
		    // 作为测试只解析第7页
		    pageNum = testPageNum;
			PDPage page = (PDPage) pages.get(pageNum);
			BufferedImage img = IcePDF.page2Image(filePath, pageNum);
			try {
				ImageIO.write(img, "png", new File("image/" + Integer.toString(pageNum + 1) + ".png"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			byte[][] pixels = ImageUtils.toPixels(img);
			byte[][] skeletonPixels = ImageUtils.getSkeleton(pixels);
			int[] rowHisto = ImageUtils.Tohisto(pixels, "row");
			int h = img.getHeight();
			int w = img.getWidth();
			int up = 0;
			// 处理页眉
			for (int i = 50; i < h; i++) {
				if (rowHisto[i] > w * 0.5) {
					up = i;
					break;
				}
			}
			for (int i = up; i < h; i++) {
				if (rowHisto[i] < 5) {
					up = i;
					break;
				}
			}
			// 处理表格开始行和结束行
			ArrayList<Integer> lines = new ArrayList<Integer>();
			boolean a, b, c;
			for (int y = up; y < h;) {
				if (rowHisto[y] > w * 0.7) {
					a = rowHisto[y - 1] < 2;
					b = rowHisto[y + 2] < 2;
					c = (rowHisto[y - 1] > 2) && (rowHisto[y - 2] < 2);
					if ((a || c) ^ b) {
						lines.add(y);
					}
					y = y + 5;
				} else
					y++;
			}

			StringBuffer text = new StringBuffer();
			if (lines.size() % 2 == 1) { // the first table in this page missed
											// its first/last row line.
				int missedRow = 0;
				int firstCol = crossedPoints(skeletonPixels, lines.get(0));
				for (int m = lines.get(0);; m--) { // find out which row this
													// line starts.
					if (pixels[m][firstCol] != 1) {
						missedRow = m;
						break;
					}
				}
				assert missedRow < lines.get(0);
				byte[] temp = pixels[lines.get(0)];
				pixels[missedRow] = temp;
				lines.add(0, missedRow);
			}
			if (lines.size() == 0) {
				text.append(PDFUtils.getRectTxT(page, new Point(0, up / 2), new Point(w / 2, h / 2)));
			} else {
				for (int i = 0; i < lines.size(); i += 2) {

					if (i == 0) {
						text.append(
								PDFUtils.getRectTxT(page, new Point(0, up / 2), new Point(w / 2, lines.get(0) / 2)));
					} else {
						text.append(PDFUtils.getRectTxT(page, new Point(0, lines.get(i - 1) / 2),
								new Point(w / 2, lines.get(i) / 2)));
					}

					text.append("-sheet" + sheetNum + "\r\n");
					pdf2Excel.form2TXT(page, sheetNum, Arrays.copyOfRange(pixels, lines.get(i), lines.get(i + 1) + 1),
							lines.get(i));
					sheetNum++;
					if (i == lines.size() - 2) {
						text.append(
								PDFUtils.getRectTxT(page, new Point(0, lines.get(i + 1) / 2), new Point(w / 2, h / 2)));
					}
				}
			}
			try {
				FileWriter writer = new FileWriter(textFile, true);
				writer.write(text.toString());
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//只解析第7页，完成后退出
			break;
		}
		System.out.println("txt成功生成！");
		System.out.println("解析完成");
	}

	public static int crossedPoints(byte[][] skeletonPixels, int rowNum) {
		byte[] row = skeletonPixels[rowNum];
		int col = 0;
		for (int i = 0; i < row.length; i++) {
			if (row[i] == 2) {
				col = i;
				break;
			}
		}
		return col;
	}

}
