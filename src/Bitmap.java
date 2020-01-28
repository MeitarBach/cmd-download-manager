import java.io.*;

public class Bitmap implements Serializable{
    private boolean[] bitmap;
    private String file_name;
    private int chunks_wrote;
    private int percentage;
    private boolean finished;

    /**
     * Bitmap Constructor
     * @param file_url a url string of a file to create a bitmap to
     */
    public Bitmap(String file_url){
        long content_length = Utils.getContentLength(file_url);
        int chunks_number = (int) (content_length / Chunk.getChunkSize());
        // add last chunk if content-length isn't divisible by CHUNK_SIZE
        if (!(content_length % Chunk.getChunkSize() == 0))
            chunks_number++;
        this.bitmap = new boolean[chunks_number];

        this.file_name = Utils.getFileName(file_url) + ".tmp";

        this.chunks_wrote = 0;

        this.percentage = 0;

        this.finished = false;

    }

    /**
     *
     * @return a boolean array indicating which chunks had been written already
     */
    public boolean[] getBitmapArray(){
        return this.bitmap;
    }

    /**
     *
     * @return the name of the file this bitmap refers to
     */
    public String getFileName (){
        return this.file_name;
    }

    /**
     *
     * @return number of chunks this bitmap keeps track of
     */
    public int getLength(){
        return this.bitmap.length;
    }

    /**
     *
     * @return percentage of chunks already written to disk
     */
    public int getPercentage(){
        return this.percentage;
    }

    /**
     *
     * @return true if the entire file has been written to disk, false otherwise
     */
    public boolean isFinished(){
        return this.finished;
    }

    /**
     * Percentage setter
     * @param percentage the new percentage to set
     */
    public void setPercentage(int percentage){
        this.percentage = percentage;
    }

    /**
     * A function which gets a chunk id and updates the bitmap to keep track of the chunks written
     * @param chunk_id an id of a chunk written to disk
     */
    public void update(int chunk_id){
        this.bitmap[chunk_id] = true;
        this.chunks_wrote++;
        int cur_percentage = (int) (((double)chunks_wrote / bitmap.length) * 100);
        if (cur_percentage > this.percentage)
            this.percentage = cur_percentage;
        if (this.percentage == 100)
            this.finished = true;
    }

    /**
     * A function which serializes the bitmap to disk
     * @return true if serialization is successful, false otherwise
     */
    public boolean serialize(){
        try {
            // write a temporary copy of the bitmap to the disk
            File temp_bitmap = new File(file_name + "1");
            ObjectOutputStream output_stream = new ObjectOutputStream(new FileOutputStream(temp_bitmap));
            output_stream.writeObject(this);
            output_stream.close();

            // rename the temp copy of the bitmap to the permanent name
            File permanent_bitmap = new File(file_name);
            if(permanent_bitmap.exists()) {
                permanent_bitmap.delete();
            }
            if(!temp_bitmap.renameTo(permanent_bitmap))
                System.out.println("Couldn't rename bitmap");
        } catch (IOException e){
            System.err.println("Couldn't serialize bitmap: " + e);
            return false;
        }

        return true;
    }

    /**
     * Deserialize a bitmap off of the disk
     * @param file_path the path of the bitmap to deserialize
     * @return The deserialized bitmap
     */
    public static Bitmap deserialize(String file_path){
        Bitmap bitmap = null;
        try {
            ObjectInputStream input_stream = new ObjectInputStream(new FileInputStream(file_path));
            bitmap = (Bitmap) input_stream.readObject();
            input_stream.close();
        } catch (Exception e){
            System.err.println("There was a problem while deserializing the bitmap: " + e);
        }

        return bitmap;
    }

    /**
     * A function for creating/getting the bitmap when the download starts/resumes
     * @param file_url the file to create/get bitmap for/of
     * @return A bitmap of the file
     */
    public static Bitmap getBitmap(String file_url){
        String file_name = Utils.getFileName(file_url) + ".tmp";
        File bitmap = new File(file_name);
        if(bitmap.exists())
            return deserialize(file_name);
        else
            return new Bitmap(file_url);
    }

    /**
     * A function which deletes the bitmap from disk
     */
    public void delete(){
        File bitmap = new File(this.file_name);
        bitmap.delete();
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append("File name: " + getFileName());
        str.append("\nFile length: " + getLength());
        str.append("\nDownloaded percentage: " + getPercentage());

        return str.toString();
    }

}
