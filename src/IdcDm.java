import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

public class IdcDm {
    public static void main(String[] args) throws Exception{
        URL url = new URL("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");
        String file_name = Utils.getFileName(url.getFile());
        File output_file = new File(file_name);

        if(output_file.createNewFile()){
            System.out.println("created file");
        } else {
            System.out.println("file exists");
        }

        LinkedBlockingQueue<Chunk> bq = new LinkedBlockingQueue<>();

        ConnectionReader connection_reader1 = new ConnectionReader(0,12167246,
                bq, url);

        ConnectionReader connection_reader = new ConnectionReader(0,24334492,
                bq, url);

        Thread connection_reader_worker1 = new Thread(connection_reader1);
        connection_reader_worker1.start();

        Thread connection_reader_worker = new Thread(connection_reader);
        connection_reader_worker.start();

        Thread writer_worker = new Thread(new Writer(connection_reader.getBlockingQueue(),output_file));
        writer_worker.start();

        connection_reader_worker1.join();
        connection_reader_worker.join();
        System.out.println(connection_reader1.getBlockingQueue());
        System.out.println(connection_reader.getBlockingQueue());
        System.out.println();
        System.out.println();
        writer_worker.join();

        File input_file = new File("C:\\Users\\Meitar\\Downloads\\test\\Mario1_500_src.avi");
        Utils.diff(input_file, output_file);
    }


}
