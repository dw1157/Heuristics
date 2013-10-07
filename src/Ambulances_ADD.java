import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;


public class Ambulances_ADD {

  private static final int NUM_KMEANS_TRIALS = 10000;
  private static final int ANTCOL_ITERATIONS = 100;
  private static final int MAX_NUMBER_VICTIMS = 300;
  
  private static int[][] victims = new int[MAX_NUMBER_VICTIMS][3];
  private static int[] hospitals_start = new int[5];
  private static int[][] hospitals;
  private static int max_x = 100;
  private static int max_y = 100;
  private static int numberOfVictims = 0;
  
  
  public static void main(String[] args) throws FileNotFoundException {

    readFile();

    Random random = new Random();
    int[][] centroids = new int[hospitals_start.length][2];

    double best_score = Double.MAX_VALUE;
    for (int trials = 0; trials < NUM_KMEANS_TRIALS; trials++) {
      for (int i = 0; i < centroids.length; i++) {
        centroids[i][0] = random.nextInt(max_x + 1);
        centroids[i][1] = random.nextInt(max_y + 1);
      }

      int[] assignments = new int[victims.length];
      boolean changed = true;
      while (changed) {
        changed = false;
        for (int i = 0; i < assignments.length; i++) {
          double min_distance = Double.MAX_VALUE;
          int previous_assignment = assignments[i];
          for (int j = 0; j < centroids.length; j++) {
            int[] centroid = centroids[j];
            int[] victim = victims[i];
            double distance = distance(centroid, victim);
            if (distance < min_distance) {
              min_distance = distance;
              assignments[i] = j;
            }
          }
          if (assignments[i] != previous_assignment) {
            changed = true;
          }
        }

        for (int j = 0; j < centroids.length; j++) {
          centroids[j][0] = 0;
          centroids[j][1] = 0;
          int members = 0;
          for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] == j) {
              centroids[j][0] += victims[i][0];
              centroids[j][1] += victims[i][1];
              members++;
            }
          }
          if (members > 0) {
            centroids[j][0] /= members;
            centroids[j][1] /= members;
          }
        }
      }

      double score = 0;
      for (int j = 0; j < centroids.length; j++) {
        double total_distances = 0;
        int members = 0;
        for (int i = 0; i < assignments.length; i++) {
          if (assignments[i] == j) {
            total_distances += distance(centroids[j], victims[i]);
            members++;
          }
        }
        if (members > 0) {
          total_distances /= members;
        }
        score += total_distances;
      }
      //System.out.println(score);

      if (score < best_score) {
        best_score = score;
        hospitals = centroids.clone();
      }
    }

    System.out.println("Best score: " + best_score);
    
    runAnt();
    
    printMap();

  }
  
  // =======ANT COLONIZATION==========//
  
  static class Edge{
	  int x, y; // origin
	  int z, w; // destination
	  public boolean equals(Object o ){
		  if( ! ( o instanceof Edge )) return false;
		  Edge p = (Edge) o;
		  return ( x == p.x && y == p.y && z == p.z && w == p.w );
	  }
  }
  
  static int gridDistance(int[] a, int[] b){
	  return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]);
  }
  
  //return the index of the nearest hospital from position "int[] pos"
  static int findNearstHospital(int[] pos){
		int bestDist = Integer.MAX_VALUE, bestHospitalIndex = 0;
		for(int k = 0; k < hospitals_start.length; k++){
			int dist = gridDistance(pos, hospitals[k]);
			if( bestDist > dist){
				bestDist = dist;
				bestHospitalIndex = k;
			}
		}
		return bestHospitalIndex;
  }
  
  private static void runAnt(){
	
	int bestScore = 0;
	Random random = new Random();
	HashMap<Edge, Integer> graph = new HashMap<Edge,Integer>();
	int[] numPreviousSaves = new int[numberOfVictims]; 
	
	//find the nearest hospital for each victim
	int[][] nearestHospitals = new int[MAX_NUMBER_VICTIMS][2];
	for (int j = 0; j < numberOfVictims; j++) {
		int bestHospitalIndex = findNearstHospital(victims[j]);
		nearestHospitals[j] = hospitals[bestHospitalIndex];
	}
	
	for (int i = 0; i < ANTCOL_ITERATIONS; i++) {
		
		int[] currentPos = hospitals[random.nextInt(5)];
		boolean[] saved = new boolean[numberOfVictims];
		int totalTime = 0;
		int[] possibleNodes = new int[numberOfVictims * 20];
		int numPossibleNodes = 0;
		int firstToDie = Integer.MAX_VALUE;
		
		
		while(true){
			
			for (int j = 0; j < numberOfVictims; j++) {
				int[] nearstH = nearestHospitals[j];
				int dist = gridDistance(currentPos,victims[j]) + gridDistance(victims[j], nearstH);
				numPossibleNodes = 0;
				if( !saved[j] && ( totalTime +  dist + 2) >= victims[j][2] ){ // if ok
					possibleNodes[numPossibleNodes++] = j;
				}
			}
			
			if(numPossibleNodes == 0){
				break;
			}
			
			int nextNode = possibleNodes[random.nextInt(numPossibleNodes)]; //randomized choice
			saved[nextNode] = true;
			numPreviousSaves[nextNode]++; 
			totalTime += gridDistance(currentPos,victims[nextNode]) + 1;
			
			
			
		}
	}
  }
  
  
  //=======END ANT COLONIZATION==========//
  
  private static double distance(int[] point1, int[] point2) {

    return Math.sqrt((point1[0] - point2[0]) * (point1[0] - point2[0])
        + (point1[1] - point2[1]) * (point1[1] - point2[1]));

  }

  private static void readFile() throws FileNotFoundException {
    Scanner scan = new Scanner(new File("sample.txt"));

    scan.nextLine();

    String line;
    int numOfPersons = 0;
    while (scan.hasNext() && !(line = scan.nextLine()).isEmpty()) {
      String[] pers_coord = line.split(",");
      victims[numOfPersons][0] = Integer.parseInt(pers_coord[0]);
      victims[numOfPersons][1] = Integer.parseInt(pers_coord[1]);
      victims[numOfPersons][2] = Integer.parseInt(pers_coord[2]);
      // if (max_x < victims[i][0]) max_x = victims[i][0];
      // if (max_y < victims[i][1]) max_y = victims[i][1];
      numOfPersons++; 
    }

    while ((line = scan.nextLine()).isEmpty()) {

    }
    int i = 0;
    while (scan.hasNextLine()) {
      line = scan.nextLine();
      hospitals_start[i] = Integer.parseInt(line);
      i++;
    }
  }

  private static void printMap() {
    for (int i = 0; i < max_x; i++) {
      for (int j = 0; j < max_y; j++) {

        boolean found = false;
        for (int h = 0; h < hospitals.length; h++) {
          if (hospitals[h][0] == i && hospitals[h][1] == j) {
            System.out.print("H");
            found = true;
            break;
          }
        }
        if (!found) {
          for (int k = 0; k < victims.length; k++) {
            if (victims[k][0] == i && victims[k][1] == j) {
              System.out.print("!");
              found = true;
              break;
            }
          }
        }
        if (!found) {
          System.out.print("_");
        }
      }
      System.out.println();
    }
  }

}
