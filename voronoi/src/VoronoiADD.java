import java.util.*;

public class VoronoiADD {
	
	final static int TOTALPLAYERS = 2;
	final static int MAXSTONES = 2;
	final static int BOARDSIZE = 100;
	
	static int[][][] stones = new int[TOTALPLAYERS][MAXSTONES][2];
	static int[] playersNumMoves = new int[TOTALPLAYERS];
	
	public static void main(String args[]){
		// place a stone of the first player in posisition (0,0);
		stones[0][0][0] = 30; 
		stones[0][0][1] = 30;
		playersNumMoves[0] = 1;
		
		int[] bestMove=  greedyRandom(1);
		System.err.println("best greedy move is: y = " + bestMove[0] + " and x = " + bestMove[1]);
	}
	
	public static double euclidianDistance(int[] a, int[] b){
		return Math.sqrt( (a[0] - b[0]) * (a[0] - b[0]) +  (a[1] - b[1]) * (a[1] - b[1]) );
	}
	
	
	/**
	 * Uses a greedy algorithm to select where to place the stone for the current player. 
	 * Uses a random approach to find the point such that the total area is maximized
	 * Assumes that the point (y,x) is represented as int[0] = y and int[1] = x;
	 * Tested and seems to be too slow
	 * 
	 * @param currentPlayer , probably our player ID, but the algorithm can also be run to find the best 
	 * position to a general player
	 * @return best position to place stone
	 */
	public static int[] greedyRandom(int currentPlayer){
		int[] ret = new int[2];
		int depth = 3;
		int random = 256;
		
		int[] begin = new int[]{0, 0};
		int[] end = new int[]{BOARDSIZE, BOARDSIZE};
		
		boolean[][] mark = new boolean[BOARDSIZE][BOARDSIZE];
		
		while(depth-- > 0){
			
			int bestArea = 0; int[] bestPoint = new int[2];
			
			for(int i= 0; i < random ; i++){
				int y = begin[1] + (int) (Math.random() * (end[1] - begin[1]));
				int x = begin[0] + (int) (Math.random() * (end[0] - begin[0]));
				int[] placeStone  = new int[]{y,x};
				Queue<int[]> queue = new LinkedList<int[]>();
				// run BFS to find the new points pulled by this stone
				queue.add(placeStone);
				int area = 0;
				for(int a= begin[0]; a < end[0]; a++){
					for(int b= begin[1]; b < end[1]; b++){
						mark[a][b] = false;
					}
				}
				while(!queue.isEmpty()){
					int[] point = queue.poll();
					if(mark[point[0]][point[1]]) continue;
					mark[point[0]][point[1]] = true;
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
											(point[1]+dx) >= 0 &&  (point[1]+dx) < BOARDSIZE){
									queue.add(new int[]{point[0]+dy, point[1]+dx});
								}
							}
						}
					}
					
				}
				
				if( area > bestArea){
					bestArea = area;
					bestPoint[0] = y; bestPoint[1] = x;
				}
			}
			int dy = (end[0] - begin[0])/2;
			int dx = (end[1] - begin[1])/2;
			if(bestPoint[0] < end[0]/2 && bestPoint[1] < end[1]/2){ // top left corner
				end[0] -= dy; end[1] -= dx;
			} else if( bestPoint[0] < end[0]/2 && bestPoint[1] >= end[1]/2){ //top right corner 
				end[0] -= dy; begin[1] += dx;
			} else if(bestPoint[0] < end[0]/2 && bestPoint[1] < end[1]/2){ //bottom left corner
				begin[0] += dy; end[1] -= dx;
			} else { // bottom right corner
				begin[0] += dy; begin[1] += dx;
			}
			ret = Arrays.copyOf(bestPoint, 2);
			random /= 2;
		}
		
		return ret;	
	}
	
}