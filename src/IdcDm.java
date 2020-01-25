import java.io.File;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class IdcDm {
    public static void main(String[] args) throws Exception{

        /******  TESTS  ******/

//        getUrlsTest();
//        booleanResetTest();
//        testBitmapCreation("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");
//        testBitmapSetPercentage("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");
//        testBitmapDesirialization("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");


        // Main Program Logic
        if(args.length > 2 || args.length < 1){
            System.out.println("usage:\n\t\tjava IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]");
            return;
        }


        URL[] urls = Utils.getUrls(args[0]);
        long content_length = Utils.getContentLength(urls[0].toString());

        Bitmap bitmap = Bitmap.getBitmap(urls[0].toString());

        LinkedBlockingQueue<Chunk> bq = new LinkedBlockingQueue<>();

        int connections_number = args.length > 1 ? Integer.parseInt(args[1]) : 1;

        ConnectionsManager readers_pool =
                new ConnectionsManager(connections_number, content_length, bq, urls, bitmap);

        readers_pool.execute();
        readers_pool.getReaderPool().shutdown();

        String file_name = Utils.getFileName(urls[0].toString());
        System.out.println("\nDownloading: " + file_name);

        File output_file = new File(file_name);
        output_file.createNewFile();

        Thread writer_worker = new Thread(new Writer(bq, output_file, bitmap));
        writer_worker.start();

        readers_pool.getReaderPool().awaitTermination(30, TimeUnit.SECONDS);
        writer_worker.join();

        bitmap.deleteBitmap();

    }





    // resetBooleanArr test
    public static void booleanResetTest(){
        boolean[] arr = {true, true, false};
        System.out.println("Before first reset:");
        Utils.printArr(arr);

        Utils.resetBooleanArr(arr);
        System.out.println("after first reset:");
        Utils.printArr(arr);

        arr[2] = true;
        Utils.resetBooleanArr(arr);
        System.out.println("after second reset:");
        Utils.printArr(arr);
    }

    // getUrls test
    public static void getUrlsTest() {
        URL[] urls = Utils.getUrls("test\\CentOS-6.10-x86_64-netinstall.iso.list");
        Utils.printArr(urls);
        urls = Utils.getUrls("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");
        System.out.println();
        Utils.printArr(urls);
    }

    // Bitmap creation and serialization test
    public static void testBitmapCreation(String file_url){
        Bitmap bitmap = Bitmap.getBitmap(file_url);
        System.out.println("The bitmap before we update:\n" + bitmap + "\n");

        System.out.println(bitmap.serialize());

        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(bitmap);

    }


    public static void testBitmapSetPercentage(String file_url){
        Bitmap bitmap = Bitmap.getBitmap(file_url);
        bitmap.setPercentage(25);
        bitmap.serialize();
        System.out.println("We updated the bitmap");
    }

    public static void testBitmapDesirialization(String file_url){
        Bitmap bitmap = Bitmap.getBitmap(file_url);
        System.out.println("The bitmap after we update:\n" + bitmap);
    }


}
