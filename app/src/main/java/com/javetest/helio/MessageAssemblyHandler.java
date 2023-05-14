package com.javetest.helio;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

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
    public boolean loadMessageChunk(String messageChunk) throws IllegibleScanException, DoubleScanException, UnexpectedPublicKeyException {

        if (this.firstChunk) //then this function is called for the first time since the instance of this class was created
        {
            //extract the total number of chunks and initialize the string message chunk array to null
            this.totalNumberOfChunks = extractPositionNumberTuple(messageChunk)[0];
            messageChunks = new String[this.totalNumberOfChunks];
        }

        if (messageChunk.substring(0,2).equals("99")){
            throw new UnexpectedPublicKeyException("unexpected PublicKey scan");
        }

        // extract the current positionNumbers
        int[] positionNumbers = extractPositionNumberTuple(messageChunk);

        if (this.chunkIsValid(positionNumbers)) //checking whether the position numbers can be extracted properly from the message chunk
        {
            //check weather the chunk was loaded before
            if (this.messageChunks[positionNumbers[1] - 1] != null) throw new DoubleScanException("The chunk at position " + Integer.toString(positionNumbers[1]) + "was already loaded");

            //add the message chunk to the right position

            this.messageChunks[positionNumbers[1] - 1] = extractMessage(messageChunk);
        }
        else
        {
            throw new IllegibleScanException("position numbers could not be extracted from QR code");
        }
        this.firstChunk = false;
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
            //TODO exception thrown
            Log.w("MessageAssemblyHandler.getRSABlocks", "RSA blocks were requested but not all message chunks have been loaded yet");
            return new String[1];
        }

        // transform the array that holds the message chunks into one single String

        String entireMessage = new String("");

        for (String chunk : this.messageChunks)
        {
            entireMessage = entireMessage + chunk;
        }

        //RSA blocks end with "==\n" zumindest glauben wir das grade sollte eventuell nochmal gecheckt werden lol
        String[] rsaBlocks = entireMessage.split("==\n"); //die funktion String.split() gibt die Strings zurück ohne das matching pattern.

        for (int i = 0; i < rsaBlocks.length; i++)
        {
            rsaBlocks[i] = rsaBlocks[i] + "==\n";
        }
        return rsaBlocks;
    }

    private boolean chunkIsValid(int[] positionNumbers)
    {
        if (this.totalNumberOfChunks < 1 || this.totalNumberOfChunks > 13) //check if the total number of chunks is within bounds
        {
            return false;
        }
        if (positionNumbers[0] != this.totalNumberOfChunks) //check if the total number of chunks are consistent with previous scans
        {
            return false;
        }
        return true;
    }


    /**
     * the function extracts the position numbers from the message chunk, check out Read.me for more information
     * @param messageChunk a concatenation of the position number and the actual message chunk
     * @return a tuple of ints, the first one is the total number of expected chunks, the second one is the position of the current chunk within the message
     */
    private int[] extractPositionNumberTuple(String messageChunk)
    {
        //extract the position number from the string (the first two character) and parse it to integer

        if (canBeParsedToInteger(messageChunk.substring(0,2)))
        {
            int positionNumber = Integer.parseInt(messageChunk.substring(0,2));

            int totalNumberOfChunks = (int) Math.ceil(-0.5 + Math.sqrt(0.25 + positionNumber * 2.0));
            int currentPosition = totalNumberOfChunks - ((((totalNumberOfChunks + 1) * totalNumberOfChunks) / 2) - positionNumber);

            return new int[]{totalNumberOfChunks, currentPosition};
        }
        return new int[]{0,0};
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

    /**
     * @param string characters that are chacked for parsing potential
     * @return true if the given string can be parsed to an integer with Integer.valueOf(String), false otherwise
     */
    private boolean canBeParsedToInteger(String string) {
        try {
            Integer.valueOf(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
