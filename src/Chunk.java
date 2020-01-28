public class Chunk {
    private static final int CHUNK_SIZE = 8192; // Maximum size of a chunk - 8KB
    private byte[] data;
    private int size;
    private long offset; // where in the file this chunk starts
    private int chunk_id;

    /**
     * Chunk Constructor
     * @param data the chunk's data buffer
     * @param size the chunk's size
     * @param offset the offset in the output file where this chunk should be written to
     */
    public Chunk(byte[] data, int size, long offset){
        this.data = data;
        this.size = size;
        this.offset = offset;
        this.chunk_id = (int)(offset / CHUNK_SIZE);
    }

    /**
     *
     * @return The size of all chunks (except the last)
     */
    public static int getChunkSize(){
        return CHUNK_SIZE;
    }

    /**
     *
     * @return the id of the chunk
     */
    public int getChunkId(){
        return chunk_id;
    }

    /**
     *
     * @return the chunk's data buffer
     */
    public byte[] getData(){
        return data;
    }

    /**
     *
     * @return The size of the chunk
     */
    public int getSize(){
        return size;
    }

    /**
     *
     * @return The offset of the chunk
     */
    public long getOffset(){
        return offset;
    }

    /**
     *
     * @return The range of the chunk
     */
    public String getRange(){
        return (offset + " - " + (offset+size-1));
    }

    @Override
    public String toString(){
        String result = "\n(";
        result += "Chunk Size:" + this.getSize();
        result += ", Starting From:" + this.getOffset();
        result += ", Chunk Range:" + this.getRange() + ")";

        return result;
    }
}
