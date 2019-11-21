import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.function.Function;

public class Application {
    public static final int CREATION_DELAY_IN_SECONDS_FOR_VOTING  = 7 * 24 * 60  * 60; // one week

    private static final Function<String, String> article = id -> "article:" + id;
    private static final Function<String, String> voted = id -> "voted:" + id;
    private static final String CREATION_TIME = "creationTime";

    private static Jedis jedis = new Jedis("localhost");

    public static void main(String[] args){
        initialize();
        vote("u001", "a001");
        System.out.println("OK");
    }

    private static void initialize() {
        jedis.hset(article.apply("a001"), CREATION_TIME, inSeconds(LocalDateTime.now().minusDays(1)).toString());
    }

    public static void vote(String user, String article){
        checkCreationTime(article);

        registerVote(user, article);
        incrementScore(article);
        incrementeVotes(article);
    }

    private static void incrementScore(String article) {
        
    }

    private static void registerVote(String userId, String articleId) {
        long votingReturn = jedis.sadd(voted.apply(articleId), userId);

        if (votingReturn == 0){
            error("DoubleVoteException");
        }
    }

    private static void checkCreationTime(String articleId) {
        long now = inSeconds(LocalDateTime.now());
        String creationTime = jedis.hget(article.apply(articleId),"creationTime");

        if (creationTime == null){
            error("UnknownArticleException");
        }

        if (Long.parseLong(creationTime) < now - CREATION_DELAY_IN_SECONDS_FOR_VOTING){
            error("CreationTimeException");
        }
    }

    private static Long inSeconds(LocalDateTime dateTime){
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }

    public static void error(String type){
        throw new RuntimeException(type);
    }
}
