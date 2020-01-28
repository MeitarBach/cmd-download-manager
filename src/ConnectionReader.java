import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

public class ConnectionReader implements Runnable {
    private static final int TIME_OUT = 30 * 1000; // Connection and read Timeout - 30 sec
    private long range_start;
    private long range_size;
    private BlockingQueue<Chunk> blocking_queue;
    private URL download_url;
    private Bitmap bitmap;

    /**
     * ConnectionReader constructor. Creates a new ConnectionReader instance
     * @param range_start
     * @param range_size
     * @param blocking_queue
     * @param download_url
     * @param bitmap
     */
    public ConnectionReader(long range_start, long range_size, BlockingQueue<Chunk> blocking_queue,
                            URL download_url, Bitmap bitmap){
        this.bitmap = bitmap;

        // Don't assign this reader chunks that were already downloaded and written to file
        boolean[] bitmap_arr = bitmap.getBitmapArray();
        int i = (int)(range_start / Chunk.getChunkSize());
        while(i < bitmap_arr.length && bitmap_arr[i++] && range_size > 0){
            range_start += Chunk.getChunkSize();
            range_size -= Chunk.getChunkSize();
        }

        this.range_start = range_start;
        this.range_size = range_size;
        this.blocking_queue = blocking_queue;
        this.download_url = download_url;
    }

    /**
     * Download the range of bytes this reader was assigned to.
     * Wrap Chunks of data and push them into a Blocking Queue.
     */
    public void run(){
        // Make sure that the range assigned is not already downloaded
        if (range_size == 0) {
            System.out.println("Reader " + threadId() + " was given a range that has already been downloaded");
            return;
        }


        System.out.println(threadId() + " Start downloading range (" + getRange() + ") " +
                "from:\n" + download_url);

        // issue a range http get request for the resource specified in the url
        HttpURLConnection http_connection = null;
        try {
            http_connection = (HttpURLConnection) download_url.openConnection();
            http_connection.setRequestMethod("GET");
            http_connection.setRequestProperty("Range", "Bytes=" + getRange());
            http_connection.setConnectTimeout(TIME_OUT);
            http_connection.setReadTimeout(TIME_OUT);
            http_connection.connect();
        } catch (IOException e) {
            System.err.println("There was a problem while opening a connection to the url: " + e);
        }

        // read chunks of data from the connection and put them into the queue
        InputStream input_stream = null;
        try {
            input_stream = http_connection.getInputStream();

            int bytes_read = 0;
            long offset = range_start;
            while (bytes_read != -1) {

                // Skip all chunks that were already written to the output file
                boolean[] bitmap_arr = bitmap.getBitmapArray();
                int i = (int) (offset / Chunk.getChunkSize());
                int bytes_to_skip = 0;
                while (i < bitmap_arr.length && bitmap_arr[i++]) {
                    bytes_to_skip += Chunk.getChunkSize();
                }
                int skipped = 0;
                while (bytes_to_skip - skipped > 0) {
                    skipped += input_stream.skip(bytes_to_skip - skipped);
                    offset += skipped;
                    if (offset > lastByteInRange()) // no more bytes to read in this range
                        return;
                }

                // Try to read a Chunk of data from the stream
                byte[] read_buffer = new byte[Chunk.getChunkSize()];
                bytes_read = input_stream.read(read_buffer);
                if (bytes_read == -1) break; // stop at end of stream

                // fill up buffer before creating a chunk
                // skip if it is the last chunk in the range
                if (offset + bytes_read - 1 < lastByteInRange()) {
                    while (Chunk.getChunkSize() - bytes_read > 0) {
                        bytes_read += input_stream.read(read_buffer, bytes_read,
                                                        Chunk.getChunkSize() - bytes_read);
                    }
                }

                // create a chunk of size CHUNK_SIZE (unless it is the last chunk) and put it into the queue
                Chunk data_chunk = new Chunk(read_buffer, bytes_read, offset);
                blocking_queue.put(data_chunk);

                // advance offset an check if this reader finished downloading it's range
                offset += bytes_read;
                if (offset > lastByteInRange())
                    System.out.println(threadId() + " Finished downloading");
            }
        } catch (SSLException e) {
            System.err.println("Reader " + threadId() + " encountered a Network problem: " + e);
        }catch (SocketException e){
            System.err.println("Reader " + threadId() + " encountered a Network problem: " + e);
        } catch (InterruptedException e){
            System.err.println("There was a problem while writing a chunk into the queue: " + e);
        } catch (IOException e){
            System.err.println("There was a problem while reading range: " + getRange() + "from the data: " + e);
        }

    }

    /**
     * @return id of current Thread
     */
    private String threadId(){
        return "[" + Thread.currentThread().getId() + "]";
    }

    /**
     * @return range assigned to this reader
     */
    public String getRange(){
        return (this.range_start + "-" + lastByteInRange());
    }


    /**
     * @return the last byte in the assigned range
     */
    public long lastByteInRange(){
        return range_start+range_size - 1;
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
