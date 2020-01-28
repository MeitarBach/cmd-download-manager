import java.net.URL;
import java.util.concurrent.*;

public class ConnectionsManager {
    private final int MIN_RANGE = 1024 * 1000 * 4; // Minimal range to assign for a reader - 4MB
    private ExecutorService readers_pool;
    private ConnectionReader[] connection_readers;

    /**
     * A function which creates an array of desired number of ConnectionReader threads, assigning each of them
     * except the last one a range of equal size to work on. The last thread gets the remaining bytes range.
     * @param connections_number number of threads to create.
     * @param content_length the size of the download file.
     * @param bq a blocking queue to which the threads put chunks.
     * @param urls an array of urls to get the file from
     * @return an array of ConnectionReader threads
     */
    public ConnectionReader[] createConnectionReaders(int connections_number, long content_length,
                                                             BlockingQueue<Chunk> bq, URL[] urls, Bitmap bitmap){

        // limit the number of connections
        // This makes sure that each connection reader gets at least MIN_RANGE bytes to download
        while(content_length / connections_number < MIN_RANGE)
            connections_number--;

        ConnectionReader[] readers_pool = new ConnectionReader[connections_number];
        boolean[] usedUrls = new boolean[urls.length];

        // check if the file's size is evenly divisible for the desired amount of threads, and act accordingly
        long reader_range_size = content_length % connections_number == 0 ?
                content_length / connections_number : content_length / connections_number+1;
        // Make the range size divisible by CHUNK_SIZE
        reader_range_size += Chunk.getChunkSize() -
                (reader_range_size % Chunk.getChunkSize());
        long cur_range_start = 0;
        for(int i = 0 ; i < readers_pool.length ; i++){
            // reset usedUrls only when all Urls are used in this round
            Utils.resetBooleanArr(usedUrls);

            // pick a random URL from the list which is not used in this round yet
            int randUrlNumber = (int)(Math.random() * urls.length);
            while(usedUrls[randUrlNumber]){
                randUrlNumber = (int)(Math.random() * urls.length);
            }
            usedUrls[randUrlNumber] = true;

            if (i != readers_pool.length - 1) // every thread except the last one
                readers_pool[i] = new ConnectionReader(cur_range_start, reader_range_size,
                                                        bq, urls[randUrlNumber], bitmap);
            else // last thread
                readers_pool[i] = new ConnectionReader(cur_range_start, content_length,
                                                        bq, urls[randUrlNumber], bitmap);

            content_length -= reader_range_size; // keep track of last thread range size
            cur_range_start += reader_range_size;
        }

        return readers_pool;
    }


    public ConnectionsManager(int connections_number, long content_length,
                              BlockingQueue<Chunk> bq, URL[] urls, Bitmap bitmap){
        this.connection_readers = createConnectionReaders(connections_number, content_length, bq, urls, bitmap);
        this.readers_pool = Executors.newFixedThreadPool(connection_readers.length);
    }

    public void execute(){
        for (ConnectionReader cr : connection_readers)
            this.readers_pool.execute(cr);
    }

    public ExecutorService getReaderPool(){
        return readers_pool;
    }

    public int getNumOfThreads(){
        return connection_readers.length;
    }


}
