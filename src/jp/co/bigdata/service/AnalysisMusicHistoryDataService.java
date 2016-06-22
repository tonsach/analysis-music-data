package jp.co.bigdata.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import jp.co.bigdata.dto.UserInfo;
import jp.co.bigdata.exception.InvalidArgumentException;

public class AnalysisMusicHistoryDataService {
    public static final String ARTIST_DATA_FILE_NAME = "profiledata_06-May-2005/artist_data.txt";
    public static final String USER_ARTIST_DATA_FILE_NAME = "profiledata_06-May-2005/user_artist_data.txt";
    public static final String ARTIST_ALIAS_FILE_NAME = "profiledata_06-May-2005/artist_alias.txt";

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        //key: unique_artist_id
        //value: total of play number
        Map<String, Integer> uniqueArtistMap = new HashMap<String, Integer>(200_000, 1.0f);

        //key: artist_id
        //value: total of play number
        Map<String, Integer> artistMap = new HashMap<String, Integer>(1_500_000, 1.0f);

        //key: user_id
        //value: user info include set of artist_id and total play
        Map<String, UserInfo> userMap = new HashMap<String, UserInfo>(1_500_000, 1.0f);

        //create list of path file
        List<String> filePaths = new ArrayList<>(10);
        for(int i = 1; i < 10; i++) {
            filePaths.add("C:\\Documents\\BigDataPrac\\profiledata_06-May-2005\\user_artist_data.txt.00" + i);
        }
        filePaths.add("C:\\Documents\\BigDataPrac\\profiledata_06-May-2005\\artist_alias.txt");

        for(int i = 0; i < filePaths.size(); i++) {
            System.out.println("start read file=" + filePaths.get(i));
            Stream<String> lines = null;
            try {
                lines = Files.lines(Paths.get(filePaths.get(i)), StandardCharsets.UTF_8);
                if(i < (filePaths.size() - 1) ) {
                     processUserArtistData(lines, artistMap, userMap);
                } else {
                    processArtistAliasData(lines, uniqueArtistMap, artistMap);
                }
            } catch(Exception e) {
                System.out.println("can not read file at location=" + filePaths.get(i) + ", because e=" + e);
            } finally {
                if(lines != null) lines.close();
                System.gc();
            }
            System.out.println("end read file=" + filePaths.get(i));
            System.out.println("=================");
        }

        calculatePlayOfUniqueArtist(uniqueArtistMap);
        System.out.println("=================");
        calculateMostActiveUser(userMap);
        System.out.println("=================");

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("run time (ms)=" + elapsedTime);
    }

    public static void calculateMostActiveUser(Map<String, UserInfo> userMap) {
        int maxPlay = 0, maxArtistNum = 0;
        String maxPlayUserId = null, maxArtistNumUserId = null;

        for(Entry<String, UserInfo> userEntry : userMap.entrySet()) {
            UserInfo userInfo = userEntry.getValue();
            int userPlayNum = userInfo.getTotalNumberPlay();
            if(userPlayNum > maxPlay) {
                maxPlay = userPlayNum;
                maxPlayUserId = userEntry.getKey();
            }

            int userArtistNum = userInfo.countArtistId();
            if(userArtistNum > maxArtistNum) {
                maxArtistNum = userArtistNum;
                maxArtistNumUserId = userEntry.getKey();
            }
        }

        System.out.println("most active user=" + maxPlayUserId + ", play number=" + maxPlay);
        System.out.println("=================");
        System.out.println("most listened artist user=" + maxArtistNumUserId + ", listened number of artist=" + maxArtistNum);
    }

    public static void calculatePlayOfUniqueArtist(Map<String, Integer> uniqueArtistMap) {
        int maxPlay = 0;
        String maxPlayUniqueArtistId = null;

        for(Entry<String, Integer> uniqueArtistEntry : uniqueArtistMap.entrySet()) {
            int totalNumberPlay = uniqueArtistEntry.getValue();
            if(totalNumberPlay > maxPlay) {
                maxPlay = totalNumberPlay;
                maxPlayUniqueArtistId = uniqueArtistEntry.getKey();
            }
        }
        System.out.println("most popular artist=" + maxPlayUniqueArtistId + ", play number=" + maxPlay);
    }

    public static final String SEPERATOR_OF_USER_ARTIST_DATA = " ";
    public static void processUserArtistData(Stream<String> lines,
                                                Map<String, Integer> artistMap,
                                                Map<String, UserInfo> userMap) throws IOException {
        //read line by line
        for(String line : (Iterable<String>) lines::iterator) {
            try {
                processUserArtistMap(line, artistMap, userMap);
            } catch(Exception e) {
                System.out.println("can not read this line because, " + e);
            }
        }
    }

    public static void processUserArtistMap(String inputLine,
                                                Map<String, Integer> artistMap,
                                                Map<String, UserInfo> userMap) throws InvalidArgumentException {
        String[] elements = inputLine.split(SEPERATOR_OF_USER_ARTIST_DATA);

        if(elements.length != 3)  {
            throw new InvalidArgumentException("invalid inputLine=" + inputLine + ", in file=" + USER_ARTIST_DATA_FILE_NAME);
        }

        String userId = elements[0];
        String artistId = elements[1];
        int playNum;
        try {
            playNum = Integer.parseInt(elements[2]);
        } catch(Exception e) {
            throw new InvalidArgumentException("invalid playNum in this line=" + inputLine + ", in file=" + USER_ARTIST_DATA_FILE_NAME);
        }

        if(userMap.containsKey(userId)) {
            userMap.get(userId)
                    .withArtisId(artistId)
                    .increaseTotalNumberPlay(playNum);
        } else {
            userMap.put(userId, new UserInfo(artistId, playNum));
        }

        if(artistMap.containsKey(artistId)) {
            artistMap.put(artistId, artistMap.get(artistId) + playNum);
        } else {
            artistMap.put(artistId, playNum);
        }
    }

    public static final String SEPERATOR_OF_ARTIST_ALIAS_DATA = "\t";
    public static void processArtistAliasData(Stream<String> lines,
                                                Map<String, Integer> uniqueArtistMap,
                                                Map<String, Integer> artistMap) throws IOException {
        //read line by line
        for(String line : (Iterable<String>) lines::iterator) {
            try {
                processUniqueArtistMap(line, uniqueArtistMap, artistMap);
            } catch (Exception e) {
                System.out.println("can not read this line because, " + e);
            }
        }
    }

    public static void processUniqueArtistMap(String inputLine,
                                                Map<String, Integer> uniqueArtistMap,
                                                Map<String, Integer> artistMap) throws Exception {
        String[] elements = inputLine.split(SEPERATOR_OF_ARTIST_ALIAS_DATA);
        if(elements.length != 2) {
            throw new InvalidArgumentException("invalid inputLine=" + inputLine + ", in file=" + ARTIST_ALIAS_FILE_NAME);
        }

        String artistId = elements[0];
        String uniqueId = elements[1];
        if(artistId == null
                || artistId.isEmpty()
                || uniqueId == null
                || uniqueId.isEmpty()) {
            throw new InvalidArgumentException("invalid data in line=" + inputLine + ", in file=" + ARTIST_ALIAS_FILE_NAME);
        }

        int playNum = (artistMap.get(artistId) == null) ? 0 : artistMap.get(artistId);
        if(uniqueArtistMap.containsKey(uniqueId)) {
            uniqueArtistMap.put(uniqueId, uniqueArtistMap.get(uniqueId) + playNum);
        } else {
            uniqueArtistMap.put(uniqueId, playNum);
        }
    }
}
