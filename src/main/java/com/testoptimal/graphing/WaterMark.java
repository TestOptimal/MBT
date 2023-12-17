/***********************************************************************************************
 * Copyright (c) 2009-2024 TestOptimal.com
 *
 * This file is part of TestOptimal MBT.
 *
 * TestOptimal MBT is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * TestOptimal MBT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with TestOptimal MBT. 
 * If not, see <https://www.gnu.org/licenses/>.
 ***********************************************************************************************/

package com.testoptimal.graphing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.testoptimal.util.DateUtil;
 
/**
 * This is an utility class to underlay a watermark image or text to an image file passed in.  It stores the 
 * output image file in the outputFile_p passed in.
 * 
 * @author yxl01
 *
 */
public class WaterMark {
	
	public static void main(String[] args) throws IOException {
//		addImage("c:/temp/testImage.jpg", "c:/temp/TestOptimalLogo.png", "c:/temp/sample1Output.jpg");
//		String inputFile = "C:/unzipped/TestOptimal/temp/WebStore.png";
//		String inputFile = "C:/unzipped/TestOptimal/temp/WebStore.png";
//		String inputFile = "C:/unzipped/TestOptimal/temp/WebStore.png";
//		String inputFile = "C:/unzipped/TestOptimal/temp/WebStore.png";
		String inputFile = "C:/unzipped/TestOptimal/temp/DemoJava.png";
        String outputFile = "c:/temp/sample2Output.bmp";
		
		addTextAllSides(inputFile, "TestOptimal", outputFile);
	}
	
	public static void addTextAllSides(String inputFile_p, String text_p, String outputFile_p) throws IOException {
		File imgFile = new File(inputFile_p);
		BufferedImage originalImg = ImageIO.read(imgFile);
        originalImg = addText(originalImg, text_p, 'A', 18); // bottom left
        originalImg = addText(originalImg, text_p, 'B', 18); // bottom middle
        originalImg = addText(originalImg, DateUtil.dateToString(new java.util.Date(), "MM/dd/yyyy HH:mm:ss"), 'C', 18); // bottom right
        originalImg = addText(originalImg, text_p, 'S', 18); // top left
        originalImg = addText(originalImg, text_p, 'T', 18); // top middle
        originalImg = addText(originalImg, text_p, 'U', 18); // top right
        originalImg = addText(originalImg, text_p, 'L', 18); // left
        originalImg = addText(originalImg, text_p, 'R', 18); // right

        String imgFormat = outputFile_p.substring(outputFile_p.lastIndexOf(".")+1);
        if (imgFormat==null || imgFormat.equals("")) imgFormat = "png";
        ImageIO.write(originalImg, imgFormat, new File(outputFile_p));
//        display(originalImg);
        return;
	}

