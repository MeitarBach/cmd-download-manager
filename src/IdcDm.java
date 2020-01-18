import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

public class IdcDm {
    public static void main(String[] args) throws Exception{
        URL url = new URL("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");
        long content_length = Utils.getContentLength(url);
        LinkedBlockingQueue<Chunk> bq = new LinkedBlockingQueue<>();
        ConnectionReader[] readers = Utils.createConnectionReaders(3, content_length, bq, url);
        ConnectionsManager readers_pool = new ConnectionsManager(readers);
        readers_pool.execute();

        String file_name = Utils.getFileName(url);
        System.out.println("File Name: " + file_name);
        File output_file = new File(file_name);

        if(output_file.createNewFile()){
            System.out.println("created file");
        } else {
            System.out.println("file exists");
        }

        Thread writer_worker = new Thread(new Writer(bq,output_file));
        writer_worker.start();

        writer_worker.join();
        readers_pool.getReaderPool().shutdown();

        File input_file = new File("C:\\Users\\Meitar\\Downloads\\test\\Mario1_500_src.avi");
        Utils.diff(input_file, output_file);
    }


}
