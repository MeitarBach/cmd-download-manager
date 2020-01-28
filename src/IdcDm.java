import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

public class IdcDm {
    public static void main(String[] args) {

        /******  TESTS  ******/

//        Tests.getUrlsTest();
//        Tests.booleanResetTest();
//        Tests.testBitmapCreation("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");
//        Tests.testBitmapSetPercentage("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");
//        Tests.testBitmapDesirialization("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");


        /******   Main Program Logic   ******/

        if(args.length > 2 || args.length < 1){
            System.out.println("usage:\n\t\tjava IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]");
            System.exit(-1);
        }


        URL[] urls = Utils.getUrls(args[0]);
        long content_length = Utils.getContentLength(urls[0].toString());

        Bitmap bitmap = Bitmap.getBitmap(urls[0].toString());

        LinkedBlockingQueue<Chunk> bq = new LinkedBlockingQueue<>();

        int connections_number = args.length > 1 ? Integer.parseInt(args[1]) : 1;

        ConnectionsManager readers_pool =
                new ConnectionsManager(connections_number, content_length, bq, urls, bitmap);

//        System.out.println("The number of created readers is: " + readers_pool.getNumOfThreads());

        readers_pool.execute();

        String file_name = Utils.getFileName(urls[0].toString());
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

        Thread writer_worker = new Thread(new Writer(bq, output_file, bitmap));
        writer_worker.start();

        try {
            readers_pool.getReaderPool().shutdown();
//            readers_pool.getReaderPool().awaitTermination(30, TimeUnit.SECONDS);
            writer_worker.join();
        }catch (InterruptedException e){
            System.err.println("Shutdown sequence was interrupted: " + e);
        }


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
