import java.util.Scanner;
import java.util.LinkedList;
import java.lang.Exception;

class Chess{
	public static void main(String[] args){
		

		ConfigHandler ch = new ConfigHandler(1, 5);
		Handler h = new Handler(ch, new Chessboard(ch));
		waitForGameOver(h);
		
		//TODO Huske dårlige åpninger
		
		System.out.println("Welcome!");
		Scanner scanner = new Scanner(System.in);
		while(true){
			try{
				System.out.println("What do you want to do?");
				switch(scanner.nextLine()){
				case "0": play();
					break;
				case "1": try{playFromFEN(scanner.nextLine());}catch(Exception e){System.out.println("Feil i input");}
					break;
				case "2": try{playFromPGN(scanner.nextLine());}catch(Exception e){System.out.println("Feil i input");}
					break;
				case "3": System.out.println("How many files and how many games do you want to improve?");String[] inp = scanner.nextLine().split(" "); improve(Integer.parseInt(inp[0]), Integer.parseInt(inp[1]));
					break;
				case "4": System.out.println("How many configs do you want to check?"); findBestConfig(Integer.parseInt(scanner.nextLine()));
					break;
				case "5": System.out.println("Bye");
					return;
				default: System.out.println("0: normal game");System.out.println("1: play from FEN");System.out.println("2: play from PGN");System.out.println("3: improve configs");System.out.println("4: find best config");System.out.println("5: quit");
				}
				
			}catch(Exception e){
				System.out.println("Forstod ikke, prov igjen");
			}
		}
	}
	
	//TODO skriv ut PGN til fil
	
	static void playFromFEN(String s) throws Exception{
		ConfigHandler ch = new ConfigHandler(0);
		Handler h = new Handler(ch, new Chessboard(s, ch));
		waitForGameOver(h);
	}
	
