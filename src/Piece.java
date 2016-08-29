import java.util.LinkedList;

class Piece{
	String name;
	char symbol;
	int value;
	boolean white;
	Point pos;
	LinkedList<Piece> canTake = new LinkedList<Piece>();
	LinkedList<Piece> canBeTakenBy = new LinkedList<Piece>();
	LinkedList<Point> possibleMoves = new LinkedList<Point>();
	ConfigHandler config;
	
	Piece(){
		name = "";
	}

	public int dekkerFelter(){
		return canTake.size() + possibleMoves.size();
	}
	
	public int defending(){
		int sum = 0;
		for(Piece p : canTake){
			if(p.white == white){
				sum++;
			}
		}
		return sum;
	}
	public int attacking(){
		int sum = 0;
		for(Piece p : canTake){
			if(p.white != white){
				sum++;
			}
		}
		return sum;
	}

	public boolean canTake(Point to){
		for(Piece p : canTake){
			if(p.pos.equals(to)){
				return true;
			}
		}
		return false;
	}

	public boolean canTake(Piece take){
		for(Piece p : canTake){
			if(p == take){
				return true;
			}
		}
		return false;
	}
	
	public boolean canMove(Point to){
		for(Point p : possibleMoves){
			if((p.equals(to))){
				return true;
			}
		}
		return canTake(to);
	}
	
	
	public int getValue(){
		return value;
	}
	
	public String getName(){
		return name;
	}
	
	
	void update(Chessboard cb, LinkedList<Piece> allPieces, Move move){
//		if(!cb.kingHasMoved){
//			if((!cb.leftRookHasMoved && ((white && ))) || !cb.rightRookHasMoved)
//		}Rokade

		if(config.game.tracePieceUpdate || config.game.tracePieceUpdateName){
			System.out.println();
			System.out.println("Updating " + this.getString());
			if(config.game.tracePieceUpdate){
				cb.printControl();
			}
		}

		//Fjerner kontrollen til brikken paa brettet
		//Resetter andres forhold til denne brikken
		//Legger til kontroll paa brettet
		if(!(this instanceof Pawn)){
			for(Piece p : canTake){
				p.removeCanBeTakenBy(this);
				if(move != null && p.pos.equals(move.to)){
					if(move.p == this || move.take == p){
						cb.removeControl(move.to, white);
					}else{
						cb.removeControl(move.from, white);
					}
				}else{
					cb.removeControl(p.pos, white);
				}
			}
			for(Point p : possibleMoves){
				cb.removeControl(p, white);
			}
		}else{//Hvis bonde
			for(Piece p : canTake){
				p.removeCanBeTakenBy(this);
			}
			if(move != null && pos.equals(move.to)){
				cb.removePawnControl(cb, this, move);
				cb.addPawnControl(cb, this);
			}
		}
		
		if(config.game.tracePieceUpdate){
			cb.printControl();
		}
		
		//Resetter denne brikkens forhold til resten
		this.possibleMoves = new LinkedList<Point>();
		this.canTake = new LinkedList<Piece>();
		
		
		if(this instanceof Pawn){
			if((pos.y == 7 && white) || (pos.y == 0 && !white)){
				getQueen(this, cb, move);
			}else{
				if(move != null && pos.equals(move.to)){
					this.addPathPawn(allPieces, true);				
				}else{
					this.addPathPawn(allPieces, false);
				}
			}
		}else if(this instanceof Knight){
			this.addPathKnight(allPieces);
		}else if(this instanceof Bishop){
			this.addPathDiagonal(allPieces);
		}else if(this instanceof Rook){
			this.addPathStright(allPieces);
		}else if(this instanceof Queen){
			this.addPathStright(allPieces);
			this.addPathDiagonal(allPieces);
		}else if(this instanceof King){
			this.addPathKing(allPieces);
		}
		
		//Fjerner ulovelige trekk ------------------------------------For mye tid
/*		for(int n = 0; n < possibleMoves.size(); n++){
			if(!cb.canMove(pos, possibleMoves.get(n))){
				possibleMoves.remove(n);
				n--;
			}
		}
		for(int n = 0; n < canTake.size(); n++){
			if(!cb.canMove(pos, canTake.get(n).pos)){
				canTake.remove(n);
				n--;
			}
		}*/

		//Legger til kontroll paa brettet
		if(!(this instanceof Pawn)){
			for(Piece p : canTake){
				cb.addControl(p.pos, white);
			}
			for(Point p : possibleMoves){
				cb.addControl(p, white);
			}
		}
		
		if(config.game.tracePieceUpdate){
			cb.printControl();
		}
		
	}
	
	
	
