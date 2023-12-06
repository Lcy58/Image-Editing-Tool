
import ij.*;
import ij.gui.*;
import ij.gui.Roi.*;
import ij.gui.HistogramWindow.*;
import ij.measure.*;
import ij.process.*;
import ij.util.*;
import ij.util.Tools;
import ij.text.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import ij.plugin.filter.*;
import ij.plugin.PlugIn;

import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.text.*;
import java.awt.event.MouseListener;


import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.event.*;
import javax.swing.colorchooser.*;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *     @version 1.0.0 date 28 Nov 2021
 *
 *     @author  Jie Shu
 *     This code is free.
 *     This tool was built in the hope that it will be useful for teaching. 
 *     The purpose of this tool is to edit color images. 
 *
 *
 */
public class ImageProcessing_Tool extends PlugInFrame implements ActionListener, ChangeListener{

    Panel panel1, panel2, panel3, panel4, panel5;
    static Frame instance;
    static double[] prob = new double[16384];
    static double totalb = 0;
    static Button MakeBinary, ImageTrans, Enhance, Resize;
    static JComboBox ColorTrans;
    static JCheckBox Qbox;
    static JTextField Sb;
    static boolean datakey;
    static int l,  h;
    static JSpinner asize;
    static JSpinner.NumberEditor numluman;
    static JSlider luminance,  Asize,  Cnuclei;
    static ImagePlus imp,  Rimage;
    static int[][] array = new int[3][1000];
    static int[] totalC = new int[1000];

    static int nucleisegmentation=1;
    /**
     *
     * functions included in the tool:
     *          make binary(grayscale and color image)
     *          image enhancement(log, histogram)
     *          color model transformation(RGB to YCbCr, YCbCr to RGB)
     *          image transformation(shift, mapping)
	 *			Resize(zoom in or out)
     *
     */
    public ImageProcessing_Tool() {
        super("Image Processing");
        if (IJ.versionLessThan("1.43t")) {
            return;
        }
        instance = this;
        addKeyListener(IJ.getInstance());
        setAlwaysOnTop(true);
		setLayout(new GridLayout(4, 1, 1, 1));

//**************Panel 1******************
        panel1 = new Panel();
        panel1.setLayout(new GridLayout(1, 3, 1, 1));

		Label textSB = new Label("Threshold");
        panel1.add(textSB);
        Sb = new JTextField("255");
		Sb.setEnabled(true);
        panel1.add(Sb);
		MakeBinary = new Button("MakeBinary");
        MakeBinary.addActionListener(this);
        MakeBinary.addKeyListener(IJ.getInstance());
        MakeBinary.setVisible(true);
        MakeBinary.setEnabled(true);
        panel1.add(MakeBinary);

//**************Panel 2******************
		panel2 = new Panel();
		panel2.setLayout(new GridLayout(1, 3, 1, 1));
		ImageTrans = new Button("Image Transformation");
        ImageTrans.addActionListener(this);
        ImageTrans.addKeyListener(IJ.getInstance());
        ImageTrans.setVisible(true);
        ImageTrans.setEnabled(true);
        panel2.add(ImageTrans);

        Enhance = new Button("Enhancement");
        Enhance.addActionListener(this);
        Enhance.addKeyListener(IJ.getInstance());
        Enhance.setVisible(true);
        Enhance.setEnabled(true);
        panel2.add(Enhance);
		
		Resize = new Button("Resize");
        Resize.addActionListener(this);
        Resize.addKeyListener(IJ.getInstance());
        Resize.setVisible(true);
        Resize.setEnabled(true);
        panel2.add(Resize);
//*********panel3*****************************
		
		panel3 = new Panel();

		//*********Drop box for pre-defined model list***********
        String[] list = {"ColorTrans", "RGBtoYCbCr", "RGBtoHSI"};
        ColorTrans = new JComboBox(list);
        ActionListener cbActionListener = new ActionListener() {//add actionlistner to listen for change
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = (String) ColorTrans.getSelectedItem();//get the selected item

                if (s.equals("ColorTrans")) {
                    IJ.showMessage("ImageProcessing_Tool", "Please select a model trans from the list");
                } else if(s.equals("RGBtoYCbCr")){
                    ColorTransform("RGBtoYCbCr");
                }

            }
        };
        ColorTrans.addActionListener(cbActionListener);
        panel3.add(ColorTrans);
		//********************End of Drop box***************
		
