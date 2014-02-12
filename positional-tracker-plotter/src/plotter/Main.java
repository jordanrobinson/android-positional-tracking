package plotter;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class Main extends AbstractAnalysis {

	public static void main(String[] args) throws Exception {
		AnalysisLauncher.open(new Main());
	}

	@Override
	public void init() throws Exception {
		int size = 1;
		float x;
		float y;
		float z;
		float a;

		Coord3d[] points = new Coord3d[size];
		Color[] colors = new Color[size];

		Frame frame = new Frame();
		FileDialog fd = new FileDialog(frame, "Please Load a File"); //setting up the file dialog		
		String textLine = null;
		File pointsFile = null;

		fd.setVisible(true);
		String fileName = fd.getFile();

		if (fileName != null) {
			pointsFile = new File(fd.getDirectory(),fileName);
			if (!fileName.endsWith(".csv") || !pointsFile.exists() || !pointsFile.canRead()) { //checks if the input file doesn't exist, or is not readable or is not a valid input file
				System.err.println("file doesn't seem to be valid");
				return;
			}
		}
		else {
			System.err.println("selection cancelled");
			return;
		}

		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(pointsFile);
		} catch (FileNotFoundException e) {
			System.err.println("file not found");
			e.printStackTrace();
			return;
		}
		System.out.println("file selected: "+fd.getDirectory()+fileName+"\n"); //outputs file path and name for testing purposes
		while (fileScanner.hasNextLine()) {
			textLine = fileScanner.nextLine().trim();
			Scanner lineScanner = new Scanner (textLine);
			lineScanner.useDelimiter(",");

			if (textLine.matches("^//+.*|^$")) { //checks if the line starts with // OR if the line is empty (blank line)
				//if so do nothing and let the line be skipped
			}		
			else {
				for (int i = 0; i < size; i++) {
					x = lineScanner.nextFloat();
					y = lineScanner.nextFloat();
					z = lineScanner.nextFloat();
					points[i] = new Coord3d(x, y, z);
					a = 0.25f;
					colors[i] = new Color(x, y, z, a);
				}
			}
			lineScanner.close();
		}
		fileScanner.close();

		Scatter scatter = new Scatter(points, colors);
		Chart chart = AWTChartComponentFactory.chart(Quality.Advanced, "newt");
		chart.getScene().add(scatter);
		
	}





}
