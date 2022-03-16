package util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class MatUtil {
	public static Mat BI2Mat(BufferedImage bi) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return new MatOfByte(byteArrayOutputStream.toByteArray());
	}
	
	public static BufferedImage Mat2BI(Mat mat) throws IOException {
		MatOfByte mob=new MatOfByte();
	    Imgcodecs.imencode(".jpg", mat, mob);
	    return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
	}
}