	public void getQueen(Piece p, Chessboard cb, Move move){
		Piece pu = new Queen(p);

		for(Piece temp : p.canBeTakenBy){
			temp.removeCanTake(p);
			temp.addCanTake(pu);
			pu.addCanBeTakenBy(temp);
		}
		for(Piece temp : p.canTake){
			temp.removeCanBeTakenBy(p);
		}
		
		pu.update(cb, cb.getAllPieces(), move);
		
		//Bytt ut brikken paa brettet
		if(white){
			cb.piecesWhite.remove(p);
			cb.piecesWhite.add(pu);
		}else{
			cb.piecesBlack.remove(p);
			cb.piecesBlack.add(pu);	
		}
	}
	
	
	void addPathStright(LinkedList<Piece> allPieces){
		Piece maxY = null;
		Piece minY = null;
		Piece maxX = null;
		Piece minX = null;
		for(Piece p : allPieces){
			if(p.pos.x == pos.x){
				if(p.pos.y > pos.y && (maxY == null || p.pos.y < maxY.pos.y)){
					maxY = p;
				}else if(p.pos.y < pos.y && (minY == null || p.pos.y > minY.pos.y)){
					minY = p;
				}
			}else if(p.pos.y == pos.y){
				if(p.pos.x > pos.x && (maxX == null || p.pos.x < maxX.pos.x)){
					maxX = p;
				}else if(p.pos.x < pos.x && (minX == null || p.pos.x > minX.pos.x)){
					minX = p;
				}
			}
		}
		int max = 8;
		int min = -1;
		if(minX != null){
			min = minX.pos.x;
			addCanTake(minX);
			minX.addCanBeTakenBy(this);
		}
		if(maxX != null){
			max = maxX.pos.x;
			addCanTake(maxX);
			maxX.addCanBeTakenBy(this);
		}
		for(int n = min+1; n < max; n++){
			if(n != pos.x){
				addMove(new Point(n, pos.y));
			}
		}
		max = 8;
		min = -1;
		if(minY != null){
			min = minY.pos.y;
			addCanTake(minY);
			minY.addCanBeTakenBy(this);
		}
		if(maxY != null){
			max = maxY.pos.y;
			addCanTake(maxY);
			maxY.addCanBeTakenBy(this);
		}
		for(int n = min+1; n < max; n++){
			if(n != pos.y){
				addMove(new Point(pos.x, n));
			}
		}
	}
	
