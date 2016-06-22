package jp.co.bigdata.dto;

public class UserInfo {
    private String artistIds;
    private int totalNumberPlay;

    public UserInfo(String artistId, int numberPlay) {
        addArtistId(artistId);
        increaseTotalNumberPlay(numberPlay);
    }

    public void increaseTotalNumberPlay(int numberPlay) {
        totalNumberPlay += numberPlay;
    }

    public void addArtistId(String artistId) {
        String addedArtistId = "-" + artistId + "-";
        if(artistIds == null) {
            artistIds = addedArtistId;
        } else {
            if(!artistIds.contains(addedArtistId))
                //artistIds = artistIds + addedArtistId;
                artistIds += addedArtistId;
        }
    }

    public UserInfo withArtisId(String artistId) {
        addArtistId(artistId);
        return this;
    }

    public String getArtistIds() {
        return artistIds;
    }

    public void setArtistIds(String artistIds) {
        this.artistIds = artistIds;
    }

    public int getTotalNumberPlay() {
        return totalNumberPlay;
    }

    public void setTotalNumberPlay(int totalNumberPlay) {
        this.totalNumberPlay = totalNumberPlay;
    }

    public int countArtistId() {
        if(artistIds == null) return 0;

        return (artistIds.length() - artistIds.replace("-", "").length()) / 2;
    }

    @Override
    public String toString() {
        return "UserInfo ["
                + "artistIds=" + artistIds
                + ", totalNumberPlay=" + totalNumberPlay
                + "]";
    }
}
