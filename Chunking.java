import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.lang.Math;
import java.security.MessageDigest;

public class Chunking {
    int maxContainerSize = ;
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

    public static byte[] getChecksum(byte[] buffer){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(buffer, 0, buffer.length);
            return md.digest();
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public static int chunkUpload(ArrayList<ArrayList<Integer>> chunks, String indexFile)
    {
        File f = new File(indexFile);
        if (f.exists())
        {
            ArrayList<Byte> container = new ArrayList<Byte>();
            int offset = 0;
            for (ArrayList<Byte> bytearr: chunks)
            {

            }
        }
        else
        {

        }
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


        ArrayList<ArrayList<Integer>> chunks = new ArrayList<>();
        int lastFlag = -1;
        int nowFlag = 0;
        for (int i = 0; i  < size - m; i++ ){
            int p = 0;

            for (int j = 0;j<m;j++){
                p += (int)buffer[i+j]%q*Math.pow(d,m-1-j)%q;
            }


            if(p%q == 0){

                nowFlag = i + m - 1;
                ArrayList<Integer> chunk = new ArrayList<>();
                for (int j = lastFlag + 1;j <= nowFlag; j++){
                    chunk.add((int)buffer[j]);
                }
                chunks.add(chunk);
                lastFlag = i + m - 1;
            }

        }
        System.out.println(chunks.get(1));



    }
}