	void addPathDiagonal(LinkedList<Piece> allPieces){
		Piece ru = null;
		Piece rd = null;
		Piece lu = null;
		Piece ld = null;
		
		for(Piece p : allPieces){
			if(p.pos.x - p.pos.y == pos.x-pos.y){
				if(p.pos.y > pos.y){
					if(ru == null || p.pos.y < ru.pos.y){
						ru = p;
					}
				}else if(p.pos.y < pos.y){
					if(ld == null || p.pos.y > ld.pos.y){
						ld = p;
					}
				}
			}else if(p.pos.x + p.pos.y == pos.x+pos.y){
				if(p.pos.y > pos.y){
					if(lu == null || p.pos.y < lu.pos.y){
						lu = p;
					}
				}else if(p.pos.y < pos.y){
					if(rd == null || p.pos.y > rd.pos.y){
						rd = p;
					}
				}
			}
		}

		int max = 8;
		int min = -1;
		if(ld != null){
			min = ld.pos.y;
			addCanTake(ld);
			ld.addCanBeTakenBy(this);
		}
		if(ru != null){
			max = ru.pos.y;
			addCanTake(ru);
			ru.addCanBeTakenBy(this);
		}
		for(int n = min+1; n < max; n++){
			if(n != pos.y && pos.x-pos.y+n >= 0 && pos.x-pos.y+n <= 7){
				addMove(new Point(pos.x-pos.y+n, n));
			}
		}
		max = 8;
		min = -1;
		if(rd != null){
			min = rd.pos.y;
			addCanTake(rd);
			rd.addCanBeTakenBy(this);
		}
		if(lu != null){
			max = lu.pos.y;
			addCanTake(lu);
			lu.addCanBeTakenBy(this);
		}
		for(int n = min+1; n < max; n++){
			if(n != pos.y && pos.x+pos.y-n >= 0 && pos.x+pos.y-n <= 7){
				addMove(new Point(pos.x+pos.y-n, n));
			}
		}
	}
	
	void addPathKnight(LinkedList<Piece> allPieces){
		boolean[] moves = {true, true, true, true, true, true, true, true};
		int[] temp1 = {-2, -2, -1, -1,  1,  1, 2, 2};
		int[] temp2 = { 1,  -1, 2, -2, 2, -2, 1, -1};
		for(Piece p : allPieces){
			if(pos.x-2 == p.pos.x){
				if(pos.y+1 == p.pos.y){
					moves[0] = false;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}else if(pos.y-1 == p.pos.y){
					moves[1] = false;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}
			}else if(pos.x-1 == p.pos.x){
				if(pos.y+2 == p.pos.y){
					moves[2] = false;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}else if(pos.y-2 == p.pos.y){
					moves[3] = false;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}
			}else if(pos.x+1 == p.pos.x){
				if(pos.y+2 == p.pos.y){
					moves[4] = false;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}else if(pos.y-2 == p.pos.y){
					moves[5] = false;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}
			}else if(pos.x+2 == p.pos.x){
				if(pos.y+1 == p.pos.y){
					moves[6] = false;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}else if(pos.y-1 == p.pos.y){
					moves[7] = false;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}
			}
		}
		
		for(int n = 0; n < temp1.length; n++){
			if(moves[n] && pos.x+temp1[n] >= 0 && pos.x+temp1[n] <= 7 && pos.y+temp2[n] >= 0 && pos.y+temp2[n] <= 7){
				addMove(new Point(temp1[n]+pos.x, temp2[n]+pos.y));
			}
		}
	}
	
	void addPathPawn(LinkedList<Piece> allPieces, boolean hasMoved){
		int forward = 2;
		
		if((pos.y != 1 && white) || (pos.y != 6 && !white)){
			forward = 1;
		}
		for(Piece p : allPieces){
			if(p.pos.x == pos.x){
				if(white){
					if(p.pos.y == pos.y+1){
						forward = 0;
					}else if(p.pos.y == pos.y+2 && forward > 1){
						forward = 1;
					}
				}else{
					if(p.pos.y == pos.y-1){
						forward = 0;
					}else if(p.pos.y == pos.y-2 && forward > 1){
						forward = 1;
					}
				}
			}else if(Math.abs(p.pos.x-pos.x) <= 1){
				if(white){
					if(p.pos.y == pos.y+1){
						if(p.pos.x == pos.x+1){
							addCanTake(p);
							p.addCanBeTakenBy(this);
						}else if(p.pos.x == pos.x-1){
							addCanTake(p);
							p.addCanBeTakenBy(this);
						}
					}
				}else{
					if(p.pos.y == pos.y-1){
						if(p.pos.x == pos.x+1){
							addCanTake(p);
							p.addCanBeTakenBy(this);
						}else if(p.pos.x == pos.x-1){
							addCanTake(p);
							p.addCanBeTakenBy(this);
						}
					}
				}
			}
		}
		
		if(white){
			for(int n = 1; n <= forward; n++){
				addMove(new Point(pos.x, pos.y+n));
			}
		}else{
			for(int n = 1; n <= forward; n++){
				addMove(new Point(pos.x, pos.y-n));
			}
		}
	}
	
