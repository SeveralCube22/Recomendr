package com.vmanam.recomendr.storm;

import com.vmanam.recomendr.storm.bolts.ReportBolt;
import com.vmanam.recomendr.storm.bolts.WriterBolt;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.topology.TopologyBuilder;

import java.util.HashMap;
import java.util.Map;

public class Topology {
    public static void main(String[] args) {
        System.setProperty("aws.accessKeyId", "KEY");
        System.setProperty("aws.secretKey", "SECRET");
        TopologyBuilder tp = new TopologyBuilder();

        KafkaSpoutConfig spoutConfig = KafkaSpoutConfig.builder("b-1-public.recomendr.2n4cqh.c2.kafka.us-west-1.amazonaws.com:9198,b-2-public.recomendr.2n4cqh.c2.kafka.us-west-1.amazonaws.com:9198", "user-events")
                .setProp("group.id", "storm-consumer")
                .setProp("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                .setProp("value.deserializer", "com.vmanam.recomendr.storm.entities.EventDeserializer")
                .setProp("security.protocol", "SASL_SSL")
                .setProp("sasl.mechanism", "AWS_MSK_IAM")
                .setProp("sasl.jaas.config", "software.amazon.msk.auth.iam.IAMLoginModule required awsProfileName=\"recomendr_user\";")
                .setProp("sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.IAMClientCallbackHandler")
                .build();

        KafkaSpout spout = new KafkaSpout(spoutConfig);
        tp.setSpout("user-events", spout);

        ReportBolt reportBolt = new ReportBolt();
        WriterBolt writerBolt = new WriterBolt();

        tp.setBolt("report-bolt", reportBolt).shuffleGrouping("user-events");
        tp.setBolt("writer-bolt", writerBolt).shuffleGrouping("user-events");

        try {
            Config config = new Config();
            config.setDebug(true);

            Map<String, Object> creds = new HashMap<>();
            creds.put("AWS_ACCESS_KEY_ID", "KEY");
            creds.put("AWS_SECRET_KEY", "SECRET");


            config.setEnvironment(creds);
            StormSubmitter submitter = new StormSubmitter();
            submitter.submitTopology("events-topology", config, tp.createTopology());
        }
        catch(Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }
    }
}
