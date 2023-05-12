package com.javetest.helio;


import java.lang.Math;

public class MessageSplittingHandler
/*
    class processes message string in order to subdivide it into chunks
    that can fit into one QR-Code each
    -> it assigns a position number to every chunk
    -> it rejects the message if it can not fit into 13 QR codes in total
*/

{
    private int maxNumberOfChunks = 13; // maximum number of QR codes
    private int maxNumberOfChars = 537; // maximum number of characters for one QR Code //TODO for max efficiency understand QR codes and set the precise number of characters to use the full capacity of the reduced QR code
    private String message = null; //entire message
    private int requiredNumberOfChunks = -1; // after the message is loaded, this variable will contain the number of chunks needed to send this message

    //define the class constructor and overload it with two different argument lists
    public MessageSplittingHandler(int maxNumberOfChunks, int maxNumberOfChars)
    {
        //if the numbers are specified, use them
        this.maxNumberOfChunks = maxNumberOfChunks;
        this.maxNumberOfChars = maxNumberOfChars;
    };

    public MessageSplittingHandler() {} //if the default constructor is called, use the values defined in this class

    public boolean loadMessage(String message)
    //load the entire message for further processing and compute the required number of chunks
    //if the message is too big (i.e. contains to many characters) the function returns false
    {
        //TODO die Berechnung von requiredNumberOfChunks berücksichtigt nicht die characters die für die positionsnummer verbraucht werden
        this.message = message;
        double length = this.message.length(); //counts characters including white spaces
        this.requiredNumberOfChunks = (int) Math.ceil(length / (double) this.maxNumberOfChunks);

        // if the message can be separated into at most maxNumberOfChunks chunks, return true, otherwise return false
        return (this.requiredNumberOfChunks <= this.maxNumberOfChunks);
    }

    public int getRequiredNumberOfChunks()
    // return the computed number if chunks required to send the entire message
    {
        return requiredNumberOfChunks;
    }

    public String getMessageChunkAtPosition(int position)
    // returns a substring of the original message together with the positionNumber
    // position refers to the index: 0 <= position < requiredNumberOfChunks;
    {
        //TODO debug and validate
        //check if position is within the possible interval, otherwise return null
        if (position >= this.requiredNumberOfChunks) return null;

        // get the start and end indices (endIndex must not be greater than the message itself)
        int startIndex = this.maxNumberOfChars * position;
        int endIndex = Math.max(this.maxNumberOfChars * (position + 1), this.message.length() -1);

        String chunkString = this.message.substring(startIndex, endIndex);
        int positionNumber = computeQRPositionNumber(new int[]{this.requiredNumberOfChunks, position + 1});

        return String.valueOf(positionNumber) + chunkString;
    }

    private int computeQRPositionNumber(int[] positionTuple)
    {
        //TODO function not yet checked for errors
        int P = positionTuple[0];
        int p = positionTuple[1];

        return ((P-1)+1)*(P-1) / 2 + p;
    }
}
