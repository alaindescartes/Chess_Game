/**
 * @author Tyler B
 * @version 0.0.1
 */

public class backGameBoard
{

    public backGameBoard()
    {
        gameBoard = new int[8][8];
        movesRecord = "";
        player1 = "";
        player2 = "";
        piecesList = "";
        turn = 'w';
    }

    public String getMovesRecord()
    {
        return movesRecord;
    }

    public void removeMove(String deletedMove)
    {
        //this function will delete the move(s) from the record of played moves (movesRecord)
    }

    public void addMove(String newMove)
    {
        movesRecord = movesRecord + newMove;
    }

    public String makeMove(char whichTurn, String requestedMove)
    {
        /**
         * This function will allow the backend to verify a move request by the frontend.
         * the string data type willl be replaced later when we know what data type it will send
         * this is for both string mentions in the function description
         */
        if (whichTurn != turn)
        {
            return "It is not this player's turn.";
        }
        else
        {
            /**
             * Take the requested move and check if it is one of the calculated legal moves.
             * if (requested move is legal)
             * {
             * update the board in this class
             * then send the reply back to the front end allowing the move /
             * update the board on the frontend
             * also, update whos turn it is
             * }
             * else
             * {
             * we return a message to the front end stating the move is not allowed
             * }
             */
        }
    }

    public void updateLegalMoves()
    {
        /**
         * Once a move is made, the board is in a new position.
         * So this function will either calculate the new legal moves,
         * or will tell legal moves to update its legal moves list
         */
    }

    public void runAI()
    {
        /**
         * This function will run when a ai needs to make a move
         * In order to not lock up the program when the ai is running, it is likely that 
         * the ai will be given it's own thread to run on (multithreading)
         */
    }

    private int[][] gameBoard;
    private String movesRecord;
    private String player1; //replace string with the abstract player class later
    private String player2; //replace string with the abstract player class later
    private String piecesList; //replace string with pieces class later
    private char turn;
}
