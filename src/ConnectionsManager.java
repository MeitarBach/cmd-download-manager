import java.util.concurrent.*;

public class ConnectionsManager {
    private ExecutorService readers_pool;
    private ConnectionReader[] connection_readers;


    public ConnectionsManager(ConnectionReader[] connection_readers){
        this.readers_pool = Executors.newFixedThreadPool(connection_readers.length);
        this.connection_readers = connection_readers;
    }

    public void execute(){
        for (ConnectionReader cr : connection_readers)
            this.readers_pool.execute(cr);
    }

    public ExecutorService getReaderPool(){
        return readers_pool;
    }

}
