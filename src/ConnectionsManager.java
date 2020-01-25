import java.net.URL;
import java.util.concurrent.*;

public class ConnectionsManager {
    private ExecutorService readers_pool;
    private ConnectionReader[] connection_readers;

    /**
     * A function which creates an array of desired number of ConnectionReader threads, assigning each of them
     * except the last one a range of equal size to work on. The last thread gets the remaining bytes range.
     * @param connections_number number of threads to create.
     * @param content_length the size of the download file.
     * @param bq a blocking queue to which the threads put chunks.
     * @param url a url to get the file from
     * @return an array of ConnectionReader threads
     */
    public static ConnectionReader[] createConnectionReaders(int connections_number, long content_length,
                                                              BlockingQueue<Chunk> bq, URL url){
        ConnectionReader[] readers_pool = new ConnectionReader[connections_number];
        // check if the file's size is evenly divisible for the desired amount of threads, and act accordingly
        long reader_range_size = content_length % connections_number == 0 ?
                content_length / connections_number : content_length / connections_number+1;
        // Make the range size divisible by CHUNK_SIZE
        reader_range_size += Chunk.getChunkSize() -
                (reader_range_size % Chunk.getChunkSize());
        long cur_range_start = 0;
        for(int i = 0 ; i < readers_pool.length ; i++){
            if (i != readers_pool.length - 1) // every thread except the last one
                readers_pool[i] = new ConnectionReader(cur_range_start, reader_range_size, bq, url);
            else // last thread
                readers_pool[i] = new ConnectionReader(cur_range_start, content_length, bq, url);
            content_length -= reader_range_size; // keep track of last thread range size
            cur_range_start += reader_range_size;
        }

        return readers_pool;
    }

    public ConnectionReader[] createConnectionReaders(int connections_number, long content_length,
                                                             BlockingQueue<Chunk> bq, URL[] urls){
        ConnectionReader[] readers_pool = new ConnectionReader[connections_number];
        boolean[] usedUrls = new boolean[urls.length];

        // limit the number of connections to 3 * (available number of servers)
        // This makes sure that each server opens a maximum of 3 connections
        connections_number = connections_number < (3 * urls.length) ? connections_number : (3 * urls.length);

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
                                                        bq, urls[randUrlNumber]);
            else // last thread
                readers_pool[i] = new ConnectionReader(cur_range_start, content_length,
                                                        bq, urls[randUrlNumber]);

            content_length -= reader_range_size; // keep track of last thread range size
            cur_range_start += reader_range_size;
        }

        return readers_pool;
    }


    public ConnectionsManager(int connections_number, long content_length,
                              BlockingQueue<Chunk> bq, URL[] urls){
        this.connection_readers = createConnectionReaders(connections_number, content_length, bq, urls);
        this.readers_pool = Executors.newFixedThreadPool(connection_readers.length);
    }

    public void execute(){
        for (ConnectionReader cr : connection_readers)
            this.readers_pool.execute(cr);
    }

    public ExecutorService getReaderPool(){
        return readers_pool;
    }

}
