package com.javetest.helio;


import android.util.Log;

import java.lang.Math;

/**

    class processes message string in order to subdivide it into chunks
    that can fit into one QR-Code each
    -> it assigns a position number to every chunk
    -> it rejects the message if it can not fit into 13 QR codes in total
*/
public class MessageSplittingHandler


{
    private int maxNumberOfChunks = 13; // maximum number of QR codes
    private int maxNumberOfChars = 0; // maximum number of characters for one QR Code
    private String message = null; //entire message
    private int requiredNumberOfChunks = -1; // after the message is loaded, this variable will contain the number of chunks needed to send this message

    //define the class constructor and overload it with two different argument lists


    /**
     * constructor, overloaded
     * @param maxNumberOfChunks maximum number of Chunks per message
     * @param maxNumberOfChars maximum number of characters per chunk. Only count the characters used to display the message text itself, not the additional 2 characters to add the position number
     */

    public MessageSplittingHandler(int maxNumberOfChunks, int maxNumberOfChars)
    {
        //if the numbers are specified, use them
        this.maxNumberOfChunks = maxNumberOfChunks;
        this.maxNumberOfChars = maxNumberOfChars - 2; // -2 to take into account the number oif characters used to display the position number
    }

    /**
     * loads the entire message for further processing and computes the required number of chunks
     * @param message String that contains the message that is supposed to be splitted in one piece
     * @return true if the message can be splitted into number of chunks that are less than the specified maxNumberOfChunks, taking into account the maxNumberOfChars, false otherwise
     */
    public boolean loadMessage(String message)

    //load the entire message for further processing and compute the required number of chunks
    //if the message is too big (i.e. contains to many characters) the function returns false

    {
        this.message = message;
        double length = this.message.length(); //counts characters including white spaces

        this.requiredNumberOfChunks = (int) Math.ceil(length / (double) this.maxNumberOfChars);

        //debug
        Log.i("MessageSplittingHandler - this.message",this.message);
        Log.i("MessageSplittingHandler - this.message.length",this.message.length()+"");
        Log.i("MessageSplittingHandler - Chunks ", requiredNumberOfChunks + "");

        // if the message can be separated into at most maxNumberOfChunks chunks, return true, otherwise return false
        return (this.requiredNumberOfChunks <= this.maxNumberOfChunks);
    }

    /**
     * @return the number of chunks required to split the message that was previously loaded into the class object
     */

    public int getRequiredNumberOfChunks()
    // return the computed number if chunks required to send the entire message
    {
        return requiredNumberOfChunks;
    }

    /**
     * splits the previously loaded message into chunks and returns the chunk at position position
     * @param position position of requested chunk
     * @param addPositionNumber if true, the piece of the message will be concatenated with a to digit integers containing information about the positioning of the chunk within the message
     * @return a single String that
     */
    public String getMessageChunkAtPosition(int position, boolean addPositionNumber)

    // returns a substring of the original message together with the positionNumber
    // position refers to the index: 0 <= position < requiredNumberOfChunks;
    {
        //check if position is within the possible interval, otherwise return null
        if (position >= this.requiredNumberOfChunks) return null;

        // get the start and end indices (endIndex must not be greater than the message itself)
        int startIndex = this.maxNumberOfChars * position;
        int endIndex = Math.min(this.maxNumberOfChars * (position + 1), this.message.length());

        String chunkString = this.message.substring(startIndex, endIndex);
        int positionNumber = computeQRPositionNumber(new int[]{this.requiredNumberOfChunks, position + 1});

        Log.i("MessageSplittingHandler - final Chunks ", chunkString + " ... i:" + position);


        if (addPositionNumber == true){
            String m = String.valueOf(positionNumber) + chunkString;
            if (positionNumber < 10)
                m="0"+m;
            return m;
        }else {
            return chunkString;
        }

    }

    private int computeQRPositionNumber(int[] positionTuple)
    {
        int P = positionTuple[0];
        int p = positionTuple[1];

        return ((P-1)+1)*(P-1) / 2 + p;
    }
}
