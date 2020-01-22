import java.io.File;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

public class IdcDm {
    public static void main(String[] args) throws Exception{
        // TESTS
        getUrlsTest();
//        booleanResetTest();


        // Main Program Logic
        URL[] urls = Utils.getUrls("test\\CentOS-6.10-x86_64-netinstall.iso.list");
        long content_length = Utils.getContentLength(urls[0]);

        LinkedBlockingQueue<Chunk> bq = new LinkedBlockingQueue<>();
        ConnectionReader[] readers = ConnectionsManager
                .createConnectionReaders(12, content_length, bq, urls);
        ConnectionsManager readers_pool = new ConnectionsManager(readers);
        readers_pool.execute();

        String file_name = Utils.getFileName(urls[0]);
        System.out.println("\nDownloading: " + file_name);

        File output_file = new File(file_name);

        if(output_file.createNewFile()){
            System.out.println("created file\n");
        } else {
            System.out.println("file exists\n");
        }

        Thread writer_worker = new Thread(new Writer(bq,output_file));
        writer_worker.start();

        writer_worker.join();
        readers_pool.getReaderPool().shutdown();


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


}
