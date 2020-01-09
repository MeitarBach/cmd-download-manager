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

        try {
            http_connection = (HttpURLConnection) download_url.openConnection();
            http_connection.setRequestProperty("Range", "Bytes=" + range_start + "-" + (range_start+range_size));
            http_connection.connect();
        } catch (IOException e) {
            System.err.println("There was a problem while opening a connection to the url:" + e.getMessage());
            System.err.println("Download Failed");
        }

        InputStream input_stream = null;

        try{
            input_stream = http_connection.getInputStream();
            byte[] read_buffer = new byte[CHUNK_SIZE];
            int bytes_read = 0;
            long offset = range_start;
            while((bytes_read = input_stream.read(read_buffer)) != -1){
                Chunk data_chunk = new Chunk(read_buffer, bytes_read, offset);
                blocking_queue.put(data_chunk);
                offset += bytes_read;
            }
        } catch (Exception e){
            System.err.println("There was a problem while reading the data and putting it into the queue:" + e.getMessage());
            System.err.println("Download Failed");
        }
    }

    public BlockingQueue<Chunk> getBlockingQueue(){
        return this.blocking_queue;
    }

}
