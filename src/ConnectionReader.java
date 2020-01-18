import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

public class ConnectionReader implements Runnable {

    private final int CHUNK_SIZE = 4096; // Maximum size of a chunk
    private long range_start;
    private long range_size;
    private BlockingQueue<Chunk> blocking_queue;
    private URL download_url;

    public ConnectionReader(long range_start, long range_size, BlockingQueue<Chunk> blocking_queue,
                            URL download_url){
        this.range_start = range_start;
        this.range_size = range_size;
        this.blocking_queue = blocking_queue;
        this.download_url = download_url;
    }

    public void run(){

        HttpURLConnection http_connection = null;

        // issue a range http get request for the resource specified in the url
        try {
            http_connection = (HttpURLConnection) download_url.openConnection();
            http_connection.setRequestProperty("Range", "Bytes=" + range_start + "-" + (range_start+range_size-1));
            http_connection.connect();
        } catch (IOException e) {
            System.err.println("There was a problem while opening a connection to the url:" + e.getMessage());
            System.err.println("Download Failed");
        }

        // read chunks of data from the connection and put them into the queue
        InputStream input_stream = null;
        try {
            input_stream = http_connection.getInputStream();

            int bytes_read = 0;
            long offset = range_start;
            while(bytes_read != -1){
                byte[] read_buffer = new byte[CHUNK_SIZE];
                bytes_read = input_stream.read(read_buffer);
                if (bytes_read == -1) break; // stop at end of stream
                // create a chunk of size equal to the amount of bytes successfully read
                // initialize the chunk's offset to be the offset in the file where this chunk starts
                Chunk data_chunk = new Chunk(read_buffer, bytes_read, offset);
                blocking_queue.put(data_chunk);
                offset += bytes_read;
            }
        } catch (IOException e){
            System.err.println("There was a problem while reading the data: " + e.getMessage());
            System.err.println("Download Failed");
        } catch (InterruptedException e){
            System.err.println("There was a problem while writing a chunk into the queue: " + e.getMessage());
            System.err.println("Download Failed");
        }
    }

    public BlockingQueue<Chunk> getBlockingQueue(){
        return this.blocking_queue;
    }

    public String getRange(){
        return (this.range_start + " - " + (range_start+range_size-1));
    }

    @Override
    public String toString(){
        String result = "Connection reader details:";
        result += "Range size:" + this.range_size;
        result += ", Starting from:" + this.range_start;
        result += ", Reader range:" + this.getRange() + ")";

        return result;
    }

}
