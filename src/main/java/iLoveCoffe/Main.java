package iLoveCoffe;

import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
			
			Map<String, Boolean> searchStatusTable = new Hashtable<>();
			Map<String, List<Rect>> searchResultTable = new Hashtable<>();
			
			// click
			boolean clickAble = true;
			searchStatusTable.put("allStop", false);
			
			// opencv
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			
			// window
			int hWnd = User32.instance.FindWindowA(null, "SM G930S");
			Rectangle rectangle = new Rectangle();
			
			Thread t = new Thread(() -> {

				WindowInfo w = WindowUtil.getWindowInfo(hWnd);
				RECT rect = w.getRect();
				synchronized (rectangle) {
//					rectangle = new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
					rectangle.x = rect.left;
					rectangle.y = rect.top;
					rectangle.width = rect.right - rect.left;
					rectangle.height = rect.bottom - rect.top;
				}
//				Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
				
				Robot robot = null;
				
				Mat brazilBeanTemplate = null;
				Mat brushTemplate = null;
				Mat starTemplate = null;
				Mat nothingTemplate = null;
//				Mat brazilSelectTemplate = null;
				
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
//					brazilSelectTemplate = loadIconForSearch("resources/brazil.select.vysor.jpg");
					
					// 뜸들이기
					Thread.sleep(500);
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

//						Mat screenCapture = Imgcodecs.imread("resources/testsource.jpg");
						
//						Mat destinationImage = Imgcodecs.imdecode(screenCapture, Imgcodecs.IMREAD_COLOR);
						Mat destinationImage = screenCapture.clone();
						// decode to gray scale
//						Mat sourceImage = Imgcodecs.imdecode(screenCapture, Imgcodecs.IMREAD_GRAYSCALE);
						Mat sourceImage = new Mat(screenCapture.size(), CvType.CV_8UC1);
						Imgproc.cvtColor(screenCapture, sourceImage, Imgproc.COLOR_RGB2GRAY);
						
						
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
						
						// 조회된게 없으면 넘긴다.
						if(contours.size() == 0) continue;
						
						List<Rect> coffeeBeanResult = new ArrayList<>();
						List<Rect> brushResult = new ArrayList<>();
						List<Rect> coffeeFinishedResult = new ArrayList<>();
						List<Rect> nothingRoastingResult = new ArrayList<>();
						List<Rect> nothingSellingResult = new ArrayList<>();
						
						Rect srch = new Rect();
						Rect rrect = null;
						for(int idx = 0; idx < contours.size(); idx++) {
							rrect = Imgproc.boundingRect(contours.get(idx));
							if (rrect.height > 90 && rrect.width > 90 && rrect.width < 130 && rrect.height < 130){
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
								
//								Imgproc.rectangle(destinationImage, new Point(rrect.br().x - rrect.width, rrect.br().y - rrect.height)
//										, rrect.br()
//										, beanBorderScalar, 2);
								Imgproc.rectangle(destinationImage, srch, beanBorderScalar, 2);
								
								Mat subSec = gradThresh.submat(srch);
								listRect.add(srch);
								
								Rect coffeeBeanRect = searchImage(subSec, brazilBeanTemplate, Imgproc.TM_CCORR, 3.0f, true);
								if(coffeeBeanRect != null) {
									coffeeBeanRect.x += srch.x;
									coffeeBeanRect.y += srch.y;
									coffeeBeanResult.add(coffeeBeanRect);
								}
								
								
								Rect brushResultRect = searchImage(subSec, brushTemplate, Imgproc.TM_CCORR_NORMED, 0.01f, true);
								if(brushResultRect != null) {
									brushResultRect.x += srch.x;
									brushResultRect.y += srch.y;
									brushResult.add(brushResultRect);
								}
								
								
								// 별 이미지중 기준선기준 좌측의 것만
								// 검출 y좌표
								Rect coffeeFinishedResultRect = searchImage(subSec, starTemplate, Imgproc.TM_CCORR_NORMED, 0.03f, true);
								if(coffeeFinishedResultRect != null && ((coffeeFinishedResultRect.x + srch.x) <  baseLineX) && (coffeeFinishedResultRect.y > 10)) {
									System.out.println(coffeeFinishedResultRect.y);
									coffeeFinishedResultRect.x += srch.x;
									coffeeFinishedResultRect.y += srch.y;
									coffeeFinishedResult.add(coffeeFinishedResultRect);
								}
								
								
								// ... 이미지중 기준선기준 우측의 것만
//								Rect nothingRoastingResultRect = searchImage(subSec, nothingTemplate, Imgproc.TM_CCORR, 10.0f);
//								Rect nothingRoastingResultRect = searchImage(subSec, nothingTemplate, Imgproc.TM_CCORR_NORMED, 0.005f, true);
								Rect nothingResultRect = searchImage(subSec, nothingTemplate, Imgproc.TM_CCORR_NORMED, 0.005f, true);
//								System.out.println(nothingRoastingResultRect);
								if(nothingResultRect != null) {
									if(20 < nothingResultRect.x && nothingResultRect.x < 30 && 40 < nothingResultRect.y && nothingResultRect.y < 50) {
										// 우측 원두
										if((nothingResultRect.x + srch.x) > baseLineX) {
											nothingRoastingResult.add(nothingResultRect);
										}
										// 좌측 커피머신
										else {
											nothingSellingResult.add(nothingResultRect);
										}
										nothingResultRect.x += srch.x;
										nothingResultRect.y += srch.y;
									}
								}
							}
						}
//						System.out.println(searchStatusTable);
						
						if(searchStatusTable.getOrDefault("coffeeBeanResult", true)) {
							if(coffeeBeanResult.size() > 0) {
								searchResultTable.put("coffeeBeanResult", coffeeBeanResult);
								searchStatusTable.put("coffeeBeanResult", false);
							}
						}
						
						for(Rect resultRect : coffeeBeanResult) {
							Imgproc.rectangle(destinationImage, resultRect, greenScalar, 2);
							Imgproc.putText(destinationImage, "Coffee Bean", resultRect.tl(), Imgproc.FONT_HERSHEY_TRIPLEX, 1.2, greenScalar, 1, 1);
						}
						
						if(searchStatusTable.getOrDefault("brushResult", true)) {
							if(brushResult.size() > 0) {
								searchResultTable.put("brushResult", brushResult);
								searchStatusTable.put("brushResult", false);
							}
						}
						
						for(Rect resultRect : brushResult) {
							Imgproc.rectangle(destinationImage, resultRect, greenScalar, 2);
							Imgproc.putText(destinationImage, "Brush", resultRect.tl(), Imgproc.FONT_HERSHEY_TRIPLEX, 1.2, greenScalar, 1, 1);
						}
						
						
						if(searchStatusTable.getOrDefault("coffeeFinishedResult", true)) {
							if(coffeeFinishedResult.size() > 0) {
								searchResultTable.put("coffeeFinishedResult", coffeeFinishedResult);
								searchStatusTable.put("coffeeFinishedResult", false);
							}
						}
						
						// star는 조정 필요
						for(Rect resultRect : coffeeFinishedResult) {
							Imgproc.rectangle(destinationImage, resultRect, greenScalar, 2);
							Imgproc.putText(destinationImage, "Finished", resultRect.tl(), Imgproc.FONT_HERSHEY_TRIPLEX, 1.2, greenScalar, 1, 1);
						}
						
						// 원두
						if(searchStatusTable.getOrDefault("nothingRoastingResult", true)) {
							if(nothingRoastingResult.size() > 0) {
								searchResultTable.put("nothingRoastingResult", nothingRoastingResult);
								searchStatusTable.put("nothingRoastingResult", false);
							}
						}
						
						for(int ri = 0; ri < nothingRoastingResult.size(); ri++) {
							Rect resultRect = nothingRoastingResult.get(ri);
							Imgproc.rectangle(destinationImage, resultRect, greenScalar, 2);
							Imgproc.putText(destinationImage, "nothing", resultRect.tl(), Imgproc.FONT_HERSHEY_TRIPLEX, 1.2, greenScalar, 1, 1);
						}
						
						// 커피
						if(searchStatusTable.getOrDefault("nothingSellingResult", true)) {
							if(nothingSellingResult.size() > 0) {
								searchResultTable.put("nothingSellingResult", nothingSellingResult);
								searchStatusTable.put("nothingSellingResult", false);
							}
						}
						
						for(int ri = 0; ri < nothingSellingResult.size(); ri++) {
							Rect resultRect = nothingSellingResult.get(ri);
							Imgproc.rectangle(destinationImage, resultRect, greenScalar, 2);
							Imgproc.putText(destinationImage, "nothing", resultRect.tl(), Imgproc.FONT_HERSHEY_TRIPLEX, 1.2, greenScalar, 1, 1);
						}
						
//						if(!searchStatusTable.get("allStop")) {
//							searchResultTable.clear();
//							coffeeBeanResult.clear();
//							brushResult.clear();
//							coffeeFinishedResult.clear();
//							nothingRoastingResult.clear();
//						}
						
						Imgproc.resize(destinationImage, destinationImage, tempSize);
						imageLabel.setIcon(new ImageIcon(MatUtil.Mat2BI(destinationImage)));
						
						Thread.sleep(10);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			});
			t.setDaemon(true);
			t.start();
			
			Thread coffeeBeanClickThread = new Thread(() -> {
				while(clickAble) {
					try {
						if(!searchStatusTable.getOrDefault("coffeeBeanResult", true)) {
							if(searchStatusTable.get("allStop")) {
								searchResultTable.remove("coffeeBeanResult");
								searchStatusTable.put("coffeeBeanResult", true);
								Thread.sleep(100);
								continue;
							}
							searchStatusTable.put("allStop", true);
							
							for(Rect resultRect : searchResultTable.get("coffeeBeanResult")) {
								int finalx = rectangle.x + resultRect.x + resultRect.width / 2;
								int finaly = rectangle.y + resultRect.y + resultRect.height / 2;
								
								click(finalx, finaly);
								Thread.sleep(200);
							}

							Thread.sleep(2000);
							searchStatusTable.put("coffeeBeanResult", true);
							searchStatusTable.put("allStop", false);
						}
					} catch(Exception e) {
						e.printStackTrace();
						searchStatusTable.put("coffeeBeanResult", true);
						searchStatusTable.put("allStop", false);
					}
				}
			});
			coffeeBeanClickThread.setDaemon(true);
			coffeeBeanClickThread.start();
			
			Thread brushThread = new Thread(() -> {
				while(clickAble) {
					try {
						if(!searchStatusTable.getOrDefault("brushResult", true)) {
							if(searchStatusTable.get("allStop")) {
								searchResultTable.remove("brushResult");
								searchStatusTable.put("brushResult", true);
								Thread.sleep(100);
								continue;
							}
							searchStatusTable.put("allStop", true);
							
							for(Rect resultRect : searchResultTable.get("brushResult")) {
								int finalx = rectangle.x + resultRect.x + resultRect.width / 2;
								int finaly = rectangle.y + resultRect.y + resultRect.height / 2;
								
								click(finalx, finaly);
								Thread.sleep(200);
							}

							Thread.sleep(1000);
							searchStatusTable.put("brushResult", true);
							searchStatusTable.put("allStop", false);
						}
					} catch(Exception e) {
						e.printStackTrace();
						searchStatusTable.put("brushResult", true);
						searchStatusTable.put("allStop", false);
					}
				}
			});
			brushThread.setDaemon(true);
			brushThread.start();
			
			Thread coffeeFinishedThread = new Thread(() -> {
				while(clickAble) {
					try {
						if(!searchStatusTable.getOrDefault("coffeeFinishedResult", true)) {
							if(searchStatusTable.get("allStop")) {
								searchResultTable.remove("coffeeFinishedResult");
								searchStatusTable.put("coffeeFinishedResult", true);
								Thread.sleep(100);
								continue;
							}
							searchStatusTable.put("allStop", true);
							
							for(Rect resultRect : searchResultTable.get("coffeeFinishedResult")) {
								int finalx = rectangle.x + resultRect.x + resultRect.width / 2;
								int finaly = rectangle.y + resultRect.y + resultRect.height / 2;
								
								click(finalx, finaly);
								Thread.sleep(200);
							}

							Thread.sleep(3000);
							searchStatusTable.put("coffeeFinishedResult", true);
							searchStatusTable.put("allStop", false);
						}
					} catch(Exception e) {
						e.printStackTrace();
						searchStatusTable.put("coffeeFinishedResult", true);
						searchStatusTable.put("allStop", false);
					}
				}
			});
			coffeeFinishedThread.setDaemon(true);
			coffeeFinishedThread.start();
			
			Thread nothingRoastingThread = new Thread(() -> {
				while(clickAble) {
					try {
						if(!searchStatusTable.getOrDefault("nothingRoastingResult", true)) {
							List<Rect> nothingRoastingResult = searchResultTable.get("nothingRoastingResult");
							if(nothingRoastingResult.size() != 6) {
								System.out.println("로스팅머신의 개수가 6이아님 " + nothingRoastingResult.size());
								searchStatusTable.put("nothingRoastingResult", true);
								continue;
							}
							
							if(searchStatusTable.get("allStop")) {
								searchResultTable.remove("nothingRoastingResult");
								searchStatusTable.put("nothingRoastingResult", true);
								Thread.sleep(100);
								continue;
							}
							searchStatusTable.put("allStop", true);
							
							for(int ri = 0; ri < nothingRoastingResult.size(); ri++) {
								Rect resultRect = nothingRoastingResult.get(ri);
								
								int finalx = rectangle.x + resultRect.x + resultRect.width / 2;
								int finaly = rectangle.y + resultRect.y + resultRect.height / 2;
								click(finalx, finaly);
								Thread.sleep(200);
								
								// 원두선택메뉴는 처음에만 들어간다.
								if(ri == 0) {
									Rect bss = new Rect(1168, 760, 300, 120);
//									System.out.println("브라질산토스: " + bss);
									if(bss == null) break;
									int bsx = rectangle.x + bss.x + bss.width / 2;
									int bsy = rectangle.y + bss.y + bss.height / 2;
									click(bsx, bsy);
									Thread.sleep(200);
									click(bsx, bsy);
									Thread.sleep(200);
								}
							}
							
							Thread.sleep(3000);
							searchStatusTable.put("nothingRoastingResult", true);
							searchStatusTable.put("allStop", false);
						}
					} catch(Exception e) {
						e.printStackTrace();
						searchStatusTable.put("nothingRoastingResult", true);
						searchStatusTable.put("allStop", false);
					}
				}
			});
			nothingRoastingThread.setDaemon(true);
			nothingRoastingThread.start();
			
			Thread nothingSellingThread = new Thread(() -> {
				while(clickAble) {
					try {
						if(!searchStatusTable.getOrDefault("nothingSellingResult", true)) {
							if(searchStatusTable.get("allStop")) {
								searchResultTable.remove("nothingSellingResult");
								searchStatusTable.put("nothingSellingResult", true);
								Thread.sleep(100);
								continue;
							}
							searchStatusTable.put("allStop", true);

							List<Rect> nothingSellingResult = searchResultTable.get("nothingSellingResult");
							
							for(int ri = 0; ri < nothingSellingResult.size(); ri++) {
								Rect resultRect = nothingSellingResult.get(ri);
								
								int finalx = rectangle.x + resultRect.x + resultRect.width / 2;
								int finaly = rectangle.y + resultRect.y + resultRect.height / 2;
								click(finalx, finaly);
								Thread.sleep(200);
								
								// 원두선택메뉴는 처음에만 들어간다.
								if(ri == 0) {
									Rect bss = new Rect(740, 300, 440, 80);
									if(bss == null) break;
									int bsx = rectangle.x + bss.x + bss.width / 2;
									int bsy = rectangle.y + bss.y + bss.height / 2;
									click(bsx, bsy);
									Thread.sleep(200);
									
									bss = new Rect(360, 850, 300, 75);
//									System.out.println("브라질산토스: " + bss);
									if(bss == null) break;
									bsx = rectangle.x + bss.x + bss.width / 2;
									bsy = rectangle.y + bss.y + bss.height / 2;
									click(bsx, bsy);
									Thread.sleep(200);
									click(bsx, bsy);
									Thread.sleep(200);
								}
							}
							
							Thread.sleep(200);
							searchStatusTable.put("nothingSellingResult", true);
							searchStatusTable.put("allStop", false);
						}
					} catch(Exception e) {
						e.printStackTrace();
						searchStatusTable.put("nothingSellingResult", true);
						searchStatusTable.put("allStop", false);
					}
				}
			});
			nothingSellingThread.setDaemon(true);
			nothingSellingThread.start();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Rect searchImage(Mat source, Mat template, int method, float rank, boolean lower) {
		Mat result = new Mat();
//		Imgproc.matchTemplate(subSec, binaryTemplateImage, result, Imgproc.TM_SQDIFF_NORMED);
		Imgproc.matchTemplate(source, template, result, method);

		MinMaxLocResult mmr = Core.minMaxLoc(result);
		Rect srchRect = new Rect();
		if(lower) {
			if(mmr.minVal <= rank){ // 1 - 0.065 = 0.935 : 93.5%
				srchRect.x = (int) (mmr.minLoc.x);
				srchRect.y = (int) (mmr.minLoc.y);
				srchRect.width = template.width();
				srchRect.height = template.height();
				
				return srchRect;
			}
		}
		else {
			System.out.println(mmr.minVal);
			if(mmr.minVal >= rank){ // 1 - 0.065 = 0.935 : 93.5%
				srchRect.x = (int) (mmr.minLoc.x);
				srchRect.y = (int) (mmr.minLoc.y);
				srchRect.width = template.width();
				srchRect.height = template.height();
				
				return srchRect;
			}
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