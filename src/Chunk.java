public class Chunk {
    private byte[] data;
    private int size;
    private long offset; // where in the file this chunk starts

    public Chunk(byte[] data, int size, long offset){
        this.data = data;
        this.size = size;
        this.offset = offset;
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
