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
    private int maxNumberOfChars = 537; // maximum number of characters for one QR Code //TODO for max efficiency understand QR codes and set the precise number of characters to use the full capacity of the reduced QR code
    private String message = null; //entire message
    private int requiredNumberOfChunks = -1; // after the message is loaded, this variable will contain the number of chunks needed to send this message

    //define the class constructor and overload it with two different argument lists

    /**
     * constructor, overloaded
     * @param maxNumberOfChunks maximum number of Chunks per message
     * @param maxNumberOfChars maximum number of characters per chunk
     */
    public MessageSplittingHandler(int maxNumberOfChunks, int maxNumberOfChars)
    {
        //if the numbers are specified, use them
        this.maxNumberOfChunks = maxNumberOfChunks;
        this.maxNumberOfChars = maxNumberOfChars;
    };

    /**
     * default constructor
     */
    public MessageSplittingHandler() {}; //if the default constructor is called, use the values defined in this class

    /**
     * loads the entire message for further processing and computes the required number of chunks
     * @param message String that contains the message that is supposed to be splitted in one piece
     * @return true if the message can be splitted into number of chunks that are less than the specified maxNumberOfChunks, taking into account the maxNumberOfChars, false otherwise
     */
    public boolean loadMessage(String message)
    {
        //TODO die Berechnung von requiredNumberOfChunks berücksichtigt nicht die characters die für die positionsnummer verbraucht werden
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
     *
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
            //TODO (lux) hier habe ich rumgepfuscht noch schön maxhen
            String m = String.valueOf(positionNumber) + chunkString;
            if (positionNumber < 10)
                m="0"+m;
            return m;
            //TODO chunkstring bytes abziehen für postionNumber
        }else {
            return chunkString;
        }

    }

    private int computeQRPositionNumber(int[] positionTuple)
    {
        //TODO function not yet checked for errors
        int P = positionTuple[0];
        int p = positionTuple[1];

        return ((P-1)+1)*(P-1) / 2 + p;
    }
}
