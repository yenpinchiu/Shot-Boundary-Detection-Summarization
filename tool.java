import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

// stactic tools , nothing important

public class tool {
	
	
	//open picture , return an array
	static public int[][][] open_picture(String picture_name,boolean rgb) {	
		BufferedImage image = null;
		try{
			image = ImageIO.read(new File(picture_name));
		} catch (IOException e){System.out.println("picture not found : " +picture_name );return null;}
		int[][][] image_array = new int[image.getWidth()][image.getHeight()][3];
		if(rgb==true){image_array = new int[image.getWidth()][image.getHeight()][3];}
		else {image_array = new int[image.getWidth()][image.getHeight()][1];}
		int pixel;
		for(int i=0;i<image.getWidth();i++){
			for(int j=0;j<image.getHeight();j++){
				pixel = image.getRGB(i,j);
				if(rgb==true){
					image_array[i][j][0] = (pixel >> 16) & 0xff;//red
					image_array[i][j][1] = (pixel >> 8) & 0xff;//green
					image_array[i][j][2] =  (pixel) & 0xff;//blue
				}else{image_array[i][j][0] =pixel; }
			}
	   }
		return image_array;
	}
	
	// transfer to gray picture , not use in the end
	static public double[][] transfer_to_gray_picture(int[][][] color_picture){
		if(color_picture==null)return null;
		double[][] gray_picture = new double[color_picture.length][color_picture[0].length];
		for(int i=0;i<color_picture.length;i++){
			for(int j=0;j<color_picture[0].length;j++){
				gray_picture[i][j] =  ((double)color_picture[i][j][0] * 0.30) + ((double)color_picture[i][j][1] * 0.59) + ((double)color_picture[i][j][2] * 0.11);
		}}
		return gray_picture;
	}
	
	// transfer rgb to hsv
	static public double[] rgb_hsv_converter(int[] rgb){
		double  r= (double) rgb[0]/256;
		double  g= (double) rgb[1]/256;
		double  b= (double) rgb[2]/256;
		double minrgb = Math.min(r,Math.min(g,b));
		double maxrgb = Math.max(r,Math.max(g,b));
		 if (minrgb==maxrgb) { 
			 double[] hsv = {0,0,minrgb};
			 return hsv; 
		}
		double d = (r==minrgb) ? g-b : ((b==minrgb) ? r-g : b-r);
		double h = (r==minrgb) ? 3 : ((b==minrgb) ? 1 : 5);
		double ch = 60*(h - d/(maxrgb - minrgb));
		double cs = (maxrgb - minrgb)/maxrgb;
		double cv = maxrgb;
		double[] hsv =  {ch,cs,cv};
	    return hsv;
	}
	
	// quere for pic frames
	static public void queue_add(int[][][][] pic_frame,int[][][] new_pic){
		for(int i=1;i<pic_frame.length;i++){
			pic_frame[i-1] = pic_frame[i];
		}
		pic_frame[pic_frame.length-1] = new_pic;
	}
	
	// split the picture
	static public int[][][][][] split(int[][][] picture,int x ,int y){
		int[][][][][] split_picture = new int[x][y][picture.length/x][picture[0].length/y][];
		for(int i=0;i<picture.length;i++){
			for(int j=0;j<picture[0].length;j++){
				if( (i/(picture.length/x)) < x && (j/(picture[0].length/y))<y ){
					split_picture[i/(picture.length/x)][j/(picture[0].length/y)][i-(i/(picture.length/x))*(picture.length/x)][j-(j/(picture[0].length/y))*(picture[0].length/y)] = picture[i][j];	
				}
		}}
		return split_picture;
	}
	//class for a frame
	static public class frame{
		List<Integer> frame_scores = new ArrayList<Integer>();
		String frame_name = "";
		boolean shot_change = false;
		boolean key_frame = false;
		int key_frame_length = -1;
		int pic_h;
		int pic_w;
		int pic_type = 5;
	}
	//calculate standard deviation
	static public double[] standard_deviation(List<frame> data){
		double[] m = new double[data.get(0).frame_scores.size()];
		for(frame fns:data){
			for(int i=0;i<fns.frame_scores.size();i++){
				m[i]+= fns.frame_scores.get(i);
		}}
		for(int i=0;i<m.length;i++){
			m[i] = m[i]/data.size();
		}
		double[] ad = new double[data.get(0).frame_scores.size()];
		for(int i=0;i<data.size();i++){
			for(int k=0;k<data.get(0).frame_scores.size();k++){
			ad[k] += (data.get(i).frame_scores.get(k)-m[k])*((double)data.get(i).frame_scores.get(k)-m[k]);
		}}
		for(int i=0;i<data.get(0).frame_scores.size();i++){
			ad[i] = Math.sqrt(ad[i]/data.size());
		}
		return ad;
	}
	//calculate median
	static public double[] median (List<frame> data){
		double[] m = new double[data.get(0).frame_scores.size()];
		for(frame fns:data){
			for(int i=0;i<fns.frame_scores.size();i++){
				m[i]+= fns.frame_scores.get(i);
		}}
		for(int i=0;i<m.length;i++){
			m[i] = m[i]/data.size();
		}
		return m;
	}
}
