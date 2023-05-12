package com.javetest.helio;

import android.util.Log;

import com.google.zxing.common.StringUtils;



/**
    class assembles messages from single message chunks
    -> it reads and extracts the position number from every chunk
    -> it returns true if the message is complete, i.e. all chunks are assembled
    -> it returns the RSA blocks separately
*/
public class MessageAssemblyHandler
{
    private boolean firstChunk = true; //will be set to false after hte first message chunk was loaded
    int totalNumberOfChunks; // will hold the number of chunks of the message after the first message chunk was loaded
    String[] messageChunks; // will contain the message chunks in an ordered manner

    /**
     * the function enables the class to load message chunks consecutively
     * @param messageChunk a concatenation of the position number and the actual message chunk
     * @return true if the laoded messageChunk is the last one to assemble the entire message, otherwise falls
     */
    public boolean loadMessageChunk(String messageChunk) throws IllegibleScanException {
        if (this.firstChunk) //then this function is called for the first time since the instance of this class was created
        {
            //TODO what if a public key is laoded? throw exception?
            //extract the total number of chunks and initialize the string message chunk array to null
            this.totalNumberOfChunks = extractPositionNumberTuple(messageChunk)[0];

            if (this.totalNumberOfChunks < 1 || this.totalNumberOfChunks > 13)
            {
                throw new IllegibleScanException("total number of chunks is invalid");
            }

            messageChunks = new String[this.totalNumberOfChunks];
            this.firstChunk = false;
        }

        int currentPosition = extractPositionNumberTuple(messageChunk)[1] - 1; //index is required not position
        Log.i("MessageAssemblyHandler", "current position: " + Integer.toString(currentPosition));
        Log.i("MessageAssemblyHandler", "total Number of Chunks: " + totalNumberOfChunks);
        Log.i("MessageAssemblyHandler", "position number " + messageChunk.substring(0,2));
        this.messageChunks[currentPosition] = extractMessage(messageChunk);

        int i = 0;
        for (String chunk : messageChunks)
        {
            Log.i("MessageAssemblyHandler", Integer.toString(i) + chunk);
            i++;
        }
        return this.allChunksLoaded();
    }

    /**
     * @return the ordered message chunks, without the position number, but not processed in any other way
     */
    public String[] getOrderedMessageChunks()
    {
        return this.messageChunks;
    }

    /**
     * the function takes the message chunks from the QR-codes, combines them and cuts them in order to return the distinct RSA blocks
     * @return the distinct RSA blocks
     */
    public String[] getRSABlocks()
    {
        // TODO soll diese methode nur funktionieren wenn alle chunks geladen wurden?
        if (!this.allChunksLoaded())
        {
            Log.w("MessageAssemblyHandler.getRSABlocks", "RSA blocks were requested but not all message chunks have been loaded yet");
            return new String[1];
        }

        // transform the array that holds the message chunks into one single String
        String entireMessage = this.messageChunks.toString();

        //RSA blocks end with "==\n" zumindest glauben wir das grade sollte eventuell nochmal gecheckt werden lol
        String[] rsaBlocks = entireMessage.split("==\n"); //die funktion String.split() gibt die Strings zurück ohne das matching pattern.
        return rsaBlocks;
    }

    /**
     * the function extracts the position numbers from the message chunk
     * @param messageChunk a concatenation of the position number and the actual message chunk
     * @return a tuple of ints, the first one is the total number of expected chunks, the second one is the position of the current chunk within the message
     */
    private int[] extractPositionNumberTuple(String messageChunk)
    {
        //extract the position number from the string (the first two character) and parse it to integer
        int positionNumber = Integer.parseInt(messageChunk.substring(0,2));

        /*
        * das genaue vorgehen ist in der Git Readme hinterlegt
        * es wird die Gaußsche summenformel verwendet
        */

        //TODO folgende Zeilen debugen
        int totalNumberOfChunks = (int) Math.ceil(-0.5 + Math.sqrt(0.25 + positionNumber * 2.0));
        int currentPosition = totalNumberOfChunks - ((((totalNumberOfChunks + 1) * totalNumberOfChunks) / 2) - positionNumber);

        return new int[]{totalNumberOfChunks, currentPosition};
    }

    public int getTotalNumberOfChunks()
    {
        return this.totalNumberOfChunks;
    }

    public int getNumberOfMissingChunks()
    {
        int missingCounter = 0;
        for (String value : this.messageChunks)
        {
            if (value == null) missingCounter++;
        }
        return missingCounter;
    }

    /**
     * the function extracts the actual message from the message chunk
     * @param messageChunk  a concatenation of the position number and the actual message chunk
     * @return the actual message
     */
    private String extractMessage(String messageChunk)
    {
        return messageChunk.substring(2);
    }

    /**
     * @return returns true if all chunks are loaded, false otherwise
     */
    private boolean allChunksLoaded()
    {
        //iterate through the String array and check if an value is null
        for (String value : this.messageChunks) if (value == null) return false;
        return true;
    }
}
