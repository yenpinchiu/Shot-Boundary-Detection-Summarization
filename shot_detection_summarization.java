import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class shot_detection_summarization {
	
	public shot_detection_summarization(String path) {
			
		List<tool.frame> frames_names_scores = new ArrayList<tool.frame>();
		
		shot_detection(path,frames_names_scores);
		
		shot_summarization(path,frames_names_scores);
		
		for (tool.frame current_frame_name_score : frames_names_scores){
			if(current_frame_name_score.shot_change == true)System.out.println("shot change detected:"+current_frame_name_score.frame_name);
			if(current_frame_name_score.key_frame == true)System.out.println("key frame detected:"+current_frame_name_score.frame_name);
		}
	}
	
	public void shot_detection(String path,List<tool.frame> frames_names_scores){
		File dir = new File(path);
		File[] filelist= dir.listFiles();
		int[][][] current_pic = null;
		String current_pic_name = null;
		int[][][][] shot_detected_frame = new int[2][][][];
		
		for(int i=0;i<filelist.length;i++){
			current_pic = tool.open_picture(path+Integer.toString(i+1)+".jpg",true);
			current_pic_name = Integer.toString(i+1)+".jpg";
		    tool.queue_add(shot_detected_frame,current_pic);	    
		    
		    tool.frame current_frame_name_score = new tool.frame();
		    current_frame_name_score.pic_w = current_pic.length;
		    current_frame_name_score.pic_h = current_pic[0].length;
		    current_frame_name_score.pic_type = 5;//set to jpg
		    
		    current_frame_name_score.frame_name=current_pic_name;

		    int shot_change_detection_down = shot_change_scoring(shot_detected_frame,current_frame_name_score);
		    if (shot_change_detection_down == 0){

		    	frames_names_scores.add(current_frame_name_score);
		    }	

		    if(i%50==0){
		    	System.out.println("");
		    }else System.out.print("-");
		}System.out.println("");
		
		double[] ad = tool.standard_deviation(frames_names_scores);
		double[] mm = tool.median(frames_names_scores);
		for (tool.frame current_frame_name_score : frames_names_scores){
			if ( current_frame_name_score.frame_scores.get(0)>mm[0]+2*ad[0] && current_frame_name_score.frame_scores.get(1)>mm[1]+2*ad[1] && current_frame_name_score.frame_scores.get(2)>mm[2]+2*ad[2]   ){
				current_frame_name_score.shot_change = true;
			}
		}	

		for (int i=0;i<frames_names_scores.size();i++){
			if(frames_names_scores.get(i).shot_change == true){
				int k=0;
				int previous_k =-1;
				int count = 2;
				int max_differ = -1;
				while(count!=0){
					if(frames_names_scores.get(i+k).shot_change == true){
						int differ = 0;
						for (int j=0;j<frames_names_scores.get(i+k).frame_scores.size();j++){
							differ+=frames_names_scores.get(i+k).frame_scores.get(j);
						}
						if (max_differ<differ){
							max_differ = differ;
							if(previous_k!=-1){frames_names_scores.get(i+previous_k).shot_change = false;}	
						}else frames_names_scores.get(i+k).shot_change = false;
						previous_k = k;
						count = 2;
					}
					k++;
					count--;
		}}}
	}
	
	public int shot_change_scoring(int[][][][] shot_detected_frame,tool.frame current_frame_name_score){
		for(int i=0;i<shot_detected_frame.length;i++){
			if(shot_detected_frame[i]==null)return -1;	
		}

		histogram_algo(shot_detected_frame,current_frame_name_score);
		
		return 0;
	}
	
	public void histogram_algo(int[][][][] shot_detected_frame,tool.frame current_frame_name_score){
		int split_x = 4;
		int split_y = 4;
		int differ_gray = 0 ;
		int differ_color = 0;
		int differ_h = 0;
		
		histogram[][] histograms = new histogram[split_x*split_y][shot_detected_frame.length];
		
		for(int i=0;i<shot_detected_frame.length;i++){
			int[][][][][] split_shot_detected_frame = tool.split(shot_detected_frame[i], split_x, split_y );
			for(int sx=0;sx<split_x;sx++){
				for(int sy=0;sy<split_y;sy++){	
					histograms[sx*sy][i] = new histogram(split_shot_detected_frame[sx][sy]);
			}}	
		}

		for(int sx=0;sx<split_x;sx++){
			for(int sy=0;sy<split_y;sy++){	
				differ_gray += histogram.histogram_gray_differ_calculate(histograms[sx*sy]);
				differ_color += histogram.histogram_rgb_differ_calculate(histograms[sx*sy]);
				differ_h += histogram.histogram_h_differ_calculate(histograms[sx*sy]);
		}}

		current_frame_name_score.frame_scores.add(differ_color);
		current_frame_name_score.frame_scores.add(differ_gray);
		current_frame_name_score.frame_scores.add(differ_h);

		return;
	}
	
	public void shot_summarization(String path,List<tool.frame> frames_names_scores){

		key_frame_extraction(frames_names_scores);
		make_summarization_layout(path,frames_names_scores);
	}
	
	public void make_summarization_layout(String path,List<tool.frame> frames_names_scores){
		int max_length = -1;
		int min_length = 999999;
		int tmp_key_frame = -1;
		int previous_shot_change = -1;
		int i;
		for(i=0;i< frames_names_scores.size();i++){
			if(frames_names_scores.get(i).shot_change==true){
				frames_names_scores.get(tmp_key_frame).key_frame_length = i-previous_shot_change;
				if(i-previous_shot_change>max_length){max_length = i-previous_shot_change;}
				if(i-previous_shot_change<min_length){min_length = i-previous_shot_change;}
				previous_shot_change = i;
			}
			if(frames_names_scores.get(i).key_frame==true){
				tmp_key_frame = i;
		}}frames_names_scores.get(tmp_key_frame).key_frame_length = i-previous_shot_change;
		if(i-previous_shot_change>max_length){max_length = i-previous_shot_change;}
		if(i-previous_shot_change<min_length){min_length = i-previous_shot_change;}
		
		int interval = (max_length - min_length)/4;
		
		for(i=0;i< frames_names_scores.size();i++){
			if(frames_names_scores.get(i).key_frame==true){frames_names_scores.get(i).key_frame_length = (frames_names_scores.get(i).key_frame_length-min_length)/interval;}
		}
		///////////////////////////////////////////////////////dirty code warnning , fix in the future////////////////////////////////////////////////////////////////////////////////
		
		BufferedImage resizedImage = new BufferedImage(frames_names_scores.get(0).pic_w*3,frames_names_scores.get(0).pic_h*5, 5);
		Graphics2D g = resizedImage.createGraphics();
		int[][][] graph_occupy = new int[3][5][4];
		
		for(i=0;i< frames_names_scores.size();i++){
			if(frames_names_scores.get(i).key_frame==true){	
				BufferedImage image = null;
				try{
					image = ImageIO.read(new File(path+frames_names_scores.get(i).frame_name));
				} catch (IOException e){}
				
				if(frames_names_scores.get(i).key_frame_length>=3){
					boolean break_flag = false;
					for(int d=0;d<graph_occupy.length;d++){
						for(int k=0;k<graph_occupy[d].length;k++){
							if(graph_occupy[d][k][0]==0){
								g.drawImage(image, k*image.getWidth(), d*image.getHeight(), image.getWidth() ,image.getHeight(), null);
								for(int j=0;j<graph_occupy[d][k].length;j++){graph_occupy[d][k][j]=1;}
								break_flag = true;
								break;
							}				
						}
						if (break_flag ==true){break;}
					}
				}
				else{
					boolean break_flag = false;
					for(int d=0;d<graph_occupy.length;d++){
						for(int k=0;k<graph_occupy[d].length;k++){
							for(int j=0;j<graph_occupy[d][k].length;j++){
								if(graph_occupy[d][k][j]==0){
									g.drawImage(image, k*image.getWidth()+(j%2)*(image.getWidth()/2), d*image.getHeight()+(j/2)*(image.getHeight()/2), image.getWidth()/2 ,image.getHeight()/2, null);
									graph_occupy[d][k][j]=1;
									break_flag = true;
									break;
								}
							}
							if (break_flag ==true){break;}
						}
						if (break_flag ==true){break;}
					}
				}
			}
		}
		g.dispose();
		try{
			ImageIO.write(resizedImage, "jpg", new File("summary.jpg")); 
		} catch (IOException e){System.out.println("picture write fail " );}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	public void key_frame_extraction(List<tool.frame> frames_names_scores){
		int tmp_key_frame_index = 0;
		int min_differ = 9999999;
		for (int i=0;i<frames_names_scores.size();i++){
			if(frames_names_scores.get(i).shot_change==true){frames_names_scores.get(tmp_key_frame_index).key_frame=true;min_differ = 9999999;tmp_key_frame_index = 0;}
			int differ = 0;
			for (int j=0;j<frames_names_scores.get(i).frame_scores.size();j++){
				differ+=frames_names_scores.get(i).frame_scores.get(j);
			}
			if(differ<min_differ){min_differ=differ;tmp_key_frame_index=i;}
		}
		frames_names_scores.get(tmp_key_frame_index).key_frame=true;min_differ = 9999999;tmp_key_frame_index = 0;
	}
	
	public static void main(String[] args){

		shot_detection_summarization sds = new shot_detection_summarization(args[0]);

	}
}
