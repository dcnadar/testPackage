package com.radyfy.common.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    public static void mains(String[] args) throws IOException {
        BufferedImage img = null;
        File f = null;

        // read image
        try {
            f = new File("/opt/temp/test.png");
            img = ImageIO.read(f);
        } catch (IOException e) {
            System.out.println(e);
        }

        // get image width and height
        int width = img.getWidth();
        int height = img.getHeight();

        /**
         * Since, file is a single pixel image so, we will be using the width and height
         * variable in this operation.
         */
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int p = img.getRGB(x, y);

                // get alpha
                int a = (p >> 24) & 0xff;

                // get red
                int r = (p >> 16) & 0xff;

                // get green
                int g = (p >> 8) & 0xff;

                // get blue
                int b = p & 0xff;
                if ((r == g && g == b && b == r) && (r >= 230 && r <= 254)) {
                    a = 255;
                    r = 255;
                    g = 255;
                    b = 255;

                    // set the pixel value
                    p = (a << 24) | (r << 16) | (g << 8) | b;
                    img.setRGB(x, y, p);
                }
            }
        }
        // write image
        try {
            f = new File("/Users/pintu/Desktop/result.png");
            ImageIO.write(img, "jpg", f);
            System.out.println("done");
        } catch (IOException e) {
            System.out.println(e);
        }
//		}
    }

}