	void addPathKing(LinkedList<Piece> allPieces){
		boolean[] temp = new boolean[8];
		
		for(Piece p : allPieces){
			if(p.pos.x == pos.x-1){
				if(p.pos.y == pos.y+1){
					temp[0] = true;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}else if(p.pos.y == pos.y){
					temp[1] = true;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}else if(p.pos.y == pos.y-1){
					temp[2] = true;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}
			}else if(p.pos.x == pos.x){
				if(p.pos.y == pos.y+1){
					temp[3] = true;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}else if(p.pos.y == pos.y-1){
					temp[4] = true;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}
			}else if(p.pos.x == pos.x+1){
				if(p.pos.y == pos.y+1){
					temp[5] = true;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}else if(p.pos.y == pos.y){
					temp[6] = true;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}else if(p.pos.y == pos.y-1){
					temp[7] = true;
					addCanTake(p);
					p.addCanBeTakenBy(this);
				}
			}
		}
		
		if(!temp[0] && pos.x-1 >= 0 && pos.y+1 <= 7){
			addMove(new Point(pos.x-1, pos.y+1));
		}
		if(!temp[1] && pos.x-1 >= 0){
			addMove(new Point(pos.x-1, pos.y));
		}
		if(!temp[2] && pos.x-1 >= 0 && pos.y-1 >= 0){
			addMove(new Point(pos.x-1, pos.y-1));
		}
		if(!temp[3] && pos.y+1 <= 7){
			addMove(new Point(pos.x, pos.y+1));
		}
		if(!temp[4] && pos.y-1 >= 0){
			addMove(new Point(pos.x, pos.y-1));
		}
		if(!temp[5] && pos.y+1 <= 7 && pos.x+1 <= 7){
			addMove(new Point(pos.x+1, pos.y+1));
		}
		if(!temp[6] && pos.x+1 <= 7){
			addMove(new Point(pos.x+1, pos.y));
		}
		if(!temp[7] && pos.y-1 >= 0 && pos.x+1 <= 7){
			addMove(new Point(pos.x+1, pos.y-1));
		}
	}
	

	void addMove(Point p){
		if(config.game.tracePossibleMoves){
			System.out.println(getString() + " adding move " + p.getString());
		}

		possibleMoves.add(p);
	}

	void addCanTake(Piece p){
		if(config.game.tracePossibleMoves){
			System.out.println(getString() + " adding can take " + p.getString());
		}
		
		
		canTake.add(p);
	}
	void addCanBeTakenBy(Piece p){
		if(config.game.tracePossibleMoves){
			System.out.println(getString() + " adding can be taken by " + p.getString());
		}
		
		
		canBeTakenBy.add(p);
	}
	
	void removeMove(Point p){
		if(config.game.tracePossibleMoves){
			System.out.println(getString() + " removing move " + p.getString());
		}
		
		
		possibleMoves.remove(p);
	}

	void removeCanTake(Piece p){
		if(config.game.tracePossibleMoves){
			System.out.println(getString() + " removing can take " + p.getString());
		}
		
		
		canTake.remove(p);
	}
	void removeCanBeTakenBy(Piece p){
		if(config.game.tracePossibleMoves){
			System.out.println(getString() + " removing can be taken by " + p.getString());
		}
		
		
		canBeTakenBy.remove(p);
	}
	
	
	public boolean equals(Piece p){
		if(pos.equals(p.pos)){
			return true;
		}
		return false;
	}
	
	
	
