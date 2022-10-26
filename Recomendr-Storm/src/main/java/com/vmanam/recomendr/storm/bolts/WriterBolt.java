package com.vmanam.recomendr.storm.bolts;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.vmanam.recomendr.storm.entities.UserEvent;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class WriterBolt extends BaseRichBolt {
    private CqlSession session;
    private OutputCollector collector;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector outputCollector) {
        DriverConfigLoader loader = DriverConfigLoader.fromClasspath("application.conf");
        try {
            collector = outputCollector;
            session = CqlSession.builder().
                    withConfigLoader(loader).
                    withSslContext(SSLContext.getDefault()).
                    withKeyspace("recomendr").
                    build();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(Tuple tuple) {
        UserEvent event = (UserEvent) tuple.getValue(4); // UserEvent obj is located at this index in logs

        String userId = event.getUserId();
        String movieName = event.getMovieName();
        String releaseDate = event.getReleaseDate();
        float eventWeight = event.getEventWeight();

        float deltaRating = eventWeight;
        Float previousRating = null;

        // get all rated items of user
        List<UserEvent> items = new ArrayList<>();

        ResultSet set = session.execute(String.format("SELECT * FROM recomendr.items_by_user WHERE userId=\'%s\';", userId));
        for(Row x : set) {
            String otherMovie = x.getString("movieName");
            String otherDate = x.getString("releaseDate");
            if(!otherMovie.equals(movieName) || (otherMovie.equals(movieName) && !otherDate.equals(releaseDate)))
                items.add(new UserEvent(userId, otherMovie, otherDate, x.getFloat("rating")));
            else {
                float weight = x.getFloat("rating");
                deltaRating = eventWeight - weight;
                previousRating = weight;
            }
        }

        // update pair counts and recalculate similarity
        UserEvent currEvent = new UserEvent(userId, movieName, releaseDate, eventWeight);
        float currentItemCount = getItemCount(movieName, releaseDate);
        float newItemCount = currentItemCount + deltaRating;

        for(UserEvent other : items)
            recalculateSimilarity(currEvent, other, previousRating, newItemCount);

        updateItemCount(movieName, releaseDate, newItemCount);
        updateUserItem(userId, movieName, releaseDate, eventWeight);
        collector.ack(tuple);
    }

    private void recalculateSimilarity(UserEvent current, UserEvent other, Float previousRating, float newItemCount) {
        String id = getId(current, other);

        Float value = getPairCount(id);
        float pairCount = value == null ? 0 : value;

        // calculate delta
        float currentCoRating = Math.min(current.getEventWeight(), other.getEventWeight());
        float previousCoRating = previousRating == null ? 0 : Math.min(previousRating, other.getEventWeight());
        float deltaCoRating = currentCoRating - previousCoRating;

        // update paircount
        pairCount += deltaCoRating;

        // recalculate similarity
        float otherItemCount = getItemCount(other.getMovieName(), other.getReleaseDate());

        double newSimilarity = (pairCount) / (Math.sqrt(newItemCount) * Math.sqrt(otherItemCount));

        // save pairCount, newSimilarity, newItemCount
        updatePairCount(id, pairCount);
        updateSimilarity(current, other, newSimilarity);
    }

    private String getId(UserEvent e1, UserEvent e2) {
        String id = null;
        int movieCompare = e1.getMovieName().compareTo(e2.getMovieName());
        int releaseCompare = e1.getReleaseDate().compareTo(e2.getReleaseDate());

        if(movieCompare < 0 || (movieCompare == 0 && releaseCompare < 0))
            id = e1.getMovieName() + e1.getReleaseDate() + e2.getMovieName() + e2.getReleaseDate();
        else if(movieCompare > 0 || (movieCompare == 0 && releaseCompare > 0))
            id = e2.getMovieName() + e2.getReleaseDate() + e1.getMovieName() + e1.getReleaseDate();
        return id;
    }

    private Float getPairCount(String id) {
        ResultSet result = session.execute(String.format("SELECT pairCount FROM recomendr.counts_by_items WHERE id=\'%s\';", id));
        Row row = result.one();
        if(row == null)
            return null;
        else
            return row.getFloat(0);
    }

    private float getItemCount(String movieName, String releaseDate) {
        ResultSet result = session.execute(String.format(
                "SELECT itemCount FROM recomendr.movie_by_id WHERE name=\'%s\' AND releaseDate=\'%s\';",
                movieName, releaseDate));
        return result.one().getFloat(0);
    }

    private void updateItemCount(String movieName, String releaseDate, float newItemCount) {
        session.execute(String.format(
                "UPDATE recomendr.movie_by_id SET itemCount=%f WHERE name=\'%s\' AND releaseDate=\'%s\';",
                newItemCount, movieName, releaseDate));
    }

    private void updatePairCount(String id, float pairCount) {
        session.execute(String.format(
                "UPDATE recomendr.counts_by_items SET pairCount=%f WHERE id=\'%s\'", pairCount, id));
    }

    private void updateSimilarity(UserEvent current, UserEvent other, double similarity) {
        session.execute(String.format(
                "UPDATE recomendr.similarities_by_items SET similarity=%.2f " +
                        "WHERE currentMovieName=\'%s\' AND currentMovieReleaseDate=\'%s\'" +
                        "AND otherMovieName=\'%s\' AND otherMovieReleaseDate=\'%s\';",
                similarity, current.getMovieName(), current.getReleaseDate(),
                other.getMovieName(), other.getReleaseDate()));

        session.execute(String.format(
                "UPDATE recomendr.similarities_by_items SET similarity=%.2f " +
                        "WHERE currentMovieName=\'%s\' AND currentMovieReleaseDate=\'%s\'" +
                        "AND otherMovieName=\'%s\' AND otherMovieReleaseDate=\'%s\';",
                similarity, other.getMovieName(), other.getReleaseDate(),
                current.getMovieName(), current.getReleaseDate()));
    }

    private void updateUserItem(String userId, String movieName, String releaseDate, float rating) {
        session.execute(String.format(
                "UPDATE recomendr.items_by_user SET rating=%f WHERE userId=\'%s\' " +
                        "AND movieName=\'%s\' AND releaseDate=\'%s\'",
                rating, userId, movieName, releaseDate));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        // terminal bolt does not emit anything
    }

    @Override
    public void cleanup() {
        session.close();
    }
}
