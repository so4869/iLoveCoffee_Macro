package iLoveCoffe;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
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
	public static void main(String[] args){
    	try {
//    		JFrame frame = new JFrame();
//    		frame.getContentPane().setLayout(new FlowLayout());
//    		frame.pack();
//    		JLabel imageLabel = new JLabel();
//    		frame.getContentPane().add(imageLabel);
//    		frame.setVisible(true);
//    		frame.setSize(100, 100);
    		
    		
    		// opencv
        	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        	
        	// window
            int hWnd = User32.instance.FindWindowA(null, "제어판");
            Thread t = new Thread(() -> {

        		WindowInfo w = WindowUtil.getWindowInfo(hWnd);
                RECT rect = w.getRect();
                Rectangle rectangle = new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
                
                Robot robot = null;
            	
                BufferedImage xImage = null;
                Mat templateImage = null;
                Scalar beanBorderScalar = new Scalar(50,205,154);
                Size tempSize = new Size(1280, 720);
                try {
                	robot = new Robot();
                    // load x
                	xImage = ImageIO.read(Paths.get("resources/bean4.jpg").toFile());
                	templateImage = Imgcodecs.imdecode(MatUtil.BI2Mat(xImage), Imgcodecs.IMREAD_GRAYSCALE);
                } catch(Exception e) {
                	e.printStackTrace();
                }
                
//            	while(true) {
            		try {
            			List<Integer[]> list = new ArrayList<>();
            			long start = System.currentTimeMillis();
//                        User32.instance.SetForegroundWindow(w.getHwnd());
                        
                    	// capture
//                        BufferedImage screenCapture = robot.createScreenCapture(rectangle);
                        BufferedImage screenCapture = ImageIO.read(Paths.get("resources/testsource.jpg").toFile());
                        
                        // decode to gray scale
                        Mat destinationImage = Imgcodecs.imdecode(MatUtil.BI2Mat(screenCapture), Imgcodecs.IMREAD_COLOR);
                        Mat sourceImage = Imgcodecs.imdecode(MatUtil.BI2Mat(screenCapture), Imgcodecs.IMREAD_GRAYSCALE);
                        
//                        Point tempLoc = new Point();
                        Mat binarySourceImage = new Mat(sourceImage.size(), CvType.CV_8UC1);
                        Mat binaryTemplateImage = new Mat(templateImage.size(), CvType.CV_8UC1);
                        
                        Imgproc.threshold(sourceImage, binarySourceImage , 200, 255, Imgproc.THRESH_OTSU);
                        Imgproc.threshold(templateImage, binaryTemplateImage , 200, 255, Imgproc.THRESH_OTSU);
                        

                        
                        
                        Mat gradThresh = new Mat();  //matrix for threshold 
                        Mat hierarchy = new Mat();    //matrix for contour hierachy
                        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//                        Imgproc.threshold(sourceImage,gradThresh, 127,255,0); // global threshold
                        Imgproc.adaptiveThreshold(sourceImage, gradThresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 12);  //block size 3
                        Imgproc.findContours(gradThresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
                        System.out.println(contours.size());
                        if(contours.size()>0) {
                            for(int idx = 0; idx < contours.size(); idx++) {
                                Rect rrect = Imgproc.boundingRect(contours.get(idx));
//                                && !(rrect.width >= 1000 - 5 && rrect.height >= 1000 - 5)
                                if (rrect.height > 20 && rrect.width > 20){
                                    Imgproc.rectangle(destinationImage, new Point(rrect.br().x - rrect.width, rrect.br().y - rrect.height)
                                            , rrect.br()
                                            , beanBorderScalar, 2);
                                }

                            }
                            
                            Imgcodecs.imwrite("screen.jpg", destinationImage);
                        }
                        
                        if(true) return;
                        
                        
                        
                        
                        
                        

                        
                        
                        
                        int templateHeight = templateImage.rows();
                        int templateWidth = templateImage.cols();
                        
                        float templateScale = 0.1f;

                        
                        for(int i = 7; i <= 12; i++){
                            int tempTemplateHeight = (int)(templateWidth * (i * templateScale));
                            int tempTemplateWidth = (int)(templateHeight * (i * templateScale));

                            Mat tempBinaryTemplateImage = new Mat(new Size(tempTemplateWidth,tempTemplateHeight), CvType.CV_8UC1);
                            Mat result = new Mat(new Size(sourceImage.cols() - tempTemplateWidth + 1,sourceImage.rows() - tempTemplateHeight + 1), CvType.CV_32FC1);

                            Imgproc.resize(binaryTemplateImage,tempBinaryTemplateImage,tempBinaryTemplateImage.size(),0,0, Imgproc.INTER_LINEAR);

//                            float degree = 20.0f;
                            float degree = 360f;


                            Mat rotateBinaryTemplateImage = new Mat(tempBinaryTemplateImage.size(), CvType.CV_8UC1);
                            for(int j = 0; j <= (360 / degree); j++){

                                for(int y = 0; y < tempTemplateHeight; y++){
                                    for(int x = 0; x < tempTemplateWidth; x++){
                                        rotateBinaryTemplateImage.put(y, x, 255);
                                    }
                                }


                                for(int y = 0; y < tempTemplateHeight; y++){
                                    for(int x = 0; x < tempTemplateWidth; x++){

                                        float radian = (float) (j * degree * Math.PI / 360);

                                        int rotateY = (int) (- Math.sin(radian) * ((float)x - (float)tempTemplateWidth / 2.0f) + Math.cos(radian) * ((float)y - (float)tempTemplateHeight / 2.0f) + tempTemplateHeight / 2);
                                        int rotateX = (int) (Math.cos(radian) * ((float)x - (float)tempTemplateWidth / 2.0f) + Math.sin(radian) * ((float)y - (float)tempTemplateHeight / 2.0f) + tempTemplateWidth / 2);

                                        if(rotateY < tempTemplateHeight && rotateX < tempTemplateWidth && rotateY >= 0 && rotateX  >= 0)
                                        	rotateBinaryTemplateImage.put(y, x, tempBinaryTemplateImage.get(rotateY, rotateX));
                                    }
                                }

                                Imgproc.matchTemplate(binarySourceImage, rotateBinaryTemplateImage, result, Imgproc.TM_SQDIFF_NORMED);

                                MinMaxLocResult mmr = Core.minMaxLoc(result);

//                                System.out.println((int)(i * templateScale * 100) + " , " + (j * degree) + " , " + (1 - mmr.minVal) * 100);

                                if(mmr.minVal <= 0.2){ // 1 - 0.065 = 0.935 : 93.5%
//                                    tempLoc.x = mmr.minLoc.x + tempTemplateWidth;
//                                    tempLoc.y = mmr.minLoc.y + tempTemplateHeight;
//                                    Imgproc.rectangle(destinationImage, mmr.minLoc, tempLoc, beanBorderScalar, 2, 8, 0);
                                	Integer[] arr = new Integer[4];
                                	arr[0] = (int) mmr.minLoc.x;
                                	arr[1] = (int) mmr.minLoc.y;
                                	arr[2] = tempTemplateWidth;
                                	arr[3] = tempTemplateHeight;
                                	list.add(arr);
                                }
                            }
                        }
                        
//                        Imgproc.resize(destinationImage, destinationImage, tempSize);
//                        ImageIO.write(MatUtil.Mat2BI(destinationImage), "png", new File("screen.png"));
//						System.out.println(System.currentTimeMillis() - start);
//                		imageLabel.setIcon(new ImageIcon(MatUtil.Mat2BI(destinationImage)));

						Window win = new Window(null) {
							private static final long serialVersionUID = 1L;

							@Override
							public void paint(Graphics g) {
								g.clearRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
								g.setColor(Color.green);
								for(Integer[] arr : list) {
									System.out.println(list.size());
									g.drawRect(arr[0], arr[1], arr[2], arr[3]);
								}
							}

							@Override
							public void update(Graphics g) {
								paint(g);
							}
						};
						win.setAlwaysOnTop(true);
//						win.setBounds(win.getGraphicsConfiguration().getBounds());
						win.setBounds(rectangle);
						win.setBackground(new Color(0, true));
						win.setVisible(true);
                		
                		Thread.sleep(10);
                	} catch(Exception e) {
                		e.printStackTrace();
                	}
//            	}
            });
//            t.setDaemon(true);
            t.start();
            
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
}