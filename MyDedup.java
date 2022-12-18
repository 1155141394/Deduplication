import java.io.*;
import java.util.ArrayList;
import java.lang.Math;
import java.security.MessageDigest;
import java.util.Arrays;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class MyDedup {
    static int maxContainerSize = 1048576; // 1MB
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

    public static int strList2File(String path, ArrayList<String> list){
        try{
            FileWriter fwriter = new FileWriter(path);
            for(String str: list) {
                fwriter.write(str + System.lineSeparator());
            }
            fwriter.close();
            return 0;
        }
        catch (Exception e){
            return 1;
        }
    }

    public static boolean checkPower(int val){
        int i = 1;
        while (val >= i){
            if (val == i) return true;
            else i <<= 1;
        }
        return false;
    }

    public static ArrayList<String> file2StrList(String path) throws IOException{
        ArrayList<String> strList = new ArrayList<>();
        BufferedReader buffreader = new BufferedReader(new FileReader(path));
        String line = buffreader.readLine();
        while (line != null) {
            strList.add(line);
            line = buffreader.readLine();
        }
        buffreader.close();
        return strList;
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

    public static int chunkUpload(ArrayList<ArrayList<Byte>> chunks, String indexFile, String uploadFileName) throws IOException {
        File f = new File("data/"+indexFile);
        // 判断Index file是否存在
        if (!f.exists())
        {
            ArrayList<Byte> container = new ArrayList<>();
            int containerNum = 0; // number of containers
            ArrayList<String> indexes = new ArrayList<>(); // fingerprint list
            int offset = 0;
            ArrayList<String> fileRecipe = new ArrayList<>(); // File recipe
            int len = 0;
            String basicPath = "data/"; // Need to change
            int numOfFile = 1;
            int numPreDedChunk = chunks.size();
            int numUniqueChunk = 0;
            int bytePreDedChunk = 0;
            int byteUniqueChunk = 0;
            // Loop the chunks
            for (ArrayList<Byte> byteList: chunks)
            {
                // Calculation
                bytePreDedChunk += byteList.size();
                // System.out.println(byteList);
                byte[] byteArr = byteList2Arr(byteList);
                len = byteList.size();
                // Check if the container has enough space
                if (offset + len >= maxContainerSize) {
                    // Store container
                    String path = basicPath + "container" + containerNum + ".txt";
                    byte2file(path, byteList2Arr(container));
                    container = new ArrayList<>(); // Optimize
                    containerNum += 1;
                    offset = 0;
                }
                // Generate the index
                byte[] checksum = getChecksum(byteArr);
                String fingerprint = Arrays.toString(checksum);
                indexes.add(fingerprint + ";" + containerNum + ";" + offset + ";" + len);
                byteUniqueChunk += byteList.size();
                numUniqueChunk += 1;
                // Store the fingerprint in file recipe
                fileRecipe.add(fingerprint);
                // Add chunk to buffer
                container.addAll(byteList);
                offset += len;
            }
            // Upload the container in memory to cloud
            if(container.size() >= 1) {
                String path = basicPath + "container" + containerNum + ".txt";
                byte2file(path, byteList2Arr(container));
                container = new ArrayList<>(); // Optimize
                containerNum += 1;
                offset = 0;
            }
            // Store the index file
            String indexFilePath = basicPath + "mydedup.index";
            strList2File(indexFilePath, indexes);
            // Store file recipe
            uploadFileName = uploadFileName.replaceAll("[//]","");
            String fileRecipePath = basicPath + uploadFileName + ".txt";
            strList2File(fileRecipePath, fileRecipe);
            // Store the container number, total num of files, num of prechunk, num of unique chunk,
            // bytes of pre chunk, bytes of unique chunk
            String conNumPath = basicPath + "info.txt";
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(Integer.toString(containerNum));
            tmp.add(Integer.toString(numOfFile));
            tmp.add(Integer.toString(numPreDedChunk));
            tmp.add(Integer.toString(numUniqueChunk));
            tmp.add(Integer.toString(bytePreDedChunk));
            tmp.add(Integer.toString(byteUniqueChunk));
            strList2File(conNumPath, tmp);
            System.out.println("Report Output:");
            System.out.printf("Total number of files that have been stored: %d\n", numOfFile);
            System.out.printf("Total number of pre-deduplicated chunks in storage: %d\n", numPreDedChunk);
            System.out.printf("Total number of unique chunks in storage: %d\n", numUniqueChunk);
            System.out.printf("Total number of bytes of pre-deduplicated chunks in storage: %d\n", bytePreDedChunk);
            System.out.printf("Total number of bytes of unique chunks in storage: %d\n", byteUniqueChunk);
            System.out.printf("Total number of containers in storage: %d\n", containerNum);
            double deduRatio = (double) bytePreDedChunk/byteUniqueChunk;
            System.out.printf("Deduplication ratio: %.2f\n", deduRatio);
        }
        // Exist the index file
        else
        {
            ArrayList<Byte> container = new ArrayList<>(); // New container
            // Get the container number from the file
            ArrayList<String> conNumList = file2StrList("data/info.txt");
            int containerNum = Integer.parseInt(conNumList.get(0));
            int numOfFile = Integer.parseInt(conNumList.get(1)) + 1;
            int numPreDedChunk = Integer.parseInt(conNumList.get(2)) + chunks.size();
            int numUniqueChunk = Integer.parseInt(conNumList.get(3));
            int bytePreDedChunk = Integer.parseInt(conNumList.get(4));
            int byteUniqueChunk = Integer.parseInt(conNumList.get(5));
            // Get index and store in a map
            ArrayList<String> indexes = file2StrList("data/mydedup.index");
            HashMap<String, Integer> indexMap = new HashMap<>();
            for(String index: indexes){
                String[] s = index.split(";");

                indexMap.put(s[0],1);
            }
            int offset = 0; // file offset
            ArrayList<String> fileRecipe = new ArrayList<>(); // File recipe
            int len = 0; // chunk length
            String basicPath = "data/"; // Need to change

            // Loop the chunks
            for (ArrayList<Byte> byteList: chunks)
            {
                byte[] byteArr = byteList2Arr(byteList);
                len = byteList.size();
                bytePreDedChunk += len;
                // Check if the container has enough space
                if (offset + len >= maxContainerSize) {
                    // Store container
                    String path = basicPath + "container" + containerNum + ".txt";
                    byte2file(path, byteList2Arr(container));
                    container = new ArrayList<>(); // Optimize
                    containerNum += 1;
                    offset = 0;
                }
                // Generate the fingerprint
                byte[] checksum = getChecksum(byteArr);
                String fingerprint = Arrays.toString(checksum);
                // Compare fingerprint
                if (!indexMap.containsKey(fingerprint))
                {
                    indexes.add(fingerprint + ";" + containerNum + ";" + offset + ";" + len);
                    container.addAll(byteList);
                    offset += len;
                    numUniqueChunk += 1;
                    byteUniqueChunk += byteList.size();
                }
//                else System.out.println(byteList.size());
                fileRecipe.add(fingerprint);
                // Add chunk to container
            }
            // Store the container in memory to cloud
            if(container.size() >= 1) {
                String path = basicPath + "container" + containerNum + ".txt";
                byte2file(path, byteList2Arr(container));
                container = new ArrayList<>(); // Optimize
                containerNum += 1;
                offset = 0;
            }
            // Store the index file
            String indexFilePath = basicPath + "mydedup.index";
            strList2File(indexFilePath, indexes);
            // Store file recipe
            uploadFileName = uploadFileName.replaceAll("[//]","");
            String fileRecipePath = basicPath + uploadFileName + ".txt";
            strList2File(fileRecipePath, fileRecipe);
            // Store the info
            String conNumPath = basicPath + "info.txt";
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(Integer.toString(containerNum));
            tmp.add(Integer.toString(numOfFile));
            tmp.add(Integer.toString(numPreDedChunk));
            tmp.add(Integer.toString(numUniqueChunk));
            tmp.add(Integer.toString(bytePreDedChunk));
            tmp.add(Integer.toString(byteUniqueChunk));
            strList2File(conNumPath, tmp);
            System.out.println("Report Output:");
            System.out.printf("Total number of files that have been stored: %d\n", numOfFile);
            System.out.printf("Total number of pre-deduplicated chunks in storage: %d\n", numPreDedChunk);
            System.out.printf("Total number of unique chunks in storage: %d\n", numUniqueChunk);
            System.out.printf("Total number of bytes of pre-deduplicated chunks in storage: %d\n", bytePreDedChunk);
            System.out.printf("Total number of bytes of unique chunks in storage: %d\n", byteUniqueChunk);
            System.out.printf("Total number of containers in storage: %d\n", containerNum);
            double deduRatio = (double) bytePreDedChunk/byteUniqueChunk;
            System.out.printf("Deduplication ratio: %.2f\n", deduRatio);
        }
        return 0;
    }

    public static ArrayList<Byte> downloadFile(String fileName) throws IOException {
        ArrayList<Byte> file = new ArrayList<>();
        ArrayList<String> fileFP;
        fileFP = file2StrList("data/"+ fileName + ".txt"); // Get fingerprint of one file
        ArrayList<String> indexes;
        indexes = file2StrList("data/mydedup.index"); // Get the chunk index
        HashMap<String, Integer> indexMap = new HashMap<>(); // Hashmap of index,record the index
        int count = 0;
        for(String index: indexes)
        {
            String[] s = index.split(";");
            indexMap.put(s[0], count);
            count += 1;
        }
        // Get the content of the file
        for(String fingerPrint: fileFP)
        {
            int index = indexMap.get(fingerPrint);
            String chunkIndex = indexes.get(index); // Get all the information of chunk
            String[] s = chunkIndex.split(";");
            String containerNum = s[1]; // Get which container file content in
            int offset = Integer.parseInt(s[2]); // Get the start point of the content
            int len = Integer.parseInt(s[3]); // Get the length of content
            String containerName = "container" + containerNum;
            byte[] container = file2byte("data/" + containerName + ".txt");
            ArrayList<Byte> fileContent = new ArrayList<>();
            for(int i = offset; i < offset + len; i++)
            {
                fileContent.add(container[i]);
            }
            file.addAll(fileContent); // Add the content part into file
        }
        return file;
    }

    public static int constructFile(String fileToDownload, String newFileName) throws IOException {
        ArrayList<Byte> fileByteList = downloadFile(fileToDownload);
        byte[] fileByteArr = byteList2Arr(fileByteList);
        byte2file(newFileName, fileByteArr);
        return 0;
    }
    public static int powMod(int a, int b,int mod){
        int pow = a;
        for(int i=0;i<b-1;i++){
            pow = (pow*a)%mod;
        }
        if(b==0){
            return 1;
        }
        return pow;
    }

    public static ArrayList<ArrayList<Byte>> getChunks(String fileName,int m,int d,int q,int maxSize){
        File file = new File(fileName);
        InputStream in = null;
        byte[] buffer;
        try {
//            System.out.println("以字节为单位读取文件内容，一次读一个字节：");
            // 一次读一个字节
            in = new FileInputStream(file);
            buffer = file2byte(fileName);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        int size = buffer.length;
//        System.out.println(size);


        ArrayList<ArrayList<Byte>> chunks = new ArrayList<>();
        int lastFlag = -1;
        int nowFlag;
        int i = 0;
        while(i<=size-m){
            int p = 0;

            for (int j = 0;j<m;j++){
                p = (p+(int)buffer[i+j]%q*powMod(d,m-1-j,q))%q;
            }

            if (i+m>=size){
                ArrayList<Byte> chunk = new ArrayList<>();
                for (int j = lastFlag+1 ;j < size; j++){
                    chunk.add(buffer[j]);
                }
                chunks.add(chunk);
                //System.out.println(chunks.get(0));
                return chunks;
            }

            nowFlag = i + m - 1;
            //System.out.println(p);
            if(p%q == 0 || nowFlag-lastFlag>=maxSize){
                ArrayList<Byte> chunk = new ArrayList<>();
                for (int j = lastFlag + 1;j <= nowFlag; j++){
                    chunk.add(buffer[j]);
                }
                chunks.add(chunk);
                lastFlag = i + m - 1;
                i = nowFlag + 1;
                continue;
            }
            i++;
        }

        return chunks;
    }

    public static void main(String[] args) {
        if(args[0].equals("upload")){
            String fileName = args[5];
            int m = Integer.parseInt(args[1]);
            int q = Integer.parseInt(args[2]);
            int maxSize = Integer.parseInt(args[3]);
            int d = Integer.parseInt(args[4]);
            if(!(checkPower(m)&&checkPower(q)&&checkPower(maxSize))){
                System.out.println("Not power of 2!");
                return;
            }


            ArrayList<ArrayList<Byte>> chunks = getChunks(fileName,m,d,q,maxSize);
//            System.out.println("Chunks size is "+chunks.size());
            try {
                chunkUpload(chunks, "mydedup.index", fileName);
            }
            catch (IOException e){
                System.out.println(e);
            }
        } else if (args[0].equals("download")) {
            try {
                String fileName = args[1].replaceAll("[//]","");
                constructFile(fileName, args[2]);
            }
            catch(IOException e){
                System.out.println(e);
            }
        }

    }
}
