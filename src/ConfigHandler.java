import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class ConfigHandler{
	GameConfig game = new GameConfig();
	Config white;
	Config black;
	
	ConfigHandler(){
		white = new Config(false);
		black = new Config(game.randomizeConfig);
	}
	
	ConfigHandler(int file1, int file2){
		white = new Config(file1, false);
		black = new Config(file2, game.randomizeConfig);
	}
	
	ConfigHandler(int file){
		white = new Config(file, false);
		black = new Config(file, game.randomizeConfig);
	}
	
	
	public Config getConfig(boolean color){
		if(color){
			return white;
		}else{
			return black;
		}
	}
}

class GameConfig{
	boolean PvC = true;
	boolean playerColor = true;							//true = white
	boolean invertedPrint = true;
	
	int kjerner = 4;
	int rec = 3;
	
	boolean randomizeConfig = true;
	
	boolean tracePieceUpdate 		= false;			//Piece name from update() and control
	boolean tracePieceUpdateName 	= false;			//Piece name from update() 
	boolean tracePossibleMoves 		= false;
	boolean traceThreads 			= false;			//Traces each threads best move
	boolean traceFindBestMove 		= false;			//Traces each findBestMove call
	boolean tracePieceScore 		= false;
	boolean tracePieces 			= false;
	boolean traceNewChessboard 		= false;
	boolean traceNewChessboardPieces= false;
	boolean traceControl 			= false;			//Printing information before each board
	boolean traceControlRemove 		= false;			//Tracing change in method
	boolean traceControlAdd 		= false;			//Tracing change in method
	boolean tracePlayerMove 		= false;			//Printing information after player move
	boolean printComputer 			= false;
	boolean traceMate 				= false;
	boolean traceAllPlayableMoves 	= false; 			//Tracing all actions before the move
	
	
	GameConfig(){
		if(tracePieceUpdate || traceFindBestMove || tracePieces || traceNewChessboard){
			kjerner = 1;
		}

		Scanner scanner;
		try {
			scanner = new Scanner(new File("gameConfig.txt"));
			while(scanner.hasNext()){
				String[] input = scanner.nextLine().split(" ");
				
				switch(input[0]){
					case "PvC": PvC = Boolean.parseBoolean(input[2]);
					break;
					case "playerColor": playerColor = Boolean.parseBoolean(input[2]);
					break;
					case "invertedPrint": invertedPrint = Boolean.parseBoolean(input[2]);
					break;
					case "kjerner": kjerner = Integer.parseInt(input[2]);
					break;
					case "rec": rec = Integer.parseInt(input[2]);
					break;
					case "randomizeConfig": randomizeConfig = Boolean.parseBoolean(input[2]);
					break;
					case "tracePieceUpdate": tracePieceUpdate = Boolean.parseBoolean(input[2]);
					break;
					case "tracePieceUpdateName": tracePieceUpdateName = Boolean.parseBoolean(input[2]);
					break;
					case "tracePossibleMoves": tracePossibleMoves = Boolean.parseBoolean(input[2]);
					break;
					case "traceThreads": traceThreads = Boolean.parseBoolean(input[2]);
					break;
					case "traceFindBestMove": traceFindBestMove = Boolean.parseBoolean(input[2]);
					break;
					case "tracePieceScore": tracePieceScore = Boolean.parseBoolean(input[2]);
					break;
					case "tracePieces": tracePieces = Boolean.parseBoolean(input[2]);
					break;
					case "traceNewChessboard": traceNewChessboard = Boolean.parseBoolean(input[2]);
					break;
					case "traceNewChessboardPieces": traceNewChessboardPieces = Boolean.parseBoolean(input[2]);
					break;
					case "traceControl": traceControl = Boolean.parseBoolean(input[2]);
					break;
					case "traceControlRemove": traceControlRemove = Boolean.parseBoolean(input[2]);
					break;
					case "traceControlAdd": traceControlAdd = Boolean.parseBoolean(input[2]);
					break;
					case "tracePlayerMove": tracePlayerMove = Boolean.parseBoolean(input[2]);
					break;
					case "printComputer": printComputer = Boolean.parseBoolean(input[2]);
					break;
					case "traceMate": traceMate = Boolean.parseBoolean(input[2]);
					break;
					case "traceAllPlayableMoves": traceAllPlayableMoves = Boolean.parseBoolean(input[2]);
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}
	
}

class Config{
	
	double brikkeverdi = 0;
	double sentralverdi = 0;
	double langtfremmeverdi = 0;
	double dekkefelterverdi = 0;
	double angrepsverdi = 0;
	double forsvarsverdi = 0;
	
	double random = 1;
	String file = "config.txt";

	Config(boolean randomize){
		read();
		if(randomize){
			randomize();
		}
	}
	
	Config(int confignr, boolean randomize){
		file = "config/config" + confignr + ".txt";
		read();
		if(randomize){
			randomize();
		}
	}
	
	void randomize(){
		brikkeverdi = brikkeverdi+(Math.random()-0.5)*random;
		sentralverdi = sentralverdi+(Math.random()-0.5)*random;
		langtfremmeverdi = langtfremmeverdi+(Math.random()-0.5)*random;
		dekkefelterverdi = dekkefelterverdi+(Math.random()-0.5)*random;
		dekkefelterverdi = dekkefelterverdi+(Math.random()-0.5)*random;
		angrepsverdi = angrepsverdi+(Math.random()-0.5)*random;
		forsvarsverdi = forsvarsverdi+(Math.random()-0.5)*random;
	}
	
	public void save(){
		PrintWriter writer;
		try {
			writer = new PrintWriter(file, "UTF-8");
			writer.println(brikkeverdi + " brikkeverdi");
			writer.println(sentralverdi + " sentralverdi");
			writer.println(langtfremmeverdi + " langtfremmeverdi");
			writer.println(dekkefelterverdi + " dekkefelterverdi");
			writer.println(angrepsverdi + " angrepsverdi");
			writer.println(forsvarsverdi + " forsvarsverdi");
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	void read(){
		Scanner scanner;
		try {
			scanner = new Scanner(new File(file));
			brikkeverdi = Double.parseDouble(scanner.nextLine().split(" ")[0]);
			sentralverdi = Double.parseDouble(scanner.nextLine().split(" ")[0]);
			langtfremmeverdi = Double.parseDouble(scanner.nextLine().split(" ")[0]);
			dekkefelterverdi = Double.parseDouble(scanner.nextLine().split(" ")[0]);
			angrepsverdi = Double.parseDouble(scanner.nextLine().split(" ")[0]);
			forsvarsverdi = Double.parseDouble(scanner.nextLine().split(" ")[0]);
		} catch (FileNotFoundException e) {
	//		e.printStackTrace();
	//		System.exit(0);
		}
	}
}