	void movesPrint(){
		System.out.println(getString() + ":");
		System.out.print("Can go: ");
		for(Point p : possibleMoves){
			if(p != null){
				System.out.print(p.getString() + ", ");
			}else{
				System.out.print("null, ");
			}
		}
		System.out.println();
		System.out.print("Can take: ");
		for(Piece p : canTake){
			if(p != null){
				System.out.print("" + ((char)(p.pos.x+97)) + (p.pos.y+1) + ", ");
			}else{
				System.out.print("null, ");
			}
		}
		System.out.println();
		System.out.print("Can be taken by: ");
		for(Piece p : canBeTakenBy){
			if(p != null){
				System.out.print("" + ((char)(p.pos.x+97)) + (p.pos.y+1) + ", ");
			}else{
				System.out.print("null, ");
			}
		}
		System.out.println();
		System.out.println();
	}
	
	public Piece clone(){
		return null;
	}
	
	public String getString(){
		return name + " " + ((char)(pos.x+97)) + (pos.y+1);
	}
}

class Queen extends Piece{
	
	Queen(Piece p){
		this.pos = p.pos.clone();
		name = "Queen";
		symbol = 'Q';
		this.white = p.white;
		value = 9;
		this.config = p.config;
	}
	Queen(boolean white, int x, int y, ConfigHandler config){
		this.pos = new Point(x, y);
		name = "Queen";
		symbol = 'Q';
		this.white = white;
		value = 9;
		this.config = config;
	}

	public Piece clone(){
		return new Queen(this);
	}
	
}
class Rook extends Piece{

	Rook(Piece p){
		this.pos = p.pos.clone();
		name = "Rook";
		symbol = 'R';
		this.white = p.white;
		value = 5;
		this.config = p.config;
	}
	Rook(boolean white, int x, int y, ConfigHandler config){
		this.pos = new Point(x, y);
		name = "Rook";
		symbol = 'R';
		this.white = white;
		value = 5;
		this.config = config;
	}
	
	public Piece clone(){
		return new Rook(this);
	}	
}
class Bishop extends Piece{
	
	Bishop(Piece p){
		this.pos = p.pos.clone();
		name = "Bishop";
		symbol = 'B';
		this.white = p.white;
		value = 3;
		this.config = p.config;
	}
	Bishop(boolean white, int x, int y, ConfigHandler config){
		this.pos = new Point(x, y);
		name = "Bishop";
		symbol = 'B';
		this.white = white;
		value = 3;
		this.config = config;
	}
	public Piece clone(){
		return new Bishop(this);
	}	
	
}
class Knight extends Piece{
	
	Knight(Piece p){
		this.pos = p.pos.clone();
		name = "Knight";
		symbol = 'N';
		this.white = p.white;
		value = 3;
		this.config = p.config;
	}
	Knight(boolean white, int x, int y, ConfigHandler config){
		this.pos = new Point(x, y);
		name = "Knight";
		symbol = 'N';
		this.white = white;
		value = 3;
		this.config = config;
	}

	public Piece clone(){
		return new Knight(this);
	}	
}
class Pawn extends Piece{
	
	boolean hasMoved = false;
	
	Pawn(Piece p){
		this.pos = p.pos.clone();
		name = "Pawn";
		symbol = 0;
		this.white = p.white;
		value = 1;
		this.config = p.config;
	}
	Pawn(boolean white, int x, int y, ConfigHandler config){
		this.pos = new Point(x, y);
		name = "Pawn";
		symbol = 0;
		this.white = white;
		value = 1;
		this.config = config;
	}

	public Piece clone(){
		return new Pawn(this);
	}	
}
class King extends Piece{
	
	King(Piece p){
		this.pos = p.pos.clone();
		name = "King";
		symbol = 'K';
		this.white = p.white;
		value = 999;
		this.config = p.config;
	}
	King(boolean white, int x, int y, ConfigHandler config){
		this.pos = new Point(x, y);
		name = "King";
		symbol = 'K';
		this.white = white;
		value = 999;
		this.config = config;
	}

	public Piece clone(){
		return new King(this);
	}	
}
