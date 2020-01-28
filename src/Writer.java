import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Writer implements Runnable{
    private static final long TIME_OUT = 1; // Maximum waiting time in minutes for a chunk to become available in the queue
    private BlockingQueue<Chunk> blocking_queue;
    private File output_file;
    private Bitmap bitmap;


    /**
     * Writer Constructor
     * @param blocking_queue a queue from which to write chunks into output file
     * @param output_file an output file to write chunks into
     * @param bitmap a bitmap to keep track of chunks written to disk
     */
    public Writer(BlockingQueue<Chunk> blocking_queue, File output_file, Bitmap bitmap){
        this.blocking_queue = blocking_queue;
        this.output_file = output_file;
        this.bitmap = bitmap;
    }

    /**
     * Write Chunks of data from queue into the file
     * Keep track of chunks written in the bitmap
     */
    @Override
    public void run(){
        // initialize a RAF to write chunks from the queue into the output file
        RandomAccessFile rafi = null;
        try {
            rafi = new RandomAccessFile(output_file, "rw");
        } catch (Exception e){
            System.err.println("There was a problem while opening the output file for writing: " + e.getMessage());
        }

        // Take chunks from the queue and write them to the correct location in the file
        try {
            Chunk chunk_to_write = null;
            boolean first_chunk = true;
            // wait TIME_OUT seconds for a chunk to become available in the queue
            while ((chunk_to_write = blocking_queue.poll(TIME_OUT, TimeUnit.MINUTES)) != null){
                if (first_chunk){
                    System.out.println("Downloaded " + bitmap.getPercentage() + "%");
                    first_chunk = false;
                }
                rafi.seek(chunk_to_write.getOffset());
                rafi.write(chunk_to_write.getData(), 0, chunk_to_write.getSize());

                // update bitmap
                int previous_percentage = bitmap.getPercentage();
                bitmap.update(chunk_to_write.getChunkId());
                int cur_percentage = bitmap.getPercentage();

                // serialize bitmap and print an update after every percent written to output file
                if (cur_percentage > previous_percentage){
                    if(bitmap.serialize()){
                        System.out.println("Downloaded " + bitmap.getPercentage() + "%");
                        if(bitmap.isFinished()) { // download is finished - don't wait for more chunks
                            break;
                        }
                    }
                }
            }

            rafi.close();

        } catch (IOException e){
            System.err.println("There was a problem while writing a chunk to the file: " + e.getMessage());
        } catch (InterruptedException e){
            System.err.println("There was a problem while retrieving a chunk from the queue: " +e.getMessage());
        }

    }

    /**
     * @return Program's Time Out value
     */
    public static long getTimeOut(){
        return TIME_OUT;
    }
}
