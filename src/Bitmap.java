import java.io.*;

public class Bitmap extends Thread implements Serializable{
    private boolean[] bitmap;
    private String file_name;
    private int percentage;
    private ObjectOutputStream output_stream;


    public Bitmap(String file){
        long content_length = Utils.getContentLength(file);
        int chunks_number = (int) (content_length / ConnectionReader.getChunkSize());
        // add one chunk if content-length isn't divisible by CHUNK_SIZE
        if (!(content_length % ConnectionReader.getChunkSize() == 0))
            chunks_number++;
        this.bitmap = new boolean[chunks_number];

        this.file_name = Utils.getFileName(file) + ".tmp";
        File newFile = new File(file_name);
        try {
            newFile.createNewFile();
            System.out.println("Created bitmap file");
        } catch (IOException e){
            System.err.println("Couldn't create the bitmap file: " + e);
        }


        this.percentage = 0;

        try {
            this.output_stream = new ObjectOutputStream(new FileOutputStream(file_name));
            System.out.println("Successfully wrote Bitmap to Disk");
        } catch (IOException e){
            System.err.println("Couldn't create bitmap: " + e);
        }

    }

    public String getFileName (){
        return this.file_name;
    }

    public int getLength(){
        return this.bitmap.length;
    }

    public int getPercentage(){
        return this.percentage;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append("File name: " + getFileName());
        str.append("\nFile length: " + getLength());
        str.append("\nDownloaded percentage: " + getPercentage());

        return str.toString();
    }

    @Override
    public void run() {
        try {
            output_stream.writeObject(this);
            System.out.println("Successfully serialized Bitmap");
        } catch (IOException e){
            System.err.println("There was a problem while serializing the Bitmap: " + e);
        }
    }
}
