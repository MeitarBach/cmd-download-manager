import java.io.File;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

public class IdcDm {
    public static void main(String[] args) throws Exception{
        // TESTS
//        getUrlsTest();
//        booleanResetTest();

//        testBitmapCreation("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");
//        testBitmapSetPercentage("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");
//        testBitmapDesirialization("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");


        // Main Program Logic
        URL[] urls = Utils.getUrls("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");
        long content_length = Utils.getContentLength(urls[0].toString());

        LinkedBlockingQueue<Chunk> bq = new LinkedBlockingQueue<>();
        ConnectionsManager readers_pool =
                new ConnectionsManager(3, content_length, bq, urls);
        readers_pool.execute();

        String file_name = Utils.getFileName(urls[0].toString());
        System.out.println("\nDownloading: " + file_name);

        File output_file = new File(file_name);

        if(output_file.createNewFile()){
            System.out.println("created file\n");
        } else {
            System.out.println("file exists\n");
        }

        Bitmap bitmap = Bitmap.getBitmap("https://ia800303.us.archive.org/19/items/Mario1_500/Mario1_500.avi");

        Thread writer_worker = new Thread(new Writer(bq, output_file, bitmap));
        writer_worker.start();

        writer_worker.join();

        readers_pool.getReaderPool().shutdown();
        bitmap.deleteBitmap();


        // Check for difference between files
//        File input_file = new File("test\\Mario1_500_src.avi");
//        Utils.diff(input_file, output_file);
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
