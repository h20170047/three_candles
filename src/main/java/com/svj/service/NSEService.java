package com.svj.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.ant.compress.taskdefs.Unzip;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.svj.utils.AppUtils.getFileNameFromDate;

@Slf4j
@Service
public class NSEService {

    private String saveFileDir;
    private String bhavCopyDir;
    public NSEService(@Value("${nse.data.savePath}")String saveFileDir,
                      @Value("${nse.data.unzip}")String bhavCopyDir){
        this.saveFileDir = saveFileDir;
        this.bhavCopyDir = bhavCopyDir;

    }
    // get response for a given date
    // https://www1.nseindia.com/ArchieveSearch?h_filetype=eqbhav&date=15-11-2022&section=EQ
    public void getBhavCopy(LocalDate day){
        try {
            Map<String, String> map= new HashMap<>();
            map.put("Accept", "*/*");
            map.put("Accept-Language", "en-US,en;q=0.5");
            map.put("Connection", "keep-alive");
            map.put("Upgrade-Insecure-Requests", "1");
            map.put("Host", "www1.nseindia.com");
            map.put("Referer", "https://www1.nseindia.com/products/content/equities/equities/archieve_eq.htm");
            map.put("Sec-GPC", "1");
            map.put("X-Requested-With", "XMLHttpRequest");
            String savedFileName = getFileNameFromDate(day);
            String url= String.format("https://www1.nseindia.com/content/historical/EQUITIES/%s/%s/%s.zip", day.getYear(), day.getMonth().toString().substring(0, 3), savedFileName);
            byte[] bytes= IOUtils.toByteArray(new URL(url));
            String sourceFile= saveFileDir.concat(savedFileName).concat(".zip");
            File file = new File(sourceFile);
            file.getParentFile().mkdirs(); // Will create parent directories if not exists
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(bytes);
                fos.close();

                Unzip unzipper = new Unzip();
                unzipper.setSrc(new File(sourceFile));
                log.info("Copying {} to path: {}",sourceFile.toString(), bhavCopyDir.toString());
                unzipper.setDest(new File(bhavCopyDir));
                unzipper.execute();
            } catch (IOException e) {
                System.err.println("Could not read the file at '" + day + ":"+e.getMessage());
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
