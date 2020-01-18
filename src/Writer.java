import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Writer implements Runnable{
    private final long TIME_OUT = 5; // Maximum waiting time in seconds for a chunk to become available in the queue
    private BlockingQueue<Chunk> blocking_queue;
    private File output_file;


    public Writer(BlockingQueue<Chunk> blocking_queue, File output_file){
        this.blocking_queue = blocking_queue;
        this.output_file = output_file;
    }

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
            // wait TIME_OUT seconds for a chunk to become available in the queue
            while ((chunk_to_write = blocking_queue.poll(TIME_OUT, TimeUnit.SECONDS)) != null){
                rafi.seek(chunk_to_write.getOffset());
                rafi.write(chunk_to_write.getData(), 0, chunk_to_write.getSize());
                System.out.println("Sucssessfully wrote chunk:" + chunk_to_write);
            }

            System.out.println("Queue is Empty. Finished writing chunks from the queue");
            rafi.close();

        } catch (IOException e){
            System.err.println("There was a problem while writing a chunk to the file: " + e.getMessage());
        } catch (InterruptedException e){
            System.err.println("There was a problem while retrieving a chunk from the queue: " +e.getMessage());
        }

    }
}