import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class Ambulances_ADD {

  // SERVER COMMUNICATION STUFF

  static Socket sock = null;
  static PrintWriter out = null;
  static BufferedReader in = null;
  static int port = 5555;
  static String host = "127.0.0.1";
  static String endofmsg = "<EOM>";
  static String teamName = "ADD";

  static String sampleResult = ""
      + "hospitals 0 (37,35); 1 (2,48); 2 (94,61); 3 (48,12); 4 (60,68)\n"
      + "ambulance 0  269 (37,34,169); 126 (34,40,87);115 (31,39,110);188 (30,35,138);(37,35)\n"
      + "ambulance 0  111 (42,38,103); 175 (45,44,87); (48,12)\n"
      + "ambulance 0  178 (31,44,157); (37,35)\n"
      + "ambulance 1  241 (40,28,100)\n" + "ambulance 1  230 (46,31,97)\n"
      + "ambulance 1  49 (53,33,72)\n" + "ambulance 1  103 (54,38,113)\n"
      + "ambulance 1  (37,35)\n" + "ambulance 20  73 (44,4,99);\n"
      + "ambulance 20  102 (38,7,104);140 (30,6,99);89 (30,3,127); (48,12)\n"
      + "ambulance 20  11 (59,9,144)\n" + "ambulance 20  215 (66,10,138)\n"
      + "ambulance 20  134 (66,5,164);(48,12)\n";

  public static String readData(BufferedReader in) throws IOException {
    String chunk = null;
    StringBuilder data = new StringBuilder();

    while ((chunk = in.readLine()) != null) {

      if (!chunk.equalsIgnoreCase(endofmsg))
        data.append(chunk).append("\n");
      else
        break;
    }

    return data.toString();
  }

  public static void sendText(PrintWriter out, String text) {
    out.println(text);
  }

  public static void sendResult(PrintWriter out, String result) {
    sendText(out, result + endofmsg);
  }

  // END OF SERVER COMMUNICATION STUFF

  private static final int NUM_KMEANS_TRIALS = 10000;
  private static final int ANTCOL_ITERATIONS = 50000;
  private static final int MAX_NUMBER_VICTIMS = 300;
  private static final int MAX_NODES_FROM_CURRENT_POSITION = 5;
  private static final int TIME_ALLOWED = 110;

  private static int[][] victims = new int[MAX_NUMBER_VICTIMS][3];
  private static int[] hospitals_start = new int[5];
  private static int[][] hospitals;
  private static int[][] centroids;
  private static int max_x = 100;
  private static int max_y = 100;
  private static int numberOfVictims = 0;
  private static long startTime;

  public static void main(String[] args) throws IOException {

    startTime = System.currentTimeMillis();

    try {
      sock = new Socket(host, port);
      out = new PrintWriter(sock.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    } catch (UnknownHostException e) {
      System.err.printf("Unknown host: %s:%d\n", host, port);
      System.exit(1);
    } catch (IOException e) {
      System.err.printf("Can't get I/O streams for the connection to: %s:%d\n",
          host, port);
      System.exit(1);
    }

    sendText(out, teamName);
    String data = readData(in);
    readFile(data);

    // readFile();

    Random random = new Random();
    centroids = new int[hospitals_start.length][2];

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
            double distance = distance(i, j);
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
          float x = 0.0f;
          float y = 0.0f;
          float members = 0.0f;
          for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] == j) {
              x += victims[i][0] * (1.0f / victims[i][2]);
              y += victims[i][1] * (1.0f / victims[i][2]);
              members += (1.0 / victims[i][2]);
            }
          }
          if (members > 0) {
            x /= members;
            y /= members;
          }
          centroids[j][0] = Math.round(x);
          centroids[j][1] = Math.round(y);
        }
      }

      double score = 0;
      int[] num_assignments = new int[centroids.length];
      for (int j = 0; j < centroids.length; j++) {
        double total_distances = 0;
        int members = 0;
        for (int i = 0; i < assignments.length; i++) {
          if (assignments[i] == j) {
            total_distances += distance(i, j) / victims[i][2];
            members++;
          }
        }
        if (members > 0) {
          total_distances /= members;
          num_assignments[j] = members;
        }
        score += total_distances;
      }

      if (score < best_score) {
        best_score = score;
        // hospitals = centroids.clone();
        hospitals = new int[centroids.length][5];
        // form comprehensive description of hospitals
        for (int i = 0; i < hospitals.length; i++) {
          hospitals[i][0] = centroids[i][0];
          hospitals[i][1] = centroids[i][1];
          hospitals[i][2] = num_assignments[i];
          hospitals[i][3] = hospitals_start[i];
          hospitals[i][4] = i;
        }
        // sort them in order of num of ambulances
        for (int i = 0; i < hospitals.length; i++) {
          for (int j = i + 1; j < hospitals.length; j++) {
            if (hospitals[i][3] < hospitals[j][3]) {
              int[] aux = hospitals[i];
              hospitals[i] = hospitals[j];
              hospitals[j] = aux;
            }
          }
        }
        // shuffle coordinates s.t. the numAssignments order matches
        for (int i = 0; i < hospitals.length; i++) {
          for (int j = i + 1; j < hospitals.length; j++) {
            if (hospitals[i][2] < hospitals[j][2]) {
              int aux = hospitals[i][0];
              hospitals[i][0] = hospitals[j][0];
              hospitals[j][0] = aux;
              aux = hospitals[i][1];
              hospitals[i][1] = hospitals[j][1];
              hospitals[j][1] = aux;
              aux = hospitals[i][2];
              hospitals[i][2] = hospitals[j][2];
              hospitals[j][2] = aux;
            }
          }
        }
        // sort them back in order of index
        for (int i = 0; i < hospitals.length; i++) {
          for (int j = i + 1; j < hospitals.length; j++) {
            if (hospitals[i][4] < hospitals[j][4]) {
              int[] aux = hospitals[i];
              hospitals[i] = hospitals[j];
              hospitals[j] = aux;
            }
          }
        }

      }
    }

    // System.out.println("Best score: " + best_score);

    // printMap();

    // // PRUNING OUT WEEDS?
    //
    // int[][] new_victims = new int[numberOfVictims][3];
    // int newTotal = 0;
    // for (int i = 0; i < numberOfVictims; i++) {
    // if (victims[i][2] >= gridDistance(
    // hospitals[findNearstHospital(victims[i])], victims[i]) + 2) {
    // new_victims[i] = victims[i];
    // newTotal++;
    // } else {
    // System.out.println("This victims is dead meat: " + i);
    // }
    // }
    // victims = new int[newTotal][3];
    // for (int i= 0; i < victims.length; i++) {
    // victims[i] = new_victims[i];
    // }
    //
    // // END OF PRUNING

    ArrayList<ArrayList<Integer>> paths = runAnt();

    printOutput(paths);
    sendText(out, endofmsg);

    String reply = readData(in);

    System.out.println("Server Response\n" + reply);

    out.close();
    in.close();
    sock.close();

  }

  // =======ANT COLONIZATION==========//

  static class Edge {
    int x, y; // origin
    int z, w; // destination

    public boolean equals(Object o) {
      if (!(o instanceof Edge))
        return false;
      Edge p = (Edge) o;
      return (x == p.x && y == p.y && z == p.z && w == p.w);
    }

    public Edge(int[] a, int[] b) {
      x = a[0];
      y = a[1];
      z = b[0];
      w = b[1];
    }

  }

  static int gridDistance(int[] a, int[] b) {
    return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]);
  }

  // return the index of the nearest hospital from position "int[] pos"
  static int findNearstHospital(int[] pos) {
    int bestDist = Integer.MAX_VALUE, bestHospitalIndex = 0;
    for (int k = 0; k < hospitals_start.length; k++) {
      int dist = gridDistance(pos, hospitals[k]);
      if (bestDist > dist) {
        bestDist = dist;
        bestHospitalIndex = k;
      }
    }
    return bestHospitalIndex;
  }

  private static ArrayList<ArrayList<Integer>> runAnt() {

    Random random = new Random();
    HashMap<Edge, Integer> graph = new HashMap<Edge, Integer>();
    int[] numPreviousSaves = new int[numberOfVictims];
    int[][] possibleNodes = new int[MAX_NODES_FROM_CURRENT_POSITION + 10][2];

    ArrayList<ArrayList<Integer>> bestSolution = null; // For each ambulance a
                                                       // list of victims
    int bestScore = 0; // total number of victims saved

    // find the nearest hospital for each victim
    int[][] nearestHospitals = new int[MAX_NUMBER_VICTIMS][2];
    for (int j = 0; j < numberOfVictims; j++) {
      int bestHospitalIndex = findNearstHospital(victims[j]);
      nearestHospitals[j] = Arrays.copyOf(hospitals[bestHospitalIndex],
          hospitals.length);
    }

    boolean keepGoing = true;
    for (int iteration = 0; iteration < ANTCOL_ITERATIONS && keepGoing; iteration++) {

      boolean[] saved = new boolean[numberOfVictims];
      ArrayList<ArrayList<Integer>> listNodes = new ArrayList<ArrayList<Integer>>();
      int ambulanceCount = 0;
      int totalIterationScore = 0; // number of victims saved in this iteration

      for (int hospital = 0; hospital < hospitals_start.length; hospital++) {

        for (int ambulance = 0; ambulance < hospitals_start[hospital]; ambulance++) {
          boolean inHospital = true; // starts in hospital
          int totalTime = 0;
          int totalOfVictimsInAmbulance = 0;
          listNodes.add(new ArrayList<Integer>());

          int[] currentPos = Arrays.copyOf(hospitals[hospital], 2);

          int firstToDie = Integer.MAX_VALUE; // The time the first victim
                                              // inside the ambulance will die

          // find the path
          while (true) {
            int numPossibleNodes = 0;
            for (int j = 0; j < numberOfVictims
                && totalOfVictimsInAmbulance < 4; j++) {
              int[] nearstH = nearestHospitals[j];
              int dist = gridDistance(currentPos, victims[j])
                  + gridDistance(victims[j], nearstH);
              if (!saved[j]
                  && (totalTime + dist + 2) <= Math.min(firstToDie,
                      victims[j][2])) {
                // is a possible exploring node if it's relatively near current
                // position
                // using insertion sort
                possibleNodes[numPossibleNodes][0] = j;
                possibleNodes[numPossibleNodes][1] = dist;
                for (int k = numPossibleNodes - 1; k >= 0; k--) {
                  // int dist2 =
                  // gridDistance(currentPos,victims[possibleNodes[k][0]]) +
                  // gridDistance(victims[possibleNodes[k][0]],
                  // nearestHospitals[k]);
                  int dist2 = possibleNodes[k][1];
                  if (dist < dist2) {
                    possibleNodes[k + 1][0] = possibleNodes[k][0];
                    possibleNodes[k + 1][1] = possibleNodes[k][1];
                    possibleNodes[k][0] = j;
                    possibleNodes[k][1] = dist;
                  } else {
                    break;
                  }
                }
                if (numPossibleNodes < MAX_NODES_FROM_CURRENT_POSITION + 1)
                  numPossibleNodes++;
              }
            }

            if (numPossibleNodes == 0) {

              if (inHospital)
                break; // not possible to get any more victims
              else {
                inHospital = true;
                totalOfVictimsInAmbulance = 0;
                int nearestHospital = findNearstHospital(currentPos);
                totalTime += gridDistance(currentPos,
                    hospitals[nearestHospital]);
                totalTime += 1; // unloadVictims
                listNodes.get(ambulanceCount).add(-(nearestHospital + 1)); // hospitals
                                                                           // are
                                                                           // stored
                                                                           // with
                                                                           // negative
                                                                           // numbers
                                                                           // and
                                                                           // 1
                                                                           // based
                currentPos = Arrays.copyOf(hospitals[nearestHospital], 2);
                firstToDie = Integer.MAX_VALUE;
                continue;
              }
            }

            int nextNode = possibleNodes[random.nextInt(numPossibleNodes)][0]; // randomized
                                                                               // choice
            saved[nextNode] = true;
            firstToDie = Math.min(firstToDie, victims[nextNode][2]);
            numPreviousSaves[nextNode]++;
            int dist = gridDistance(currentPos, victims[nextNode]) + 1;
            totalTime += dist;
            Edge e = new Edge(currentPos, victims[nextNode]);
            if (!graph.containsKey(e)) {
              graph.put(e, 1);
            } else {
              graph.put(e, graph.get(e) + 1);
            }
            currentPos[0] = victims[nextNode][0];
            currentPos[1] = victims[nextNode][1];
            listNodes.get(ambulanceCount).add(nextNode);
            inHospital = false;
            totalOfVictimsInAmbulance++;
            totalIterationScore++;

          }

          ambulanceCount++;
        }
      }

      if (bestScore < totalIterationScore) {
        bestScore = totalIterationScore;
        bestSolution = listNodes;
      }
      
      long endTime = System.currentTimeMillis();
      if(endTime - startTime > TIME_ALLOWED * 1000) {
        keepGoing = false;
        System.err.println("BAILING OUT FOR LACK OF TIME.");
      }
    }
    System.err.println("Best Ant Score = " + bestScore);
    return bestSolution;
  }

  // =======END ANT COLONIZATION==========//

  private static void printOutput(ArrayList<ArrayList<Integer>> paths) {
    // PrintStream out = System.out;

    int cont = 0;
    out.print("hospitals");
    for (int h = 0; h < hospitals.length; h++) {
      out.print(" " + h + " (" + hospitals[h][0] + "," + hospitals[h][1] + ")");
      if (h < hospitals.length - 1)
        out.print(";");
    }
    out.println();
    for (ArrayList<Integer> ambulance : paths) {
      out.print("ambulance " + (cont++));
      for (int node : ambulance) {
        if (node >= 0) {
          out.print(" " + node + " (" + victims[node][0] + ","
              + victims[node][1] + "," + victims[node][2] + ");");
        } else {
          int hospital = (node * (-1)) - 1;
          out.print(" (" + hospitals[hospital][0] + ","
              + hospitals[hospital][1] + ");");
        }
      }
      out.println();
    }
  }

  private static double distance2(int victim, int hospital) {

    int[] point1 = victims[victim];
    int[] point2 = centroids[hospital];

    return Math.sqrt((point1[0] - point2[0]) * (point1[0] - point2[0])
        + (point1[1] - point2[1]) * (point1[1] - point2[1]));

  }

  private static double distance(int victim, int hospital) {

    return distance2(victim, hospital);

  }

  private static void readFile() throws FileNotFoundException {
    readFile(new Scanner(new File("sample.txt")));
  }

  private static void readFile(String data) {
    readFile(new Scanner(data));
  }

  private static void readFile(Scanner scan) {

    scan.nextLine();

    String line;
    while (scan.hasNext() && !(line = scan.nextLine()).isEmpty()) {
      String[] pers_coord = line.split(",");
      victims[numberOfVictims][0] = Integer.parseInt(pers_coord[0]);
      victims[numberOfVictims][1] = Integer.parseInt(pers_coord[1]);
      victims[numberOfVictims][2] = Integer.parseInt(pers_coord[2]);
      // if (max_x < victims[i][0]) max_x = victims[i][0];
      // if (max_y < victims[i][1]) max_y = victims[i][1];
      numberOfVictims++;
    }

    while ((line = scan.nextLine()).isEmpty()) {

    }
    int i = 0;
    while (scan.hasNextLine()) {
      line = scan.nextLine();
      if (line.isEmpty())
        break;
      hospitals_start[i] = Integer.parseInt(line);
      i++;
    }
  }

  private static void printMap() {
    for (int i = 0; i < max_x; i++) {
      for (int j = 0; j < max_y; j++) {

        boolean found = false;
        for (int h = 0; h < hospitals.length; h++) {
          if (hospitals[h][0] == j && hospitals[h][1] == i) {
            System.out.print("H" + h + "\t");
            found = true;
            break;
          }
        }
        if (!found) {
          for (int k = 0; k < victims.length; k++) {
            if (victims[k][0] == j && victims[k][1] == i) {
              System.out.print(k + "\t");
              found = true;
              break;
            }
          }
        }
        if (!found) {
          System.out.print("\t");
        }
      }
      System.out.println();
    }
  }

}
