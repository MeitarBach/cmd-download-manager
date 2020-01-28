import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

public class ConnectionReader implements Runnable {
    private final int TIME_OUT = 30 * 1000; // Connect and read timeout is after 30 sec
    private long range_start;
    private long range_size;
    private BlockingQueue<Chunk> blocking_queue;
    private URL download_url;
    private Bitmap bitmap;

    public ConnectionReader(long range_start, long range_size, BlockingQueue<Chunk> blocking_queue,
                            URL download_url, Bitmap bitmap){
        this.bitmap = bitmap;

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

    public void run(){
        if (range_size == 0) {
            System.out.println("Reader " + threadId() + " was given a range that has already been downloaded");
            return;
        }

        System.out.println(threadId() + " Start downloading range (" + getRange() + ") " +
                "from:\n" + download_url);
        HttpURLConnection http_connection = null;

        // issue a range http get request for the resource specified in the url
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


                byte[] read_buffer = new byte[Chunk.getChunkSize()];
                bytes_read = input_stream.read(read_buffer);
                if (bytes_read == -1) break; // stop at end of stream

                // fill up buffer before creating a chunk
                // skip if it is the last chunk in the range
                if (offset + bytes_read - 1 < lastByteInRange()) {
                    while (Chunk.getChunkSize() - bytes_read > 0) {
                        bytes_read += input_stream.read(read_buffer, bytes_read, Chunk.getChunkSize() - bytes_read);
                    }
                }
                // create a chunk of size equal to the amount of bytes successfully read
                // initialize the chunk's offset to be the offset in the file where this chunk starts
                Chunk data_chunk = new Chunk(read_buffer, bytes_read, offset);
                blocking_queue.put(data_chunk);
//                if (data_chunk.getSize() != Chunk.getChunkSize())
//                    System.out.println(data_chunk);
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

    public BlockingQueue<Chunk> getBlockingQueue(){
        return this.blocking_queue;
    }

    private String threadId(){
        return "[" + Thread.currentThread().getId() + "]";
    }

    public String getRange(){
        return (this.range_start + "-" + lastByteInRange());
    }

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
