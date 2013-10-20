import java.util.*;

public class VoronoiADD {
	
	final static int TOTALPLAYERS = 2;
	final static int MAXSTONES = 4;
	final static int BOARDSIZE = 1000;
	
	static int[][][] stones = new int[TOTALPLAYERS][MAXSTONES][2];
	static int[] playersNumMoves = new int[TOTALPLAYERS];
	
	public static void main(String args[]){
		stones[0][0][0] = 500; 
		stones[0][0][1] = 500;
		stones[0][0][0] = 750; 
		stones[0][0][1] = 750;
		stones[0][0][0] = 300; 
		stones[0][0][1] = 300;
		playersNumMoves[0] = 3;

		stones[0][1][0] = 510; 
		stones[0][1][1] = 510;
		stones[0][1][0] = 750; 
		stones[0][1][1] = 710;
		playersNumMoves[1] = 2;
		
		long ini = System.currentTimeMillis();
		int[] bestMove=  greedyRandom(1);
		System.out.println("best greedy move is: y = " + bestMove[0] + " and x = " + bestMove[1]);
		System.out.println("total time = " + ( System.currentTimeMillis() - ini));
	}
	
	public static double euclidianDistance(int[] a, int[] b){
		return Math.sqrt( (a[0] - b[0]) * (a[0] - b[0]) +  (a[1] - b[1]) * (a[1] - b[1]) );
	}

	
	/**
	 * Uses a greedy algorithm to select where to place the stone for the current player.
	 * Uses a random approach to find the point such that the total area is maximized
	 * Assumes that the point (y,x) is represented as int[0] = y and int[1] = x;
	 * Is quite slow because the board is too big
	 * 
	 * @param currentPlayer , probably our player ID, but the algorithm can also be run to find the best 
	 * position to a general player
	 * @return best position to place stone
	 */
	public static int[] greedyRandom(int currentPlayer){
		int[] ret = new int[2];
		int depth = 7;
		int random = 100;
		
		int[] begin = new int[]{0, 0};
		int[] end = new int[]{BOARDSIZE, BOARDSIZE};
		
		boolean[][] mark = new boolean[BOARDSIZE][BOARDSIZE];
		int bestArea = 0; int[] bestPoint = new int[2];
		
		while(depth-- > 0){	
			
			for(int i= 0; i < random ; i++){
				int y = begin[1] + (int) (Math.random() * (end[1] - begin[1]));
				int x = begin[0] + (int) (Math.random() * (end[0] - begin[0]));
				//System.out.println("("+depth+","+i+") best point : y = " +  bestPoint[0] + " and x = " + bestPoint[1] + " with area = " + bestArea);
				int[] placeStone  = new int[]{y,x};
				Queue<int[]> queue = new LinkedList<int[]>();
				// run BFS to find the new points pulled by this stone
				queue.add(placeStone);
				int area = 0;
				for(int a= 0; a < BOARDSIZE; a++){
					for(int b= 0; b < BOARDSIZE; b++){
						mark[a][b] = false;
					}
				}
				int iterations= 0;
				while(!queue.isEmpty()){
					iterations++;
					int[] point = queue.poll();
					double biggestPull = 0; int pointColor = -1;
					
					for( int player = 0; player < TOTALPLAYERS; player++){
						double totalPull = 0;
						for(int stone = 0; stone < playersNumMoves[player] ; stone++ ){
							double dist = euclidianDistance(point, stones[player][stone]);
							totalPull += 1.0 / (dist * dist);
						}
						if(player == currentPlayer){
							double dist = euclidianDistance(point, placeStone);
							totalPull += 1.0 / (dist * dist);
						}
						if(totalPull > biggestPull ){
							biggestPull = totalPull;
							pointColor = player;
						}
					}
					
					if( pointColor == currentPlayer){
						area++;
						for(int dy= -1; dy <= 1; dy++ ){
							for( int dx = -1; dx <= 1; dx++){
	
								if( (dy != 1 || dx != 1) && 
										(point[0]+dy) >= 0 && (point[0]+dy) < BOARDSIZE && 
											(point[1]+dx) >= 0 &&  (point[1]+dx) < BOARDSIZE &&
												!mark[point[0]+dy][point[1]+dx]){
									queue.add(new int[]{point[0]+dy, point[1]+dx});
									mark[point[0]+dy][point[1]+dx] = true;
								}
							}
						}
					}
					
				}
				//System.out.println("iterations num = " + iterations + " " + area);
				if( area > bestArea){
					bestArea = area;
					bestPoint[0] = y; bestPoint[1] = x;
				}
			}
			int dy = (end[0] - begin[0])/2;
			int dx = (end[1] - begin[1])/2;
			int middley = begin[0] + end[0]/2;
			int middlex = begin[1] + end[1]/2;
			if(bestPoint[0] < middley && bestPoint[1] < middlex ){ // top left corner
				end[0] -= dy; end[1] -= dx;
			} else if( bestPoint[0] < middley && bestPoint[1] >= middlex){ //top right corner 
				end[0] -= dy; begin[1] += dx;
			} else if(bestPoint[0] >= middley && bestPoint[1] < middlex){ //bottom left corner
				begin[0] += dy; end[1] -= dx;
			} else { // bottom right corner
				begin[0] += dy; begin[1] += dx;
			}
			//System.out.println("begin and end = (" + begin[0] + "," + begin[1] + ") _ (" + end[0] + "," + end[1] + ')' );
			ret = Arrays.copyOf(bestPoint, 2);
		}
		System.out.println("best area = " + bestArea);
		return ret;	
	}
	
}