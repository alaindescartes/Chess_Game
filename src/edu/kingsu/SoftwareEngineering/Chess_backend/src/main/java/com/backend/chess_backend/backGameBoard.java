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

    private int[][] gameBoard;
    private String movesRecord;
}
