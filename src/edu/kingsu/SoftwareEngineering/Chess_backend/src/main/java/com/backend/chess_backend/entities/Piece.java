//Piece.java

package com.backend.chess_backend.entities;

import java.util.*;

/**
 * Abstract Piece class.  Member vars and methods all chess pieces will share.  Abstract methods along with setters, getters, and constructor.
 * @author Jonathan Erikson
 * @version 1.0
*/

public abstract class Piece {
	// ===== Member Variables =====
	
	/**
	 *Name of the chess piece on the board.
	*/
	private String name;
	/**
	 *The colour the chess piece belongs to.
	*/
	private char colour;
	/**
	 *The importance of the chess piece in dictated by a number value. 1 low, > is more.
	*/
	private int value;
	/**
	 *The column location of the chess piece in reference to the board. {1,2,3,4,5,6,7,8}  The board sees this as {0,1,2,3,4,5,6,7}. spans across the board.
	*/
	private int xLoc;
	/**
	 *The row location of the chess piece in reference to the board. {A,B,C,D,E,F,G,H}  The board sees this as {0,1,2,3,4,5,6,7}. spans up/down the board.
	*/
	private int yLoc;
	/**
	 *Each chess piece will hold it's own legal valid moves pairs in a dynamic integer list.  {(x1,y1), (x2,y2), ... , (xn, yn)}
	*/
	private List<int[]> currentLegalMoves;
	
	
	// ===== CONSTRUCTOR =====
	
	/** 
	 * Creates each chess piece object assigning it's values with the passed parameters.
	 * @param name The name of the chess piece as represented on the game board.
	 * @param colour The colour the piece belongs to represented by 'B' or 'W'.
	 * @param value The importance of a game piece represented by a numerical value.  1 = lowest.  > is worth more.
	 * @param xLoc Row of the current piece.
	 * @param yLox Column of the current piece.
	 */
	Piece(String name, char colour, int value, int xLoc, int yLoc){
		this.name = name;
		this.colour = colour;
		this.value = value;
		this.xLoc = xLoc;
		this.yLoc = yLoc;
	}
	
	// ===== METHODS =====
	
		// * ABSTRACT *
	/**
	 * Abstract method to be implemented by each individual chess piece.  
	 <p>
	 *It is the standard move the piece can make.  eg. diagonal, L, etc.
	*/
	public abstract void standardMove();
	
		// * SHARED *
	/**
	 * Updates the move list as a string output.
	 * eg.  W PB1 -> D1
	 * @return Returns the movement of the move made by a player for the piece.  
	*/
	public String movement(){
		// To be written
		return "";
	}
	/**
	 * Removes the last movement entry from the list.
	 <p>
	 * The allows the player to undo their past move and make another attempt.
	 <p>
	 * Records are updated here, logic handled elsewhere.
	*/
	public void reverseMovement(){
		// To be written
	}
	/**
	 * Resets and recalulates the individual chess pieces legal moves list.  
	 * @return currentLegalMoves Updates the legal moves list.
	*/
	public List<int[]>  updateLegalMoves(){
		currentLegalMoves.clear();
		// Will add later (with Legal Moves);
		//currentLegalMoves.add(new int[]{x, y};
		return currentLegalMoves;
	}
	// ===== GETTERS & SETTERS =====
	/**
	 * Sets the column position of the chess piece.
	 * @param x The selected column on the chess board.
	*/
	public void setXLoc(int x){
		xLoc = x;
	}
	/**
	 * Sets the row position of the chess piece.
	 * @param y The selected row on the chess board.
	*/
	public void setYLoc(int y){
		yLoc = y;
	}
	/**
	 * Returns the name of the piece.
	 * @return name Provides the name of the chess piece.
	*/
	public String getName(){
		return name;
	}
	/**
	 * Returns the colour of the player the piece belongs to.
	 * @return  colour The colour of the chess piece
	*/
	public char getColour(){
		return colour;
	}
	/**
	 * Returns the value of the chess piece
	 * @return  colour The value of the chess piece
	*/
	public int getValue(){
		return value;
	}
	/**
	 * Returns the x location of the piece.   The column the piece resides.
	 * @return  colour The column of the chess piece
	*/
	public int getXLoc(){
		return xLoc;
	}
	/**
	 * Returns the y location of the piece.  The row the peice resides.
	 * @return  colour The row of the chess piece
	*/
	public int getYLoc(){
		return yLoc;
	}
	/**
	 * Provides access to the pieces legal moves list (int pair (x,y)).
	 <p>
	 * Legal moves references this in and holds access to all game pieces per player.
	 <p>
	 * Board will use this array list to highlight a selected pieces allowed moves.
	*/
	public void getLegalMoves(){
		return currentLegalMoves;
	}
}