	public static void addWaterMark(String inputFile_p, String outputFile_p) throws IOException {
		File imgFile = new File(inputFile_p);
		BufferedImage originalImg = ImageIO.read(imgFile);
        originalImg = addText(originalImg, "TestOptimal", 'A', 12); // bottom left
//      originalImg = addText(originalImg, text_p, 'B'); // bottom middle
        originalImg = addText(originalImg, DateUtil.dateToString(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"), 'C', 12); // bottom right
//        originalImg = addText(originalImg, text_p, 'S'); // top left
//        originalImg = addText(originalImg, text_p, 'T'); // top middle
//        originalImg = addText(originalImg, text_p, 'U'); // top right
//        originalImg = addText(originalImg, text_p, 'L'); // left
//        originalImg = addText(originalImg, text_p, 'R'); // right

        String imgFormat = outputFile_p.substring(outputFile_p.lastIndexOf(".")+1);
        if (imgFormat==null || imgFormat.equals("")) imgFormat = "png";
        ImageIO.write(originalImg, imgFormat, new File(outputFile_p));
//        display(originalImg);
        return;
	}

	/**
	 * adds a watermark image to the image passed in.
	 * @param imageFile_p
	 * @param waterMarkFile_p
	 * @param outputFile_p
	 * @throws IOException
	 */
    public static void addImage(String imageFile_p, String waterMarkFile_p, String outputFile_p) throws IOException {
        BufferedImage originalImg = ImageIO.read(new File(imageFile_p));
        BufferedImage waterMarkImg = ImageIO.read(new File(waterMarkFile_p));

        Graphics2D g = originalImg.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        g.drawImage(waterMarkImg, (originalImg.getWidth()-waterMarkImg.getWidth())/2, (originalImg.getHeight()-waterMarkImg.getHeight())/2, null);
        g.dispose();
        String imgFormat = outputFile_p.substring(outputFile_p.lastIndexOf(".")+1);
        if (imgFormat==null || imgFormat.equals("")) imgFormat = "png";
        ImageIO.write(originalImg, imgFormat, new File(outputFile_p));
    }
 
    private static void display(BufferedImage image) {
        JFrame f = new JFrame("WaterMark");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new JLabel(new ImageIcon(image)));
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
    
    
    /**
     * adds a text as watermark to the image passed in. The output image format is determined by
     * the extension of the output file name.
     * 
     * If the image size is too small, only certain text will be printed.
     * 
     * @param imageFile_p
     * @param waterMarkText_p
     * @param side_p code to indicate where to add the text: T for top, B for bottom, L for left, R for right
     *      S for top left, U for top right, A for bottom left and C for bottom right.
     * @throws IOException
     */
    private static BufferedImage addText(BufferedImage originalImg, String waterMarkText_p, char side_p, int fontSize_p) throws IOException {

        Graphics2D g = originalImg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("Lucida Bright", Font.ITALIC, fontSize_p));
//        g.rotate(-Math.PI/4, originalImg.getWidth()/2, originalImg.getHeight()/2);
        TextLayout tl = new TextLayout(waterMarkText_p, g.getFont(), g.getFontRenderContext());
        Rectangle2D bounds = tl.getBounds();
        double x = (originalImg.getWidth()-bounds.getWidth())/2 - bounds.getX();
        double y = (originalImg.getHeight());
        switch (side_p) {
	        case 'L':
	        	if (bounds.getWidth() * 1.2 > originalImg.getHeight()) return originalImg; // too short
	            g.rotate(Math.PI/2, originalImg.getWidth()/2, originalImg.getHeight()/2);
	            x = (originalImg.getWidth() - bounds.getWidth())/2;
	            y = -(originalImg.getWidth() -originalImg.getHeight())/2 + bounds.getHeight();
	        	break;
	        case 'R':
	        	if (bounds.getWidth() * 1.2 > originalImg.getHeight()) return originalImg; // too short
	            g.rotate(-Math.PI/2, originalImg.getWidth()/2, originalImg.getHeight()/2);
	            x = (originalImg.getWidth() - bounds.getWidth())/2;
	            y = -(originalImg.getWidth() -originalImg.getHeight())/2 + bounds.getHeight();// - bounds.getHeight();
	        	break;
	        case 'S':
	        	if (originalImg.getWidth() < bounds.getWidth()*2.5) return originalImg;
	            x = bounds.getX();
	            y = -bounds.getY() + bounds.getHeight()/4;
	        	break;
	        case 'T':
	        	if (originalImg.getWidth() >= bounds.getWidth()*2.5 &&
	    	        originalImg.getWidth() < bounds.getWidth()*3.5) return originalImg;
	            x = (originalImg.getWidth()-bounds.getWidth())/2 - bounds.getX();
	            y = -bounds.getY();
	        	break;
	        case 'U':
	        	if (originalImg.getWidth() < bounds.getWidth()*2.5) return originalImg;
	            x = (originalImg.getWidth()-bounds.getWidth())- bounds.getX();
	            y = -bounds.getY() + bounds.getHeight()/4;
	        	break;
	        case 'A':
	        	if (originalImg.getWidth() < bounds.getWidth()*2.5) return originalImg;
	            x = bounds.getX();
	            y = (originalImg.getHeight()) - bounds.getHeight()/4;
	        	break;
	        case 'B':
	        	if (originalImg.getWidth() >= bounds.getWidth()*2.5 &&
	    	        originalImg.getWidth() < bounds.getWidth()*3.5) return originalImg;
	            x = (originalImg.getWidth()-bounds.getWidth())/2 - bounds.getX();
	            y = (originalImg.getHeight()) -  bounds.getHeight()/4;;
	        	break;
	        case 'C':
	        	if (originalImg.getWidth() < bounds.getWidth()*2.5) return originalImg;
	            x = (originalImg.getWidth()-bounds.getWidth())- bounds.getX();
	            y = (originalImg.getHeight()) - bounds.getHeight()/4;
	        	break;
        }
        
        Shape outline = tl.getOutline(AffineTransform.getTranslateInstance(x+2, y+1));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.setPaint(Color.WHITE);
        g.draw(outline);
        g.setPaint(new GradientPaint(0, 0, Color.GRAY, 30, 20, Color.LIGHT_GRAY, true));
        tl.draw(g, (float)x, (float)y);
        g.dispose();
        return originalImg;
    }
}

