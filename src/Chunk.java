public class Chunk {
    private static final int CHUNK_SIZE = 4096; // Maximum size of a chunk
    private byte[] data;
    private int size;
    private long offset; // where in the file this chunk starts
    private int chunk_id;

    public Chunk(byte[] data, int size, long offset){

        this.data = data;
        this.size = size;
        this.offset = offset;
        this.chunk_id = (int)(offset / CHUNK_SIZE);
    }

    public static int getChunkSize(){
        return CHUNK_SIZE;
    }

    public int getChunkId(){
        return chunk_id;
    }

    public byte[] getData(){
        return data;
    }

    public int getSize(){
        return size;
    }

    public long getOffset(){
        return offset;
    }

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