		//****** create the slider and attached with a listener
       luminance = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);
       luminance.addChangeListener(this);
	   SpinnerNumberModel modelcb = new SpinnerNumberModel(luminance.getValue(), 0, 255, 1);
	   JPanel jp = new JPanel();
       jp.add(new JLabel("0"));
       jp.add(luminance);
       jp.add(new JLabel("255"));
       panel3.add(jp);
		//********************End of slider***************	   
        add(panel1);
		add(panel2);
		add(panel3);
		
        pack();
        GUI.center(this);
        setVisible(true);
    }

	public void stateChanged(ChangeEvent ce) {
        int val;
        if (ce.getSource() == luminance) {
			Sb.setText(String.valueOf(luminance.getValue()));
            }
	}
	
    public void windowClosed(WindowEvent e) {
        super.windowClosed(e);
        instance = null;
    }

    public void actionPerformed(ActionEvent e) {
		//Get the opened image 
        ImageProcessing_Tool.imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.beep();
            IJ.showStatus("No image");
            return;
        }
        ImageProcessor ip = imp.getProcessor();
        String label = e.getActionCommand();
        if (label == null) {
			IJ.showMessage("a ","null");
            return;
        }
		runCommand(label, imp, ip);
    }
	void ColorTransform(String command){
		//Get the opened image 
		ImageProcessing_Tool.imp = WindowManager.getCurrentImage();
		if (imp == null) {
            IJ.beep();
            IJ.showStatus("No image");
            return;
        }
		ImageProcessor ip = imp.getProcessor();
		runCommand(command, imp, ip);
	}
	
	 void runCommand(String command, ImagePlus imp, ImageProcessor ip) {
		 //add your code here
		if (command.equals("MakeBinary")) {
			IJ.showStatus("Make Binary...");
			ImageProcessor ipsb = imp.getProcessor();//set a processor to process a gray scale image
			ColorProcessor cp = (ColorProcessor) imp.getProcessor();//set a color processor to process a RGB color image
			int R, G, B;
			int thres = Integer.valueOf(ImageProcessing_Tool.Sb.getText());//get value from JTextField
			int pix = ((0 & 0xff) << 16) + ((0 & 0xff) << 8) + (0 & 0xff);//a color value, R=0, G=0, B=0
			for (int x = 0; x < imp.getWidth(); x++) {
            for (int y = 0; y < imp.getHeight(); y++) {
				R = (cp.get(x, y) & 0xff0000) >> 16;
                G = (cp.get(x, y) & 0x00ff00) >> 8;
                B = (cp.get(x, y) & 0x0000ff);
				if(R >= thres && G >= thres && B >= thres){
					ipsb.set(x,y,pix);//Set the color value at the specified (x, y) position 
				}
			}
			}
			imp.updateAndDraw();//Update the changed image 
		 } 
		if (command.equals("RGBtoYCbCr")) {
			//IJ.showMessage("","RGB to YCbCr");
			ColorProcessor color = (ColorProcessor) imp.getProcessor();//set a color processor to process a RGB color image
			double red, green, blue;
			int thres = Integer.valueOf(ImageProcessing_Tool.Sb.getText());//get value from JTextField
			for (int x = 0; x < ip.getWidth(); x++) {
				for (int y = 0; y < ip.getHeight(); y++) {
					red = (int) (color.get(x, y) & 0xff0000) >> 16;//get R channel
					green = (int) (color.get(x, y) & 0x00ff00) >> 8;//get B channel
					blue = (int) (color.get(x, y) & 0x0000ff);//get B channel
					//RGB to YCbCr
					double Cb = (0.5 * blue - 0.169 * red - 0.331 * green + 128);
					double Cr = (0.5 * red - 0.419 * green - 0.081 * blue + 128);
					//Color Burn
					int Y = Math.round((int) (0.299 * red + 0.587 * green + 0.114 * blue))-thres;
					//YCbCr to RGB
					red = Y + 1.402*(Cr-128);
					green = Y - 0.344*(Cb-128)-0.714*(Cr-128);
					blue = Y + 1.772*(Cb-128);
					int pix = (((int) red & 0xff) << 16) + (((int) green & 0xff) << 8) + ((int) blue & 0xff);
					ip.set(x, y, pix);//Set the color value at the specified (x, y) position 
				}
			}
			imp.updateAndDraw();//Update the changed image 
		}
}
}


