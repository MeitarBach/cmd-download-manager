import java.net.URL;

public class Tests {

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
