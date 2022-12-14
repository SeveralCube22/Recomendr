package com.vmanam.recomendr.storm.bolts;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.util.HashMap;
import java.util.Map;

public class ReportBolt extends BaseRichBolt {

    private static final long serialVersionUID = 6102304822420418016L;

    private Map<String, Long> counts;
    private OutputCollector collector;

    @Override @SuppressWarnings("rawtypes")
    public void prepare(Map stormConf, TopologyContext context, OutputCollector outCollector) {
        collector = outCollector;
        counts = new HashMap<String, Long>();
    }

    @Override

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // terminal bolt = does not emit anything
    }

    @Override
    public void execute(Tuple tuple) {
        System.out.println("HELLO " + tuple);
        collector.ack(tuple);
    }

    @Override
    public void cleanup() {
        System.out.println("HELLO FINAL");
    }
}