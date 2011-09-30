/**
 *   Copyright 2011 Quest Software, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.quest.oraoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.cloudera.sqoop.mapreduce.db.DBInputFormat;

class OraOopDBInputSplit extends DBInputFormat.DBInputSplit {

    int splitId;
    double totalNumberOfBlocksInAllSplits;
    String splitLocation;
    private List<OraOopOracleDataChunk> oracleDataChunks;

    // NB: Update serialize(), deserialize() and getDebugDetails() if you add fields here.

    public OraOopDBInputSplit() {

        this.splitId = -1;
        this.splitLocation = "";
        this.oracleDataChunks = new ArrayList<OraOopOracleDataChunk>();
    }

    public OraOopDBInputSplit(List<OraOopOracleDataChunk> dataChunks) {

        setOracleDataChunks(dataChunks);
    }

    public void setOracleDataChunks(List<OraOopOracleDataChunk> dataChunks) {

        this.oracleDataChunks = dataChunks;
    }

    public List<OraOopOracleDataChunk> getDataChunks() {

        return this.oracleDataChunks;
    }

    public int getNumberOfDataChunks() {

        if (this.getDataChunks() == null)
            return 0;
        else
            return this.getDataChunks().size();
    }

    public String[] getLocations() throws IOException {

        if (this.splitLocation.isEmpty())
            return new String[] {};
        else
            return new String[] { this.splitLocation };

    }

    /**
     * @return The total number of blocks within the data-chunks of this split
     */
    public long getLength() {

        return this.getTotalNumberOfBlocksInThisSplit();
    }

    public int getTotalNumberOfBlocksInThisSplit() {

        if (this.getNumberOfDataChunks() == 0)
            return 0;

        int result = 0;
        for (OraOopOracleDataChunk dataChunk : this.getDataChunks())
            result += dataChunk.getNumberOfBlocks();

        return result;
    }

    public OraOopOracleDataChunk findDataChunkById(int id) {

        for (OraOopOracleDataChunk dataChunk : this.getDataChunks()) {
            if (dataChunk.id == id)
                return dataChunk;
        }
        return null;
    }

    protected void serialize(DataOutput output) throws IOException {

        output.writeInt(splitId);

        if (this.oracleDataChunks == null)
            output.writeInt(0);
        else {
            output.writeInt(this.oracleDataChunks.size());
            for (OraOopOracleDataChunk dataChunk : this.oracleDataChunks)
                dataChunk.serialize(output);
        }
    }

    protected void deserialize(DataInput input) throws IOException {

        this.splitId = input.readInt();

        int dataChunkCount = input.readInt();
        if (dataChunkCount == 0)
            this.oracleDataChunks = null;
        else {
            this.oracleDataChunks = new ArrayList<OraOopOracleDataChunk>(dataChunkCount);
            for (int idx = 0; idx < dataChunkCount; idx++) {
                OraOopOracleDataChunk dataChunk = new OraOopOracleDataChunk();
                dataChunk.deserialize(input);
                this.oracleDataChunks.add(dataChunk);
            }
        }
    }

    public String getDebugDetails() {

        StringBuilder result = new StringBuilder();

        if (this.getNumberOfDataChunks() == 0)
            result.append(String.format("Split[%d] does not contain any Oracle data-chunks.", this.splitId));
        else {
            result.append(String.format("Split[%d] includes the Oracle data-chunks:\n", this.splitId));
            for (OraOopOracleDataChunk dataChunk : getDataChunks()) {
                result.append(String.format("\t\tRelative DataFile Number=%d\tstart-block=%s\tfinish-block=%d\t=> %d blocks.\n"
                                           ,dataChunk.relativeDatafileNumber
                                           ,dataChunk.startBlockNumber
                                           ,dataChunk.finishBlockNumber
                                           ,(dataChunk.finishBlockNumber - dataChunk.startBlockNumber) + 1));
            }
        }
        return result.toString();
    }

}