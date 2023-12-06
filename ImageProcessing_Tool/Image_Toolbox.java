import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;

public class Image_Toolbox implements PlugIn {
	static boolean showArgs = true;

	public void run(String arg) {
            
		String msg = "";
		if (arg.equals("plugins"))
			msg = "Plugins>Image_Toolbox (Plugins)";
       	ImageProcessing_Tool it=new ImageProcessing_Tool(); 

	}
	

}
