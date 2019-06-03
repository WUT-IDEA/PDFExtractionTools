package tool;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtils {
	public static int isBlack(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() <= 600) {
			return 1;
		}
		return 0;
	}

	public static int[] Tohisto(byte[][] pixels, String type) {
		int width = pixels[0].length;
		int height = pixels.length;
		if (type == "row") {
			int[] histo = new int[height];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (pixels[y][x] == 1) {
						histo[y]++;
					}
				}
			}
			return histo;
		}
		if (type == "col") {
			int[] histo = new int[width];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (pixels[y][x] == 1) {
						histo[x]++;
					}
				}
			}
			return histo;
		}
		return new int[0];
	}

	/*
	 * 将二值图像转成二值数组
	 */
	public static byte[][] toPixels(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		byte[][] pixels = new byte[h][w];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (isBlack(img.getRGB(x, y)) == 1) {
					pixels[y][x] = 1;
				} else {
					pixels[y][x] = 0;
				}
			}
		}
		return pixels;
	}

	/*
	 * 腐蚀操作
	 */
	public static byte[][] erode(byte[][] pixels, Point[] H) {
		int h = pixels.length;
		int w = pixels[0].length;
		int Hlen = H.length;
		boolean flag = true;

		byte[][] temp = new byte[h][w];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (pixels[y][x] == 1) {
					flag = true;
					for (int i = 0; i < Hlen; i++) {
						if (x + H[i].x < w && x + H[i].x >= 0 && y + H[i].y < h && y + H[i].y >= 0
								&& pixels[(int) (y + H[i].y)][(int) (x + H[i].x)] == 0) {
							flag = false;
							break;
						}
					}
					if (flag) {
						temp[y][x] = 1;
					}
				}
			}
		}
		return temp;
	}

	/*
	 * 膨胀操作
	 */
	public static byte[][] dilate(byte[][] pixels, Point[] H) {
		int h = pixels.length;
		int w = pixels[0].length;
		int Hlen = H.length;
		byte[][] temp = new byte[h][w];

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (pixels[y][x] == 1) {
					for (int i = 0; i < Hlen; i++) {
						if (x + H[i].x < w && x + H[i].x >= 0 && y + H[i].y < h && y + H[i].y >= 0) {
							temp[(int) (y + H[i].y)][(int) (x + H[i].x)] = 1;
						}
					}
				}
			}
		}
		return temp;
	}

	/*
	 * 将二维数组转化为图像
	 */
	public static BufferedImage pixels2Image(byte[][] pixels) {
		int h = pixels.length;
		int w = pixels[0].length;
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (pixels[y][x] == 0) {
					img.setRGB(x, y, Color.white.getRGB());
				}
			}
		}
		return img;
	}

	public static byte[][] open(byte[][] pixels, Point[] H) {
		pixels = erode(pixels, H);
		pixels = dilate(pixels, H);
		return pixels;
	}

	public static byte[][] getSkeleton(byte[][] pixels) {
		int h = pixels.length;
		int w = pixels[0].length;
		Point[] hRow = new Point[25];
		for (int i = 0; i < hRow.length; i++) {
			hRow[i] = new Point(i - 5, 0);
		}

		Point[] hCol = new Point[25];
		for (int i = 0; i < hCol.length; i++) {
			hCol[i] = new Point(0, i - 5);
		}
		byte[][] rowPixels;
		byte[][] colPixels;
		rowPixels = fix(open(pixels, hRow));
		colPixels = fix(open(pixels, hCol));

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (rowPixels[y][x] == 1) {
					if (y > 0 && rowPixels[y - 1][x] == 1) {
						continue;
					}
					if (colPixels[y][x] == 1) {
						pixels[y][x] = 2;
						x += 2;
					}
				}
			}
		}

		return pixels;
	}

	public static byte[][] fix(byte[][] pixels) {
		int h = pixels.length;
		int w = pixels[0].length;
		for (int y = 0; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				if (y == 0) {
					if (pixels[y][x] == 0 && pixels[y][x - 1] + pixels[y + 1][x] + pixels[y][x + 1] >= 2) {
						pixels[y][x] = 1;
					}
					continue;
				}
				if (pixels[y][x] == 0
						&& pixels[y - 1][x] + pixels[y][x - 1] + pixels[y + 1][x] + pixels[y][x + 1] >= 2) {
					pixels[y][x] = 1;
				}
			}
		}
		return pixels;
	}

	public static void main(String[] args) {
		try {
			BufferedImage img = ImageIO.read(new File("1.png"));
			byte[][] pixels = toPixels(img);
			Point[] hRow = new Point[25];
			for (int i = 0; i < hRow.length; i++) {
				hRow[i] = new Point(i - 5, 0);
			}
			ImageIO.write(pixels2Image(fix(open(pixels, hRow))), "png", new File("heng.png"));
			Point[] hCol = new Point[25];
			for (int i = 0; i < hCol.length; i++) {
				hCol[i] = new Point(0, i - 5);
			}
			ImageIO.write(pixels2Image(fix(open(pixels, hCol))), "png", new File("shu.png"));
			byte[][] sk=getSkeleton(pixels);
	         ImageIO.write(pixels2Image(sk), "png", new File("sk.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
