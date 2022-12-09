import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.lang.Math;
import java.security.MessageDigest;

public class Chunking {

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

    public static byte[] getChecksum(byte[] buffer) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(buffer, 0, buffer.length);
        return md.digest();
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
        int p = 0;
        int c = 0;
        for (int i = 0; i  < size - m; i++ ){

            if (i == 0){
                for (int j = 0;j<m;j++){
                    p += (int)buffer[i+j]*Math.pow(d,m-1-j);
                }
            }else{
                p = p % ((int)Math.pow(d,m-1)) * d + buffer[i+m-1];
            }

            if(p%q == 11){
                c ++;
            }
        }
        System.out.println(c);
        System.out.println(size);

    }
}
