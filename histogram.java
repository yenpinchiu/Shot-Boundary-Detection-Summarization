//the histogram class , which is a histogram

public class histogram {
	
	//paremeter
	static int bin_range = 8;
	static int gray_bin_range = 8;
	static int h_bin_range = 3;
	static int sv_bin_range = 5;
	int[][][] rgb_bin = null;
	double[] gray_bin = null;
	double[][][] hsv_bin = null;	
	
	// build the histogram once the class build
	public histogram(int[][][] picure){	
		rgb_bin = new int[256/bin_range][256/bin_range][256/bin_range];
		gray_bin = new double[256/gray_bin_range];
		hsv_bin = new double [(360/h_bin_range)+1] [(100/sv_bin_range) +1][(101/sv_bin_range) +1];
		for(int i=0;i<picure.length;i++){
			for(int j=0;j<picure[i].length;j++){
				rgb_bin[picure[i][j][0]/bin_range][picure[i][j][1]/bin_range][picure[i][j][2]/bin_range] ++;
				gray_bin[(int)( (double)picure[i][j][0]* 0.30+  (double)picure[i][j][1]* 0.59 +  (double)picure[i][j][2] * 0.11)/ gray_bin_range] ++;
				double[] hsv = tool.rgb_hsv_converter(picure[i][j]);
				hsv_bin[(int)(hsv[0]/h_bin_range)][(int)(hsv[1]*100/sv_bin_range)][(int)(hsv[2]*100/sv_bin_range)] ++;
		}}
	}
	
	//caculate rgb differ
	static public int histogram_rgb_differ_calculate(histogram[] histograms){
		int differ = 0;
		for(int dd=1;dd<histograms.length;dd++){
			for(int i=0;i<256/bin_range;i++){
				for(int j=0;j<256/bin_range;j++){
					for(int k=0;k<256/bin_range;k++){	
						differ += Math.abs(histograms[dd-1].rgb_bin[i][j][k] - histograms[dd].rgb_bin[i][j][k] );
					}}}	
		}
		return differ;
	}
	//caculate gray differ
	static public int histogram_gray_differ_calculate(histogram[] histograms){
		int differ = 0;
		for(int dd=1;dd<histograms.length;dd++){
			for(int i=0;i<256/gray_bin_range;i++){				
					differ += Math.abs(histograms[dd-1].gray_bin[i] - histograms[dd].gray_bin[i] );
			}
		}	
		return differ;
	}
	//caculate hsv differ
	static public int histogram_h_differ_calculate(histogram[] histograms){
		int differ = 0;
		for(int dd=1;dd<histograms.length;dd++){
			for(int i=0;i<(360/h_bin_range)+1;i++){
				int i_total_1 =0 ;
				int i_total_2 =0 ;
				for(int j=0;j<(100/sv_bin_range)+1;j++){
					for(int k=0;k<(100/sv_bin_range)+1;k++){	
						i_total_1 += histograms[dd-1].hsv_bin[i][j][k];
						i_total_2 += histograms[dd].hsv_bin[i][j][k];
					}}
				differ += Math.abs(i_total_1 - i_total_2  );
			}
		}
		return differ;
	}
}
