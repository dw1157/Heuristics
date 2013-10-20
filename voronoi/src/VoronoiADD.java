import java.util.*;

public class VoronoiADD {
	
	final static int TOTALPLAYERS = 2;
	final static int MAXSTONES = 4;
	final static int BOARDSIZE = 1000;
	
	static int[][][] stones = new int[TOTALPLAYERS][MAXSTONES][2];
	static int[] playersNumMoves = new int[TOTALPLAYERS];
	
	public static void main(String args[]){
		stones[0][playersNumMoves[0]][0] = 500; 
		stones[0][playersNumMoves[0]][1] = 500;
		playersNumMoves[0]++;
		stones[0][playersNumMoves[0]][0] = 750; 
		stones[0][playersNumMoves[0]][1] = 750;
		playersNumMoves[0]++;
		stones[0][playersNumMoves[0]][0] = 200; 
		stones[0][playersNumMoves[0]][1] = 200;
		playersNumMoves[0]++;
		stones[1][playersNumMoves[1]][0] = 300; 
		stones[1][playersNumMoves[1]][1] = 300;
		playersNumMoves[1]++;
		stones[1][playersNumMoves[1]][0] = 520; 
		stones[1][playersNumMoves[1]][1] = 520;
		playersNumMoves[1]++;
		
		for(int a= 0; a < BOARDSIZE; a++){
			for(int b= 0; b < BOARDSIZE; b++){
				board[a][b] = -1; // remove all colors
			}
		}
		BFS(1, 0, 1);
		BFS(1, 1, 1);
		printBoard(1);
		long ini = System.currentTimeMillis();
		int[] bestMove=  greedyRandom(1);
		System.out.println("best greedy move is: y = " + bestMove[0] + " and x = " + bestMove[1]);
		System.out.println("total time = " + ( System.currentTimeMillis() - ini));
		
		for(int a= 0; a < BOARDSIZE; a++){
			for(int b= 0; b < BOARDSIZE; b++){
				board[a][b] = -1; // remove all colors
			}
		}
		stones[1][playersNumMoves[1]][0] = bestMove[0]; 
		stones[1][playersNumMoves[1]][1] = bestMove[1];
		playersNumMoves[1]++;
		BFS(1, 0, 1);
		BFS(1, 1, 1);
		printBoard(1);
	}
	
	public static double euclidianDistance(int[] a, int[] b){
		return Math.sqrt( (a[0] - b[0]) * (a[0] - b[0]) +  (a[1] - b[1]) * (a[1] - b[1]) );
	}
	
	/**
	 * Greedy algorithm to select where to place the stone for the current player.
	 * Uses a random approach to find the point such that the total area is maximized.
	 * Also uses the KD-tree approach to reduce the search space 
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
		int random = 50;
		
		int[] begin = new int[]{0, 0};
		int[] end = new int[]{BOARDSIZE, BOARDSIZE};
		
		
		int bestArea = 0; int[] bestPoint = new int[2];
		
		while(depth-- > 0){	
			
			for(int iteration= 0; iteration < random ; iteration++){
				int y = begin[1] + (int) (Math.random() * (end[1] - begin[1]));
				int x = begin[0] + (int) (Math.random() * (end[0] - begin[0]));
				//System.out.println("("+depth+","+iteration+") best point : y = " +  bestPoint[0] + " and x = " + bestPoint[1] + " with area = " + bestArea);
				
				for(int a= 0; a < BOARDSIZE; a++){
					for(int b= 0; b < BOARDSIZE; b++){
						board[a][b] = -1; // remove all colors
					}
				}
				int pos = playersNumMoves[currentPlayer];
				stones[currentPlayer][pos][0] = y;
				stones[currentPlayer][pos][1] = x;
				playersNumMoves[currentPlayer]++;
				int area = 0;
				for(int stone =0; stone < playersNumMoves[currentPlayer]; stone++ ){
					area += BFS(currentPlayer, stone, currentPlayer);
				}
				stones[currentPlayer][pos][0] = 0;
				stones[currentPlayer][pos][1] = 0;
				playersNumMoves[currentPlayer]--;
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
			System.err.println("begin and end = (" + begin[0] + "," + begin[1] + ") _ (" + end[0] + "," + end[1] + ')' );
			ret = Arrays.copyOf(bestPoint, 2);
		}
		System.err.println("best area = " + bestArea);
		return ret;	
	}
	
	static int[][] board = new int[BOARDSIZE][BOARDSIZE];
	
	public static void printBoard(int currentPlayer){
		for(int i = 0; i< BOARDSIZE; i++){
			for( int  j = 0; j < BOARDSIZE;j++){
				boolean isStone = false;
				for(int player = 0; player < TOTALPLAYERS; player++){
					for(int stone =0 ; stone < playersNumMoves[player]; stone++){
						if(stones[player][stone][0] == i && stones[player][stone][1] == j ){
							System.out.printf("%d", player);
							isStone = true;
						}
					}
				}
				if(isStone) continue;
				if( board[i][j] == currentPlayer ){
					System.out.printf("+");
				} else {
					System.out.printf("-");
				}
			}
			System.out.println("");
		}
	}
	
	/**
	 * BFS algorithm to find the area of the current player's color 
	 * @param placeStone the new stone to be added. If null only the stones on the 
	 * board will be considered 
	 * @return the total area
	 */
	public static int BFS(int currentPlayer, int stoneIndex, int newStoneColor){
		Queue<int[]> queue = new LinkedList<int[]>();
		// run BFS to find the new points pulled by this stone
		
		queue.add(stones[currentPlayer][stoneIndex]);
		int area = 0;
		
		while(!queue.isEmpty()){
			int[] point = queue.poll();
			if(board[point[0]][point[1]] != -1 ){
				continue;
			}
			double biggestPull = 0; int pointColor = -1;
			
			for( int player = 0; player < TOTALPLAYERS; player++){
				double totalPull = 0;
				for(int stone = 0; stone < playersNumMoves[player] ; stone++ ){
					double dist = euclidianDistance(point, stones[player][stone]);
					totalPull += 1.0 / (dist * dist);
				}
				if(totalPull > biggestPull ){
					biggestPull = totalPull;
					pointColor = player;
				}
			}
			board[point[0]][point[1]] = pointColor;
			if( pointColor == currentPlayer){
				area++;
				for(int dy= -1; dy <= 1; dy++ ){
					for( int dx = -1; dx <= 1; dx++){
						if( (dy != 1 || dx != 1) && 
								(point[0]+dy) >= 0 && (point[0]+dy) < BOARDSIZE && 
									(point[1]+dx) >= 0 &&  (point[1]+dx) < BOARDSIZE &&
										(board[point[0]+dy][point[1]+dx] == -1)){
							queue.add(new int[]{point[0]+dy, point[1]+dx});
						}
					}
				}
			}
			
		}
		return area;
	}
	
	
}