	static void findBestConfig(int ant){
		int[] wins = new int[ant];
		for(int n = 0; n < ant; n++){
			for(int m = n+1; m < ant; m++){
				ConfigHandler ch = new ConfigHandler(n, m);
				ch.game.PvC = false;
				ch.black.read();
				Handler h = new Handler(ch, new Chessboard(ch));

				while(!h.gameOver){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				switch(h.outcome){
				case 0: break;
				case 1: wins[n]++;break;
				case 2: wins[m]++;break;
				case -1: System.out.println("failed");break;
				}
			}
		}
		int best = 0;
		for(int n = 0; n < ant; n++){
			if(wins[n] > wins[best]){
				best = n;
			}
		}

		ConfigHandler ch = new ConfigHandler(best);
		ch.black.file = "config.txt";
		ch.black.save();
	}
	
	
	static void play(){
		ConfigHandler ch = new ConfigHandler();
		Handler h = new Handler(ch, new Chessboard(ch));
		waitForGameOver(h);
	}
	
	//forbedre seg selv
	static void improve(int to, int times){
		for(int n = 0; n < times; n++){
			for(int m = 0; m < to; m++){
				ConfigHandler ch = new ConfigHandler(m);
				ch.game.PvC = false;
				Handler h = new Handler(ch, new Chessboard(ch));
				waitForGameOver(h);
			}
		}
	}
	
	
	
	static void playFromPGN(String PGN){
		ConfigHandler ch = new ConfigHandler();
		Chessboard cb = new Chessboard(ch);
		
		String[] inp = PGN.split(" ");
		boolean white = true;
		int moves = 0;
		for(String s : inp){
			if(!s.contains(".")){
				try {
					if(white){
						moves++;
						System.out.print(moves + ". ");
					}
					System.out.print(cb.movePlayer(s, white) + " ");
					white = !white;
				} catch (FeilInputException e) {
					System.out.println(s);
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		Handler h = new Handler(ch, cb, moves, white);
		waitForGameOver(h);
	}
	
	static void waitForGameOver(Handler h){
		while(!h.gameOver){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}



class Handler extends Thread{
	ConfigHandler config;
	LinkedList<Chessboard> cbHistory = new LinkedList<Chessboard>();
	LinkedList<LinkedList<Piece>> possibleRepeat = new LinkedList<LinkedList<Piece>>(); 
	Chessboard chessboard;
	LinkedList<Action> allPlayableMoves;
	Thread[] threads;
	int done = 0;
	Action best = null;
	boolean color = true;
	Scanner in = new Scanner(System.in);
	int moves = 0;
	boolean gameOver = false;
	int outcome = -1; //remis = 0; white = 1; black = 2;
	
	Handler(ConfigHandler ch, Chessboard chessboard){
		config = ch;
		threads = new Thread[config.game.kjerner];
		this.chessboard = chessboard;
		cbHistory.add(new Chessboard(chessboard));
		nextMove();
	}
	
	Handler(ConfigHandler ch, Chessboard cb, int moves, boolean color){
		config = ch;
		threads = new Thread[config.game.kjerner];
		this.chessboard = cb;
		this.moves = moves;
		this.color = color;
		cbHistory.add(new Chessboard(chessboard));
		nextMove();
	}
	
	private void nextMove(){
		//Trace
		if(config.game.tracePieces){
			for(Piece p : chessboard.piecesWhite){
				p.movesPrint();
			}
			for(Piece p : chessboard.piecesBlack){
				p.movesPrint();
			}
		}
		
		if(config.game.traceControl){
			chessboard.printControl();
		}
		
		
		//Print Board
		printBoard();
		
		
		//Move player
		if(config.game.PvC && color == config.game.playerColor){
			//Spiller
			boolean notMoved = true;
			while(notMoved){
				try{
					String input = in.nextLine();
					switch(input){
					case "undo": 	if(cbHistory.size() >= 3){
										cbHistory.removeLast();
										cbHistory.removeLast();
										chessboard = new Chessboard(cbHistory.getLast());
										printBoard();
									}else{
										System.out.println("Nothing to undo");
									}
									break;
					default:	if(chessboard.movePlayer(input, color) != null){
									notMoved = false;
								}
					}
					
				}catch(FeilInputException e){
					System.out.println(e.melding);
				}
			}
			cbHistory.add(new Chessboard(chessboard));
			color = !color;
		}
		
		if(chessboard.remis(possibleRepeat)){
			System.out.println();
			System.out.println("Remis");
			gameOver(0);
			return;
		}

		//Move computer
		playComputer();
		
		if(chessboard.remis(possibleRepeat)){
			System.out.println();
			System.out.println("Remis");
			gameOver(0);
			return;
		}
		
	}
	
	void printBoard(){
		if(config.game.PvC){
			if(config.game.invertedPrint){
				chessboard.printColor(!config.game.playerColor);
			}else{
				chessboard.printColor(config.game.playerColor);
			}
		}else{
			if(color && !config.game.printComputer){
				moves++;
				System.out.print(moves + ". ");
			}
			if(config.game.printComputer){
				chessboard.print();
			}
		}
	}
	
	//Styr finn trekket
	public void playComputer(){
		allPlayableMoves = chessboard.getAllPlayableMoves(color);
		
		//Trace
		if(config.game.traceAllPlayableMoves){
			for(Action a : allPlayableMoves){
				System.out.println(a.getString());
			}
		}

		for(int n = 0; n < config.game.kjerner; n++){
			threads[n] = new Threader(color, chessboard, this, config);
			threads[n].start();
		}
	}
	
	public synchronized Action getAction(){
		if(allPlayableMoves.isEmpty()){
			return null;
		}
		return allPlayableMoves.poll();
	}
	
	public synchronized void finished(Action a){
		done++;
		if(best == null || (a != null && ((a.score > best.score && color) || (a.score < best.score && !color)))){
			best = a;
		}/*else if((a.score == Double.MAX_VALUE && color) || (a.score == Double.MIN_VALUE && !color)){
			//TODO Finn raskeste matt
		}*/
		if(done >= config.game.kjerner){
			if(best == null){
				System.out.println();
				System.out.println("Sjakk matt");
				if(color){
					System.out.println("Black wins");
					config.black.save();
					gameOver(2);
				}else{
					System.out.println("White wins");
					config.white.save();
					gameOver(1);
				}
				
				
				//Trace
				if(config.game.traceMate){
					chessboard.printPieces();
					chessboard.printControl();
					Piece king = chessboard.getPiece('K', color);
					for(Piece p : king.canTake){
						Chessboard cb = new Chessboard(chessboard);
						System.out.println(cb.move(king.pos, p.pos));
						System.out.println("Move: " + (char)(p.pos.x+97) + (p.pos.y+1) + "------------------------------------------------");
						cb.printPieces();
					}
				}
				return;
			}
			//Save position for possible repeat
			LinkedList<Piece> temp = new LinkedList<Piece>();
			for(Piece p : chessboard.getAllPieces()){
				temp.add(p.clone());
			}
			String move = chessboard.move(best);
			System.out.print(move + " ");
			
			if(move.charAt(1) == 'x' || move.charAt(0) >= 97){
				possibleRepeat.clear();
			}
			
			//Lagre historien
			cbHistory.add(new Chessboard(chessboard));
			possibleRepeat.add(temp);

			best = null;
			color = !color;
			done = 0;
			nextMove();
		}
	}
	
	void gameOver(int n){
		outcome = n;
		gameOver = true;
	}
	
}

class Threader extends Thread{
	boolean color;
	int recursive;
	Thread t;
	Chessboard chessboard;
	Handler h;
	LinkedList<LinkedList<Piece>> possibleRepeat; 
	ConfigHandler config;
	
	Threader(boolean color, Chessboard chessboard, Handler h, ConfigHandler ch){
		this.color = color;
		this.recursive = ch.game.rec;
		this.chessboard = chessboard;
		this.h = h;
		possibleRepeat = (LinkedList<LinkedList<Piece>>) h.possibleRepeat.clone();
		this.config = ch;
	}
	
	public void run(){
		Action best = null;
		Action a;
		do{
			a= h.getAction();
			Chessboard cb = new Chessboard(chessboard);
			
			try{
				String move = cb.move(a);
				if(move == null || a == null){
//					a = h.getAction();
					continue;
				}

//				cb.analyze();
				
				if(config.game.traceFindBestMove){
					System.out.println("Analyzing move " + a.getString());
				}
				//oppdaterer 
				possibleRepeat.add(cb.getAllPieces());
				if(move.charAt(1) == 'x' || move.charAt(0) >= 97){
					cb.findBestMove(!color, recursive-1, a, new LinkedList<LinkedList<Piece>>());
				}else{
					cb.findBestMove(!color, recursive-1, a, possibleRepeat);
				}
				
				if(cb.remis(possibleRepeat)){
					a.score = 0;
				}
				
				if(config.game.traceThreads){
					System.out.println(this.getName() + ": " + chessboard.getPiece(a.from).symbol + a.from.getString()  + "-" + a.to.getString() + " score " + a.score);
				}
				if(best == null || (a.score > best.score && color) || (a.score < best.score && !color)){
					best = a;
				}
				possibleRepeat.removeLast();
			}catch(Exception e){
				System.out.println();
				cb.printPieces();
				e.printStackTrace();
				h.gameOver(-2);
				this.interrupt();
				return;
			//	System.exit(0);
			}
		}while(a != null);

		//Lever det beste trekket og avslutt tråden
		h.finished(best);
		this.interrupt();
	}
	
}


class Chessboard{
	short[][] white = new short[8][8];
	short[][] black = new short[8][8];
	
	LinkedList<Piece> piecesWhite = new LinkedList<Piece>();
	LinkedList<Piece> piecesBlack = new LinkedList<Piece>();
	String[] bokstav = {"a", "b", "c", "d", "e", "f", "g", "h"};
	LinkedList<Action> allPlayableMoves;
	
	ConfigHandler config;
	
	boolean wlr = true;
	boolean wrr = true;
	boolean blr = true;
	boolean brr = true;
	
	boolean turn = true;
	
	
	//Lag et normalt brett
	Chessboard(ConfigHandler ch){
		config = ch;
		try{
			createPiecesFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");
		}catch(Exception e){
			System.out.println("Contact the creator, he made a bug!");
			System.exit(0);
		}
		updateAllPieces();
	}
	
	Chessboard(String FEN, ConfigHandler ch) throws Exception{
		config = ch;
		createPiecesFromFEN(FEN);
		updateAllPieces();
	}
	
	void updateAllPieces(){
		LinkedList<Piece> allPieces = getAllPieces();
		
		for(Piece p : piecesWhite){
			p.update(this, allPieces, null);
			if(p instanceof Pawn){
				addPawnControl(this, p);
			}
		}for(Piece p : piecesBlack){
			p.update(this, allPieces, null);
			if(p instanceof Pawn){
				addPawnControl(this, p);
			}
		}
	}
	
	private void createPiecesFromFEN(String s) throws Exception{
		wlr = false;
		wrr = false;
		blr = false;
		brr = false;
		boolean setup = true;
		int x = 0;
		int y = 7;
		for(char c : s.toCharArray()){
			if(setup){
				if(c == '/'){
					y--;
					x = 0;
				}else if(c == 'r'){
					piecesBlack.add(new Rook(false, x, y, config));
					x++;
				}else if(c == 'n'){
					piecesBlack.add(new Knight(false, x, y, config));
					x++;
				}else if(c == 'b'){
					piecesBlack.add(new Bishop(false, x, y, config));
					x++;
				}else if(c == 'q'){
					piecesBlack.add(new Queen(false, x, y, config));
					x++;
				}else if(c == 'k'){
					piecesBlack.add(new King(false, x, y, config));
					x++;
				}else if(c == 'p'){
					piecesBlack.add(new Pawn(false, x, y, config));
					x++;
				}else if(c == 'R'){
					piecesWhite.add(new Rook(true, x, y, config));
					x++;
				}else if(c == 'N'){
					piecesWhite.add(new Knight(true, x, y, config));
					x++;
				}else if(c == 'B'){
					piecesWhite.add(new Bishop(true, x, y, config));
					x++;
				}else if(c == 'Q'){
					piecesWhite.add(new Queen(true, x, y, config));
					x++;
				}else if(c == 'K'){
					piecesWhite.add(new King(true, x, y, config));
					x++;
				}else if(c == 'P'){
					piecesWhite.add(new Pawn(true, x, y, config));
					x++;
				}else if(c-48 > 0 && c-48 <= 8){
					x += c-48;
				}else if(c == ' '){
					setup = false;
				}else{
					throw new Exception("Wrong input");
				}
			}else{
				if(c == 'b'){
					turn = false;
				}else if(c == 'w'){
					turn = true;
				}else if(c == 'K'){
					wrr = true;
				}else if(c == 'Q'){
					wlr = true;
				}else if(c == 'k'){
					brr = true;
				}else if(c == 'q'){
					blr = true;
				}
			}
		}
	}
	
	//Plasser brikker som input
	Chessboard(Chessboard cb){
		for(Piece p : cb.piecesWhite){
			Piece temp = null; 
			if(p instanceof Pawn){
				temp = new Pawn(p);
			}else if(p instanceof Bishop){
				temp = new Bishop(p);
			}else if(p instanceof Rook){
				temp = new Rook(p);
			}else if(p instanceof Knight){
				temp = new Knight(p);
			}else if(p instanceof King){
				temp = new King(p);
			}else if(p instanceof Queen){
				temp = new Queen(p);
			} 
			temp.canBeTakenBy = p.canBeTakenBy;
			temp.canTake = p.canTake;
			temp.possibleMoves = p.possibleMoves;
			piecesWhite.add(temp);
		}
		for(Piece p : cb.piecesBlack){
			Piece temp = null; 
			if(p instanceof Pawn){
				temp = new Pawn(p);
			}else if(p instanceof Bishop){
				temp = new Bishop(p);
			}else if(p instanceof Rook){
				temp = new Rook(p);
			}else if(p instanceof Knight){
				temp = new Knight(p);
			}else if(p instanceof King){
				temp = new King(p);
			}else if(p instanceof Queen){
				temp = new Queen(p);
			} 

			temp.canBeTakenBy = p.canBeTakenBy;
			temp.canTake = p.canTake;
			temp.possibleMoves = p.possibleMoves;
			piecesBlack.add(temp);
		}
		//Oppdatere pekrer
		for(Piece p : getAllPieces()){
			LinkedList<Piece> temp = (LinkedList<Piece>) p.canBeTakenBy.clone();
			p.canBeTakenBy = new LinkedList<Piece>();
			for(Piece peker : temp){
				p.addCanBeTakenBy(getPiece(peker.pos));
			}
			temp = (LinkedList<Piece>) p.canTake.clone();
			p.canTake = new LinkedList<Piece>();
			for(Piece peker : temp){
				Piece pi = getPiece(peker.pos);
				p.addCanTake(pi);
			}
		}
		
		//clone black and white
		for(int n = 0; n < white.length; n++){
			for(int m = 0; m < white[n].length; m++){
				white[n][m] = cb.white[n][m];
				black[n][m] = cb.black[n][m];
			}
		}
		
		//Trace
		config = cb.config;
		if(config.game.traceNewChessboard){
			System.out.println("New Chessboard");
			if(config.game.traceNewChessboardPieces){
				for(Piece p : getAllPieces()){
					p.movesPrint();
				}
			}
			if(config.game.PvC){
				printColor(config.game.playerColor);
			}else{
				this.print();
			}
		}
		wlr = cb.wlr;
		blr = cb.blr;
		wrr = cb.wrr;
		brr = cb.brr;
	}
	
	
	
	//Finn brikkeverdien
	public int getScore(){
		int sum = 0;
		for(Piece p : getAllPieces()){
			if(p.white){
				sum += p.value;
			}else{
				sum -= p.value;
			}
		}
		return sum;
	}
	
	//Finn stillingsverdien
	public double getScoreAdvanced(boolean analyze){
		
		//COLOR ER ANALYSEFARGE, IKKE TREKK
		
		
		double sum = 0;
		double lastSum = 0;
		for(Piece p : getAllPieces()){
			
			//TODO Fix piece exchange
			//Sjekke om den er truet av bytte
/*			if((white[p.pos.x][p.pos.y]-black[p.pos.x][p.pos.y] > 0 && !p.white && analyze) || (white[p.pos.x][p.pos.y]-black[p.pos.x][p.pos.y] < 0 && p.white && !analyze)){
				continue;
			}
			*/
			
/*			boolean truet = false;
			for(Piece att : p.canBeTakenBy){//Bytte brikker osv... 
				if(att.value < p.value || (att.value == p.value && white[p.pos.x][p.pos.y] == black[p.pos.x][p.pos.y])){
//						System.out.println(att.getName() + " truer " + p.getName());
					truet = true;
					break;
				}
			}
	*/		
			//Sjekke om truet av mindre verdig brikke
			/*for(Piece att : p.canBeTakenBy){
				if(p.value > att.value && p.white != att.white){
					truet = true;
					break;
				}
			}
			if(truet){
				continue;
			}*/
			
			if(p.white){
				sum += p.value*config.getConfig(analyze).brikkeverdi;
				sum += config.getConfig(analyze).sentralverdi/(Math.abs(p.pos.x-3.5)*2);
				sum += config.getConfig(analyze).langtfremmeverdi*p.pos.y;
				sum += p.dekkerFelter()*config.getConfig(analyze).dekkefelterverdi;
				sum += p.defending()*config.getConfig(analyze).forsvarsverdi;
				sum -= p.attacking()*config.getConfig(analyze).angrepsverdi;
				
			}else{
				sum -= p.value*config.getConfig(analyze).brikkeverdi;
				sum -= config.getConfig(analyze).sentralverdi/(Math.abs(p.pos.x-3.5)*2);
				sum -= config.getConfig(analyze).langtfremmeverdi*(7-p.pos.y);
				sum -= p.dekkerFelter()*config.getConfig(analyze).dekkefelterverdi;
				sum -= p.defending()*config.getConfig(analyze).forsvarsverdi;
				sum += p.attacking()*config.getConfig(analyze).angrepsverdi;
			}
			if(config.game.tracePieceScore){
				System.out.println("Piece " + p.getString() + " get the score " + (sum-lastSum));
			}
			lastSum = sum;
		}
	//	System.out.println();
	//	print();
		
		return sum;
	}
	
	
	public Action findBestMove(boolean color, int rec, Action action, LinkedList<LinkedList<Piece>> possibleRepeat) throws Exception{
		/*
		 * Motta Action a
		 * Oppdater a ved a legge til muligheter
		 * Let igjennom etter den beste
		 * 
		 */
		
		allPlayableMoves = getAllPlayableMoves(color);
//		System.out.println(allPlayableMoves.size());
		
		do{
			Action a = allPlayableMoves.poll();
			Chessboard c = new Chessboard(this);
			String move = c.move(a);
			if(move == null){
				continue;
			}
			
			possibleRepeat.add(c.getAllPieces());

			boolean empty = false;
			if(move.charAt(1) == 'x' || move.charAt(0) >= 97){
				empty = true;
			}
			
			if(rec <= 0){
				a.score = c.getScoreAdvanced(color); //Sender med samme farge for analysen, ikke trekket
			}else{
				if(config.game.traceFindBestMove){
					System.out.println("Ny rec " + color + ", " + (rec-1) + ", " + a.getString());
				}
				
				//gjentagelse sending
				if(empty){
					a = c.findBestMove(!color, rec-1, a, new LinkedList<LinkedList<Piece>>());
				}else{
					a = c.findBestMove(!color, rec-1, a, possibleRepeat);
				}
			}
			
			if(remis(possibleRepeat)){
				a.score = 0;
			}
			possibleRepeat.removeLast();
			
			
			action.actions.add(a);
			
			if(config.game.traceFindBestMove){
				System.out.println(a.getString() + " score " + a.score);
			}

			if(action.best == null || (a.score > action.best.score && color) || (a.score < action.best.score && !color)){
				action.best = a;
			}
		}while(!allPlayableMoves.isEmpty());
		
		if(action.best != null){
			action.score = action.best.score;
		}else{//Hvis kan ikke flytte, sjekk om patt og gi score
			if(color){
				Piece king = getPiece('K', true);
				if(black[king.pos.x][king.pos.y] > 0){
					action.score = -Double.MAX_VALUE;
				}else{
					action.score = 0;
				}
			}else{
				Piece king = getPiece('K', false);
				if(white[king.pos.x][king.pos.y] > 0){
					action.score = Double.MAX_VALUE;
				}else{
					action.score = 0;
				}
			}
		}
		return action;
	}
	
	
	//Flytt spiller sin brikke utifra diverse input
	public String movePlayer(String input, boolean white) throws FeilInputException{
		Point to = null;
		Piece p = null;
		try{
			String[] inp = input.split(" ");
			if(inp.length == 1){
				if(input.equals("0-0") && rokade(white, true)){
					if(white){
						return move(new Point(4, 0), new Point(6, 0));
					}else{
						return move(new Point(4, 7), new Point(6, 7));
					}
				}else if(input.equals("0-0-0") && rokade(white, false)){
					if(white){
						return move(new Point(4, 0), new Point(2, 0));
					}else{
						return move(new Point(4, 7), new Point(2, 7));
					}
				}
			}
			if(inp.length == 2){
				int x = inp[0].charAt(0)-97;
				int x2 = inp[1].charAt(0)-97;
				int y = inp[0].charAt(1)-48-1;
				int y2 = inp[1].charAt(1)-48-1;
				return move(new Point(x, y), new Point(x2, y2));
			}else{
				if(input.length() == 3){
					to = new Point(input.charAt(1)-97, input.charAt(2)-48-1);
					p = getPiece(input.charAt(0), white, to);
				}else if(input.length() == 2){
					to = new Point(input.charAt(0)-97, input.charAt(1)-48-1);
					p = getPiece((char)0, white, to);
				}else if(input.length() == 4){
					if(input.charAt(1) == 'x'){
						int from = input.charAt(0);
						to = new Point(input.charAt(2)-97, input.charAt(3)-48-1);
						if(from >= 97 && from < 97+8){
							p = getPawnByX(from-97, white, to);
						}else{
							p = getPiece((char)from, white, to);
						}
					}else{
						int c = input.charAt(1);
						if(c < 48 && c > 48+8){
							to = new Point(input.charAt(2)-97, input.charAt(3)-48-1);
							p = getPieceByY(input.charAt(0), white, to, c-48-1);
						}else{
							to = new Point(input.charAt(2)-97, input.charAt(3)-48-1);
							p = getPieceByX(input.charAt(0), white, to, c-97);
						}
					}
				}
			}
		}catch(StringIndexOutOfBoundsException e){
			throw new FeilInputException("Feil format");
		}catch(NumberFormatException e){
			throw new FeilInputException("Feil format");
		}
		if(p == null){
			throw new FeilInputException("Finner ikke brikken");
		}
		if(to == null){
			throw new FeilInputException("Skjonner ikke, :'(");
		}
		if(!p.canMove(to)){
			throw new FeilInputException("Ulovlig trekk");//TODO can white do this?
		}
		
		Piece top = getPiece(new Point(to.x, to.y));
		if(top != null && top.white == p.white){
			throw new FeilInputException("Det er din brikke du prover a ta...");
		}
		String s = move(p.pos, to);
		
		if(s != null){
			if(config.game.tracePieces || config.game.tracePlayerMove){
				printColor(true);
				for(Piece pu : getAllPieces()){
					pu.movesPrint();
				}
			}
		}
		return s;
	}
	
	
	LinkedList<Piece> getAllPieces(){
		LinkedList<Piece> pi = new LinkedList<Piece>();
		pi.addAll(piecesWhite);
		pi.addAll(piecesBlack);
		return pi;
	}
	
	String move(Action a){
		if(a == null){
			return null;
		}
		return move(a.from, a.to);
	}
	
	String move(Point from, Point to){
		if(from == null){
			return null;
		}
		
		Piece p = getPiece(from);
		Piece temp = getPiece(to);
		
		
		if(p == null){
			return null;
		}
		
		//Odelegg rokade
		if(wlr && (from.equals(new Point(0, 0)) || to.equals(new Point(0, 0)))){
			wlr = false;
		}
		if(wrr && (from.equals(new Point(7, 0)) || to.equals(new Point(7, 0)))){
			wrr = false;
		}
		if(from.equals(new Point(4, 0))){
			wrr = false;
			wlr = false;
		}
		if(blr && (from.equals(new Point(0, 7)) || to.equals(new Point(0, 7)))){
			blr = false;
		}
		if(brr && (from.equals(new Point(7, 7)) || to.equals(new Point(7, 7)))){
			brr = false;
		}
		if(from.equals(new Point(4, 7))){
			brr = false;
			blr = false;
		}
		
		
		short print = 0;
		
		//Sjekk om flere brikker kan flytte til to
		if(getPiece(p.symbol, p.white, to) == null){
			if(getPieceByX(p.symbol, p.white, to, p.pos.x) != null){
				print = 1;
			}else{
				print = 2;
			}
		}
		
		//Update pieces
		updatePieces(new Move(from, to, p, temp));
		
		//Hvis rokade
		if(p instanceof King && from.equals(new Point(4, 0)) && to.equals(new Point(2, 0))){
			updatePieces(new Move(new Point(0, 0), new Point(3, 0), getPiece(new Point(0, 0)), null));
			return "0-0-0";
		}else if(p instanceof King && from.equals(new Point(4, 0)) && to.equals(new Point(6, 0))){
			updatePieces(new Move(new Point(7, 0), new Point(5, 0), getPiece(new Point(7, 0)), null));
			return "0-0-0";
		}else if(p instanceof King && from.equals(new Point(4, 7)) && to.equals(new Point(2, 7))){
			updatePieces(new Move(new Point(0, 7), new Point(3, 7), getPiece(new Point(0, 7)), null));
			return "0-0";
		}else if(p instanceof King && from.equals(new Point(4, 7)) && to.equals(new Point(6, 7))){
			updatePieces(new Move(new Point(7, 7), new Point(5, 7), getPiece(new Point(7, 7)), null));
			return "0-0";
		}
		
		//Check if illegal move
		for(Piece att : getPiece('K', p.white).canBeTakenBy){
			if(att.white != p.white){
				return null;
			}
		}
		
		//Preform move
		if(temp != null){
			if(p instanceof Pawn){
				return (char)(from.x+97) + "x" + (char)(to.x+97) + "" + (to.y+1);
			}
			switch(print){
			case 0: return p.symbol + "x" + (char)(to.x+97) + "" + (to.y+1);
			case 1: return p.symbol + "" + (char)(from.x+97) + "x" + (char)(to.x + 97) + "" + (to.y+1);
			case 2: return p.symbol + "" + (char)(from.y+1) + "x" + (char)(to.x+97) + "" + (to.y+1);
			}
		}else{
			if(p instanceof Pawn){
				return (char)(to.x+97) + "" + (to.y+1);
			}
			switch(print){
			case 0: return p.symbol + "" + (char)(to.x+97) + "" + (to.y+1) + "";
			case 1: return p.symbol + "" + (char)(from.x+97) + "" + (char)(to.x+97) + "" + (to.y+1);
			case 2: return p.symbol + "" + (from.y+1) + "" + (char)(to.x+97) + "" + (to.y+1);
			}
		}
		return null;
	}
	
	void updatePieces(Move move){
		move.p.pos = move.to;

		if(move.take != null){
			if(move.take.white){
				piecesWhite.remove(move.take);
			}else{
				piecesBlack.remove(move.take);
			}
			
			//De temp kunne ta kan ikke lenger bli tatt av temp
			//Oppdater hvilke felter som er dekket
			if(!(move.take instanceof Pawn)){
				for(Piece pu : move.take.canTake){
					pu.removeCanBeTakenBy(move.take);
					if(pu == move.p){
						removeControl(move.from, move.take.white);
					}else{
						removeControl(pu.pos, move.take.white);
					}
				}
				for(Point po : move.take.possibleMoves){
					removeControl(po, move.take.white);
				}
			}else{
				for(Piece pu : move.take.canTake){
					pu.removeCanBeTakenBy(move.take);
				}
				removePawnControl(this, move.take);
			}
		}
		
		LinkedList<Piece> allPieces = getAllPieces();
		
		//Oppdater alle som tror de kan gå til to
		move.p.update(this, allPieces, move);
		for(Piece pu : getAllPieces()){
			if((pu.canMove(move.to) || pu.canMove(move.from) || pu instanceof Pawn || pu instanceof King) && pu != move.p){
				pu.update(this, allPieces, move);
			}
		}
		
	}
	
	
	//TODO Using too much time
	public boolean canMove(Point from, Point to){
		Chessboard cb = new Chessboard(this);
		Piece temp = cb.getPiece(from);
		temp.pos = to;
		LinkedList<Piece> allPieces = cb.getAllPieces();

		for(Piece pu : cb.getAllPieces()){
			if((pu.canMove(to) || pu.canMove(from) || pu instanceof Pawn || pu instanceof King) && pu != temp){
				//Resetter denne brikkens forhold til resten
				pu.possibleMoves = new LinkedList<Point>();
				pu.canTake = new LinkedList<Piece>();
				
				
				if(pu instanceof Pawn){
					if((pu.pos.y == 7 && pu.white) || (pu.pos.y == 0 && !pu.white)){
					}else{
						if(pu.pos.equals(to)){
							pu.addPathPawn(allPieces, true);				
						}else{
							pu.addPathPawn(allPieces, false);
						}
					}
				}else if(pu instanceof Knight){
					pu.addPathKnight(allPieces);
				}else if(pu instanceof Bishop){
					pu.addPathDiagonal(allPieces);
				}else if(pu instanceof Rook){
					pu.addPathStright(allPieces);
				}else if(pu instanceof Queen){
					pu.addPathStright(allPieces);
					pu.addPathDiagonal(allPieces);
				}else if(pu instanceof King){
					pu.addPathKing(allPieces);
				}
				//Legger til kontroll paa brettet
				if(!(pu instanceof Pawn)){
					for(Piece p : pu.canTake){
						cb.addControl(p.pos, pu.white);
					}
					for(Point p : pu.possibleMoves){
						cb.addControl(p, pu.white);
					}
				}
			}
		}
		
		if(temp.white){
			Piece king = getPiece('K', true);
			if(cb.black[king.pos.x][king.pos.y] > 0){
				return false;
			}
		}else{
			Piece king = getPiece('K', false);
			if(cb.white[king.pos.x][king.pos.y] > 0){
				return false;
			}
		}
		
		
		return true;
	}
	
	
	public Piece getPiece(Point pos){
		for(Piece p : piecesWhite){
			if(p.pos.equals(pos)){
				return p;
			}
		}
		for(Piece p : piecesBlack){
			if(p.pos.equals(pos)){
				return p;
			}
		}
		return null;
	}
	
	public Piece getPiece(char c, boolean white){
		Piece find = null;
		if(white){
			for(Piece p : piecesWhite){
				if(p.symbol == c){
					if(find != null){
						return null;
					}else{
						find = p;
					}
				}
			}
		}else{
			for(Piece p : piecesBlack){
				if(p.symbol == c){
					if(find != null){
						return null;
					}else{
						find = p;
					}
				}
			}
		}
		return find;
	}
	
	public Piece getPiece(char c, boolean white, Point to){
		Piece find = null;
		if(white){
			for(Piece p : piecesWhite){
				if(p.symbol == c && p.canMove(to)){
					if(find != null){
						return null;
					}else{
						find = p;
					}
				}
			}
		}else{
			for(Piece p : piecesBlack){
				if(p.symbol == c && p.canMove(to)){
					if(find != null){
						return null;
					}else{
						find = p;
					}
				}
			}
		}
		return find;
	}
	
	public Piece getPieceByY(char c, boolean white, Point to, int y){
		Piece find = null;
		if(white){
			for(Piece p : piecesWhite){
				if(p.symbol == c && p.canMove(to) && p.pos.y == y){
					if(find != null){
						return null;
					}else{
						find = p;
					}
				}
			}
		}else{
			for(Piece p : piecesBlack){
				if(p.symbol == c && p.canMove(to) && p.pos.y == y){
					if(find != null){
						return null;
					}else{
						find = p;
					}
				}
			}
		}
		return find;
	}

	public Piece getPieceByX(char c, boolean white, Point to, int x){
		Piece find = null;
		if(white){
			for(Piece p : piecesWhite){
				if(p.symbol == c && p.canMove(to) && p.pos.x == x){
					if(find != null){
						return null;
					}else{
						find = p;
					}
				}
			}
		}else{
			for(Piece p : piecesBlack){
				if(p.symbol == c && p.canMove(to) && p.pos.x == x){
					if(find != null){
						return null;
					}else{
						find = p;
					}
				}
			}
		}
		return find;
	}
	
	public Piece getPawnByX(int x, boolean white, Point to){
		Piece find = null;
		if(white){
			for(Piece p : piecesWhite){
				if(p instanceof Pawn && p.canMove(to) && p.pos.x == x){
					if(find != null){
						return null;
					}else{
						find = p;
					}
				}
			}
		}else{
			for(Piece p : piecesBlack){
				if(p instanceof Pawn && p.canMove(to) && p.pos.x == x){
					if(find != null){
						return null;
					}else{
						find = p;
					}
				}
			}
		}
		return find;
	}
	
	
	public LinkedList<Action> getAllPlayableMoves(boolean color){
		LinkedList<Action> pm = new LinkedList<Action>();
		
		//TODO ampasang
		
		//Sjekk rokade
		if(rokade(color, false)){
			if(color){
				pm.add(new Action(new Point(4, 0), new Point(2, 0)));
			}else{
				pm.add(new Action(new Point(4, 7), new Point(2, 7)));
			}
		}
		if(rokade(color, true)){
			if(color){
				pm.add(new Action(new Point(4, 0), new Point(6, 0)));
			}else{
				pm.add(new Action(new Point(4, 7), new Point(6, 7)));
			}
		}
		
		
		//Legg til resten av trekkene
		if(color){
			for(Piece p : piecesWhite){
				Point from = p.pos;
				for(Point to : p.possibleMoves){
					pm.add(new Action(from, to));
				}
				for(Piece ta : p.canTake){
					if(ta.white != p.white){
						pm.add(new Action(from, ta.pos));
					}
				}
			}
		}else{
			for(Piece p : piecesBlack){
				Point from = p.pos;
				for(Point to : p.possibleMoves){
					pm.add(new Action(from, to));
				}
				for(Piece ta : p.canTake){
					if(ta.white != p.white){
						pm.add(new Action(from, ta.pos));
					}
				}
			}
		}
		
		return pm;
	}
	
	boolean rokade(boolean color, boolean shortRokade){
		if(color){
			if(!shortRokade){
				if(wlr && black[2][0] == 0 && black[3][0] == 0 && black[4][0] == 0 && getPiece(new Point(1, 0)) == null && getPiece(new Point(2, 0)) == null && getPiece(new Point(3, 0)) == null){
					return true;
				}
			}else{
				if(wrr && black[6][0] == 0 && black[5][0] == 0 && black[4][0] == 0 && getPiece(new Point(5, 0)) == null && getPiece(new Point(6, 0)) == null){
					return true;
				}
			}
		}else{
			if(!shortRokade){
				if(blr && white[2][7] == 7 && white[3][7] == 7 && white[4][7] == 7 && getPiece(new Point(1, 7)) == null && getPiece(new Point(2, 7)) == null && getPiece(new Point(3, 7)) == null){
					return true;
					
				}
			}else{
				if(brr && white[6][7] == 7 && white[5][7] == 7 && white[4][7] == 7 && getPiece(new Point(5, 7)) == null && getPiece(new Point(6, 7)) == null){
					return true;
				}
			}
		}
		return false;
	}
	
	
	Action getAction(LinkedList<Action> ar, Point from, Point to){
		for(Action a : ar){
			if(a.from.x == from.x && a.from.y == from.y && a.to.y == to.y && a.to.x == to.x){
				return a;
			}
		}
		return null;
	}

	void addPawnControl(Chessboard cb, Piece p){
		if(p.white){
			if(p.pos.y < 7){
				if(p.pos.x+1 < 8){
					cb.addControl(p.pos.x+1, p.pos.y+1, p.white);
				}
				if(p.pos.x-1 >= 0){
					cb.addControl(p.pos.x-1, p.pos.y+1, p.white);
				}
			}
		}else{
			if(p.pos.y > 0){
				if(p.pos.x+1 < 8){
					cb.addControl(p.pos.x+1, p.pos.y-1, p.white);
				}
				if(p.pos.x-1 >= 0){
					cb.addControl(p.pos.x-1, p.pos.y-1, p.white);
				}
			}
		}
	}

	void removePawnControl(Chessboard cb, Piece p, Move move){
		if(move != null && p.pos.equals(move.to)){
			if(p.white){
				if(move.from.y < 7){
					if(move.from.x+1 < 8){
						cb.removeControl(move.from.x+1, move.from.y+1, p.white);
					}
					if(move.from.x-1 >= 0){
						cb.removeControl(move.from.x-1, move.from.y+1, p.white);
					}
				}
			}else{
				if(move.from.y > 0){
					if(move.from.x+1 < 8){
						cb.removeControl(move.from.x+1, move.from.y-1, p.white);
					}
					if(move.from.x-1 >= 0){
						cb.removeControl(move.from.x-1, move.from.y-1, p.white);
					}
				}
			}
			return;
		}
		if(p.white){
			if(p.pos.y < 7){
				if(p.pos.x+1 < 8){
					cb.removeControl(p.pos.x+1, p.pos.y+1, p.white);
				}
				if(p.pos.x-1 >= 0){
					cb.removeControl(p.pos.x-1, p.pos.y+1, p.white);
				}
			}
		}else{
			if(p.pos.y > 0){
				if(p.pos.x+1 < 8){
					cb.removeControl(p.pos.x+1, p.pos.y-1, p.white);
				}
				if(p.pos.x-1 >= 0){
					cb.removeControl(p.pos.x-1, p.pos.y-1, p.white);
				}
			}
		}
	}
	void removePawnControl(Chessboard cb, Piece p){
		if(p.white){
			if(p.pos.y < 7){
				if(p.pos.x+1 < 8){
					cb.removeControl(p.pos.x+1, p.pos.y+1, p.white);
				}
				if(p.pos.x-1 >= 0){
					cb.removeControl(p.pos.x-1, p.pos.y+1, p.white);
				}
			}
		}else{
			if(p.pos.y > 0){
				if(p.pos.x+1 < 8){
					cb.removeControl(p.pos.x+1, p.pos.y-1, p.white);
				}
				if(p.pos.x-1 >= 0){
					cb.removeControl(p.pos.x-1, p.pos.y-1, p.white);
				}
			}
		}
	}
	
	public void removeControl(Point p, boolean color){
		removeControl(p.x, p.y, color);
	}
	public void addControl(Point p, boolean color){
		addControl(p.x, p.y, color);
	}
	

	public void removeControl(int x, int y, boolean color){
/*		StackTraceElement[] tree = Thread.currentThread().getStackTrace();
		for(StackTraceElement e : tree){
			System.out.println(e.getMethodName());
		}
		if(tree[1].getMethodName().equals("finished")){
			System.out.println("lol");
		}*/
		if(color){
			if(config.game.traceControlRemove){
				System.out.println("Removing control for white on " + (char)(x+97) + (y+1));
			}
			white[x][y]--;
		}else{
			if(config.game.traceControlRemove){
				System.out.println("Removing control for black on " + (char)(x+97) + (y+1));
			}
			black[x][y]--;
		}
	}
	
	public void addControl(int x, int y, boolean color){
		if(color){
			if(config.game.traceControlAdd){
				System.out.println("Adding control for white on " + (char)(x+97) + (y+1));
			}
			white[x][y]++;
		}else{
			if(config.game.traceControlAdd){
				System.out.println("Adding control for black on " + (char)(x+97) + (y+1));
			}
			black[x][y]++;
		}
	}
	
	public boolean remis(LinkedList<LinkedList<Piece>> possibleRepeat){
		int size = possibleRepeat.size();
		
		if(size >= 50){
			return true;
		}
		
		//Hvis gjentagelse sett score 0
		for(int a = 0; a < size; a++){
			boolean like = false;
			for(int b = a+1; b < size; b++){
				if(remisHelp(possibleRepeat.get(a), possibleRepeat.get(b))){
					if(like){
						return true;
					}else{
						like = true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean remisHelp(LinkedList<Piece> a, LinkedList<Piece> b){
		for(int n = 0; n < a.size() && n < b.size(); n++){
			if(!a.get(n).equals(b.get(n))){
				return false;
			}
		}
		return true;
	}
	
	public void print(){
		Piece[][] board = new Piece[8][8];
		
		for(Piece p : piecesWhite){
			board[p.pos.x][p.pos.y] = p;
		}for(Piece p : piecesBlack){
			board[p.pos.x][p.pos.y] = p;
		}
	
		int n = 0;
		for(Piece[] pi : board){
			System.out.print(bokstav[n] + "\t");
			for(Piece p : pi){
				if(p != null){
					if(!p.white){
						System.out.print("b");
					}
					System.out.print(p.name + "\t");
				}else{
					System.out.print("-\t");
				}
			}
			System.out.println();
			System.out.println();
			n++;
		}
		System.out.print("\t");
		for(n = 0; n < board.length; n++){
			System.out.print((n+1) + "\t");
		}
		System.out.println();
	}
	
	public void printColor(boolean white){
		Piece[][] board = new Piece[8][8];
		
		for(Piece p : piecesWhite){
			if(p.pos.x == -1 || p.pos.y == -1){
				p.movesPrint();
			}
			board[p.pos.x][p.pos.y] = p;
		}for(Piece p : piecesBlack){
			board[p.pos.x][p.pos.y] = p;
		}
		
		System.out.println();
		System.out.println();
		if(!white){
			for(int n = 0; n < board.length; n++){
				System.out.print((n+1) + "\t");
				for(int m = board[n].length-1; m >= 0; m--){
					if(board[m][n] != null){
						if(!board[m][n].white){
							System.out.print("b");
						}
						System.out.print(board[m][n].name + "\t");
					}else{
						System.out.print("-\t");
					}
				}
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
			}
			System.out.print("\t");
			for(int n = board.length-1; n >= 0; n--){
				System.out.print(bokstav[n] + "\t");
			}
			System.out.println();
		}else{
			for(int n = board.length-1; n >= 0; n--){
				System.out.print((n+1) + "\t");
				for(int m = 0; m < board[n].length; m++){
					if(board[m][n] != null){
						if(!board[m][n].white){
							System.out.print("b");
						}
						System.out.print(board[m][n].name + "\t");
					}else{
						System.out.print("-\t");
					}
				}
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
			}
			System.out.print("\t");
			for(int n = 0; n < bokstav.length; n++){
				System.out.print(bokstav[n] + "\t");
			}
			System.out.println();
			
		}
	}
	
	public void printControl(){
		System.out.println();
		for(int n = 0; n < white.length; n++){
			for(int m = 0; m < white[n].length; m++){
				System.out.print(white[n][m] + "\t");
			}
			System.out.print("\t");
			for(int m = 0; m < white[n].length; m++){
				System.out.print(black[n][m] + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public void printPieces(){
		for(Piece p : getAllPieces()){
			p.movesPrint();
		}
	}
}

class Point{
	int x, y;
	
	Point(int x, int y){
		this.x = x;
		this.y = y;
	}

	public String getString() {
		return "" + ((char)(x+97)) + (y+1);
	}
	
	public boolean equals(Point p){
		if(p.x == x && p.y == y){
			return true;
		}
		return false;
	}
	
	public Point clone(){
		return new Point(x, y);
	}
}

class CantMoveException extends Exception{}
class FeilInputException extends Exception{
	String melding = "";
	FeilInputException(String e){
		melding = e;
	}
}
class FinnerIkkeBrikkeException extends Exception{
	String melding = "";
	FinnerIkkeBrikkeException(String e){
		melding = e;
	}
}

class Action{
	String[] bokstav = {"a", "b", "c", "d", "e", "f", "g", "h"};
	Point from, to;
	double score = 0;
	Action best;
	LinkedList<Action> actions = new LinkedList<Action>();
	
	Action(Point from, Point to){
		this.from = from;
		this.to = to;
	}
	
	
	public void print(){
		System.out.println(getString());
	}
	
	public String getString(){
		return bokstav[from.x] + (from.y+1) + " - " + bokstav[to.x] + (to.y+1);
	}
	
}

class Move{
	Point from;
	Point to;
	Piece p;
	Piece take;
	
	Move(Point from, Point to, Piece p){
		this.from = from;
		this.to = to;
		this.p = p;
	}
	Move(Point from, Point to, Piece p, Piece take){
		this.from = from;
		this.to = to;
		this.p = p;
		this.take = take;
	}
}

