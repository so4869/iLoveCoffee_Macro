package iLoveCoffe;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import util.MatUtil;
import util.WindowUtil;
import util.WindowUtil.RECT;
import util.WindowUtil.User32;
import util.WindowUtil.WindowInfo;

public class Main {
	public static final int margin = 13;
	public static final int WM_LBUTTONUP = 514;
	public static final int WM_LBUTTONDOWN = 513;
	
	
	public static void main(String[] args){
		try {
			JFrame frame = new JFrame();
			frame.getContentPane().setLayout(new FlowLayout());
			frame.pack();
			JLabel imageLabel = new JLabel();
			frame.getContentPane().add(imageLabel);
			frame.setVisible(true);
			frame.setSize(1280, 720);
			
			
			// opencv
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			
			// window
			int hWnd = User32.instance.FindWindowA(null, "SM G930S");
			Thread t = new Thread(() -> {

				WindowInfo w = WindowUtil.getWindowInfo(hWnd);
				RECT rect = w.getRect();
				Rectangle rectangle = new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
//				Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
				
				Robot robot = null;
				
				BufferedImage xImage = null;
				Mat brazilBeanTemplate = null;
				Mat brushTemplate = null;
				Mat starTemplate = null;
				Mat nothingTemplate = null;
				Mat brazilSelectTemplate = null;
				
				Scalar beanBorderScalar = new Scalar(0, 0, 255);
				Scalar greenScalar = new Scalar(0, 255, 0);
				Size tempSize = new Size(1280, 720);

				int searchx = 280, searchy = 180;
				int searchwidth = 1300, searchheight = 620;
				int baseLineX = 960;
				
				Rect searchBoxRect = new Rect();
				searchBoxRect.x = searchx;
				searchBoxRect.y = searchy;
				searchBoxRect.width = searchwidth;
				searchBoxRect.height = searchheight;
				
				Rect baseLineRect = new Rect();
				baseLineRect.x = baseLineX;
				baseLineRect.y = searchy;
				baseLineRect.width = 1;
				baseLineRect.height = searchheight;
				
				try {
					robot = new Robot();
					// load resources
					brazilBeanTemplate = loadIconForSearch("resources/brazil.vysor.png");
					brushTemplate = loadIconForSearch("resources/brush.vysor.png");
					starTemplate = loadIconForSearch("resources/star.vysor.png");
					nothingTemplate = loadIconForSearch("resources/nothing.vysor.jpg");
					brazilSelectTemplate = loadIconForSearch("resources/brazil.select.vysor.jpg");
					
					// 뜸들이기
					Thread.sleep(2000);
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				while(true) {
					try {
						List<Rect> listRect = new ArrayList<>();
						long start = System.currentTimeMillis();
//						User32.instance.SetForegroundWindow(w.getHwnd());
						
						// capture
						BufferedImage bi = robot.createScreenCapture(rectangle);
						Mat screenCapture = MatUtil.BI2Mat(bi);
//						byteArrayOutputStream.toByteArray()

						
//						System.out.println(screenCapture);
//						Mat screenCapture = Imgcodecs.imread("resources/testsource.jpg");
						
//						Imgcodecs.imwrite("/Volumes/d/java_project/iLoveCoffee_Macro/screen.png", screenCapture);
//						if(true) return;
						
//						Mat destinationImage = Imgcodecs.imdecode(screenCapture, Imgcodecs.IMREAD_COLOR);
						Mat destinationImage = screenCapture.clone();
						// decode to gray scale
//						Mat sourceImage = Imgcodecs.imdecode(screenCapture, Imgcodecs.IMREAD_GRAYSCALE);
						Mat sourceImage = new Mat(screenCapture.size(), CvType.CV_8UC1);
						Imgproc.cvtColor(screenCapture, sourceImage, Imgproc.COLOR_RGB2GRAY);
						
//						Point tempLoc = new Point();
//						Mat binarySourceImage = new Mat(sourceImage.size(), CvType.CV_8UC1);
//						Mat binaryTemplateImage = new Mat(templateImage.size(), CvType.CV_8UC1);
						
//						Imgproc.threshold(sourceImage, binarySourceImage, 200, 255, Imgproc.THRESH_OTSU);
//						Imgproc.threshold(templateImage, binaryTemplateImage , 200, 255, Imgproc.THRESH_OTSU);
//						Imgproc.threshold(templateImage, binaryTemplateImage , 255, 255, Imgproc.THRESH_OTSU);
//						Imgproc.adaptiveThreshold(templateImage, binaryTemplateImage, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 25);
						
						
						
						
						Mat gradThresh = new Mat();  //matrix for threshold 
						Mat hierarchy = new Mat();	//matrix for contour hierachy
						List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
						Imgproc.threshold(sourceImage, gradThresh, 200, 255, 0); // global threshold
//						Imgproc.adaptiveThreshold(sourceImage, gradThresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 3, 12);  //block size 3
						
						Mat searchThresh = gradThresh.colRange(searchx, searchx + searchwidth).rowRange(searchy, searchy + searchheight);
						Imgproc.rectangle(destinationImage, searchBoxRect, beanBorderScalar, 2);
						Imgproc.rectangle(destinationImage, baseLineRect, beanBorderScalar, 2);
//						Imgproc.findContours(gradThresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
						Imgproc.findContours(searchThresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
//						System.out.println(contours.size());
						
						// 조회된게 없으면 넘긴다.
						if(contours.size() == 0) continue;
						
						List<Rect> coffeeBeanResult = new ArrayList<>();
						List<Rect> brushResult = new ArrayList<>();
						List<Rect> starResult = new ArrayList<>();
						List<Rect> nothingRoastingResult = new ArrayList<>();
						
						Rect srch = new Rect();
						Rect rrect = null;
						for(int idx = 0; idx < contours.size(); idx++) {
							rrect = Imgproc.boundingRect(contours.get(idx));
							if (rrect.height > 80 && rrect.width > 80 && rrect.width < 150 && rrect.height < 150){
//								System.out.println("rrect");
//								Imgproc.rectangle(destinationImage, rrect, beanBorderScalar);
								
								srch.x = searchx + rrect.x - margin;
								srch.y = searchy + rrect.y - margin;
								srch.width = rrect.width + (margin * 2);
								srch.height = rrect.height + (margin * 2);

								int tix = (int) (destinationImage.width() - srch.br().x);
								int tiy = (int) (destinationImage.height() - srch.br().y);
								if(tix < 0) srch.width += tix;
								if(tiy < 0) srch.height += tiy;
//								Rect srch = rrect;
								
//								Imgproc.rectangle(destinationImage, new Point(rrect.br().x - rrect.width, rrect.br().y - rrect.height)
//										, rrect.br()
//										, beanBorderScalar, 2);
								Imgproc.rectangle(destinationImage, srch, beanBorderScalar, 2);
								
								Mat subSec = gradThresh.submat(srch);
								listRect.add(srch);
								
								Rect coffeeBeanRect = searchImage(subSec, brazilBeanTemplate, Imgproc.TM_CCORR, 0.0f);
								if(coffeeBeanRect != null) {
									coffeeBeanRect.x += srch.x;
									coffeeBeanRect.y += srch.y;
									coffeeBeanResult.add(coffeeBeanRect);
								}
								
								
								Rect brushResultRect = searchImage(subSec, brushTemplate, Imgproc.TM_CCORR, 5.0f);
								if(brushResultRect != null) {
									brushResultRect.x += srch.x;
									brushResultRect.y += srch.y;
									brushResult.add(brushResultRect);
								}
								
								
								Rect starResultRect = searchImage(subSec, starTemplate, Imgproc.TM_CCORR, 5.0f);
								if(starResultRect != null) {
									starResultRect.x += srch.x;
									starResultRect.y += srch.y;
									starResult.add(starResultRect);
								}
								
								
								// ... 이미지중 기준선기준 우측의 것만
								Rect nothingRoastingResultRect = searchImage(subSec, nothingTemplate, Imgproc.TM_CCORR, 5.0f);
								if(nothingRoastingResultRect != null && ((nothingRoastingResultRect.x + srch.x) > baseLineX)) {
									if(20 < nothingRoastingResultRect.x && nothingRoastingResultRect.x < 30 && 40 < nothingRoastingResultRect.y && nothingRoastingResultRect.y < 50) {
										nothingRoastingResultRect.x += srch.x;
										nothingRoastingResultRect.y += srch.y;
										nothingRoastingResult.add(nothingRoastingResultRect);
									}
								}
							}
						}
						
						for(Rect resultRect : coffeeBeanResult) {
							Imgproc.rectangle(destinationImage, resultRect, greenScalar, 2);
							Imgproc.putText(destinationImage, "Coffee Bean", resultRect.tl(), Imgproc.FONT_HERSHEY_TRIPLEX, 1.2, greenScalar, 1, 1);
							
							int finalx = rectangle.x + resultRect.x + resultRect.width / 2;
							int finaly = rectangle.y + resultRect.y + resultRect.height / 2;
							
							click(finalx, finaly);
							Thread.sleep(200);
						}
						
						for(Rect resultRect : brushResult) {
							Imgproc.rectangle(destinationImage, resultRect, greenScalar, 2);
							Imgproc.putText(destinationImage, "Brush", resultRect.tl(), Imgproc.FONT_HERSHEY_TRIPLEX, 1.2, greenScalar, 1, 1);

							int finalx = rectangle.x + resultRect.x + resultRect.width / 2;
							int finaly = rectangle.y + resultRect.y + resultRect.height / 2;
							
							click(finalx, finaly);
							Thread.sleep(200);
						}
						
						for(Rect resultRect : starResult) {
							Imgproc.rectangle(destinationImage, resultRect, greenScalar, 2);
							Imgproc.putText(destinationImage, "Finished", resultRect.tl(), Imgproc.FONT_HERSHEY_TRIPLEX, 1.2, greenScalar, 1, 1);

							int finalx = rectangle.x + resultRect.x + resultRect.width / 2;
							int finaly = rectangle.y + resultRect.y + resultRect.height / 2;
							
//							click(finalx, finaly);
//							Thread.sleep(200);
						}
						
						for(int ri = 0; ri < nothingRoastingResult.size(); ri++) {
							Rect resultRect = nothingRoastingResult.get(ri);
							Imgproc.rectangle(destinationImage, resultRect, greenScalar, 2);
							Imgproc.putText(destinationImage, "nothing", resultRect.tl(), Imgproc.FONT_HERSHEY_TRIPLEX, 1.2, greenScalar, 1, 1);
							
							int finalx = rectangle.x + resultRect.x + resultRect.width / 2;
							int finaly = rectangle.y + resultRect.y + resultRect.height / 2;
							click(finalx, finaly);
							Thread.sleep(200);
							
							// 원두선택메뉴는 처음에만 들어간다.
							if(ri == 0) {
//								Rect bss = searchImage(gradThresh, brazilSelectTemplate, 5.0f);
								Rect bss = new Rect(1168, 760, 300, 120);
								System.out.println("브라질산토스: " + bss);
								if(bss == null) break;
								int bsx = rectangle.x + bss.x + bss.width / 2;
								int bsy = rectangle.y + bss.y + bss.height / 2;
								Thread.sleep(150);
								click(bsx, bsy);
								Thread.sleep(150);
								click(bsx, bsy);
								Thread.sleep(105);
							}
						}
						
//						if(true) return;
						
//						Imgcodecs.imwrite("tresh.jpg", gradThresh);
//						Imgcodecs.imwrite("screen.jpg", destinationImage);
						
//						listRect
						
//						System.out.println(System.currentTimeMillis() - start);
						
						Imgproc.resize(destinationImage, destinationImage, tempSize);
						imageLabel.setIcon(new ImageIcon(MatUtil.Mat2BI(destinationImage)));
						
						Thread.sleep(2000);
						if(true) continue;
						
//						int templateHeight = templateImage.rows();
//						int templateWidth = templateImage.cols();
//						
//						float templateScale = 0.1f;
//
//						
//						for(int i = 7; i <= 12; i++){
//							int tempTemplateHeight = (int)(templateWidth * (i * templateScale));
//							int tempTemplateWidth = (int)(templateHeight * (i * templateScale));
//
//							Mat tempBinaryTemplateImage = new Mat(new Size(tempTemplateWidth,tempTemplateHeight), CvType.CV_8UC1);
//							Mat result = new Mat(new Size(sourceImage.cols() - tempTemplateWidth + 1,sourceImage.rows() - tempTemplateHeight + 1), CvType.CV_32FC1);
//
//							Imgproc.resize(binaryTemplateImage,tempBinaryTemplateImage,tempBinaryTemplateImage.size(),0,0, Imgproc.INTER_LINEAR);
//
////							float degree = 20.0f;
//							float degree = 360f;
//
//
//							Mat rotateBinaryTemplateImage = new Mat(tempBinaryTemplateImage.size(), CvType.CV_8UC1);
//							for(int j = 0; j <= (360 / degree); j++){
//
//								for(int y = 0; y < tempTemplateHeight; y++){
//									for(int x = 0; x < tempTemplateWidth; x++){
//										rotateBinaryTemplateImage.put(y, x, 255);
//									}
//								}
//
//
//								for(int y = 0; y < tempTemplateHeight; y++){
//									for(int x = 0; x < tempTemplateWidth; x++){
//
//										float radian = (float) (j * degree * Math.PI / 360);
//
//										int rotateY = (int) (- Math.sin(radian) * ((float)x - (float)tempTemplateWidth / 2.0f) + Math.cos(radian) * ((float)y - (float)tempTemplateHeight / 2.0f) + tempTemplateHeight / 2);
//										int rotateX = (int) (Math.cos(radian) * ((float)x - (float)tempTemplateWidth / 2.0f) + Math.sin(radian) * ((float)y - (float)tempTemplateHeight / 2.0f) + tempTemplateWidth / 2);
//
//										if(rotateY < tempTemplateHeight && rotateX < tempTemplateWidth && rotateY >= 0 && rotateX  >= 0)
//											rotateBinaryTemplateImage.put(y, x, tempBinaryTemplateImage.get(rotateY, rotateX));
//									}
//								}
//
//								Imgproc.matchTemplate(binarySourceImage, rotateBinaryTemplateImage, result, Imgproc.TM_SQDIFF_NORMED);
//
//								MinMaxLocResult mmr = Core.minMaxLoc(result);
//
////								System.out.println((int)(i * templateScale * 100) + " , " + (j * degree) + " , " + (1 - mmr.minVal) * 100);
//
//								if(mmr.minVal <= 0.2){ // 1 - 0.065 = 0.935 : 93.5%
////									tempLoc.x = mmr.minLoc.x + tempTemplateWidth;
////									tempLoc.y = mmr.minLoc.y + tempTemplateHeight;
////									Imgproc.rectangle(destinationImage, mmr.minLoc, tempLoc, beanBorderScalar, 2, 8, 0);
//									Integer[] arr = new Integer[4];
//									arr[0] = (int) mmr.minLoc.x;
//									arr[1] = (int) mmr.minLoc.y;
//									arr[2] = tempTemplateWidth;
//									arr[3] = tempTemplateHeight;
//									list.add(arr);
//								}
//							}
//						}
						
//						Imgproc.resize(destinationImage, destinationImage, tempSize);
//						ImageIO.write(MatUtil.Mat2BI(destinationImage), "png", new File("screen.png"));
//						System.out.println(System.currentTimeMillis() - start);
//						imageLabel.setIcon(new ImageIcon(MatUtil.Mat2BI(destinationImage)));

//						Window win = new Window(null) {
//							private static final long serialVersionUID = 1L;
//
//							@Override
//							public void paint(Graphics g) {
//								g.clearRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
//								g.setColor(Color.green);
//								for(Integer[] arr : list) {
//									System.out.println(list.size());
//									g.drawRect(arr[0], arr[1], arr[2], arr[3]);
//								}
//							}
//
//							@Override
//							public void update(Graphics g) {
//								paint(g);
//							}
//						};
//						win.setAlwaysOnTop(true);
////						win.setBounds(win.getGraphicsConfiguration().getBounds());
//						win.setBounds(rectangle);
//						win.setBackground(new Color(0, true));
//						win.setVisible(true);
						
						Thread.sleep(10);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			});
			t.setDaemon(true);
			t.start();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Rect searchImage(Mat source, Mat template, int method, float rank) {
		Mat result = new Mat();
//		Imgproc.matchTemplate(subSec, binaryTemplateImage, result, Imgproc.TM_SQDIFF_NORMED);
		Imgproc.matchTemplate(source, template, result, method);

		MinMaxLocResult mmr = Core.minMaxLoc(result);
		Rect srchRect = new Rect();
//		System.out.println(mmr.minVal);
//		if(mmr.minVal <= 0.5){ // 1 - 0.065 = 0.935 : 93.5%
//		if(mmr.minVal <= 3){ // 1 - 0.065 = 0.935 : 93.5%
		if(mmr.minVal <= rank){ // 1 - 0.065 = 0.935 : 93.5%
//		if(mmr.minVal <= 0){ // 1 - 0.065 = 0.935 : 93.5%
//			srchRect.x = (int) (srch.x + mmr.minLoc.x);
//			srchRect.y = (int) (srch.y + mmr.minLoc.y);
//			srchRect.width = binaryTemplateImage.width();
//			srchRect.height = binaryTemplateImage.height();
			srchRect.x = (int) (mmr.minLoc.x);
			srchRect.y = (int) (mmr.minLoc.y);
			srchRect.width = template.width();
			srchRect.height = template.height();
//			
//			Scalar s = new Scalar(255, 255, 0);
//			Imgproc.rectangle(destinationImage, srchRect, s, 2);
//			Imgproc.putText(destinationImage, idx + "", srchRect.tl(), Imgproc.FONT_HERSHEY_TRIPLEX, 1.2, s);
			return srchRect;
		}
		return null;
	}
	
	public static Mat loadIconForSearch(String path) throws Exception {
		Mat templateImage = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
		Mat binaryTemplateImage = new Mat(templateImage.size(), CvType.CV_8UC1);
		Imgproc.adaptiveThreshold(templateImage, binaryTemplateImage, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 25);
		return binaryTemplateImage;
	}
	
	public static void click(int x, int y) throws Exception {
//		if(true) return;
//		System.out.println(System.currentTimeMillis());
	    Robot bot = new Robot();
	    bot.mouseMove(x, y);
	    bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
	    Thread.sleep(10);
	    bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}
	
}