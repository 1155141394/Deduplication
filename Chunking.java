import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.lang.Math;
import java.security.MessageDigest;
import java.util.Arrays;

public class Chunking {
    static int maxContainerSize = 1048576;
    public static byte[] file2byte(String path)
    {
        try {
            FileInputStream in =new FileInputStream(new File(path));
            //当文件没有结束时，每次读取一个字节显示
            byte[] data=new byte[in.available()];
            in.read(data);
            in.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int byte2file(String path, byte[] buffer)
    {
        try (FileOutputStream stream = new FileOutputStream(path)) {
            stream.write(buffer);
            return 0;
        }
        catch (Exception e){
            return 1;
        }
    }

    public static byte[] byteList2Arr(ArrayList<Byte> byteList)
    {
        byte[] byteArr = new byte[byteList.size()];
        for(int i = 0; i < byteList.size(); i++) {
            byteArr[i] = byteList.get(i);
        }
        return byteArr;
    }

    public static byte[] getChecksum(byte[] buffer){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(buffer, 0, buffer.length);
            return md.digest();
        }
        // 如果hash失败返回一个空指针
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public static int chunkUpload(ArrayList<ArrayList<Byte>> chunks, String indexFile, String uploadFileName)
    {
        File f = new File(indexFile);
        // 判断Index file是否存在
        if (!f.exists())
        {
            ArrayList<Byte> container = new ArrayList<>();
            int containerNum = 0; // number of containers
            ArrayList<String> indexes = new ArrayList<>(); // fingerprint list
            int offset = 0;
            ArrayList<String> fileRecipe = new ArrayList<>(); // File recipe
            int len = 0;
            String basicConPath = "./data/"; // Need to change
            for (ArrayList<Byte> byteList: chunks)
            {
                byte[] byteArr = byteList2Arr(byteList);
                len = byteList.size();
                // Check if the container has enough space
                if (offset + len >= maxContainerSize) {
                    // Store container
                    String path = basicConPath + containerNum;
                    byte2file(path, byteList2Arr(container));
                    containerNum += 1;
                    offset = 0;
                }
                // Generate the index
                byte[] checksum = getChecksum(byteArr);
                String fingerprint = Arrays.toString(checksum);
                indexes.add(fingerprint + "," + offset + "," + len);
                // Add chunk to buffer

                offset += len;

            }
            // Store the index file

            // Store file recipe
        }
        else
        {

        }
        return 0;
    }

    public static void main(String[] args) {
        String fileName = "./test.jpg";
        File file = new File(fileName);
        InputStream in = null;
        byte[] buffer;
        try {
            System.out.println("以字节为单位读取文件内容，一次读一个字节：");
            // 一次读一个字节
            in = new FileInputStream(file);
            buffer = file2byte(fileName);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        int size = buffer.length;

        int m = 4;
        int d = 257;
        int q = 13;


        ArrayList<ArrayList<Byte>> chunks = new ArrayList<>();
        int lastFlag = -1;
        int nowFlag = 0;
        for (int i = 0; i  < size - m; i++ ){
            int p = 0;

            for (int j = 0;j<m;j++){
                p += (int)buffer[i+j]%q*Math.pow(d,m-1-j)%q;
            }


            if(p%q == 0){

                nowFlag = i + m - 1;
                ArrayList<Byte> chunk = new ArrayList<>();
                for (int j = lastFlag + 1;j <= nowFlag; j++){
                    chunk.add((byte) buffer[j]);
                }
                chunks.add(chunk);
                lastFlag = i + m - 1;
            }

        }
        System.out.println(chunks.get(1));



    }
}
