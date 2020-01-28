import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class IdcDm {
    public static void main(String[] args) {

        /******  TESTS  ******/

//        Tests.getUrlsTest();
//        Tests.booleanResetTest();
//        Tests.testBitmapCreation("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");
//        Tests.testBitmapSetPercentage("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");
//        Tests.testBitmapDesirialization("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");


        /******   Main Program Logic   ******/

        // validate proper usage
        if(args.length > 2 || args.length < 1){
            System.out.println("usage:\n\t\tjava IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]");
            System.exit(-1);
        }

        // Set up a pool of ConnectionReader Threads
        URL[] urls = Utils.getUrls(args[0]);
        String download_url = urls[0].toString();
        long content_length = Utils.getContentLength(download_url);
        Bitmap bitmap = Bitmap.getBitmap(download_url);
        LinkedBlockingQueue<Chunk> bq = new LinkedBlockingQueue<>();
        int connections_number = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        ConnectionsManager readers_pool =
                new ConnectionsManager(connections_number, content_length, bq, urls, bitmap);

        // Execute Reader Threads
        readers_pool.execute();

        // Create/Open output file
        String file_name = Utils.getFileName(download_url);
        System.out.println("\nDownloading: " + file_name);
        File output_file = null;
        try {
            output_file = new File(file_name);
            output_file.createNewFile();
        } catch (IOException e){
            System.err.println("Couldn't create/open output file on disk: " + e);
        }

        if (output_file == null)
            System.exit(-1);


        // Set up a single Writer Thread
        Thread writer_worker = new Thread(new Writer(bq, output_file, bitmap));
        writer_worker.start();

        // Block and wait for download to finish
        try {
            readers_pool.getReaderPool().shutdown();
            readers_pool.getReaderPool().awaitTermination(Writer.getTimeOut(), TimeUnit.MINUTES);
            writer_worker.join();
        }catch (InterruptedException e){
            System.err.println("Shutdown sequence was interrupted: " + e);
        }

        // Check for download success/failure and print according message
        if (bitmap.isFinished()){
            System.out.println("Download succeeded");
            bitmap.delete();
        }
        else{
            System.out.println("Download failed");
            System.exit(-1);
        }
    }

}
