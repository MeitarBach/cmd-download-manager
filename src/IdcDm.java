import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

public class IdcDm {
    public static void main(String[] args) throws Exception{
        URL url = new URL("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");

        ConnectionReader connection_reader = new ConnectionReader(0,32768,
                new LinkedBlockingQueue<>(), url);

        Thread connection_reader_worker = new Thread(connection_reader);
        connection_reader_worker.run();

        System.out.println(connection_reader.getBlockingQueue());
    }
}
